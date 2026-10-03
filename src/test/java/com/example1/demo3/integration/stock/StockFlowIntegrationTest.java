package com.example1.demo3.integration.stock;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.entity.User;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StockFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MakerRepository makerRepository;

    @Autowired
    private StockDetailRepository stockDetailRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    // 正常系: 商品を登録して入庫・出庫を実行し、在庫・メーカー別内訳・履歴・集計APIが最新状態になることを確認する
    void stockFlow_successfulInAndOut_updatesStockHistoryAndSummary() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("トマト", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 10)
                .andExpect(status().isOk());

        Product afterIn = productRepository.findById(product.getId()).orElseThrow();
        StockDetail detailAfterIn = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> inHistory = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(10, afterIn.getStock());
        assertEquals(10, detailAfterIn.getQuantity());
        assertEquals(1, inHistory.size());
        assertEquals(10, inHistory.get(0).getQuantity());
        assertEquals(10, inHistory.get(0).getStock());
        assertEquals("IN", inHistory.get(0).getType());

        performStockOut(session, product, maker, 4)
                .andExpect(status().isOk());

        Product afterOut = productRepository.findById(product.getId()).orElseThrow();
        StockDetail detailAfterOut = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> outHistory = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(6, afterOut.getStock());
        assertEquals(6, detailAfterOut.getQuantity());
        assertEquals(1, outHistory.size());
        assertEquals(4, outHistory.get(0).getQuantity());
        assertEquals(6, outHistory.get(0).getStock());
        assertEquals("OUT", outHistory.get(0).getType());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'トマト')].stock", contains(6)));

        mockMvc.perform(get("/api/stock/summary/maker/{productName}", "トマト").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.maker == 'メーカーA')].stock", contains(6)));
    }

    @Test
    // 履歴整合性: 入庫と出庫が時系列で残り、quantity・stock・maker・productNameが正しく保持されることを確認する
    void stockFlow_keepsHistoryInTimeOrderAndRequiredFields() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("レタス", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 10).andExpect(status().isOk());
        performStockOut(session, product, maker, 4).andExpect(status().isOk());
        performStockIn(session, product, maker, 3).andExpect(status().isOk());
        performStockOut(session, product, maker, 2).andExpect(status().isOk());

        List<StockHistory> history = stockHistoryRepository
                .findByProductIdOrderByDateTimeAsc(product.getId());

        assertEquals(4, history.size());
        assertEquals("IN", history.get(0).getType());
        assertEquals(10, history.get(0).getQuantity());
        assertEquals(10, history.get(0).getStock());
        assertEquals(maker.getId(), history.get(0).getMaker().getId());
        assertEquals("レタス", history.get(0).getProductName());

        assertEquals("OUT", history.get(1).getType());
        assertEquals(4, history.get(1).getQuantity());
        assertEquals(6, history.get(1).getStock());

        assertEquals("IN", history.get(2).getType());
        assertEquals(3, history.get(2).getQuantity());
        assertEquals(9, history.get(2).getStock());

        assertEquals("OUT", history.get(3).getType());
        assertEquals(2, history.get(3).getQuantity());
        assertEquals(7, history.get(3).getStock());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'レタス')].stock", contains(7)));
    }

    @Test
    // 境界値: 0や負数の出庫は失敗し、等しい数量は成功、1不足だけ失敗することを確認する
    void stockFlow_rejectsZeroAndNegativeQuantitiesAndChecksBoundary() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("キャベツ", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 5).andExpect(status().isOk());

        performStockOut(session, product, maker, 0)
                .andExpect(status().isBadRequest());
        performStockOut(session, product, maker, -1)
                .andExpect(status().isBadRequest());

        performStockOut(session, product, maker, 5)
                .andExpect(status().isOk());

        Product exactProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail exactDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        assertEquals(0, exactProduct.getStock());
        assertEquals(0, exactDetail.getQuantity());

        performStockOut(session, product, maker, 1)
                .andExpect(status().isBadRequest());

        Product finalProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail finalDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        assertEquals(0, finalProduct.getStock());
        assertEquals(0, finalDetail.getQuantity());
    }

    @Test
    // 複数メーカー: Aメーカーの入出庫がBメーカーに影響しないことと、maker別集計が独立していることを確認する
    void stockFlow_tracksInventorySeparatelyForMultipleMakers() throws Exception {
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        Product product = createProduct("きゅうり", makerA, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, makerA, 10).andExpect(status().isOk());
        performStockIn(session, product, makerB, 6).andExpect(status().isOk());
        performStockOut(session, product, makerA, 4).andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail detailA = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), makerA.getId()).orElseThrow();
        StockDetail detailB = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), makerB.getId()).orElseThrow();

        assertEquals(12, updatedProduct.getStock());
        assertEquals(6, detailA.getQuantity());
        assertEquals(6, detailB.getQuantity());

        mockMvc.perform(get("/api/stock/summary/maker/{productName}", "きゅうり").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.maker == 'メーカーA')].stock", contains(6)))
                .andExpect(jsonPath("$[?(@.maker == 'メーカーB')].stock", contains(6)));
    }

    @Test
    // 不正入力: 必須項目が欠落していても、既存在庫・履歴が変わらず400を返すことを確認する
    void stockFlow_rejectsMissingRequiredFieldsWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ピーマン", maker, 5);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        List<String> invalidRequests = List.of(
                "{\"quantity\":2,\"makerId\":" + maker.getId() + ",\"unit\":\"個\",\"category\":\"野菜\"}",
                "{\"productId\":" + product.getId() + ",\"quantity\":2,\"unit\":\"個\",\"category\":\"野菜\"}",
                "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":" + maker.getId() + ",\"unit\":\"\",\"category\":\"野菜\"}",
                "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":" + maker.getId() + ",\"unit\":\"個\",\"category\":\"\"}",
                "{\"productId\":" + product.getId() + ",\"makerId\":" + maker.getId() + ",\"unit\":\"個\",\"category\":\"野菜\"}");

        for (String requestBody : invalidRequests) {
            performStockIn(session, requestBody).andExpect(status().isBadRequest());
            performStockOut(session, requestBody).andExpect(status().isBadRequest());
        }

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // 存在しないデータ: 商品ID・メーカーID・組み合わせが不正な場合は404で失敗し、既存データは保持されることを確認する
    void stockFlow_rejectsUnknownProductOrMakerWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("大根", maker, 5);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        String unknownProductRequest = "{\"productId\":2147483647,\"quantity\":2,\"makerId\":" + maker.getId() + ",\"unit\":\"個\",\"category\":\"野菜\"}";
        String unknownMakerRequest = "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":2147483647,\"unit\":\"個\",\"category\":\"野菜\"}";
        String mismatchedMakerRequest = "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":999999,\"unit\":\"個\",\"category\":\"野菜\"}";

        performStockIn(session, unknownProductRequest).andExpect(status().isNotFound());
        performStockIn(session, unknownMakerRequest).andExpect(status().isNotFound());
        performStockOut(session, unknownProductRequest).andExpect(status().isNotFound());
        performStockOut(session, mismatchedMakerRequest).andExpect(status().isNotFound());

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // 一連フロー総合整合性: 10入庫→4出庫→3入庫→2出庫の流れで最終在庫7、履歴とsummaryが一致することを確認する
    void stockFlow_keepsFinalInventoryAndSummaryConsistentAcrossMultipleMovements() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ナス", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 10).andExpect(status().isOk());
        performStockOut(session, product, maker, 4).andExpect(status().isOk());
        performStockIn(session, product, maker, 3).andExpect(status().isOk());
        performStockOut(session, product, maker, 2).andExpect(status().isOk());

        Product finalProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail finalDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdOrderByDateTimeAsc(product.getId());

        assertEquals(7, finalProduct.getStock());
        assertEquals(7, finalDetail.getQuantity());
        assertEquals(4, history.size());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'ナス')].stock", contains(7)));
    }

    @Test
    // 商品とメーカーの紐付け不整合: 商品がメーカーAだがメーカーBで出庫すると失敗し、在庫と履歴が変わらないことを確認する
    void stockFlow_rejectsMakerMismatchWithoutChangingInventory() throws Exception {
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        Product product = createProduct("きゅうり", makerA, 10);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(makerA);
        detail.setQuantity(10);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        String mismatchRequest = "{\"productId\":" + product.getId()
                + ",\"quantity\":3,\"makerId\":" + makerB.getId()
                + ",\"unit\":\"個\",\"category\":\"野菜\"}";
        performStockOut(session, mismatchRequest)
                .andExpect(status().isNotFound());

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), makerA.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(10, unchangedProduct.getStock());
        assertEquals(10, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // StockDetail未作成: 商品は存在するがメーカー別内訳が存在しない状態で出庫すると失敗し、履歴が増えないことを確認する
    void stockFlow_rejectsWhenMakerStockDetailDoesNotExist() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("トマト", maker, 7);
        MockHttpSession session = loginAsAdmin();

        performStockOut(session, product, maker, 2)
                .andExpect(status().isNotFound());

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(7, unchangedProduct.getStock());
        assertTrue(history.isEmpty());
    }

    @Test
    // 履歴の件数とsummary再計算: 複数回の入出庫後に history 件数・stock・maker summary が一致することを確認する
    void stockFlow_keepsSummaryConsistentAfterMultipleMoves() throws Exception {
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        Product product = createProduct("ナス", makerA, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, makerA, 10).andExpect(status().isOk());
        performStockOut(session, product, makerA, 4).andExpect(status().isOk());
        performStockIn(session, product, makerB, 5).andExpect(status().isOk());
        performStockOut(session, product, makerA, 1).andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        List<StockHistory> allHistory = stockHistoryRepository
                .findByProductIdOrderByDateTimeAsc(product.getId());

        assertEquals(10, updatedProduct.getStock());
        assertEquals(4, allHistory.size());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'ナス')].stock", contains(10)));

        mockMvc.perform(get("/api/stock/summary/maker/{productName}", "ナス").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.maker == 'メーカーA')].stock", contains(5)))
                .andExpect(jsonPath("$[?(@.maker == 'メーカーB')].stock", contains(5)));
    }

    private ResultActions performStockIn(
            MockHttpSession session, Product product, Maker maker, int quantity) throws Exception {
        String requestBody = "{\"productId\":" + product.getId()
                + ",\"quantity\":" + quantity
                + ",\"makerId\":" + maker.getId()
                + ",\"unit\":\"個\",\"category\":\"野菜\"}";

        return performStockIn(session, requestBody);
    }

    private ResultActions performStockIn(MockHttpSession session, String requestBody) throws Exception {
        return mockMvc.perform(post("/api/stock/in")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions performStockOut(
            MockHttpSession session, Product product, Maker maker, int quantity) throws Exception {
        String requestBody = "{\"productId\":" + product.getId()
                + ",\"quantity\":" + quantity
                + ",\"makerId\":" + maker.getId()
                + ",\"unit\":\"個\",\"category\":\"野菜\"}";

        return performStockOut(session, requestBody);
    }

    private ResultActions performStockOut(MockHttpSession session, String requestBody) throws Exception {
        return mockMvc.perform(post("/api/stock/out")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ADMIN");
        userRepository.saveAndFlush(admin);

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    private Maker createMaker(String name) {
        Maker maker = new Maker();
        maker.setName(name);
        return makerRepository.saveAndFlush(maker);
    }

    private Product createProduct(String name, Maker maker, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setCategory("野菜");
        product.setUnit("個");
        product.setStock(stock);
        product.setMaker(maker);
        return productRepository.saveAndFlush(product);
    }
}

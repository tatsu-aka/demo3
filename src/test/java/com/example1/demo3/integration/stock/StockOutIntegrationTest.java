package com.example1.demo3.integration.stock;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class StockOutIntegrationTest {

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
    // 商品を出庫したとき、商品在庫・メーカー別在庫・出庫履歴が一緒に更新されることを確認する
    void stockOut_updatesProductDetailAndHistory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("キャベツ", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 10)
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        performStockOut(session, product, maker, 4)
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail detail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(6, updatedProduct.getStock());
        assertEquals(6, detail.getQuantity());
        assertEquals(1, history.size());
        assertEquals(4, history.get(0).getQuantity());
        assertEquals(6, history.get(0).getStock());
        assertEquals("OUT", history.get(0).getType());
        assertEquals(maker.getId(), history.get(0).getMaker().getId());
        assertEquals("キャベツ", history.get(0).getProductName());
        assertEquals("個", history.get(0).getUnit());
        assertEquals("野菜", history.get(0).getCategory());
    }

    @Test
    // 出庫数が現在庫を超えると失敗し、商品在庫・メーカー別在庫・履歴が変わらないことを確認する
    void stockOut_rejectsWhenQuantityExceedsAvailableStock() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("レタス", maker, 0);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        Product persistedProduct = productRepository.findById(product.getId()).orElseThrow();
        persistedProduct.setStock(5);
        productRepository.saveAndFlush(persistedProduct);
        MockHttpSession session = loginAsAdmin();

        performStockOut(session, product, maker, 6)
                .andExpect(status().isBadRequest());

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // 複数メーカーで在庫を持つ商品を出庫したとき、メーカーごとの在庫と集計が独立していることを確認する
    void stockOut_tracksInventorySeparatelyForMultipleMakers() throws Exception {
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
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(12, updatedProduct.getStock());
        assertEquals(6, detailA.getQuantity());
        assertEquals(6, detailB.getQuantity());
        assertEquals(1, history.size());
        assertEquals(4, history.get(0).getQuantity());
        assertEquals(12, history.get(0).getStock());
        assertEquals(makerA.getId(), history.get(0).getMaker().getId());
    }

    @Test
    // 出庫後の全体サマリーとメーカー別サマリーAPIが最新の在庫数を返すことを確認する
    void stockOut_updatesStockSummaryApis() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ナス", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 10).andExpect(status().isOk());
        performStockOut(session, product, maker, 4).andExpect(status().isOk());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'ナス')].stock", contains(6)));

        mockMvc.perform(get("/api/stock/summary/maker/{productName}", "ナス").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.maker == 'メーカーA')].stock", contains(6)));
    }

    @Test
    // 数量0・負数・数量未指定の出庫は400となり、在庫・内訳・履歴が変わらないことを確認する
    void stockOut_rejectsInvalidQuantitiesWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ピーマン", maker, 5);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        String productAndMaker = "\"productId\":" + product.getId()
                + ",\"makerId\":" + maker.getId()
                + ",\"unit\":\"個\",\"category\":\"野菜\"";
        List<String> invalidRequests = List.of(
                "{" + productAndMaker + ",\"quantity\":0}",
                "{" + productAndMaker + ",\"quantity\":-1}",
                "{" + productAndMaker + "}");

        for (String requestBody : invalidRequests) {
            performStockOut(session, requestBody)
                    .andExpect(status().isBadRequest());
        }

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // 商品ID・メーカーID・単位・カテゴリの欠落時に400となることを確認し、既存在庫が維持されることを確認する
    void stockOut_rejectsMissingRequiredFieldsWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ブロッコリー", maker, 5);
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
                "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":" + maker.getId() + ",\"unit\":\"個\",\"category\":\"\"}");

        for (String requestBody : invalidRequests) {
            performStockOut(session, requestBody)
                    .andExpect(status().isBadRequest());
        }

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // 存在しない商品IDまたはメーカーIDで出庫できず、既存在庫と履歴が変わらないことを確認する
    void stockOut_rejectsUnknownProductOrMakerWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("大根", maker, 5);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        String unknownProductRequest = "{\"productId\":2147483647,\"quantity\":2,\"makerId\":"
                + maker.getId() + ",\"unit\":\"個\",\"category\":\"野菜\"}";
        performStockOut(session, unknownProductRequest)
                .andExpect(status().isNotFound());

        String unknownMakerRequest = "{\"productId\":" + product.getId()
                + ",\"quantity\":2,\"makerId\":2147483647,\"unit\":\"個\",\"category\":\"野菜\"}";
        performStockOut(session, unknownMakerRequest)
                .andExpect(status().isNotFound());

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
    }

    @Test
    // 出庫数量がちょうど現在在庫数と一致したときは成功し、1不足したときだけ失敗する境界値を確認する
    void stockOut_supportsExactStockBoundaryAndRejectsOneAbove() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("人参", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 5).andExpect(status().isOk());
        performStockOut(session, product, maker, 5).andExpect(status().isOk());

        Product exactProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail exactDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        assertEquals(0, exactProduct.getStock());
        assertEquals(0, exactDetail.getQuantity());

        performStockOut(session, product, maker, 1)
                .andExpect(status().isBadRequest());

        Product afterReject = productRepository.findById(product.getId()).orElseThrow();
        StockDetail afterRejectDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        assertEquals(0, afterReject.getStock());
        assertEquals(0, afterRejectDetail.getQuantity());
    }

    @Test
    // 商品のメーカーと出庫依頼のメーカーが異なる場合、メーカー別在庫が見つからず失敗し、在庫が変わらないことを確認する
    void stockOut_rejectsMakerMismatchWithoutChangingInventory() throws Exception {
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        Product product = createProduct("キャベツ", makerA, 10);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(makerA);
        detail.setQuantity(10);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        String makerMismatchRequest = "{\"productId\":" + product.getId()
                + ",\"quantity\":3,\"makerId\":" + makerB.getId()
                + ",\"unit\":\"個\",\"category\":\"野菜\"}";
        performStockOut(session, makerMismatchRequest)
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
    // 商品は存在するがメーカー別在庫の内訳が存在しない状態で出庫すると失敗し、在庫が変わらないことを確認する
    void stockOut_rejectsWhenMakerStockDetailDoesNotExist() throws Exception {
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
    // 既存の出庫履歴がある状態で追加出庫しても履歴が追記され、サマリーが現在の在庫数に整合することを確認する
    void stockOut_appendsToExistingHistoryAndKeepsSummaryConsistent() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ナス", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 10).andExpect(status().isOk());
        performStockOut(session, product, maker, 4).andExpect(status().isOk());
        performStockOut(session, product, maker, 2).andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        List<StockHistory> outHistory = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "OUT");

        assertEquals(4, updatedProduct.getStock());
        assertEquals(2, outHistory.size());
        assertEquals(4, outHistory.get(0).getQuantity());
        assertEquals(6, outHistory.get(0).getStock());
        assertEquals(2, outHistory.get(1).getQuantity());
        assertEquals(4, outHistory.get(1).getStock());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'ナス')].stock", contains(4)));
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

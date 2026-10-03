package com.example1.demo3.integration.stock;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private com.example1.demo3.service.StockOutService stockOutService;

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
    // 商品登録→入庫→出庫→履歴→summary の連続ビジネスフローで、最終在庫と集計が整合していることを確認する
    void stockBusinessFlow_createProductThenInOutThenHistoryAndSummary() throws Exception {
        Maker maker = createMaker("メーカーA");
        MockHttpSession session = loginAsAdmin();

        Product createdProduct = createProductViaApi(session, maker, "きゅうり");

        performStockIn(session, createdProduct, maker, 10).andExpect(status().isOk());
        performStockOut(session, createdProduct, maker, 4).andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(createdProduct.getId()).orElseThrow();
        StockDetail detail = stockDetailRepository
                .findByProductIdAndMakerId(createdProduct.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdOrderByDateTimeAsc(createdProduct.getId());

        assertEquals(6, updatedProduct.getStock());
        assertEquals(6, detail.getQuantity());
        assertEquals(2, history.size());
        assertEquals("IN", history.get(0).getType());
        assertEquals(10, history.get(0).getQuantity());
        assertEquals(10, history.get(0).getStock());
        assertEquals("OUT", history.get(1).getType());
        assertEquals(4, history.get(1).getQuantity());
        assertEquals(6, history.get(1).getStock());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'きゅうり')].stock", contains(6)));

        mockMvc.perform(get("/api/stock/summary/maker/{productName}", "きゅうり").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.maker == 'メーカーA')].stock", contains(6)));
    }

    @Test
    // 商品登録・更新時にメーカーが必須で、未指定や存在しないメーカーIDでは失敗することを確認する
    void productMaster_requiresMakerOnCreateAndUpdate() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(post("/api/products/master")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ナス\",\"category\":\"野菜\",\"unit\":\"個\"}"))
                .andExpect(status().isBadRequest());

        Maker maker = createMaker("メーカーA");
        Product product = createProduct("トマト", maker, 0);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/products/master/{id}", product.getId())
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新後\",\"category\":\"野菜\",\"unit\":\"個\",\"maker\":{\"id\":999999}}"))
                .andExpect(status().isNotFound());

        Product persisted = productRepository.findById(product.getId()).orElseThrow();
        assertEquals("トマト", persisted.getName());
    }

    @Test
    // 商品削除後に在庫明細と履歴が関連付けから外れ、商品一覧から取り除かれることを確認する
    void productDelete_clearsRelatedStockDetailAndHistory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("きゅうり", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 7).andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/products/master/{id}", product.getId())
                .session(session)
                .with(csrf()))
                .andExpect(status().isOk());

        assertTrue(productRepository.findById(product.getId()).isEmpty());
        assertTrue(stockDetailRepository.findByProductId(product.getId()).isEmpty());
        assertTrue(stockHistoryRepository.findByProductIdOrderByDateTimeAsc(product.getId()).isEmpty());
    }

    @Test
    // 同時出庫でも在庫がマイナスにならず、最終在庫が0未満にならないことを確認する
    void stockOut_concurrentRequests_doNotCreateNegativeStock() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("キャベツ", maker, 5);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 5).andExpect(status().isOk());

        int requestCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            futures.add(executor.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                try {
                    stockOutService.outStock(product.getId(), 1, "個", "野菜", maker.getId());
                } catch (Exception e) {
                    // 競合時は失敗しても在庫がマイナスになってはいけない
                }
                return null;
            }));
        }

        start.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        Product finalProduct = productRepository.findById(product.getId()).orElseThrow();
        assertTrue(finalProduct.getStock() >= 0);
        assertTrue(finalProduct.getStock() <= 10);
    }

    private Product createProductViaApi(MockHttpSession session, Maker maker, String name) throws Exception {
        mockMvc.perform(post("/api/products/master")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"category\":\"野菜\",\"unit\":\"個\",\"maker\":{\"id\":" + maker.getId() + "}}"))
                .andExpect(status().isOk());

        return productRepository.findByNameContaining(name).stream()
                .filter(product -> product.getMaker() != null && product.getMaker().getId().equals(maker.getId()))
                .findFirst()
                .orElseThrow();
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

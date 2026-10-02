package com.example1.demo3.integration.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.contains;
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
class StockInIntegrationTest {

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
    // 入庫APIで商品在庫・メーカー別在庫・入庫履歴が一緒に更新されることを確認する
    void stockIn_updatesProductDetailAndHistory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("レタス", maker, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 5)
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail detail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(5, updatedProduct.getStock());
        assertEquals(5, detail.getQuantity());
        assertEquals(1, history.size());
        assertEquals(5, history.get(0).getQuantity());
        assertEquals(5, history.get(0).getStock());
        assertEquals(maker.getId(), history.get(0).getMaker().getId());
        assertEquals("レタス", history.get(0).getProductName());
        assertEquals("個", history.get(0).getUnit());
        assertEquals("野菜", history.get(0).getCategory());
    }

    @Test
    // 同じ商品・メーカーへの2回目の入庫で内訳が加算され、履歴が各回分記録されることを確認する
    void stockIn_addsToExistingMakerDetailAndAppendsHistory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("トマト", maker, 3);
        StockDetail existingDetail = new StockDetail();
        existingDetail.setProduct(product);
        existingDetail.setMaker(maker);
        existingDetail.setQuantity(3);
        stockDetailRepository.saveAndFlush(existingDetail);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, maker, 2)
                .andExpect(status().isOk());
        performStockIn(session, product, maker, 4)
                .andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail updatedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(9, updatedProduct.getStock());
        assertEquals(9, updatedDetail.getQuantity());
        assertEquals(2, history.size());
        assertTrue(history.stream().anyMatch(item -> item.getQuantity() == 2 && item.getStock() == 5));
        assertTrue(history.stream().anyMatch(item -> item.getQuantity() == 4 && item.getStock() == 9));
    }

    @Test
    // 同じ商品を複数メーカーから入庫したとき、合計在庫とメーカー別内訳・履歴が分かれて記録されることを確認する
    void stockIn_tracksInventorySeparatelyForMultipleMakers() throws Exception {
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        Product product = createProduct("きゅうり", makerA, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, makerA, 3)
                .andExpect(status().isOk());
        performStockIn(session, product, makerB, 4)
                .andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail detailA = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), makerA.getId()).orElseThrow();
        StockDetail detailB = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), makerB.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(7, updatedProduct.getStock());
        assertEquals(3, detailA.getQuantity());
        assertEquals(4, detailB.getQuantity());
        assertEquals(2, history.size());
        assertTrue(history.stream().anyMatch(item -> item.getMaker().getId().equals(makerA.getId())
                && item.getQuantity() == 3 && item.getStock() == 3));
        assertTrue(history.stream().anyMatch(item -> item.getMaker().getId().equals(makerB.getId())
                && item.getQuantity() == 4 && item.getStock() == 7));
    }

    @Test
    // 入庫後の全体・メーカー別サマリーAPIが履歴に基づく集計値を返すことを確認する
    void stockIn_updatesStockSummaryApis() throws Exception {
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        Product product = createProduct("きゅうり", makerA, 0);
        MockHttpSession session = loginAsAdmin();

        performStockIn(session, product, makerA, 3)
                .andExpect(status().isOk());
        performStockIn(session, product, makerB, 4)
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/stock/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'きゅうり')].stock", contains(7)));

        mockMvc.perform(get("/api/stock/summary/maker/{productName}", "きゅうり").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.maker == 'メーカーA')].stock", contains(3)))
                .andExpect(jsonPath("$[?(@.maker == 'メーカーB')].stock", contains(4)));
    }

    @Test
    // 数量0・負数・数量未指定の入庫は400となり、商品在庫・内訳・履歴が変わらないことを確認する
    void stockIn_rejectsInvalidQuantitiesWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("ほうれん草", maker, 5);
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
            performStockIn(session, requestBody)
                    .andExpect(status().isBadRequest());
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
    // 商品ID・メーカーIDの欠落、単位・カテゴリの空欄では400となり、在庫・内訳・履歴が変わらないことを確認する
    void stockIn_rejectsMissingRequiredFieldsWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("水菜", maker, 5);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        List<String> invalidRequests = List.of(
                "{\"quantity\":2,\"makerId\":" + maker.getId()
                        + ",\"unit\":\"個\",\"category\":\"野菜\"}",
                "{\"productId\":" + product.getId()
                        + ",\"quantity\":2,\"unit\":\"個\",\"category\":\"野菜\"}",
                "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":"
                        + maker.getId() + ",\"unit\":\"\",\"category\":\"野菜\"}",
                "{\"productId\":" + product.getId() + ",\"quantity\":2,\"makerId\":"
                        + maker.getId() + ",\"unit\":\"個\",\"category\":\"\"}");

        for (String requestBody : invalidRequests) {
            performStockIn(session, requestBody)
                    .andExpect(status().isBadRequest());
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
    // 存在しない商品IDまたはメーカーIDで入庫できず、既存在庫と履歴が変わらないことを確認する
    void stockIn_rejectsUnknownProductOrMakerWithoutChangingInventory() throws Exception {
        Maker maker = createMaker("メーカーA");
        Product product = createProduct("小松菜", maker, 5);
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(5);
        stockDetailRepository.saveAndFlush(detail);
        MockHttpSession session = loginAsAdmin();

        String unknownProductRequest = "{\"productId\":2147483647,\"quantity\":2,\"makerId\":"
                + maker.getId() + ",\"unit\":\"個\",\"category\":\"野菜\"}";
        performStockIn(session, unknownProductRequest)
                .andExpect(status().isNotFound());

        String unknownMakerRequest = "{\"productId\":" + product.getId()
                + ",\"quantity\":2,\"makerId\":2147483647,\"unit\":\"個\",\"category\":\"野菜\"}";
        performStockIn(session, unknownMakerRequest)
                .andExpect(status().isNotFound());

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        StockDetail unchangedDetail = stockDetailRepository
                .findByProductIdAndMakerId(product.getId(), maker.getId()).orElseThrow();
        List<StockHistory> history = stockHistoryRepository
                .findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN");

        assertEquals(5, unchangedProduct.getStock());
        assertEquals(5, unchangedDetail.getQuantity());
        assertTrue(history.isEmpty());
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
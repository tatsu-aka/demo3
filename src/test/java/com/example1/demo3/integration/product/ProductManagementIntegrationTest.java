package com.example1.demo3.integration.product;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
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

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.User;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void admin_canAccessProductRegistrationPage() throws Exception {
        MockHttpSession session = loginAs("admin", "password", "ADMIN");

        mockMvc.perform(get("/products/master/new").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void admin_canCreateProduct_andItAppearsInMasterList() throws Exception {
        MockHttpSession session = loginAs("admin", "password", "ADMIN");

        mockMvc.perform(post("/api/products/master")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apple\",\"category\":\"果物\",\"unit\":\"個\",\"stock\":10,\"costPrice\":100}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apple")));

        mockMvc.perform(get("/api/products/master").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apple")));
    }

    @Test
    void admin_canUpdateProductAndChangeValue() throws Exception {
        Product product = new Product();
        product.setName("Apple");
        product.setCategory("果物");
        product.setUnit("個");
        product.setStock(10);
        product.setCostPrice(100);
        Product saved = productRepository.saveAndFlush(product);

        MockHttpSession session = loginAs("admin", "password", "ADMIN");

        mockMvc.perform(put("/api/products/master/{id}", saved.getId())
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Orange\",\"category\":\"果物\",\"unit\":\"箱\",\"stock\":20,\"costPrice\":150}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orange")));

        mockMvc.perform(get("/api/products/master").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orange")));
    }

    @Test
    void admin_canDeleteProductAndItDisappears() throws Exception {
        Product product = new Product();
        product.setName("Apple");
        product.setCategory("果物");
        product.setUnit("個");
        product.setStock(10);
        product.setCostPrice(100);
        Product saved = productRepository.saveAndFlush(product);

        MockHttpSession session = loginAs("admin", "password", "ADMIN");

        mockMvc.perform(delete("/api/products/master/{id}", saved.getId())
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/master").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Apple"))));
    }

    @Test
    void unauthenticatedUser_isRedirectedToLoginWhenAccessingProductRegistrationPage() throws Exception {
        mockMvc.perform(get("/products/master/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    private MockHttpSession loginAs(String username, String rawPassword, String role) throws Exception {
        createUser(username, rawPassword, role);

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", username)
                        .param("password", rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"))
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    private void createUser(String username, String rawPassword, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }
}

package com.example1.demo3.integration.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.example1.demo3.entity.User;
import com.example1.demo3.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    // 未認証ユーザーが保護されたページにアクセスしようとした場合、ログインページにリダイレクトされることを確認するテスト
    void unauthenticatedUser_isRedirectedToLoginWhenAccessingProtectedPage() throws Exception {
        mockMvc.perform(get("/product"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    // 有効な管理者資格情報でログインした場合、製品ページにリダイレクトされ、アクセスが許可されることを確認するテスト
    void loginWithValidAdminCredentials_redirectsToProductAndAllowsAccess() throws Exception {
        createUser("admin", "password", "ADMIN");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/product").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/makers").session(session))
                .andExpect(status().isOk());
    }

    @Test
    // ユーザーが無効な資格情報でログインしようとした場合、ログインページにリダイレクトされ、エラーメッセージが表示されることを確認するテスト
    void loginWithInvalidCredentials_redirectsToLoginWithError() throws Exception {
        createUser("admin", "password", "ADMIN");

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    // 管理者がADMIN専用API にアクセスできることを確認するテスト
    void adminRole_canAccessAdminApi() throws Exception {
        createUser("admin", "password", "ADMIN");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/users")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new-user\",\"password\":\"pass123\",\"role\":\"USER\"}"))
                .andExpect(status().isOk());
    }

    @Test
    // USERが一般画面にはアクセスできるが、ADMIN専用画面にはアクセスできない境界を確認するテスト
    void userRole_canAccessGeneralPage_butCannotAccessAdminPage() throws Exception {
        createUser("user", "password", "USER");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/product").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/makers").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/products/master").session(session))
                .andExpect(status().isForbidden());
    }

    private void createUser(String username, String rawPassword, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }
}

package com.example1.demo3.unit.config;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example1.demo3.service.CustomUserDetailsService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    //公開パスへのアクセスが許可されていることを確認するテスト
    void publicPaths_areAllowed() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/js/login.js"))
                .andExpect(status().isOk());
    }

    @Test
    //管理者専用パスとAPIへのアクセスが一般ユーザーには制限されていることを確認するテスト
    void adminOnlyPaths_areRestrictedForGeneralUser() throws Exception {
        mockMvc.perform(get("/makers").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/products/master").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/users")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"general-user-api-test\",\"password\":\"password\",\"role\":\"USER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    //管理者ユーザーが管理者専用パスとAPIにアクセスできることを確認するテスト
    void adminUser_canAccessAdminOnlyPaths() throws Exception {
        mockMvc.perform(get("/makers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/master").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"admin-api-allow-test\",\"password\":\"password\",\"role\":\"USER\"}"))
                .andExpect(status().isOk());
    }

    @Test
    //一般ユーザーがアクセス許可された画面とAPIを利用でき、管理者専用のAPIは制限されることを確認するテスト
    void userRole_hasLimitedAccess() throws Exception {
        mockMvc.perform(get("/product").with(user("user").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/stock/list").with(user("user").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/list-by-maker").with(user("user").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"user-api-block-test\",\"password\":\"password\",\"role\":\"USER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    //ログイン成功後に正しいリダイレクトが行われることを確認するテスト
    void login_success_redirectsToProduct() throws Exception {
        when(customUserDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin")
                        .password(passwordEncoder.encode("password"))
                        .roles("ADMIN")
                        .build());

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"));
    }
}

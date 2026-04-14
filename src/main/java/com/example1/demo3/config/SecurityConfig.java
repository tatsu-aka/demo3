package com.example1.demo3.config;

import java.security.Security;

import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example1.demo3.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())// 無効化
                .authorizeHttpRequests(auth -> auth.requestMatchers("/login", "/css/**", "/js/**").permitAll()// ログイン画面はok
                        
                        //権限制御
                        .requestMatchers("/product/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/price/**").hasAnyRole("ADMIN")
                        .requestMatchers("/makers/**").hasAnyRole("ADMIN")
                        .requestMatchers("/stock/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/products/master/**").hasAnyRole("ADMIN")
                        .requestMatchers("/users/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN")
                        .anyRequest().authenticated()// ログイン必須
                )

                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))

                .formLogin(form -> form.loginPage("/login")// login.htmlのパス
                        .loginProcessingUrl("/login")//POST先
                        .defaultSuccessUrl("/product", true)// ログイン成功後の移動先
                        .permitAll())

                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

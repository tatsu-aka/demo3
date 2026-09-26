package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example1.demo3.controller.api.UserInfoController;

@ExtendWith(MockitoExtension.class)
class UserInfoControllerTest {

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserInfoController userInfoController;

    @Test
    //正常系　認証情報からユーザー名と権限を取得
    void me_shouldReturnUsernameAndAuthorities() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getName()).thenReturn("sakan");
        doReturn(authorities).when(authentication).getAuthorities();

        Map<String, Object> result = userInfoController.me(authentication);

        assertEquals("sakan", result.get("username"));
        assertEquals(authorities, result.get("roles"));
    }

    @Test
    //異常系　ユーザーに権限がない場合、空のリストを返す
    void me_shouldReturnEmptyAuthoritiesWhenUserHasNoRoles() {
        when(authentication.getName()).thenReturn("sakan");
        doReturn(List.of()).when(authentication).getAuthorities();

        Map<String, Object> result = userInfoController.me(authentication);

        assertEquals("sakan", result.get("username"));
        assertEquals(List.of(), result.get("roles"));
    }
}
package com.template.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("密码工具类单元测试")
class PasswordUtilTest {

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private PasswordUtil passwordUtil;

    @Test
    @DisplayName("密码加密成功")
    void encode_Success() {
        String rawPassword = "password123";
        when(encoder.encode(rawPassword)).thenReturn("$2a$10$encoded");

        String encodedPassword = passwordUtil.encode(rawPassword);

        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isEqualTo("$2a$10$encoded");
    }

    @Test
    @DisplayName("密码匹配成功")
    void matches_Success() {
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encoded";
        when(encoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean matches = passwordUtil.matches(rawPassword, encodedPassword);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("密码匹配失败 - 错误密码")
    void matches_Fail() {
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encoded";
        when(encoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean matches = passwordUtil.matches(rawPassword, encodedPassword);

        assertThat(matches).isFalse();
    }
}

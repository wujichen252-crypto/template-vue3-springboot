package com.template.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 工具类单元测试")
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "your-256-bit-secret-key-here-for-testing-only";
    private static final Long ACCESS_EXPIRE = 7200000L; // 2小时
    private static final Long REFRESH_EXPIRE = 604800000L; // 7天

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpire", ACCESS_EXPIRE);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpire", REFRESH_EXPIRE);
        jwtUtil.init();
    }

    @Test
    @DisplayName("生成 Access Token 成功")
    void generateAccessToken_Success() {
        Long userId = 1L;

        String token = jwtUtil.generateAccessToken(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 验证 Token 可以解析
        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("type")).isEqualTo("access");
        assertThat(claims.get("userId")).isEqualTo(userId.intValue());
    }

    @Test
    @DisplayName("生成 Refresh Token 成功")
    void generateRefreshToken_Success() {
        Long userId = 1L;

        String token = jwtUtil.generateRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 验证 Token 可以解析
        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("type")).isEqualTo("refresh");
    }

    @Test
    @DisplayName("解析 Token 成功")
    void parseToken_Success() {
        Long userId = 1L;
        String token = jwtUtil.generateAccessToken(userId);

        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("解析 Token 失败 - 无效 Token")
    void parseToken_Invalid() {
        assertThatThrownBy(() -> jwtUtil.parseToken("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("检查 Token 是否过期 - 未过期")
    void isTokenExpired_False() {
        Long userId = 1L;
        String token = jwtUtil.generateAccessToken(userId);

        boolean expired = jwtUtil.isTokenExpired(token);

        assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("检查 Token 是否过期 - 已过期")
    void isTokenExpired_True() {
        // 生成一个过期的 Token
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() - 1000); // 1秒前过期

        String expiredToken = Jwts.builder()
                .subject("1")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();

        boolean expired = jwtUtil.isTokenExpired(expiredToken);

        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("检查 Token 是否过期 - 无效 Token 返回过期")
    void isTokenExpired_InvalidToken() {
        boolean expired = jwtUtil.isTokenExpired("invalid.token");

        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("从 Token 获取用户 ID")
    void getUserIdFromToken_Success() {
        Long userId = 123L;
        String token = jwtUtil.generateAccessToken(userId);

        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("从 Token 获取类型 - Access")
    void getTypeFromToken_Access() {
        String token = jwtUtil.generateAccessToken(1L);

        String type = jwtUtil.getTypeFromToken(token);

        assertThat(type).isEqualTo("access");
    }

    @Test
    @DisplayName("从 Token 获取类型 - Refresh")
    void getTypeFromToken_Refresh() {
        String token = jwtUtil.generateRefreshToken(1L);

        String type = jwtUtil.getTypeFromToken(token);

        assertThat(type).isEqualTo("refresh");
    }

    @Test
    @DisplayName("获取 Access Token 过期时间配置")
    void getAccessExpire_Success() {
        Long expire = jwtUtil.getAccessExpire();

        assertThat(expire).isEqualTo(ACCESS_EXPIRE);
    }
}

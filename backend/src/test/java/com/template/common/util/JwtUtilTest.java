// file: backend/src/test/java/com/template/common/util/JwtUtilTest.java
package com.template.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
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
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "myTestSecretKeyForJwtTokenGeneration1234567890";
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
    void shouldGenerateAccessTokenSuccessfully() {
        // given
        Long userId = 1L;

        // when
        String token = jwtUtil.generateAccessToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("userId")).isEqualTo(userId.intValue());
        assertThat(claims.get("type")).isEqualTo("access");
    }

    @Test
    void shouldGenerateRefreshTokenSuccessfully() {
        // given
        Long userId = 1L;

        // when
        String token = jwtUtil.generateRefreshToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("userId")).isEqualTo(userId.intValue());
        assertThat(claims.get("type")).isEqualTo("refresh");
    }

    @Test
    void shouldParseTokenSuccessfully() {
        // given
        Long userId = 1L;
        String token = jwtUtil.generateAccessToken(userId);

        // when
        Claims claims = jwtUtil.parseToken(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("userId")).isEqualTo(userId.intValue());
        assertThat(claims.get("type")).isEqualTo("access");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenParseInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThatThrownBy(() -> jwtUtil.parseToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldGetUserIdFromTokenSuccessfully() {
        // given
        Long userId = 123L;
        String token = jwtUtil.generateAccessToken(userId);

        // when
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void shouldGetTypeFromTokenSuccessfully() {
        // given
        String accessToken = jwtUtil.generateAccessToken(1L);
        String refreshToken = jwtUtil.generateRefreshToken(1L);

        // when
        String accessType = jwtUtil.getTypeFromToken(accessToken);
        String refreshType = jwtUtil.getTypeFromToken(refreshToken);

        // then
        assertThat(accessType).isEqualTo("access");
        assertThat(refreshType).isEqualTo("refresh");
    }

    @Test
    void shouldReturnNotExpiredForValidToken() {
        // given
        String token = jwtUtil.generateAccessToken(1L);

        // when
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // then
        assertThat(isExpired).isFalse();
    }

    @Test
    void shouldReturnExpiredForExpiredToken() {
        // given - 生成一个已过期 token
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date past = new Date(now.getTime() - 1000); // 1秒前过期

        String expiredToken = Jwts.builder()
                .subject("1")
                .claim("userId", 1)
                .claim("type", "access")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(past)
                .signWith(key)
                .compact();

        // when
        boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    void shouldReturnExpiredForInvalidToken() {
        // given
        String invalidToken = "invalid.token";

        // when
        boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    void shouldGetAccessExpireSuccessfully() {
        // when
        Long expire = jwtUtil.getAccessExpire();

        // then
        assertThat(expire).isEqualTo(ACCESS_EXPIRE);
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;

        // when
        String token1 = jwtUtil.generateAccessToken(userId1);
        String token2 = jwtUtil.generateAccessToken(userId2);

        // then
        assertThat(token1).isNotEqualTo(token2);

        Claims claims1 = jwtUtil.parseToken(token1);
        Claims claims2 = jwtUtil.parseToken(token2);

        assertThat(claims1.getSubject()).isEqualTo("1");
        assertThat(claims2.getSubject()).isEqualTo("2");
    }

    @Test
    void shouldGenerateDifferentTokensForSameUserAtDifferentTime() throws InterruptedException {
        // given
        Long userId = 1L;

        // when
        String token1 = jwtUtil.generateAccessToken(userId);
        Thread.sleep(10); // 确保时间不同
        String token2 = jwtUtil.generateAccessToken(userId);

        // then
        assertThat(token1).isNotEqualTo(token2);

        // 但解析出的 userId 应该相同
        assertThat(jwtUtil.getUserIdFromToken(token1)).isEqualTo(jwtUtil.getUserIdFromToken(token2));
    }
}

package com.yc.admin.auth.service;

import com.yc.admin.auth.config.JwtProperties;
import com.yc.admin.auth.dto.AuthLoginUser;
import com.yc.admin.system.api.dto.AuthUserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TokenService 单元测试
 *
 * @author yc
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService 单元测试")
class TokenServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private TokenService tokenService;

    private AuthLoginUser authLoginUser;
    private final String testSecret = "test-secret-key-for-jwt-token-generation-and-validation-must-be-long-enough";
    private final long testExpiration = 3600000L; // 1小时

    @BeforeEach
    void setUp() {
        // 设置JWT配置
        lenient().when(jwtProperties.getSecret()).thenReturn(testSecret);
        lenient().when(jwtProperties.getExpiration()).thenReturn(testExpiration);

        // 创建测试用户DTO
        AuthUserDTO userDTO = AuthUserDTO.builder()
                .id(1L)
                .userName("testuser")
                .password("password")
                .status("0")
                .nickName("测试用户")
                .email("test@example.com")
                .phone("13800138000")
                .avatar("/avatar/test.jpg")
                .build();

        // 创建测试登录用户
        authLoginUser = AuthLoginUser.builder()
                .user(userDTO)
                .permissions(Set.of("user:read", "user:write"))
                .roles(Set.of("ADMIN", "USER"))
                .loginTime(System.currentTimeMillis())
                .expireTime(System.currentTimeMillis() + testExpiration)
                .build();
    }

    @Nested
    @DisplayName("创建令牌测试")
    class CreateTokenTests {

        @Test
        @DisplayName("成功创建令牌")
        void testCreateToken_Success() {
            // When
            String token = tokenService.createToken(authLoginUser);

            // Then
            assertThat(token).isNotNull().isNotEmpty();
            assertThat(authLoginUser.getToken()).isNotNull();
            
            // 验证缓存操作
            verify(cacheService).set(anyString(), eq(authLoginUser), eq(testExpiration), eq(TimeUnit.MILLISECONDS));
        }

        @Test
        @DisplayName("创建令牌时设置用户UUID")
        void testCreateToken_SetsUserUuid() {
            // When
            String token = tokenService.createToken(authLoginUser);

            // Then
            assertThat(authLoginUser.getToken()).isNotNull();
            
            // 解析令牌验证UUID
            Claims claims = tokenService.parseToken(token);
            assertThat(claims.get("login_user_uuid")).isEqualTo(authLoginUser.getToken());
        }
    }

    @Nested
    @DisplayName("获取登录用户测试")
    class GetLoginUserTests {

        @Test
        @DisplayName("成功获取登录用户")
        void testGetLoginUser_Success() {
            // Given
            String token = tokenService.createToken(authLoginUser);
            String userKey = "login_user:" + authLoginUser.getToken();
            when(cacheService.get(userKey)).thenReturn(authLoginUser);

            // When
            AuthLoginUser result = tokenService.getLoginUser(token);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            verify(cacheService).get(userKey);
        }

        @Test
        @DisplayName("令牌为空时返回null")
        void testGetLoginUser_EmptyToken() {
            // When
            AuthLoginUser result = tokenService.getLoginUser("");

            // Then
            assertThat(result).isNull();
            verifyNoInteractions(cacheService);
        }

        @Test
        @DisplayName("令牌为null时返回null")
        void testGetLoginUser_NullToken() {
            // When
            AuthLoginUser result = tokenService.getLoginUser(null);

            // Then
            assertThat(result).isNull();
            verifyNoInteractions(cacheService);
        }

        @Test
        @DisplayName("无效令牌时返回null")
        void testGetLoginUser_InvalidToken() {
            // When
            AuthLoginUser result = tokenService.getLoginUser("invalid-token");

            // Then
            assertThat(result).isNull();
            verifyNoInteractions(cacheService);
        }

        @Test
        @DisplayName("缓存中无用户信息时返回null")
        void testGetLoginUser_UserNotInCache() {
            // Given
            String token = tokenService.createToken(authLoginUser);
            String userKey = "login_user:" + authLoginUser.getToken();
            when(cacheService.get(userKey)).thenReturn(null);

            // When
            AuthLoginUser result = tokenService.getLoginUser(token);

            // Then
            assertThat(result).isNull();
            verify(cacheService).get(userKey);
        }
    }

    @Nested
    @DisplayName("验证令牌测试")
    class VerifyTokenTests {

        @Test
        @DisplayName("有效令牌验证成功")
        void testVerifyToken_ValidToken() {
            // Given
            authLoginUser.setToken("test-uuid");
            authLoginUser.setExpireTime(System.currentTimeMillis() + testExpiration);

            // When
            boolean result = tokenService.verifyToken(authLoginUser);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("用户为null时验证失败")
        void testVerifyToken_NullUser() {
            // When
            boolean result = tokenService.verifyToken(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("令牌为空时验证失败")
        void testVerifyToken_EmptyToken() {
            // Given
            authLoginUser.setToken("");

            // When
            boolean result = tokenService.verifyToken(authLoginUser);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("令牌即将过期时自动刷新")
        void testVerifyToken_RefreshWhenNearExpiry() {
            // Given - 设置令牌在15分钟后过期（小于20分钟阈值）
            authLoginUser.setToken("test-uuid");
            authLoginUser.setExpireTime(System.currentTimeMillis() + 900000L); // 15分钟

            // When
            boolean result = tokenService.verifyToken(authLoginUser);

            // Then
            assertThat(result).isTrue();
            verify(cacheService).set(anyString(), eq(authLoginUser), eq(testExpiration), eq(TimeUnit.MILLISECONDS));
        }
    }

    @Nested
    @DisplayName("刷新令牌测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("成功刷新令牌")
        void testRefreshToken_Success() throws InterruptedException {
            // Given
            authLoginUser.setToken("test-uuid");
            long oldLoginTime = authLoginUser.getLoginTime();
            long oldExpireTime = authLoginUser.getExpireTime();

            // 等待1毫秒确保时间差异
            Thread.sleep(1);

            // When
            tokenService.refreshToken(authLoginUser);

            // Then
            assertThat(authLoginUser.getLoginTime()).isGreaterThan(oldLoginTime);
            assertThat(authLoginUser.getExpireTime()).isGreaterThan(oldExpireTime);
            verify(cacheService).set(anyString(), eq(authLoginUser), eq(testExpiration), eq(TimeUnit.MILLISECONDS));
        }
    }

    @Nested
    @DisplayName("删除令牌测试")
    class DeleteTokenTests {

        @Test
        @DisplayName("成功删除令牌")
        void testDeleteToken_Success() {
            // Given
            String token = tokenService.createToken(authLoginUser);
            String userKey = "login_user:" + authLoginUser.getToken();

            // When
            tokenService.deleteToken(token);

            // Then
            verify(cacheService).delete(userKey);
        }

        @Test
        @DisplayName("令牌为空时不执行删除")
        void testDeleteToken_EmptyToken() {
            // When
            tokenService.deleteToken("");

            // Then
            verifyNoInteractions(cacheService);
        }

        @Test
        @DisplayName("令牌为null时不执行删除")
        void testDeleteToken_NullToken() {
            // When
            tokenService.deleteToken(null);

            // Then
            verifyNoInteractions(cacheService);
        }

        @Test
        @DisplayName("无效令牌时不执行删除")
        void testDeleteToken_InvalidToken() {
            // When
            tokenService.deleteToken("invalid-token");

            // Then
            verifyNoInteractions(cacheService);
        }
    }

    @Nested
    @DisplayName("解析令牌测试")
    class ParseTokenTests {

        @Test
        @DisplayName("成功解析有效令牌")
        void testParseToken_ValidToken() {
            // Given
            String token = tokenService.createToken(authLoginUser);

            // When
            Claims claims = tokenService.parseToken(token);

            // Then
            assertThat(claims).isNotNull();
            assertThat(claims.get("login_user_uuid")).isEqualTo(authLoginUser.getToken());
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        }

        @Test
        @DisplayName("解析无效令牌抛出异常")
        void testParseToken_InvalidToken() {
            // When & Then
            assertThatThrownBy(() -> tokenService.parseToken("invalid-token"))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("解析过期令牌抛出异常")
        void testParseToken_ExpiredToken() {
            // Given - 创建一个已过期的令牌
            lenient().when(jwtProperties.getExpiration()).thenReturn(-1000L); // 负数表示已过期
            String expiredToken = tokenService.createToken(authLoginUser);
            
            // 恢复正常过期时间
            lenient().when(jwtProperties.getExpiration()).thenReturn(testExpiration);

            // When & Then
            assertThatThrownBy(() -> tokenService.parseToken(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class);
        }
    }

    @Nested
    @DisplayName("获取令牌过期时间测试")
    class GetTokenExpirationTests {

        @Test
        @DisplayName("获取令牌过期时间（秒）")
        void testGetTokenExpiration() {
            // When
            long expiration = tokenService.getTokenExpiration();

            // Then
            assertThat(expiration).isEqualTo(testExpiration / 1000);
        }
    }
}
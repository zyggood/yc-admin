package com.yc.admin.auth.service;

import com.yc.admin.auth.dto.AuthLoginUser;
import com.yc.admin.auth.dto.LoginDTO;
import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.api.RoleApiService;
import com.yc.admin.system.api.UserApiService;
import com.yc.admin.system.api.dto.AuthRoleDTO;
import com.yc.admin.system.api.dto.AuthUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginService 单元测试
 *
 * @author yc
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService 测试")
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserApiService userApiService;

    @Mock
    private RoleApiService roleApiService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @InjectMocks
    private LoginService loginService;

    private LoginDTO loginDTO;
    private AuthUserDTO authUserDTO;
    private AuthLoginUser authLoginUser;
    private AuthRoleDTO authRoleDTO;

    @BeforeEach
    void setUp() {
        // 模拟RequestContextHolder - 使用真实的ServletRequestAttributes
        ServletRequestAttributes realAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(realAttributes);
        
        // 设置默认的HttpServletRequest mock行为
        lenient().when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Test Browser)");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        
        // 初始化测试数据
        loginDTO = LoginDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        authUserDTO = AuthUserDTO.builder()
                .id(1L)
                .userName("testuser")
                .password("$2a$10$encoded_password")
                .status("0")
                .nickName("测试用户")
                .email("test@example.com")
                .phone("13800138000")
                .build();

        authLoginUser = AuthLoginUser.builder()
                .user(authUserDTO)
                .permissions(Set.of("system:user:list", "system:user:add"))
                .roles(Set.of("admin", "user"))
                .token("test-token")
                .loginTime(System.currentTimeMillis())
                .expireTime(System.currentTimeMillis() + 3600000)
                .build();

        authRoleDTO = AuthRoleDTO.builder()
                .id(1L)
                .roleKey("admin")
                .roleName("管理员")
                .status("0")
                .build();
    }

    @AfterEach
    void tearDown() {
        // 清理RequestContextHolder
        RequestContextHolder.resetRequestAttributes();
        // 清理SecurityContextHolder
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void testLogin_Success() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authLoginUser);
            when(tokenService.createToken(authLoginUser)).thenReturn("jwt-token");
            when(tokenService.getTokenExpiration()).thenReturn(3600L);
            when(userApiService.findById(1L)).thenReturn(Optional.of(authUserDTO));

            // When
            Map<String, Object> result = loginService.login(loginDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("token")).isEqualTo("jwt-token");
            assertThat(result.get("tokenType")).isEqualTo("Bearer");
            assertThat(result.get("expiresIn")).isEqualTo(3600L);
            assertThat(result.get("user")).isEqualTo(Optional.of(authUserDTO));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).createToken(authLoginUser);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("用户名为空时登录失败")
        void testLogin_EmptyUsername() {
            // Given
            loginDTO.setUsername("");

            // When & Then
            assertThatThrownBy(() -> loginService.login(loginDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户名不能为空");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("用户名为null时登录失败")
        void testLogin_NullUsername() {
            // Given
            loginDTO.setUsername(null);

            // When & Then
            assertThatThrownBy(() -> loginService.login(loginDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户名不能为空");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("密码为空时登录失败")
        void testLogin_EmptyPassword() {
            // Given
            loginDTO.setPassword("");

            // When & Then
            assertThatThrownBy(() -> loginService.login(loginDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("密码不能为空");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("密码为null时登录失败")
        void testLogin_NullPassword() {
            // Given
            loginDTO.setPassword(null);

            // When & Then
            assertThatThrownBy(() -> loginService.login(loginDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("密码不能为空");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("认证失败时登录失败")
        void testLogin_AuthenticationFailed() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("用户名或密码错误"));
            when(userApiService.findAuthUserByUsername("testuser"))
                    .thenReturn(Optional.of(authUserDTO));

            // When & Then
            assertThatThrownBy(() -> loginService.login(loginDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("登录失败: 用户名或密码错误");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("用户不存在时登录失败")
        void testLogin_UserNotFound() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("用户不存在"));
            when(userApiService.findAuthUserByUsername("testuser"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loginService.login(loginDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("登录失败: 用户不存在");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(eventPublisher).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("登出测试")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void testLogout_Success() {
            // Given
            lenient().when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
            when(tokenService.getLoginUser("jwt-token")).thenReturn(authLoginUser);

            // When
            loginService.logout(request);

            // Then
            verify(tokenService).getLoginUser("jwt-token");
            verify(tokenService).deleteToken("jwt-token");
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("无token时登出")
        void testLogout_NoToken() {
            // Given
            when(request.getHeader("Authorization")).thenReturn(null);

            // When
            loginService.logout(request);

            // Then
            verify(tokenService, never()).getLoginUser(any());
            verify(tokenService, never()).deleteToken(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("token格式错误时登出")
        void testLogout_InvalidTokenFormat() {
            // Given
            when(request.getHeader("Authorization")).thenReturn("InvalidToken");

            // When
            loginService.logout(request);

            // Then
            verify(tokenService, never()).getLoginUser(any());
            verify(tokenService, never()).deleteToken(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("token无效时登出")
        void testLogout_InvalidToken() {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
            when(tokenService.getLoginUser("invalid-token")).thenReturn(null);

            // When
            loginService.logout(request);

            // Then
            verify(tokenService).getLoginUser("invalid-token");
            verify(tokenService).deleteToken("invalid-token");
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("刷新令牌测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("刷新令牌成功")
        void testRefreshToken_Success() {
            // Given
            String oldToken = "old-token";
            String newToken = "new-token";
            when(tokenService.getLoginUser(oldToken)).thenReturn(authLoginUser);
            when(tokenService.verifyToken(authLoginUser)).thenReturn(true);
            when(tokenService.createToken(authLoginUser)).thenReturn(newToken);
            when(tokenService.getTokenExpiration()).thenReturn(3600L);

            // When
            Map<String, Object> result = loginService.refreshToken(oldToken);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("token")).isEqualTo(newToken);
            assertThat(result.get("tokenType")).isEqualTo("Bearer");
            assertThat(result.get("expiresIn")).isEqualTo(3600L);

            verify(tokenService).getLoginUser(oldToken);
            verify(tokenService).verifyToken(authLoginUser);
            verify(tokenService).createToken(authLoginUser);
            verify(tokenService).deleteToken(oldToken);
        }

        @Test
        @DisplayName("令牌为空时刷新失败")
        void testRefreshToken_EmptyToken() {
            // When & Then
            assertThatThrownBy(() -> loginService.refreshToken(""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("令牌不能为空");

            verify(tokenService, never()).getLoginUser(any());
        }

        @Test
        @DisplayName("令牌为null时刷新失败")
        void testRefreshToken_NullToken() {
            // When & Then
            assertThatThrownBy(() -> loginService.refreshToken(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("令牌不能为空");

            verify(tokenService, never()).getLoginUser(any());
        }

        @Test
        @DisplayName("令牌无效时刷新失败")
        void testRefreshToken_InvalidToken() {
            // Given
            String invalidToken = "invalid-token";
            when(tokenService.getLoginUser(invalidToken)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> loginService.refreshToken(invalidToken))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("刷新令牌失败: 令牌无效");

            verify(tokenService).getLoginUser(invalidToken);
            verify(tokenService, never()).verifyToken(any());
        }

        @Test
        @DisplayName("令牌验证失败时刷新失败")
        void testRefreshToken_VerifyFailed() {
            // Given
            String token = "expired-token";
            when(tokenService.getLoginUser(token)).thenReturn(authLoginUser);
            when(tokenService.verifyToken(authLoginUser)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> loginService.refreshToken(token))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("刷新令牌失败: 令牌验证失败");

            verify(tokenService).getLoginUser(token);
            verify(tokenService).verifyToken(authLoginUser);
            verify(tokenService, never()).createToken(any());
        }
    }

    @Nested
    @DisplayName("获取当前用户测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("获取当前登录用户成功")
        void testGetCurrentLoginUser_Success() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authLoginUser);

            // When
            AuthLoginUser result = loginService.getCurrentLoginUser();

            // Then
            assertThat(result).isEqualTo(authLoginUser);
        }

        @Test
        @DisplayName("无认证信息时获取当前登录用户")
        void testGetCurrentLoginUser_NoAuthentication() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            AuthLoginUser result = loginService.getCurrentLoginUser();

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Principal不是AuthLoginUser时获取当前登录用户")
        void testGetCurrentLoginUser_WrongPrincipalType() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn("wrong-type");

            // When
            AuthLoginUser result = loginService.getCurrentLoginUser();

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("获取当前用户信息成功")
        void testGetCurrentUser_Success() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authLoginUser);
            when(userApiService.findById(1L)).thenReturn(Optional.of(authUserDTO));

            // When
            AuthUserDTO result = loginService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(authUserDTO);
            verify(userApiService).findById(1L);
        }

        @Test
        @DisplayName("无登录用户时获取当前用户信息")
        void testGetCurrentUser_NoLoginUser() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            AuthUserDTO result = loginService.getCurrentUser();

            // Then
            assertThat(result).isNull();
            verify(userApiService, never()).findById(any());
        }

        @Test
        @DisplayName("获取当前用户ID成功")
        void testGetCurrentUserId_Success() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authLoginUser);

            // When
            Long result = loginService.getCurrentUserId();

            // Then
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("获取当前用户名成功")
        void testGetCurrentUsername_Success() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authLoginUser);

            // When
            String result = loginService.getCurrentUsername();

            // Then
            assertThat(result).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("获取用户权限测试")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("获取当前用户权限成功")
        void testGetCurrentUserPermissions_Success() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authLoginUser);
            when(roleApiService.findAuthRolesByUserId(1L))
                    .thenReturn(List.of(authRoleDTO));

            // When
            Map<String, Object> result = loginService.getCurrentUserPermissions();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("userId")).isEqualTo(1L);
            assertThat(result.get("username")).isEqualTo("testuser");
            assertThat(result.get("authorities")).isEqualTo(authLoginUser.getAuthorities());
            assertThat(result.get("roles")).isEqualTo(List.of("admin"));

            verify(roleApiService).findAuthRolesByUserId(1L);
        }

        @Test
        @DisplayName("用户未登录时获取权限失败")
        void testGetCurrentUserPermissions_NotLoggedIn() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> loginService.getCurrentUserPermissions())
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户未登录");

            verify(roleApiService, never()).findAuthRolesByUserId(any());
        }
    }

    @Nested
    @DisplayName("令牌相关测试")
    class TokenTests {

        @Test
        @DisplayName("创建令牌成功")
        void testCreateToken_Success() {
            // Given
            String expectedToken = "jwt-token";
            when(tokenService.createToken(authLoginUser)).thenReturn(expectedToken);

            // When
            String result = loginService.createToken(authLoginUser);

            // Then
            assertThat(result).isEqualTo(expectedToken);
            verify(tokenService).createToken(authLoginUser);
        }

        @Test
        @DisplayName("获取令牌过期时间成功")
        void testGetTokenExpiration_Success() {
            // Given
            long expectedExpiration = 3600L;
            when(tokenService.getTokenExpiration()).thenReturn(expectedExpiration);

            // When
            long result = loginService.getTokenExpiration();

            // Then
            assertThat(result).isEqualTo(expectedExpiration);
            verify(tokenService).getTokenExpiration();
        }
    }
}
package com.yc.admin.system.user.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.dept.entity.Dept;
import com.yc.admin.system.dept.repository.DeptRepository;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.system.user.dto.UserDTO;

import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import com.yc.admin.system.user.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 *
 * @author yc
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeptRepository deptRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private PasswordEncoder passwordEncoder;



    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO.CreateDTO createDTO;
    private UserDTO.UpdateDTO updateDTO;
    private Dept testDept;
    private Role testRole;
    private Role testRole2;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setNickName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setSex("0");
        testUser.setAvatar("/avatar/test.jpg");
        testUser.setPassword("encodedPassword");
        testUser.setStatus("0");
        testUser.setDelFlag(0);
        testUser.setCreateBy("admin");
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateBy("admin");
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setRemark("测试用户");

        createDTO = UserDTO.CreateDTO.builder()
                .userName("newuser")
                .nickName("新用户")
                .email("new@example.com")
                .phone("13900139000")
                .sex("0")
                .avatar("/avatar/new.jpg")
                .password("123456")
                .status("0")
                .remark("新用户")
                .deptId(1L)
                .roleIds(Arrays.asList(1L, 2L))
                .build();

        updateDTO = new UserDTO.UpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setUserName("updateduser");
        updateDTO.setNickName("更新用户");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPhone("13700137000");
        updateDTO.setSex("1");
        updateDTO.setAvatar("/avatar/updated.jpg");
        updateDTO.setStatus("0");
        updateDTO.setRemark("更新用户");
        updateDTO.setDeptId(2L);
        updateDTO.setRoleIds(Arrays.asList(2L, 3L));

        testDept = new Dept();
        testDept.setId(1L);
        testDept.setDeptName("测试部门");
        testDept.setDelFlag(0);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("测试角色");
        testRole.setDelFlag(0);
        
        testRole2 = new Role();
        testRole2.setId(2L);
        testRole2.setRoleName("测试角色2");
        testRole2.setDelFlag(0);
    }

    @Nested
    @DisplayName("查询测试")
    class QueryTests {

        @Test
        @DisplayName("根据ID查询用户 - 成功")
        void testFindById_Success() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // UserDTOConverter现在是静态方法，不需要mock

        // When
        UserDTO result = userService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserName()).isEqualTo("testuser");
            assertThat(result.getNickName()).isEqualTo("测试用户");
        }

        @Test
        @DisplayName("根据ID查询用户 - 用户不存在")
        void testFindById_UserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户不存在: 999");
        }

        @Test
        @DisplayName("根据用户名查询用户 - 成功")
        void testFindByUsername_Success() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("testuser", 0)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.findByUsername("testuser");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUserName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("根据用户名查询用户 - 用户不存在")
        void testFindByUsername_UserNotFound() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("nonexistent", 0)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userService.findByUsername("nonexistent");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据邮箱查询用户 - 成功")
        void testFindByEmail_Success() {
            // Given
            when(userRepository.findByEmailAndDelFlag("test@example.com", 0)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.findByEmail("test@example.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("根据手机号查询用户 - 成功")
        void testFindByPhone_Success() {
            // Given
            when(userRepository.findByPhoneAndDelFlag("13800138000", 0)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.findByPhone("13800138000");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getPhone()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("查询所有用户")
        void testFindAll() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
            when(userRepository.findByDelFlagOrderByCreateTimeDesc(0, pageable)).thenReturn(userPage);
        // UserDTOConverter现在是静态方法，不需要mock

        // When
        Page<UserDTO> result = userService.findAll(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("分页查询用户")
        void testFindByConditions() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
            when(userRepository.findByConditions("testuser", "测试用户", "13800138000", "0", 0, pageable)).thenReturn(userPage);
        // UserDTOConverter现在是静态方法，不需要mock

        // When
        Page<UserDTO> result = userService.findByConditions("testuser", "测试用户", "13800138000", "0", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("创建测试")
    class CreateTests {

        @Test
        @DisplayName("创建用户 - 成功")
        void testCreateUser_Success() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDelFlag("new@example.com", 0)).thenReturn(Optional.empty());
            when(userRepository.findByPhoneAndDelFlag("13900139000", 0)).thenReturn(Optional.empty());
            when(deptRepository.findById(1L)).thenReturn(Optional.of(testDept));
            when(roleRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(testRole, testRole2));
            when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDTO result = userService.createUser(createDTO);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(userRoleService).assignRolesToUser(any(Long.class), eq(Arrays.asList(1L, 2L)));
        }

        @Test
        @DisplayName("创建用户 - 用户名已存在")
        void testCreateUser_UsernameExists() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户名已存在: newuser");
        }

        @Test
        @DisplayName("创建用户 - 邮箱已存在")
        void testCreateUser_EmailExists() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDelFlag("new@example.com", 0)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("邮箱已存在: new@example.com");
        }

        @Test
        @DisplayName("创建用户 - 手机号已存在")
        void testCreateUser_PhoneExists() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDelFlag("new@example.com", 0)).thenReturn(Optional.empty());
            when(userRepository.findByPhoneAndDelFlag("13900139000", 0)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("手机号已存在: 13900139000");
        }

        @Test
        @DisplayName("创建用户 - 部门不存在")
        void testCreateUser_DeptNotExists() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDelFlag("new@example.com", 0)).thenReturn(Optional.empty());
            when(userRepository.findByPhoneAndDelFlag("13900139000", 0)).thenReturn(Optional.empty());
            when(deptRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("部门不存在: 1");
        }

        @Test
        @DisplayName("创建用户 - 角色不存在")
        void testCreateUser_RoleNotExists() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDelFlag("new@example.com", 0)).thenReturn(Optional.empty());
            when(userRepository.findByPhoneAndDelFlag("13900139000", 0)).thenReturn(Optional.empty());
            when(deptRepository.findById(1L)).thenReturn(Optional.of(testDept));
            when(roleRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList()); // 返回空列表

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("角色不存在或已删除");
        }
    }

    @Nested
    @DisplayName("更新测试")
    class UpdateTests {

        @Test
        @DisplayName("更新用户 - 成功")
        void testUpdateUser_Success() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUserNameAndIdNotAndDelFlag("updateduser", 1L, 0)).thenReturn(false);
            when(userRepository.existsByEmailAndIdNotAndDelFlag("updated@example.com", 1L, 0)).thenReturn(false);
            when(userRepository.existsByPhoneAndIdNotAndDelFlag("13700137000", 1L, 0)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDTO result = userService.updateUser(1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(userRoleService).assignRolesToUser(eq(1L), eq(Arrays.asList(2L, 3L)));
        }

        @Test
        @DisplayName("更新用户 - 用户不存在")
        void testUpdateUser_UserNotExists() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户不存在: 1");
        }

        @Test
        @DisplayName("更新用户状态 - 成功")
        void testUpdateUserStatus_Success() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        // UserDTOConverter现在是静态方法，不需要mock

        // When
        UserDTO result = userService.updateUserStatus(1L, "1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("1");
        verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("批量更新用户状态 - 成功")
        void testBatchUpdateStatus_Success() {
            // Given
            List<Long> userIds = Arrays.asList(1L, 2L);
            when(userRepository.updateStatusByIds(userIds, "1")).thenReturn(2);

            // When
            int result = userService.batchUpdateStatus(userIds, "1");

            // Then
            assertThat(result).isEqualTo(2);
            verify(userRepository).updateStatusByIds(userIds, "1");
        }

        @Test
        @DisplayName("重置密码 - 成功")
        void testResetPassword_Success() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newpassword")).thenReturn("newEncodedPassword");
            when(userRepository.resetPassword(1L, "newEncodedPassword")).thenReturn(1);

            // When
            int result = userService.resetPassword(1L, "newpassword");

            // Then
            assertThat(result).isEqualTo(1);
            verify(userRepository).resetPassword(1L, "newEncodedPassword");
        }
    }

    @Nested
    @DisplayName("删除测试")
    class DeleteTests {

        @Test
        @DisplayName("删除用户 - 成功")
        void testDeleteUser_Success() {
            // Given
            Long userId = 2L; // 使用非管理员用户ID
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.deleteUser(userId);

            // Then
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("删除用户 - 用户不存在")
        void testDeleteUser_UserNotExists() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("用户不存在: 999");
        }

        @Test
        @DisplayName("删除用户 - 不能删除管理员")
        void testDeleteUser_CannotDeleteAdmin() {
            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(User.ADMIN_USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("不能删除管理员用户");
        }

        @Test
        @DisplayName("批量删除用户 - 成功")
        void testBatchDeleteUsers_Success() {
            // Given
            List<Long> userIds = Arrays.asList(2L, 3L);
            when(userRepository.deleteByIds(userIds)).thenReturn(2);

            // When
            int result = userService.batchDeleteUsers(userIds);

            // Then
            assertThat(result).isEqualTo(2);
            verify(userRepository).deleteByIds(userIds);
        }

        @Test
        @DisplayName("批量删除用户 - 包含管理员")
        void testBatchDeleteUsers_ContainsAdmin() {
            // Given

            // When & Then
            assertThatThrownBy(() -> userService.batchDeleteUsers(Arrays.asList(User.ADMIN_USER_ID, 2L)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("不能删除管理员用户");
        }
    }

    @Nested
    @DisplayName("可用性检查测试")
    class AvailabilityTests {

        @Test
        @DisplayName("检查用户名可用性 - 可用")
        void testIsUserNameAvailable_Available() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("newuser", 0)).thenReturn(Optional.empty());

            // When
            boolean result = userService.isUserNameAvailable("newuser", null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("检查用户名可用性 - 不可用")
        void testIsUserNameAvailable_NotAvailable() {
            // Given
            when(userRepository.findByUserNameAndDelFlag("existinguser", 0)).thenReturn(Optional.of(testUser));

            // When
            boolean result = userService.isUserNameAvailable("existinguser", null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("检查邮箱可用性 - 可用")
        void testIsEmailAvailable_Available() {
            // Given
            when(userRepository.findByEmailAndDelFlag("new@example.com", 0)).thenReturn(Optional.empty());

            // When
            boolean result = userService.isEmailAvailable("new@example.com", null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("检查手机号可用性 - 可用")
        void testIsPhoneAvailable_Available() {
            // Given
            when(userRepository.findByPhoneAndDelFlag("13900139000", 0)).thenReturn(Optional.empty());

            // When
            boolean result = userService.isPhoneAvailable("13900139000", null);

            // Then
            assertThat(result).isTrue();
        }
    }

    // 注意：isValidEmail和isValidPhone是私有方法，通过其他公共方法间接测试

    @Nested
    @DisplayName("边界测试")
    class BoundaryTests {

        @Test
        @DisplayName("用户名长度边界测试")
        void testUserNameLength() {
            // Given
            UserDTO.CreateDTO shortNameDTO = UserDTO.CreateDTO.builder()
                    .userName("a") // 1个字符，小于最小长度2
                    .nickName("测试")
                    .password("123456")
                    .build();

            UserDTO.CreateDTO longNameDTO = UserDTO.CreateDTO.builder()
                    .userName("a".repeat(31)) // 31个字符，超过最大长度30
                    .nickName("测试")
                    .password("123456")
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(shortNameDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户名长度必须在2-30个字符之间");

            assertThatThrownBy(() -> userService.createUser(longNameDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户名长度必须在2-30个字符之间");
        }

        @Test
        @DisplayName("密码长度边界测试")
        void testPasswordLength() {
            // Given
            UserDTO.CreateDTO shortPasswordDTO = UserDTO.CreateDTO.builder()
                    .userName("testuser")
                    .nickName("测试")
                    .password("12345") // 5个字符，小于最小长度6
                    .build();

            UserDTO.CreateDTO longPasswordDTO = UserDTO.CreateDTO.builder()
                    .userName("testuser")
                    .nickName("测试")
                    .password("a".repeat(21)) // 21个字符，超过最大长度20
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(shortPasswordDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("密码长度必须在6-20个字符之间");

            assertThatThrownBy(() -> userService.createUser(longPasswordDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("密码长度必须在6-20个字符之间");
        }
    }
}
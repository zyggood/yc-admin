package com.yc.admin.system.role.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.role.dto.RoleDTO;
import com.yc.admin.system.role.dto.RoleDTOConverter;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.repository.RoleRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RoleService 单元测试
 *
 * @author YC
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@Transactional
@DisplayName("角色服务测试")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleDTOConverter roleDTOConverter;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private RoleDTO testRoleDTO;
    private RoleDTO.CreateDTO createDTO;
    private RoleDTO.UpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("测试角色");
        testRole.setRoleKey("test_role");
        testRole.setRoleSort(1);
        testRole.setDataScope(Role.DataScope.DEPT);
        testRole.setMenuCheckStrictly(true);
        testRole.setDeptCheckStrictly(true);
        testRole.setStatus(Role.Status.NORMAL);
        testRole.setRemark("测试角色备注");
        testRole.setDelFlag(0);
        testRole.setCreateTime(LocalDateTime.now());

        testRoleDTO = new RoleDTO();
        testRoleDTO.setId(1L);
        testRoleDTO.setRoleName("测试角色");
        testRoleDTO.setRoleKey("test_role");
        testRoleDTO.setRoleSort(1);
        testRoleDTO.setDataScope(Role.DataScope.DEPT);
        testRoleDTO.setMenuCheckStrictly(true);
        testRoleDTO.setDeptCheckStrictly(true);
        testRoleDTO.setStatus(Role.Status.NORMAL);
        testRoleDTO.setRemark("测试角色备注");

        createDTO = new RoleDTO.CreateDTO();
        createDTO.setRoleName("新角色");
        createDTO.setRoleKey("new_role");
        createDTO.setRoleSort(2);
        createDTO.setDataScope(Role.DataScope.DEPT);
        createDTO.setMenuCheckStrictly(true);
        createDTO.setDeptCheckStrictly(true);
        createDTO.setStatus(Role.Status.NORMAL);
        createDTO.setRemark("新角色备注");

        updateDTO = new RoleDTO.UpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setRoleName("更新角色");
        updateDTO.setRoleKey("updated_role");
        updateDTO.setRoleSort(3);
        updateDTO.setDataScope(Role.DataScope.ALL);
        updateDTO.setMenuCheckStrictly(false);
        updateDTO.setDeptCheckStrictly(false);
        updateDTO.setStatus(Role.Status.NORMAL);
        updateDTO.setRemark("更新角色备注");
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodsTest {

        @Test
        @DisplayName("根据ID查询角色 - 成功")
        void testFindById_Success() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleDTOConverter.toDTO(testRole)).thenReturn(testRoleDTO);

            // When
            RoleDTO result = roleService.findById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getRoleName()).isEqualTo("测试角色");
            verify(roleRepository).findById(1L);
            verify(roleDTOConverter).toDTO(testRole);
        }

        @Test
        @DisplayName("根据ID查询角色 - 角色不存在")
        void testFindById_NotFound() {
            // Given
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.findById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色不存在");
            verify(roleRepository).findById(999L);
            verify(roleDTOConverter, never()).toDTO(any());
        }

        @Test
        @DisplayName("根据ID查询角色 - 空ID")
        void testFindById_NullId() {
            // When & Then
            assertThatThrownBy(() -> roleService.findById(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色ID不能为空");
            verify(roleRepository, never()).findById(any());
        }

        @Test
        @DisplayName("根据用户ID查询角色列表 - 成功")
        void testFindByUserId_Success() {
            // Given
            List<Role> roles = Arrays.asList(testRole);
            when(roleRepository.findByUserId(1L)).thenReturn(roles);

            // When
            List<Role> result = roleService.findByUserId(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(roleRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("根据用户ID查询角色列表 - 空用户ID")
        void testFindByUserId_NullUserId() {
            // When
            List<Role> result = roleService.findByUserId(null);
            
            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据角色权限字符串查询角色 - 成功")
        void testFindByRoleKey_Success() {
            // Given
            when(roleRepository.findByRoleKeyAndDelFlag("test_role", 0)).thenReturn(Optional.of(testRole));
            when(roleDTOConverter.toDTO(testRole)).thenReturn(testRoleDTO);

            // When
            Optional<RoleDTO> result = roleService.findByRoleKey("test_role");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getRoleKey()).isEqualTo("test_role");
            verify(roleRepository).findByRoleKeyAndDelFlag("test_role", 0);
            verify(roleDTOConverter).toDTO(testRole);
        }

        @Test
        @DisplayName("根据角色权限字符串查询角色 - 角色不存在")
        void testFindByRoleKey_NotFound() {
            // Given
            when(roleRepository.findByRoleKeyAndDelFlag("nonexistent", 0)).thenReturn(Optional.empty());

            // When
            Optional<RoleDTO> result = roleService.findByRoleKey("nonexistent");

            // Then
            assertThat(result).isEmpty();
            verify(roleRepository).findByRoleKeyAndDelFlag("nonexistent", 0);
            verify(roleDTOConverter, never()).toDTO(any());
        }

        @Test
        @DisplayName("根据角色名称查询角色 - 成功")
        void testFindByRoleName_Success() {
            // Given
            when(roleRepository.findByRoleNameAndDelFlag("测试角色", 0)).thenReturn(Optional.of(testRole));
            when(roleDTOConverter.toDTO(testRole)).thenReturn(testRoleDTO);

            // When
            Optional<RoleDTO> result = roleService.findByRoleName("测试角色");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getRoleName()).isEqualTo("测试角色");
            verify(roleRepository).findByRoleNameAndDelFlag("测试角色", 0);
            verify(roleDTOConverter).toDTO(testRole);
        }

        @Test
        @DisplayName("查询所有角色 - 成功")
        void testFindAll_Success() {
            // Given
            List<Role> roles = Arrays.asList(testRole);
            List<RoleDTO> roleDTOs = Arrays.asList(testRoleDTO);
            when(roleRepository.findByDelFlagOrderByRoleSortAsc(0)).thenReturn(roles);
            when(roleDTOConverter.toDTOList(roles)).thenReturn(roleDTOs);

            // When
            List<RoleDTO> result = roleService.findAll();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(roleRepository).findByDelFlagOrderByRoleSortAsc(0);
            verify(roleDTOConverter).toDTOList(roles);
        }

        @Test
        @DisplayName("根据状态查询角色 - 有状态参数")
        void testFindByStatus_WithStatus() {
            // Given
            List<Role> roles = Arrays.asList(testRole);
            List<RoleDTO> roleDTOs = Arrays.asList(testRoleDTO);
            when(roleRepository.findAllForSelect(Role.Status.NORMAL, 0)).thenReturn(roles);
            when(roleDTOConverter.toDTOList(roles)).thenReturn(roleDTOs);

            // When
            List<RoleDTO> result = roleService.findByStatus(Role.Status.NORMAL);

            // Then
            assertThat(result).hasSize(1);
            verify(roleRepository).findAllForSelect(Role.Status.NORMAL, 0);
            verify(roleDTOConverter).toDTOList(roles);
        }

        @Test
        @DisplayName("根据状态查询角色 - 无状态参数")
        void testFindByStatus_WithoutStatus() {
            // Given
            List<Role> roles = Arrays.asList(testRole);
            List<RoleDTO> roleDTOs = Arrays.asList(testRoleDTO);
            when(roleRepository.findByDelFlagOrderByRoleSortAsc(0)).thenReturn(roles);
            when(roleDTOConverter.toDTOList(roles)).thenReturn(roleDTOs);

            // When
            List<RoleDTO> result = roleService.findByStatus("");

            // Then
            assertThat(result).hasSize(1);
            verify(roleRepository).findByDelFlagOrderByRoleSortAsc(0);
            verify(roleDTOConverter).toDTOList(roles);
        }

        @Test
        @DisplayName("分页查询角色 - 成功")
        void testFindAllPaged_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Role> rolePage = new PageImpl<>(Arrays.asList(testRole), pageable, 1);
            Page<RoleDTO> roleDTOPage = new PageImpl<>(Arrays.asList(testRoleDTO), pageable, 1);
            when(roleRepository.findByDelFlagOrderByRoleSortAsc(0, pageable)).thenReturn(rolePage);
            when(roleDTOConverter.toDTOPage(rolePage)).thenReturn(roleDTOPage);

            // When
            Page<RoleDTO> result = roleService.findAll(0, 10);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(roleRepository).findByDelFlagOrderByRoleSortAsc(0, pageable);
            verify(roleDTOConverter).toDTOPage(rolePage);
        }

        @Test
        @DisplayName("复合条件分页查询角色 - 成功")
        void testFindByConditions_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Role> rolePage = new PageImpl<>(Arrays.asList(testRole), pageable, 1);
            Page<RoleDTO> roleDTOPage = new PageImpl<>(Arrays.asList(testRoleDTO), pageable, 1);
            when(roleRepository.findByConditions("测试", "test", Role.Status.NORMAL, 0, pageable))
                    .thenReturn(rolePage);
            when(roleDTOConverter.toDTOPage(rolePage)).thenReturn(roleDTOPage);

            // When
            Page<RoleDTO> result = roleService.findByConditions("测试", "test", Role.Status.NORMAL, 0, 10);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(roleRepository).findByConditions("测试", "test", Role.Status.NORMAL, 0, pageable);
            verify(roleDTOConverter).toDTOPage(rolePage);
        }

        @Test
        @DisplayName("统计正常状态角色数量 - 成功")
        void testCountNormalRoles_Success() {
            // Given
            when(roleRepository.countByStatusAndDelFlag(Role.Status.NORMAL, 0)).thenReturn(5L);

            // When
            long result = roleService.countNormalRoles();

            // Then
            assertThat(result).isEqualTo(5L);
            verify(roleRepository).countByStatusAndDelFlag(Role.Status.NORMAL, 0);
        }

        @Test
        @DisplayName("查询所有正常状态角色用于下拉选择 - 成功")
        void testFindAllForSelect_Success() {
            // Given
            List<Role> roles = Arrays.asList(testRole);
            RoleDTO.SelectorDTO selectorDTO = new RoleDTO.SelectorDTO();
            selectorDTO.setId(1L);
            selectorDTO.setRoleName("测试角色");
            List<RoleDTO.SelectorDTO> selectorDTOs = Arrays.asList(selectorDTO);
            when(roleRepository.findAllForSelect(Role.Status.NORMAL, 0)).thenReturn(roles);
            when(roleDTOConverter.toSelectorDTOList(roles)).thenReturn(selectorDTOs);

            // When
            List<RoleDTO.SelectorDTO> result = roleService.findAllForSelect();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(roleRepository).findAllForSelect(Role.Status.NORMAL, 0);
            verify(roleDTOConverter).toSelectorDTOList(roles);
        }
    }

    @Nested
    @DisplayName("创建和更新方法测试")
    class CreateAndUpdateMethodsTest {

        @Test
        @DisplayName("创建角色 - 成功")
        void testCreateRole_Success() {
            // Given
            Role newRole = new Role();
            newRole.setRoleName("新角色");
            newRole.setRoleKey("new_role");
            newRole.setRoleSort(2);
            newRole.setDataScope(Role.DataScope.DEPT);
            newRole.setMenuCheckStrictly(true);
            newRole.setDeptCheckStrictly(true);
            newRole.setStatus(Role.Status.NORMAL);
            newRole.setRemark("新角色备注");
            newRole.setDelFlag(0);
            
            Role savedRole = new Role();
            savedRole.setId(2L);
            savedRole.setRoleName("新角色");
            savedRole.setRoleKey("new_role");
            savedRole.setRoleSort(2);
            savedRole.setDataScope(Role.DataScope.DEPT);
            savedRole.setMenuCheckStrictly(true);
            savedRole.setDeptCheckStrictly(true);
            savedRole.setStatus(Role.Status.NORMAL);
            savedRole.setRemark("新角色备注");
            savedRole.setDelFlag(0);
            savedRole.setCreateTime(LocalDateTime.now());
            
            RoleDTO savedRoleDTO = new RoleDTO();
            savedRoleDTO.setId(2L);
            savedRoleDTO.setRoleName("新角色");
            savedRoleDTO.setRoleKey("new_role");

            when(roleRepository.findByRoleKeyAndDelFlag("new_role", 0)).thenReturn(Optional.empty());
            when(roleRepository.findByRoleNameAndDelFlag("新角色", 0)).thenReturn(Optional.empty());
            when(roleDTOConverter.toEntity(createDTO)).thenReturn(newRole);
            when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
            when(roleDTOConverter.toDTO(savedRole)).thenReturn(savedRoleDTO);

            // When
            RoleDTO result = roleService.createRole(createDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getRoleName()).isEqualTo("新角色");
            verify(roleRepository).findByRoleKeyAndDelFlag("new_role", 0);
            verify(roleRepository).findByRoleNameAndDelFlag("新角色", 0);
            verify(roleDTOConverter).toEntity(createDTO);
            verify(roleRepository).save(any(Role.class));
            verify(roleDTOConverter).toDTO(savedRole);
        }

        @Test
        @DisplayName("创建角色 - 角色权限字符串已存在")
        void testCreateRole_RoleKeyExists() {
            // Given
            when(roleRepository.findByRoleKeyAndDelFlag("new_role", 0)).thenReturn(Optional.of(testRole));

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色权限字符串已存在");
            verify(roleRepository).findByRoleKeyAndDelFlag("new_role", 0);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("创建角色 - 角色名称已存在")
        void testCreateRole_RoleNameExists() {
            // Given
            when(roleRepository.findByRoleKeyAndDelFlag("new_role", 0)).thenReturn(Optional.empty());
            when(roleRepository.findByRoleNameAndDelFlag("新角色", 0)).thenReturn(Optional.of(testRole));

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色名称已存在");
            verify(roleRepository).findByRoleKeyAndDelFlag("new_role", 0);
            verify(roleRepository).findByRoleNameAndDelFlag("新角色", 0);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("创建角色 - 参数为空")
        void testCreateRole_NullCreateDTO() {
            // When & Then
            assertThatThrownBy(() -> roleService.createRole(null))
                    .isInstanceOf(NullPointerException.class);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新角色 - 成功")
        void testUpdateRole_Success() {
            // Given
            Role updatedRole = new Role();
            updatedRole.setId(1L);
            updatedRole.setRoleName("更新角色");
            updatedRole.setRoleKey("updated_role");
            updatedRole.setRoleSort(3);
            updatedRole.setDataScope(Role.DataScope.ALL);
            updatedRole.setMenuCheckStrictly(false);
            updatedRole.setDeptCheckStrictly(false);
            updatedRole.setStatus(Role.Status.NORMAL);
            updatedRole.setRemark("更新角色备注");
            updatedRole.setDelFlag(0);
            updatedRole.setUpdateTime(LocalDateTime.now());
            
            RoleDTO updatedRoleDTO = new RoleDTO();
            updatedRoleDTO.setId(1L);
            updatedRoleDTO.setRoleName("更新角色");
            updatedRoleDTO.setRoleKey("updated_role");

            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.existsByRoleKeyAndIdNotAndDelFlag("updated_role", 1L, 0)).thenReturn(false);
            when(roleRepository.existsByRoleNameAndIdNotAndDelFlag("更新角色", 1L, 0)).thenReturn(false);
            when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);
            when(roleDTOConverter.toDTO(updatedRole)).thenReturn(updatedRoleDTO);

            // When
            RoleDTO result = roleService.updateRole(updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getRoleName()).isEqualTo("更新角色");
            verify(roleRepository).findById(1L);
            verify(roleRepository).existsByRoleKeyAndIdNotAndDelFlag("updated_role", 1L, 0);
            verify(roleRepository).existsByRoleNameAndIdNotAndDelFlag("更新角色", 1L, 0);
            verify(roleDTOConverter).updateEntity(testRole, updateDTO);
            verify(roleRepository).save(any(Role.class));
            verify(roleDTOConverter).toDTO(updatedRole);
        }

        @Test
        @DisplayName("更新角色 - 角色不存在")
        void testUpdateRole_RoleNotFound() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色不存在");
            verify(roleRepository).findById(1L);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新角色 - 角色权限字符串已被其他角色使用")
        void testUpdateRole_RoleKeyUsedByOther() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.existsByRoleKeyAndIdNotAndDelFlag("updated_role", 1L, 0)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色权限字符串已被其他角色使用");
            verify(roleRepository).findById(1L);
            verify(roleRepository).existsByRoleKeyAndIdNotAndDelFlag("updated_role", 1L, 0);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("更新角色 - 角色名称已被其他角色使用")
        void testUpdateRole_RoleNameUsedByOther() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.existsByRoleKeyAndIdNotAndDelFlag("updated_role", 1L, 0)).thenReturn(false);
            when(roleRepository.existsByRoleNameAndIdNotAndDelFlag("更新角色", 1L, 0)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色名称已被其他角色使用");
            verify(roleRepository).findById(1L);
            verify(roleRepository).existsByRoleKeyAndIdNotAndDelFlag("updated_role", 1L, 0);
            verify(roleRepository).existsByRoleNameAndIdNotAndDelFlag("更新角色", 1L, 0);
            verify(roleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("删除方法测试")
    class DeleteMethodsTest {

        @Test
        @DisplayName("删除角色 - 成功")
        void testDeleteRole_Success() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.save(any(Role.class))).thenReturn(testRole);

            // When
            roleService.deleteRole(1L);

            // Then
            verify(roleRepository).findById(1L);
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("删除角色 - 角色不存在")
        void testDeleteRole_RoleNotFound() {
            // Given
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色不存在");
            verify(roleRepository).findById(999L);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("删除角色 - 空ID")
        void testDeleteRole_NullId() {
            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("角色ID不能为空");
            verify(roleRepository, never()).findById(any());
        }

        @Test
        @DisplayName("删除角色 - 超级管理员角色")
        void testDeleteRole_AdminRole() {
            // Given
            Role adminRole = new Role();
            adminRole.setId(1L);
            adminRole.setRoleKey("admin");
            adminRole.setDelFlag(0);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("不能删除超级管理员角色");
            verify(roleRepository).findById(1L);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("批量删除角色 - 成功")
        void testDeleteRoles_Success() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L);
            Role role2 = new Role();
            role2.setId(2L);
            role2.setRoleKey("test_role2");
            role2.setDelFlag(0);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.findById(2L)).thenReturn(Optional.of(role2));
            when(roleRepository.save(any(Role.class))).thenReturn(testRole, role2);

            // When
            roleService.deleteRoles(ids);

            // Then
            verify(roleRepository, times(2)).findById(any());
            verify(roleRepository, times(2)).save(any(Role.class));
        }

        @Test
        @DisplayName("批量删除角色 - 空ID列表")
        void testDeleteRoles_EmptyIds() {
            // When & Then
            assertThatThrownBy(() -> roleService.deleteRoles(Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("角色ID列表不能为空");
            verify(roleRepository, never()).findById(any());
        }

        @Test
        @DisplayName("批量删除角色 - 空ID列表（null）")
        void testDeleteRoles_NullIds() {
            // When & Then
            assertThatThrownBy(() -> roleService.deleteRoles(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("角色ID列表不能为空");
            verify(roleRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("状态管理方法测试")
    class StatusManagementTest {

        @Test
        @DisplayName("启用角色 - 成功")
        void testEnableRole_Success() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.save(any(Role.class))).thenReturn(testRole);

            // When
            roleService.enableRole(1L);

            // Then
            verify(roleRepository).findById(1L);
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("启用角色 - 角色不存在")
        void testEnableRole_RoleNotFound() {
            // Given
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.enableRole(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色不存在");
            verify(roleRepository).findById(999L);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("停用角色 - 成功")
        void testDisableRole_Success() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.save(any(Role.class))).thenReturn(testRole);

            // When
            roleService.disableRole(1L);

            // Then
            verify(roleRepository).findById(1L);
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("停用角色 - 角色不存在")
        void testDisableRole_RoleNotFound() {
            // Given
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.disableRole(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色不存在");
            verify(roleRepository).findById(999L);
            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("停用角色 - 超级管理员角色")
        void testDisableRole_AdminRole() {
            // Given
            Role adminRole = new Role();
            adminRole.setId(1L);
            adminRole.setRoleKey("admin");
            adminRole.setDelFlag(0);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

            // When & Then
            assertThatThrownBy(() -> roleService.disableRole(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("不能停用超级管理员角色");
            verify(roleRepository).findById(1L);
            verify(roleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("创建角色 - 角色名称长度边界测试")
        void testCreateRole_RoleNameLengthBoundary() {
            // Given - 31个字符的角色名称
            RoleDTO.CreateDTO longNameDTO = new RoleDTO.CreateDTO();
            longNameDTO.setRoleName("a".repeat(31));
            longNameDTO.setRoleKey("test_role");
            longNameDTO.setRoleSort(1);

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(longNameDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色名称长度不能超过30个字符");
        }

        @Test
        @DisplayName("创建角色 - 角色权限字符串长度边界测试")
        void testCreateRole_RoleKeyLengthBoundary() {
            // Given - 101个字符的角色权限字符串
            RoleDTO.CreateDTO longKeyDTO = new RoleDTO.CreateDTO();
            longKeyDTO.setRoleName("测试角色");
            longKeyDTO.setRoleKey("a".repeat(101));
            longKeyDTO.setRoleSort(1);

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(longKeyDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("角色权限字符串长度不能超过100个字符");
        }

        @Test
        @DisplayName("创建角色 - 备注长度边界测试")
        void testCreateRole_RemarkLengthBoundary() {
            // Given - 501个字符的备注
            RoleDTO.CreateDTO longRemarkDTO = new RoleDTO.CreateDTO();
            longRemarkDTO.setRoleName("测试角色");
            longRemarkDTO.setRoleKey("test_role");
            longRemarkDTO.setRoleSort(1);
            longRemarkDTO.setRemark("a".repeat(501));

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(longRemarkDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("备注长度不能超过500个字符");
        }

        @Test
        @DisplayName("分页查询 - 边界页码测试")
        void testFindAllPaged_BoundaryPageNumbers() {
            // Given
            Pageable pageable = PageRequest.of(0, 1);
            Page<Role> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            Page<RoleDTO> emptyDTOPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(roleRepository.findByDelFlagOrderByRoleSortAsc(0, pageable)).thenReturn(emptyPage);
            when(roleDTOConverter.toDTOPage(emptyPage)).thenReturn(emptyDTOPage);

            // When
            Page<RoleDTO> result = roleService.findAll(0, 1);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(roleRepository).findByDelFlagOrderByRoleSortAsc(0, pageable);
        }

        @Test
        @DisplayName("复合条件查询 - 空字符串参数测试")
        void testFindByConditions_EmptyStringParameters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Role> rolePage = new PageImpl<>(Arrays.asList(testRole), pageable, 1);
            Page<RoleDTO> roleDTOPage = new PageImpl<>(Arrays.asList(testRoleDTO), pageable, 1);
            when(roleRepository.findByConditions(null, null, null, 0, pageable))
                    .thenReturn(rolePage);
            when(roleDTOConverter.toDTOPage(rolePage)).thenReturn(roleDTOPage);

            // When
            Page<RoleDTO> result = roleService.findByConditions("", "", "", 0, 10);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(roleRepository).findByConditions(null, null, null, 0, pageable);
        }
    }
}
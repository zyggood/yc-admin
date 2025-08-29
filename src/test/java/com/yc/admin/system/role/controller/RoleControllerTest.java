package com.yc.admin.system.role.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.admin.system.role.dto.RoleDTO;
import com.yc.admin.system.role.service.RoleService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@WithMockUser(
    username = "admin",
    roles = {"ADMIN"})
@DisplayName("角色控制器集成测试")
public class RoleControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private RoleService roleService;

  private RoleDTO testRole;
  private RoleDTO.CreateDTO createDTO;
  private RoleDTO.UpdateDTO updateDTO;

  @BeforeEach
  void setUp() {
    // 准备测试数据
    testRole =
        RoleDTO.builder()
            .id(1L)
            .roleName("测试角色")
            .roleKey("test_role")
            .roleSort(1)
            .dataScope("1")
            .menuCheckStrictly(true)
            .deptCheckStrictly(true)
            .status("0")
            .delFlag(0)
            .createBy("admin")
            .createTime(LocalDateTime.now())
            .updateBy("admin")
            .updateTime(LocalDateTime.now())
            .remark("测试角色")
            .build();

    createDTO =
        RoleDTO.CreateDTO.builder()
            .roleName("新角色")
            .roleKey("new_role")
            .roleSort(2)
            .dataScope("1")
            .menuCheckStrictly(true)
            .deptCheckStrictly(true)
            .status("0")
            .remark("新建角色")
            .build();

    updateDTO =
        RoleDTO.UpdateDTO.builder()
            .roleName("更新角色")
            .roleKey("updated_role")
            .roleSort(3)
            .dataScope("2")
            .menuCheckStrictly(false)
            .deptCheckStrictly(false)
            .status("0")
            .remark("更新角色信息")
            .build();
  }

  @Nested
  @DisplayName("查询接口测试")
  class QueryTests {

    @Test
    @DisplayName("分页查询角色列表 - 无条件")
    void testGetRoleList_NoConditions() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/api/roles")
                  .param("page", "0")
                  .param("size", "10")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("查询成功"))
          .andExpect(jsonPath("$.data.data").isArray());
    }

    @Test
    @DisplayName("分页查询角色列表 - 带条件")
    void testGetRoleList_WithConditions() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/api/roles")
                  .param("page", "0")
                  .param("size", "10")
                  .param("roleName", "测试")
                  .param("roleKey", "test")
                  .param("status", "0")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("查询成功"))
          .andExpect(jsonPath("$.data.data").isArray());
    }

    @Test
    @DisplayName("根据ID查询角色详情")
    void testGetRoleById() throws Exception {
      // 创建测试角色
      RoleDTO createdRole = roleService.createRole(createDTO);

      // 执行测试
      mockMvc
          .perform(get("/api/roles/" + createdRole.getId()).contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("查询成功"))
          .andExpect(jsonPath("$.data.id").value(createdRole.getId().intValue()))
          .andExpect(jsonPath("$.data.roleName").value(createdRole.getRoleName()));
    }

    @Test
    @DisplayName("查询角色下拉选择列表")
    void testGetRolesForSelect() throws Exception {
      // 执行测试
      mockMvc
          .perform(get("/api/roles/select").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("查询成功"))
          .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取角色统计信息")
    void testGetRoleStatistics() throws Exception {
      // 执行测试
      mockMvc
          .perform(get("/api/roles/stats").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("查询成功"))
          .andExpect(jsonPath("$.data.totalRoles").exists())
          .andExpect(jsonPath("$.data.normalRoles").exists())
          .andExpect(jsonPath("$.data.disabledRoles").exists());
    }

    @Test
    @DisplayName("获取数据权限范围选项")
    void testGetDataScopes() throws Exception {
      // 执行测试
      mockMvc
          .perform(get("/api/roles/data-scopes").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("查询成功"))
          .andExpect(jsonPath("$.data").isMap());
    }
  }

  @Nested
  @DisplayName("创建接口测试")
  class CreateTests {

    @Test
    @DisplayName("创建角色 - 成功")
    void testCreateRole_Success() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              post("/api/roles")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("角色创建成功"));
    }

    @Test
    @DisplayName("创建角色 - 参数校验失败")
    void testCreateRole_ValidationFailed() throws Exception {
      // 准备无效数据
      RoleDTO.CreateDTO invalidDTO =
          RoleDTO.CreateDTO.builder()
              .roleName("") // 空角色名称
              .roleKey("") // 空角色权限字符串
              .build();

      // 执行测试
      mockMvc
          .perform(
              post("/api/roles")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidDTO)))
          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(400));
    }
  }

  @Nested
  @DisplayName("更新接口测试")
  class UpdateTests {

    @Test
    @DisplayName("更新角色信息 - 成功")
    void testUpdateRole_Success() throws Exception {
      // 创建测试角色
      RoleDTO createdRole = roleService.createRole(createDTO);
      updateDTO.setId(createdRole.getId());

      // 执行测试
      mockMvc
          .perform(
              put("/api/roles/" + createdRole.getId())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateDTO)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("角色更新成功"));
    }

    @Test
    @DisplayName("启用角色 - 成功")
    void testEnableRole_Success() throws Exception {
      // 创建测试角色
      RoleDTO createdRole = roleService.createRole(createDTO);

      // 执行测试
      mockMvc
          .perform(
              put("/api/roles/" + createdRole.getId() + "/enable")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("角色启用成功"));
    }

    @Test
    @DisplayName("停用角色 - 成功")
    void testDisableRole_Success() throws Exception {
      // 创建测试角色
      RoleDTO createdRole = roleService.createRole(createDTO);

      // 执行测试
      mockMvc
          .perform(
              put("/api/roles/" + createdRole.getId() + "/disable")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("角色停用成功"));
    }
  }

  @Nested
  @DisplayName("删除接口测试")
  class DeleteTests {

    @Test
    @DisplayName("删除角色 - 成功")
    void testDeleteRole_Success() throws Exception {
      // 创建测试角色
      RoleDTO createdRole = roleService.createRole(createDTO);

      // 执行测试
      mockMvc
          .perform(
              delete("/api/roles/" + createdRole.getId())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("角色删除成功"));
    }

    @Test
    @DisplayName("批量删除角色 - 成功")
    void testBatchDeleteRoles_Success() throws Exception {
      // 创建测试角色
      RoleDTO role1 = roleService.createRole(createDTO);
      RoleDTO.CreateDTO createDTO2 =
          RoleDTO.CreateDTO.builder()
              .roleName("测试角色2")
              .roleKey("test_role2")
              .roleSort(2)
              .dataScope("1")
              .menuCheckStrictly(true)
              .deptCheckStrictly(true)
              .status("0")
              .remark("测试角色2")
              .build();
      RoleDTO role2 = roleService.createRole(createDTO2);
      List<Long> roleIds = Arrays.asList(role1.getId(), role2.getId());

      // 执行测试
      mockMvc
          .perform(
              delete("/api/roles/batch")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(roleIds)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("批量删除成功"));
    }
  }

  @Nested
  @DisplayName("异常处理测试")
  class ExceptionTests {

    @Test
    @DisplayName("查询不存在的角色")
    void testServiceException() throws Exception {
      // 执行测试 - 查询不存在的角色
      mockMvc
          .perform(get("/api/roles/999").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value(containsString("角色不存在")));
    }

    @Test
    @DisplayName("创建重复角色权限字符串")
    void testCreateDuplicateRoleKey() throws Exception {
      // 先创建一个角色
      roleService.createRole(createDTO);

      // 再次创建相同角色权限字符串的角色
      mockMvc
          .perform(
              post("/api/roles")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value(containsString("角色权限字符串已存在")));
    }
  }
}
package com.yc.admin.system.user.controller;

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
import com.yc.admin.system.user.dto.UserDTO;
import com.yc.admin.system.user.service.UserService;
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
@DisplayName("用户控制器集成测试")
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserService userService;

  private UserDTO testUser;
  private UserDTO.CreateDTO createDTO;
  private UserDTO.UpdateDTO updateDTO;

  @BeforeEach
  void setUp() {
    // 准备测试数据
    testUser =
        UserDTO.builder()
            .id(1L)
            .userName("testuser")
            .nickName("测试用户")
            .email("test@example.com")
            .phone("13800138000")
            .sex("0")
            .avatar("/avatar/default.jpg")
            .status("0")
            .delFlag(0)
            .createBy("admin")
            .createTime(LocalDateTime.now())
            .updateBy("admin")
            .updateTime(LocalDateTime.now())
            .remark("测试用户")
            .deptId(1L)
            .deptName("测试部门")
            .roleIds(Arrays.asList(1L, 2L))
            .roleNames(Arrays.asList("管理员", "普通用户"))
            .build();

    createDTO =
        UserDTO.CreateDTO.builder()
            .userName("newuser")
            .nickName("新用户")
            .email("new@example.com")
            .phone("13900139000")
            .sex("0")
            .password("123456")
            .status("0")
            .deptId(null)
            .roleIds(null)
            .remark("新建用户")
            .build();

    updateDTO =
        UserDTO.UpdateDTO.builder()
            .nickName("更新用户")
            .email("updated@example.com")
            .phone("13700137000")
            .sex("1")
            .status("0")
            .deptId(null)
            .roleIds(null)
            .remark("更新用户信息")
            .build();
  }

  @Nested
  @DisplayName("查询接口测试")
  class QueryTests {

    @Test
    @DisplayName("分页查询用户列表 - 无条件")
    void testGetUserList_NoConditions() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/users")
                  .param("page", "0")
                  .param("size", "10")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("分页查询用户列表 - 带条件")
    void testGetUserList_WithConditions() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/users")
                  .param("page", "0")
                  .param("size", "10")
                  .param("userName", "test")
                  .param("nickName", "测试")
                  .param("phone", "138")
                  .param("status", "0")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("根据ID查询用户详情")
    void testGetUserById() throws Exception {
      // 创建测试用户
      UserDTO createdUser = userService.createUser(createDTO);

      // 执行测试
      mockMvc
          .perform(get("/users/" + createdUser.getId()).contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data.id").value(createdUser.getId().intValue()))
          .andExpect(jsonPath("$.data.userName").value(createdUser.getUserName()));
    }

    @Test
    @DisplayName("检查用户名是否可用")
    void testCheckUserName() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/users/check-username")
                  .param("userName", "newuser")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value(containsString("操作成功")))
          .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("检查邮箱是否可用")
    void testCheckEmail() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/users/check-email")
                  .param("email", "new@example.com")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"));
    }

    @Test
    @DisplayName("检查手机号是否可用")
    void testCheckPhone() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              get("/users/check-phone")
                  .param("phone", "13900139000")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("获取用户统计信息")
    void testGetUserStatistics() throws Exception {
      // 执行测试
      mockMvc
          .perform(get("/users/statistics").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data.normalUserCount").value(1));
    }
  }

  @Nested
  @DisplayName("创建接口测试")
  class CreateTests {

    @Test
    @DisplayName("创建用户 - 成功")
    void testCreateUser_Success() throws Exception {
      // 执行测试
      mockMvc
          .perform(
              post("/users")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("用户创建成功"));
    }

    @Test
    @DisplayName("创建用户 - 参数校验失败")
    void testCreateUser_ValidationFailed() throws Exception {
      // 准备无效数据
      UserDTO.CreateDTO invalidDTO =
          UserDTO.CreateDTO.builder()
              .userName("") // 空用户名
              .nickName("")
              .email("invalid-email") // 无效邮箱
              .build();

      // 执行测试
      mockMvc
          .perform(
              post("/users")
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
    @DisplayName("更新用户信息 - 成功")
    void testUpdateUser_Success() throws Exception {
      // 创建测试用户
      UserDTO createdUser = userService.createUser(createDTO);

      // 执行测试
      mockMvc
          .perform(
              put("/users/" + createdUser.getId())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateDTO)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("用户更新成功"));
    }

    @Test
    @DisplayName("更新用户状态 - 成功")
    void testUpdateUserStatus_Success() throws Exception {
      // 创建测试用户
      UserDTO createdUser = userService.createUser(createDTO);

      // 执行测试
      mockMvc
          .perform(
              put("/users/" + createdUser.getId() + "/status")
                  .with(csrf())
                  .param("status", "1")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("用户状态更新成功"));
    }

    @Test
    @DisplayName("批量更新用户状态 - 成功")
    void testBatchUpdateStatus_Success() throws Exception {
      // 创建测试用户
      UserDTO user1 = userService.createUser(createDTO);
      UserDTO.CreateDTO createDTO2 =
          UserDTO.CreateDTO.builder()
              .userName("testuser2")
              .nickName("测试用户2")
              .email("test2@example.com")
              .phone("13800138001")
              .sex("0")
              .password("123456")
              .status("0")
              .deptId(null)
              .roleIds(null)
              .remark("测试用户2")
              .build();
      UserDTO user2 = userService.createUser(createDTO2);
      List<Long> userIds = List.of(user1.getId(), user2.getId());

      // 执行测试
      mockMvc
          .perform(
              put("/users/batch/status")
                  .with(csrf())
                  .param("status", "1")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(userIds)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("批量更新成功，共更新 2 条记录"));
    }

    @Test
    @DisplayName("重置用户密码 - 成功")
    void testResetPassword_Success() throws Exception {
      // 创建测试用户
      UserDTO createdUser = userService.createUser(createDTO);

      // 执行测试
      mockMvc
          .perform(
              put("/users/" + createdUser.getId() + "/password")
                  .with(csrf())
                  .param("newPassword", "newpassword")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("密码重置成功"));
    }
  }

  @Nested
  @DisplayName("删除接口测试")
  class DeleteTests {

    @Test
    @DisplayName("删除用户 - 成功")
    void testDeleteUser_Success() throws Exception {
      // 创建测试用户
      UserDTO createdUser = userService.createUser(createDTO);

      // 执行测试
      mockMvc
          .perform(
              delete("/users/" + createdUser.getId())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("用户删除成功"));
    }

    @Test
    @DisplayName("批量删除用户 - 成功")
    void testBatchDeleteUsers_Success() throws Exception {
      // 创建测试用户
      UserDTO user1 = userService.createUser(createDTO);
      UserDTO.CreateDTO createDTO2 =
          UserDTO.CreateDTO.builder()
              .userName("testuser3")
              .nickName("测试用户3")
              .email("test3@example.com")
              .phone("13800138002")
              .sex("0")
              .password("123456")
              .status("0")
              .deptId(null)
              .roleIds(null)
              .remark("测试用户3")
              .build();
      UserDTO user2 = userService.createUser(createDTO2);
      List<Long> userIds = Arrays.asList(user1.getId(), user2.getId());

      // 执行测试
      mockMvc
          .perform(
              delete("/users/batch")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(userIds)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("批量删除成功，共删除 2 条记录"));
    }
  }

  @Nested
  @DisplayName("导出接口测试")
  class ExportTests {

    @Test
    @DisplayName("导出用户数据 - 成功")
    void testExportUsers_Success() throws Exception {
      // 执行测试
      mockMvc
          .perform(get("/users/export").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("导出成功，共 1 条记录"));
    }
  }

  @Nested
  @DisplayName("异常处理测试")
  class ExceptionTests {

    @Test
    @DisplayName("查询不存在的用户")
    void testServiceException() throws Exception {
      // 执行测试 - 查询不存在的用户
      mockMvc
          .perform(get("/users/999").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("用户不存在: 999"));
    }

    @Test
    @DisplayName("参数校验异常")
    void testValidationException() throws Exception {
      // 执行测试 - 缺少必需参数
      mockMvc
          .perform(get("/users/check-username").contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.code").value(500));
    }
  }
}

package com.yc.admin.system.menu.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.admin.system.menu.dto.MenuDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 菜单控制器集成测试 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@WithMockUser(
    username = "admin",
    authorities = {
      "system:menu:query",
      "system:menu:list",
      "system:menu:add",
      "system:menu:edit",
      "system:menu:remove"
    })
@DisplayName("菜单控制器集成测试")
class MenuControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private MenuDTO.CreateDTO createDTO;
  private MenuDTO.UpdateDTO updateDTO;

  @BeforeEach
  void setUp() {
    // 初始化创建DTO
    createDTO =
        MenuDTO.CreateDTO.builder()
            .menuName("测试菜单")
            .parentId(0L)
            .orderNum(1)
            .path("/test")
            .component("test/index")
            .menuType("C")
            .visible(0)
            .status(0)
            .perms("test:menu:view")
            .icon("test")
            .remark("测试菜单")
            .build();

    // 初始化更新DTO
    updateDTO =
        MenuDTO.UpdateDTO.builder()
            .id(null) // ID将在各个测试类的@BeforeEach中动态设置
            .menuName("更新测试菜单")
            .parentId(0L)
            .orderNum(2)
            .path("/test-update")
            .component("test/update/put")
            .menuType("C")
            .visible(0)
            .status(0)
            .perms("test:menu:update:1")
            .icon("test-update")
            .remark("更新测试菜单")
            .build();
  }

  @Nested
  @DisplayName("查询测试")
  class QueryTests {

    private Long createdMenuId1;
    private Long createdMenuId2;

    @BeforeEach
    void createMenu() throws Exception {
      // 先创建两个菜单用于查询
      MenuDTO.CreateDTO testMenu1 =
          MenuDTO.CreateDTO.builder()
              .menuName("菜单查询权限")
              .parentId(0L)
              .orderNum(98)
              .menuType("F")
              .visible(0)
              .status(0)
              .perms("test:menu:query")
              .remark("菜单查询权限")
              .build();

      MenuDTO.CreateDTO testMenu2 =
          MenuDTO.CreateDTO.builder()
              .menuName("菜单查询权限2")
              .parentId(0L)
              .orderNum(97)
              .menuType("F")
              .visible(0)
              .status(0)
              .perms("test:menu:query2")
              .remark("菜单查询权限2")
              .build();

      // 创建第一个菜单并获取ID
      String response1 =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu1)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      JsonNode jsonNode1 = objectMapper.readTree(response1);
      createdMenuId1 = jsonNode1.get("data").get("id").asLong();

      // 创建第二个菜单并获取ID
      String response2 =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu2)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      JsonNode jsonNode2 = objectMapper.readTree(response2);
      createdMenuId2 = jsonNode2.get("data").get("id").asLong();
    }

    @Test
    @DisplayName("根据ID查询菜单")
    void testGetMenuById() throws Exception {
      mockMvc
          .perform(get("/menu/" + createdMenuId1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("获取菜单列表")
    void testGetMenuList() throws Exception {
      mockMvc
          .perform(get("/menu/list").param("menuName", "系统").param("status", "0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("分页查询菜单")
    void testGetMenuPage() throws Exception {
      mockMvc
          .perform(get("/menu/page").param("page", "1").param("size", "10").param("menuName", "系统"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("获取菜单树")
    void testGetMenuTree() throws Exception {
      mockMvc
          .perform(get("/menu/tree"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取子菜单")
    void testGetChildMenus() throws Exception {
      mockMvc
          .perform(get("/menu/children/" + createdMenuId1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("根据用户ID获取菜单权限")
    void testGetMenuPermissionsByUserId() throws Exception {
      mockMvc
          .perform(get("/menu/user/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("根据角色ID获取菜单权限")
    void testGetMenuPermissionsByRoleId() throws Exception {
      mockMvc
          .perform(get("/menu/role/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("获取菜单统计信息")
    void testGetMenuStatistics() throws Exception {
      mockMvc
          .perform(get("/menu/statistics"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("操作成功"))
          .andExpect(jsonPath("$.data.totalCount").exists());
    }
  }

  @Nested
  @DisplayName("创建测试")
  class CreateTests {

    @Test
    @DisplayName("创建菜单成功")
    void testCreateMenu_Success() throws Exception {
      mockMvc
          .perform(
              post("/menu")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("菜单创建成功"))
          .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("创建菜单失败 - 菜单名称为空")
    void testCreateMenu_EmptyMenuName() throws Exception {
      createDTO.setMenuName("");

      mockMvc
          .perform(
              post("/menu")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("创建菜单失败 - 菜单类型无效")
    void testCreateMenu_InvalidMenuType() throws Exception {
      createDTO.setMenuType("X");

      mockMvc
          .perform(
              post("/menu")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(400));
    }
  }

  @Nested
  @DisplayName("更新测试")
  class UpdateTests {

    @BeforeEach
    void createMenu() throws Exception {
      // 先创建一个菜单用于更新测试
      MenuDTO.CreateDTO testMenu =
          MenuDTO.CreateDTO.builder()
              .menuName("待更新菜单")
              .parentId(0L)
              .orderNum(99)
              .path("/test-update")
              .component("test/update/pre")
              .menuType("C")
              .visible(0)
              .status(0)
              .perms("test:menu:update")
              .icon("test-update")
              .remark("待更新菜单")
              .build();

      // 创建菜单并获取返回的ID
      String response =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // 解析响应获取创建的菜单ID
      JsonNode jsonNode = objectMapper.readTree(response);
      Long createdMenuId = jsonNode.get("data").get("id").asLong();

      // 更新updateDTO的ID为实际创建的菜单ID
      updateDTO.setId(createdMenuId);
    }

    @Test
    @DisplayName("更新菜单成功")
    void testUpdateMenu_Success() throws Exception {
      mockMvc
          .perform(
              put("/menu/" + updateDTO.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("菜单更新成功"));
    }

    @Test
    @DisplayName("更新菜单失败 - ID为空")
    void testUpdateMenu_NullId() throws Exception {
      // 测试请求体中ID为null的情况
      updateDTO.setId(null);

      mockMvc
          .perform(
              put("/menu/" + 999999L) // 使用一个不存在的ID
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateDTO)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("启用菜单")
    void testEnableMenu() throws Exception {
      mockMvc
          .perform(put("/menu/" + updateDTO.getId() + "/enable"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("菜单启用成功"));
    }

    @Test
    @DisplayName("停用菜单")
    void testDisableMenu() throws Exception {
      mockMvc
          .perform(put("/menu/" + updateDTO.getId() + "/disable"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("菜单停用成功"));
    }

    @Test
    @DisplayName("批量更新菜单状态")
    void testBatchUpdateMenuStatus() throws Exception {
      // 创建第二个菜单用于批量操作
      MenuDTO.CreateDTO testMenu2 =
          MenuDTO.CreateDTO.builder()
              .menuName("批量更新菜单2")
              .parentId(0L)
              .orderNum(98)
              .path("/test-batch2") // 添加路由地址
              .component("test/batch2/index") // 添加组件路径
              .menuType("C")
              .visible(0)
              .status(0)
              .perms("test:menu:batch2")
              .icon("test-batch2") // 添加图标
              .remark("批量更新菜单2")
              .build();

      String response2 =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu2)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      JsonNode jsonNode2 = objectMapper.readTree(response2);
      Long createdMenuId2 = jsonNode2.get("data").get("id").asLong();

      MenuDTO.BatchStatusUpdateDTO batchDTO = new MenuDTO.BatchStatusUpdateDTO();
      batchDTO.setIds(Arrays.asList(updateDTO.getId(), createdMenuId2));
      batchDTO.setStatus(1);

      mockMvc
          .perform(
              put("/menu/batch/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(batchDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("批量停用菜单成功，更新数量: 2"));
    }
  }

  @Nested
  @DisplayName("删除测试")
  class DeleteTests {

    @Test
    @DisplayName("删除菜单成功")
    void testDeleteMenu_Success() throws Exception {
      // 先创建一个菜单
      MenuDTO.CreateDTO testMenu =
          MenuDTO.CreateDTO.builder()
              .menuName("测试删除菜单")
              .parentId(0L)
              .orderNum(99)
              .menuType("F")
              .visible(0)
              .status(0)
              .perms("test:delete")
              .remark("用于测试删除的菜单")
              .build();

      // 创建菜单并获取返回的ID
      String createResponse =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // 解析返回的菜单ID
      JsonNode jsonNode = objectMapper.readTree(createResponse);
      long menuId = jsonNode.get("data").get("id").asLong();

      // 删除刚创建的菜单
      mockMvc
          .perform(delete("/menu/" + menuId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("菜单删除成功"));
    }

    @Test
    @DisplayName("批量删除菜单")
    void testBatchDeleteMenus() throws Exception {
      // 先创建两个菜单用于批量删除
      MenuDTO.CreateDTO testMenu1 =
          MenuDTO.CreateDTO.builder()
              .menuName("批量删除测试菜单1")
              .parentId(0L)
              .orderNum(98)
              .menuType("F")
              .visible(0)
              .status(0)
              .perms("test:batch:delete1")
              .remark("用于批量删除测试的菜单1")
              .build();

      MenuDTO.CreateDTO testMenu2 =
          MenuDTO.CreateDTO.builder()
              .menuName("批量删除测试菜单2")
              .parentId(0L)
              .orderNum(97)
              .menuType("F")
              .visible(0)
              .status(0)
              .perms("test:batch:delete2")
              .remark("用于批量删除测试的菜单2")
              .build();

      // 创建第一个菜单
      String createResponse1 =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu1)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // 创建第二个菜单
      String createResponse2 =
          mockMvc
              .perform(
                  post("/menu")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(testMenu2)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // 解析返回的菜单ID
      com.fasterxml.jackson.databind.JsonNode jsonNode1 = objectMapper.readTree(createResponse1);
      com.fasterxml.jackson.databind.JsonNode jsonNode2 = objectMapper.readTree(createResponse2);
      Long menuId1 = jsonNode1.get("data").get("id").asLong();
      Long menuId2 = jsonNode2.get("data").get("id").asLong();

      // 批量删除菜单
      var batchDTO = List.of(menuId1, menuId2);

      mockMvc
          .perform(
              delete("/menu/batch")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(batchDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(200))
          .andExpect(jsonPath("$.message").value("批量删除菜单成功，删除数量: 2"));
    }
  }

  @Nested
  @DisplayName("异常处理测试")
  class ExceptionTests {

    @Test
    @DisplayName("查询不存在的菜单")
    void testGetNonExistentMenu() throws Exception {
      mockMvc
          .perform(get("/menu/99999"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(500))
          .andExpect(jsonPath("$.message").value("菜单不存在: 99999"));
    }

    @Test
    @DisplayName("删除不存在的菜单")
    void testDeleteNonExistentMenu() throws Exception {
      mockMvc
          .perform(delete("/menu/99999"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value(500))
          .andExpect(jsonPath("$.message").value("菜单不存在: 99999"));
    }

    @Test
    @DisplayName("无效的请求参数")
    void testInvalidRequestParams() throws Exception {
      mockMvc
          .perform(get("/menu/page").param("page", "-1").param("size", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(400));
    }
  }
}

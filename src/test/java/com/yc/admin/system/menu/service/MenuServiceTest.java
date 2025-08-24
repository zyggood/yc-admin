package com.yc.admin.system.menu.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.menu.dto.MenuDTO;
import com.yc.admin.system.menu.dto.MenuDTOConverter;
import com.yc.admin.system.menu.entity.Menu;
import com.yc.admin.system.menu.repository.MenuRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * MenuService单元测试
 * 测试正常流程、异常流程和边界条件
 * 使用@Mock模拟外部依赖
 * 纯单元测试，不启动Spring容器
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService单元测试")
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuDTOConverter menuDTOConverter;

    private Menu testMenu;
    private MenuDTO testMenuDTO;
    private MenuDTO.CreateDTO testCreateDTO;
    private MenuDTO.UpdateDTO testUpdateDTO;
    private MenuDTO.QueryDTO testQueryDTO;
    private MenuDTO.TreeNodeDTO testTreeNodeDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试菜单实体
        testMenu = Menu.builder()
                .menuName("测试菜单")
                .parentId(0L)
                .orderNum(1)
                .path("/test")
                .component("test/index")
                .menuType("C")
                .status(0)
                .visible(0)
                .perms("test:menu:list")
                .icon("test-icon")
                .isFrame(0)
                .isCache(0)
                .remark("测试菜单")
                .build();
        testMenu.setId(1L);
        testMenu.setDelFlag(0);
        testMenu.setCreateTime(LocalDateTime.now());
        testMenu.setUpdateTime(LocalDateTime.now());

        // 初始化测试DTO
        testMenuDTO = MenuDTO.builder()
                .id(1L)
                .menuName("测试菜单")
                .parentId(0L)
                .orderNum(1)
                .path("/test")
                .component("test/index")
                .menuType("C")
                .status(0)
                .visible(0)
                .perms("test:menu:list")
                .icon("test-icon")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .remark("测试菜单")
                .build();

        // 初始化创建DTO
        testCreateDTO = MenuDTO.CreateDTO.builder()
                .menuName("新菜单")
                .parentId(0L)
                .orderNum(1)
                .path("/new")
                .component("new/index")
                .menuType("C")
                .status(0)
                .visible(0)
                .perms("new:menu:list")
                .icon("new-icon")
                .remark("新菜单")
                .build();

        // 初始化更新DTO
        testUpdateDTO = new MenuDTO.UpdateDTO();
        testUpdateDTO.setId(1L);
        testUpdateDTO.setMenuName("更新菜单");
        testUpdateDTO.setParentId(0L);
        testUpdateDTO.setOrderNum(2);
        testUpdateDTO.setPath("/updated");
        testUpdateDTO.setComponent("updated/index");
        testUpdateDTO.setMenuType("C");
        testUpdateDTO.setStatus(0);
        testUpdateDTO.setVisible(0);
        testUpdateDTO.setPerms("updated:menu:list");
        testUpdateDTO.setIcon("updated-icon");
        testUpdateDTO.setRemark("更新菜单");

        // 初始化查询DTO
        testQueryDTO = new MenuDTO.QueryDTO();
        testQueryDTO.setMenuName("测试");
        testQueryDTO.setMenuType("C");
        testQueryDTO.setStatus(0);
        testQueryDTO.setVisible(0);

        // 初始化树节点DTO
        testTreeNodeDTO = MenuDTO.TreeNodeDTO.builder()
                .id(1L)
                .label("测试菜单")
                .parentId(0L)
                .children(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("查询功能测试")
    class FindTests {

        @Test
        @DisplayName("根据ID查询菜单 - 成功")
        void testFindById_Success() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuDTOConverter.toDTO(testMenu)).thenReturn(testMenuDTO);

            // When
            MenuDTO result = menuService.findById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getMenuName()).isEqualTo("测试菜单");
            verify(menuRepository).findById(1L);
            verify(menuDTOConverter).toDTO(testMenu);
        }

        @Test
        @DisplayName("根据ID查询菜单 - 未找到")
        void testFindById_NotFound() {
            // Given
            when(menuRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.findById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("菜单不存在");
            verify(menuRepository).findById(999L);
        }

        @Test
        @DisplayName("根据ID查询菜单 - 参数为null")
        void testFindById_NullId() {
            // When & Then
            assertThatThrownBy(() -> menuService.findById(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单ID不能为空");
        }

        @Test
        @DisplayName("根据ID查询菜单 - 参数为负数")
        void testFindById_NegativeId() {
            // Given
            when(menuRepository.findById(-1L)).thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> menuService.findById(-1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单不存在");
        }

        @Test
        @DisplayName("查询所有菜单 - 成功")
        void testFindAll_Success() {
            // Given
            List<Menu> menuList = Arrays.asList(testMenu);
            List<MenuDTO> menuDTOList = Arrays.asList(testMenuDTO);
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(menuList);
            when(menuDTOConverter.toDTOList(menuList)).thenReturn(menuDTOList);

            // When
            List<MenuDTO> result = menuService.findAll();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMenuName()).isEqualTo("测试菜单");
            verify(menuRepository).findByDelFlagOrderByOrderNumAsc(0);
            verify(menuDTOConverter).toDTOList(menuList);
        }

        @Test
        @DisplayName("查询所有菜单 - 空结果")
        void testFindAll_EmptyResult() {
            // Given
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(Collections.emptyList());
            when(menuDTOConverter.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<MenuDTO> result = menuService.findAll();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(menuRepository).findByDelFlagOrderByOrderNumAsc(0);
            verify(menuDTOConverter).toDTOList(Collections.emptyList());
        }

        @Test
        @DisplayName("条件查询菜单 - 成功")
        void testFindByConditions_Success() {
            // Given
            List<Menu> menuList = Arrays.asList(testMenu);
            Page<Menu> menuPage = new PageImpl<>(menuList, PageRequest.of(0, 10), 1);
            Page<MenuDTO> menuDTOPage = new PageImpl<>(Arrays.asList(testMenuDTO), PageRequest.of(0, 10), 1);
            when(menuRepository.findByConditions("测试", "C", 0, 0, 0, PageRequest.of(0, 10))).thenReturn(menuPage);
            when(menuDTOConverter.toDTOPage(menuPage)).thenReturn(menuDTOPage);

            // When
            Page<MenuDTO> result = menuService.findByConditions("测试", "C", 0, 0, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(menuRepository).findByConditions("测试", "C", 0, 0, 0, PageRequest.of(0, 10));
            verify(menuDTOConverter).toDTOPage(menuPage);
        }

        @Test
        @DisplayName("条件查询菜单 - 无匹配结果")
        void testFindByConditions_NoMatch() {
            // Given
            Page<Menu> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            Page<MenuDTO> emptyDTOPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(menuRepository.findByConditions("不存在", "C", 0, 0, 0, PageRequest.of(0, 10))).thenReturn(emptyPage);
            when(menuDTOConverter.toDTOPage(emptyPage)).thenReturn(emptyDTOPage);

            // When
            Page<MenuDTO> result = menuService.findByConditions("不存在", "C", 0, 0, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(menuRepository).findByConditions("不存在", "C", 0, 0, 0, PageRequest.of(0, 10));
            verify(menuDTOConverter).toDTOPage(emptyPage);
        }
    }

    @Nested
    @DisplayName("创建功能测试")
    class CreateTests {

        @Test
        @DisplayName("创建菜单 - 成功")
        void testCreateMenu_Success() {
            // Given
            Menu newMenu = Menu.builder()
                    .menuName("新菜单")
                    .parentId(0L)
                    .orderNum(1)
                    .path("/new")
                    .component("new/index")
                    .menuType("C")
                    .status(0)
                    .visible(0)
                    .perms("new:menu:list")
                    .icon("new-icon")
                    .isFrame(0)
                    .isCache(0)
                    .remark("新菜单")
                    .build();
            newMenu.setId(2L);
            newMenu.setDelFlag(0);

            when(menuDTOConverter.toEntity(testCreateDTO)).thenReturn(newMenu);
            when(menuRepository.save(any(Menu.class))).thenReturn(newMenu);
            when(menuDTOConverter.toDTO(newMenu)).thenReturn(testMenuDTO);

            // When
            MenuDTO result = menuService.createMenu(testCreateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(menuDTOConverter).toEntity(testCreateDTO);
            verify(menuRepository).save(any(Menu.class));
            verify(menuDTOConverter).toDTO(newMenu);
        }

        @Test
        @DisplayName("创建菜单 - 参数为null")
        void testCreateMenu_NullParam() {
            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单信息不能为空");
        }

        @Test
        @DisplayName("创建菜单 - 菜单名为空")
        void testCreateMenu_EmptyMenuName() {
            // Given
            testCreateDTO.setMenuName("");

            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(testCreateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单名称不能为空");
        }

        @Test
        @DisplayName("创建菜单 - 菜单名重复")
        void testCreateMenu_DuplicateMenuName() {
            // Given
            when(menuRepository.existsByMenuNameAndParentIdAndIdNotAndDelFlag("新菜单", 0L, -1L, 0)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(testCreateDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("同级菜单名称已存在");
        }

        @Test
        @DisplayName("创建菜单 - 父菜单不存在")
        void testCreateMenu_ParentNotExists() {
            // Given
            testCreateDTO.setParentId(999L);
            when(menuRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(testCreateDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("父菜单不存在");
        }

        @Test
        @DisplayName("创建菜单 - 数据库异常")
        void testCreateMenu_DatabaseException() {
            // Given
            Menu newMenu = Menu.builder().menuName("新菜单").build();
            when(menuDTOConverter.toEntity(testCreateDTO)).thenReturn(newMenu);
            when(menuRepository.save(any(Menu.class))).thenThrow(new RuntimeException("数据库连接失败"));

            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(testCreateDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("数据库连接失败");
        }
    }

    @Nested
    @DisplayName("更新功能测试")
    class UpdateTests {

        @Test
        @DisplayName("更新菜单 - 成功")
        void testUpdateMenu_Success() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);
            when(menuDTOConverter.toDTO(testMenu)).thenReturn(testMenuDTO);

            // When
            MenuDTO result = menuService.updateMenu(testUpdateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(menuRepository).findById(1L);
            verify(menuRepository).save(any(Menu.class));
            verify(menuDTOConverter).toDTO(testMenu);
        }

        @Test
        @DisplayName("更新菜单 - 参数为null")
        void testUpdateMenu_NullParam() {
            // When & Then
            assertThatThrownBy(() -> menuService.updateMenu(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单信息不能为空");
        }

        @Test
        @DisplayName("更新菜单 - ID为null")
        void testUpdateMenu_NullId() {
            // Given
            testUpdateDTO.setId(null);

            // When & Then
            assertThatThrownBy(() -> menuService.updateMenu(testUpdateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单ID不能为空");
        }

        @Test
        @DisplayName("更新菜单 - 菜单不存在")
        void testUpdateMenu_MenuNotExists() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.updateMenu(testUpdateDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("菜单不存在");
        }

        @Test
        @DisplayName("更新菜单 - 菜单名重复")
        void testUpdateMenu_DuplicateMenuName() {
            // Given
            when(menuRepository.existsByMenuNameAndParentIdAndIdNotAndDelFlag("更新菜单", 0L, 1L, 0)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> menuService.updateMenu(testUpdateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("同级菜单名称已存在");
        }

        @Test
        @DisplayName("更新菜单状态 - 成功")
        void testUpdateMenuStatus_Success() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L);
            when(menuRepository.updateStatusByIds(ids, 1)).thenReturn(2);

            // When
            int result = menuService.updateMenuStatus(ids, 1);

            // Then
            assertThat(result).isEqualTo(2);
            verify(menuRepository).updateStatusByIds(ids, 1);
        }

        @Test
        @DisplayName("更新菜单状态 - ID列表为空")
        void testUpdateMenuStatus_EmptyIds() {
            // When
            int result = menuService.updateMenuStatus(Collections.emptyList(), 1);
            
            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("更新菜单状态 - 状态值无效")
        void testUpdateMenuStatus_InvalidStatus() {
            // Given
            List<Long> ids = Arrays.asList(1L);
            when(menuRepository.updateStatusByIds(ids, 2)).thenReturn(1);

            // When
            int result = menuService.updateMenuStatus(ids, 2);
            
            // Then
            assertThat(result).isEqualTo(1);
            verify(menuRepository).updateStatusByIds(ids, 2);
        }
    }

    @Nested
    @DisplayName("删除功能测试")
    class DeleteTests {

        @Test
        @DisplayName("删除菜单 - 成功")
        void testDeleteMenu_Success() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuRepository.existsByParentIdAndDelFlag(1L, 0)).thenReturn(false);
            when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);

            // When
            menuService.deleteMenu(1L);

            // Then
            verify(menuRepository).findById(1L);
            verify(menuRepository).existsByParentIdAndDelFlag(1L, 0);
            verify(menuRepository).save(any(Menu.class));
        }

        @Test
        @DisplayName("删除菜单 - 存在子菜单")
        void testDeleteMenu_HasChildren() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuRepository.existsByParentIdAndDelFlag(1L, 0)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> menuService.deleteMenu(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("存在子菜单，无法删除");
        }

        @Test
        @DisplayName("删除菜单 - ID为null")
        void testDeleteMenu_NullId() {
            // When & Then
            assertThatThrownBy(() -> menuService.deleteMenu(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单ID不能为空");
        }

        @Test
        @DisplayName("批量删除菜单 - 成功")
        void testBatchDeleteMenu_Success() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L);
            Menu testMenu2 = Menu.builder().menuName("测试菜单2").build();
            testMenu2.setId(2L);
            testMenu2.setDelFlag(0);
            
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuRepository.findById(2L)).thenReturn(Optional.of(testMenu2));
            when(menuRepository.existsByParentIdAndDelFlag(1L, 0)).thenReturn(false);
            when(menuRepository.existsByParentIdAndDelFlag(2L, 0)).thenReturn(false);
            when(menuRepository.deleteByIds(ids)).thenReturn(2);

            // When
            int result = menuService.deleteMenus(ids);

            // Then
            assertThat(result).isEqualTo(2);
            verify(menuRepository).findById(1L);
            verify(menuRepository).findById(2L);
            verify(menuRepository).existsByParentIdAndDelFlag(1L, 0);
            verify(menuRepository).existsByParentIdAndDelFlag(2L, 0);
            verify(menuRepository).deleteByIds(ids);
        }

        @Test
        @DisplayName("批量删除菜单 - ID列表为空")
        void testBatchDeleteMenu_EmptyIds() {
            // When
            int result = menuService.deleteMenus(Collections.emptyList());
            
            // Then
            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("菜单树功能测试")
    class MenuTreeTests {

        @Test
        @DisplayName("构建菜单树 - 成功")
        void testBuildMenuTree_Success() {
            // Given
            Menu parentMenu = Menu.builder()
                    .menuName("父菜单")
                    .parentId(0L)
                    .orderNum(1)
                    .menuType("M")
                    .build();
            parentMenu.setId(1L);

            Menu childMenu = Menu.builder()
                    .menuName("子菜单")
                    .parentId(1L)
                    .orderNum(1)
                    .menuType("C")
                    .build();
            childMenu.setId(2L);

            List<Menu> menuList = Arrays.asList(parentMenu, childMenu);
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(menuList);

            MenuDTO.TreeNodeDTO parentNode = MenuDTO.TreeNodeDTO.builder()
                    .id(1L)
                    .label("父菜单")
                    .parentId(0L)
                    .children(new ArrayList<>())
                    .build();

            MenuDTO.TreeNodeDTO childNode = MenuDTO.TreeNodeDTO.builder()
                    .id(2L)
                    .label("子菜单")
                    .parentId(1L)
                    .children(new ArrayList<>())
                    .build();

            // 设置父子关系
            parentNode.getChildren().add(childNode);
            List<MenuDTO.TreeNodeDTO> expectedResult = Arrays.asList(parentNode);
            
            when(menuDTOConverter.toTreeNodeDTOList(menuList)).thenReturn(expectedResult);

            // When
            List<MenuDTO.TreeNodeDTO> result = menuService.buildMenuTree();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLabel()).isEqualTo("父菜单");
            assertThat(result.get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getLabel()).isEqualTo("子菜单");
        }

        @Test
        @DisplayName("构建菜单树 - 空数据")
        void testBuildMenuTree_EmptyData() {
            // Given
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(Collections.emptyList());

            // When
            List<MenuDTO.TreeNodeDTO> result = menuService.buildMenuTree();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("构建菜单树 - 多层级")
        void testBuildMenuTree_MultiLevel() {
            // Given
            Menu level1 = Menu.builder().menuName("一级菜单").parentId(0L).build();
            level1.setId(1L);
            Menu level2 = Menu.builder().menuName("二级菜单").parentId(1L).build();
            level2.setId(2L);
            Menu level3 = Menu.builder().menuName("三级菜单").parentId(2L).build();
            level3.setId(3L);

            List<Menu> menuList = Arrays.asList(level1, level2, level3);
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(menuList);

            MenuDTO.TreeNodeDTO node1 = MenuDTO.TreeNodeDTO.builder()
                    .id(1L).label("一级菜单").parentId(0L).children(new ArrayList<>()).build();
            MenuDTO.TreeNodeDTO node2 = MenuDTO.TreeNodeDTO.builder()
                    .id(2L).label("二级菜单").parentId(1L).children(new ArrayList<>()).build();
            MenuDTO.TreeNodeDTO node3 = MenuDTO.TreeNodeDTO.builder()
                    .id(3L).label("三级菜单").parentId(2L).children(new ArrayList<>()).build();

            // 设置多层级关系
            node2.getChildren().add(node3);
            node1.getChildren().add(node2);
            List<MenuDTO.TreeNodeDTO> expectedResult = Arrays.asList(node1);
            
            when(menuDTOConverter.toTreeNodeDTOList(menuList)).thenReturn(expectedResult);

            // When
            List<MenuDTO.TreeNodeDTO> result = menuService.buildMenuTree();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getChildren()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("菜单名长度边界测试")
        void testMenuNameLength() {
            // 测试最大长度
            String maxLengthName = "a".repeat(50);
            testCreateDTO.setMenuName(maxLengthName);
            
            // 应该成功
            Menu newMenu = Menu.builder().menuName(maxLengthName).build();
            when(menuDTOConverter.toEntity(testCreateDTO)).thenReturn(newMenu);
            when(menuRepository.save(any(Menu.class))).thenReturn(newMenu);
            when(menuDTOConverter.toDTO(newMenu)).thenReturn(testMenuDTO);
            
            assertThatCode(() -> menuService.createMenu(testCreateDTO)).doesNotThrowAnyException();

            // 测试超长
            String tooLongName = "a".repeat(51);
            testCreateDTO.setMenuName(tooLongName);
            
            assertThatThrownBy(() -> menuService.createMenu(testCreateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("菜单名称长度不能超过50个字符");
        }

        @Test
        @DisplayName("排序号边界测试")
        void testOrderNumBoundary() {
            // 测试最小值
            testCreateDTO.setOrderNum(0);
            Menu newMenu = Menu.builder().orderNum(0).build();
            when(menuDTOConverter.toEntity(testCreateDTO)).thenReturn(newMenu);
            when(menuRepository.save(any(Menu.class))).thenReturn(newMenu);
            when(menuDTOConverter.toDTO(newMenu)).thenReturn(testMenuDTO);
            
            assertThatCode(() -> menuService.createMenu(testCreateDTO)).doesNotThrowAnyException();

            // 测试负数
            testCreateDTO.setOrderNum(-1);
            assertThatThrownBy(() -> menuService.createMenu(testCreateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("排序号不能为负数");
        }

        @Test
        @DisplayName("大量数据处理测试")
        void testLargeDataSet() {
            // Given
            List<Menu> largeMenuList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Menu menu = Menu.builder().menuName("菜单" + i).build();
                menu.setId((long) i);
                largeMenuList.add(menu);
            }
            
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(largeMenuList);
            when(menuDTOConverter.toDTOList(largeMenuList)).thenReturn(Collections.emptyList());

            // When & Then
            assertThatCode(() -> menuService.findAll()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("并发访问测试")
        void testConcurrentAccess() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuDTOConverter.toDTO(testMenu)).thenReturn(testMenuDTO);

            // When & Then - 模拟并发访问
            assertThatCode(() -> {
                for (int i = 0; i < 100; i++) {
                    menuService.findById(1L);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("查询性能测试")
        @Timeout(5) // 5秒超时
        void testQueryPerformance() {
            // Given
            when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
            when(menuDTOConverter.toDTO(testMenu)).thenReturn(testMenuDTO);

            // When & Then
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                menuService.findById(1L);
            }
            long endTime = System.currentTimeMillis();
            
            assertThat(endTime - startTime).isLessThan(5000);
        }

        @Test
        @DisplayName("树构建性能测试")
        @Timeout(10) // 10秒超时
        void testTreeBuildPerformance() {
            // Given
            List<Menu> largeMenuList = new ArrayList<>();
            List<MenuDTO.TreeNodeDTO> nodeList = new ArrayList<>();
            
            for (int i = 0; i < 500; i++) {
                Menu menu = Menu.builder()
                        .menuName("菜单" + i)
                        .parentId(i == 0 ? 0L : (long)(i / 10))
                        .build();
                menu.setId((long) i);
                largeMenuList.add(menu);
                
                MenuDTO.TreeNodeDTO node = MenuDTO.TreeNodeDTO.builder()
                        .id((long) i)
                        .label("菜单" + i)
                        .parentId(i == 0 ? 0L : (long)(i / 10))
                        .children(new ArrayList<>())
                        .build();
                nodeList.add(node);
                
                lenient().when(menuDTOConverter.toTreeNodeDTO(menu)).thenReturn(node);
            }
            
            when(menuRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(largeMenuList);

            // When & Then
            assertThatCode(() -> menuService.buildMenuTree()).doesNotThrowAnyException();
        }
    }
}
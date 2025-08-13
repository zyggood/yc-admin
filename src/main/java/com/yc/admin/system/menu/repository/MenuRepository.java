package com.yc.admin.system.menu.repository;

import com.yc.admin.system.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 菜单数据访问层
 * 
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 根据父菜单ID查询子菜单列表
     * @param parentId 父菜单ID
     * @param delFlag 删除标志
     * @return 子菜单列表
     */
    List<Menu> findByParentIdAndDelFlagOrderByOrderNumAsc(Long parentId, Integer delFlag);

    /**
     * 根据菜单类型查询菜单列表
     * @param menuType 菜单类型
     * @param delFlag 删除标志
     * @return 菜单列表
     */
    List<Menu> findByMenuTypeAndDelFlagOrderByOrderNumAsc(String menuType, Integer delFlag);

    /**
     * 根据状态查询菜单列表
     * @param status 状态
     * @param delFlag 删除标志
     * @return 菜单列表
     */
    List<Menu> findByStatusAndDelFlagOrderByOrderNumAsc(Integer status, Integer delFlag);

    /**
     * 根据可见性查询菜单列表
     * @param visible 可见性
     * @param delFlag 删除标志
     * @return 菜单列表
     */
    List<Menu> findByVisibleAndDelFlagOrderByOrderNumAsc(Integer visible, Integer delFlag);

    /**
     * 根据权限标识查询菜单
     * @param perms 权限标识
     * @param delFlag 删除标志
     * @return 菜单
     */
    Optional<Menu> findByPermsAndDelFlag(String perms, Integer delFlag);

    /**
     * 查询所有正常状态的菜单（用于构建菜单树）
     * @param delFlag 删除标志
     * @return 菜单列表
     */
    List<Menu> findByDelFlagOrderByOrderNumAsc(Integer delFlag);

    /**
     * 根据条件分页查询菜单
     * @param menuName 菜单名称关键字
     * @param menuType 菜单类型
     * @param status 状态
     * @param visible 可见性
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT m FROM Menu m WHERE " +
           "(:menuName IS NULL OR m.menuName LIKE %:menuName%) AND " +
           "(:menuType IS NULL OR m.menuType = :menuType) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:visible IS NULL OR m.visible = :visible) AND " +
           "m.delFlag = :delFlag " +
           "ORDER BY m.orderNum ASC")
    Page<Menu> findByConditions(@Param("menuName") String menuName,
                               @Param("menuType") String menuType,
                               @Param("status") Integer status,
                               @Param("visible") Integer visible,
                               @Param("delFlag") Integer delFlag,
                               Pageable pageable);

    /**
     * 查询指定父菜单下是否存在子菜单
     * @param parentId 父菜单ID
     * @param delFlag 删除标志
     * @return 是否存在子菜单
     */
    boolean existsByParentIdAndDelFlag(Long parentId, Integer delFlag);

    /**
     * 检查菜单名称是否已存在（排除指定ID）
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @param id 排除的菜单ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.menuName = :menuName AND m.parentId = :parentId AND m.id != :id AND m.delFlag = :delFlag")
    boolean existsByMenuNameAndParentIdAndIdNotAndDelFlag(@Param("menuName") String menuName,
                                                         @Param("parentId") Long parentId,
                                                         @Param("id") Long id,
                                                         @Param("delFlag") Integer delFlag);

    /**
     * 检查权限标识是否已存在（排除指定ID）
     * @param perms 权限标识
     * @param id 排除的菜单ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.perms = :perms AND m.id != :id AND m.delFlag = :delFlag")
    boolean existsByPermsAndIdNotAndDelFlag(@Param("perms") String perms,
                                           @Param("id") Long id,
                                           @Param("delFlag") Integer delFlag);

    /**
     * 根据父菜单ID查询所有子菜单ID（包括子子菜单）
     * @param parentId 父菜单ID
     * @param delFlag 删除标志
     * @return 子菜单ID列表
     */
    @Query(value = "WITH RECURSIVE menu_tree AS (" +
                   "  SELECT id, parent_id FROM sys_menu WHERE parent_id = :parentId AND del_flag = :delFlag " +
                   "  UNION ALL " +
                   "  SELECT m.id, m.parent_id FROM sys_menu m " +
                   "  INNER JOIN menu_tree mt ON m.parent_id = mt.id " +
                   "  WHERE m.del_flag = :delFlag" +
                   ") " +
                   "SELECT id FROM menu_tree", nativeQuery = true)
    List<Long> findAllChildrenIds(@Param("parentId") Long parentId, @Param("delFlag") Integer delFlag);

    /**
     * 更新菜单状态
     * @param menuId 菜单ID
     * @param status 新状态
     */
    @Modifying
    @Query("UPDATE Menu m SET m.status = :status WHERE m.id = :menuId")
    void updateStatusById(@Param("menuId") Long menuId, @Param("status") Integer status);

    /**
     * 批量更新菜单状态
     * @param menuIds 菜单ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE Menu m SET m.status = :status WHERE m.id IN :menuIds AND m.delFlag = 0")
    int updateStatusByIds(@Param("menuIds") List<Long> menuIds, @Param("status") Integer status);

    /**
     * 批量逻辑删除菜单
     * @param menuIds 菜单ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Query("UPDATE Menu m SET m.delFlag = 1 WHERE m.id IN :menuIds")
    int deleteByIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 根据角色ID查询菜单权限
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Query("SELECT m FROM Menu m " +
           "INNER JOIN RoleMenu rm ON m.id = rm.menuId " +
           "WHERE rm.roleId = :roleId AND m.status = 0 AND m.delFlag = 0 " +
           "ORDER BY m.orderNum ASC")
    List<Menu> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询菜单权限
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "INNER JOIN RoleMenu rm ON m.id = rm.menuId " +
           "INNER JOIN UserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND m.status = 0 AND m.delFlag = 0 " +
           "ORDER BY m.orderNum ASC")
    List<Menu> findByUserId(@Param("userId") Long userId);

    /**
     * 查询顶级菜单列表
     * @param delFlag 删除标志
     * @return 顶级菜单列表
     */
    List<Menu> findByParentIdAndDelFlagAndStatusOrderByOrderNumAsc(Long parentId, Integer delFlag, Integer status);

    /**
     * 根据菜单类型和状态查询菜单数量
     * @param menuType 菜单类型
     * @param status 状态
     * @param delFlag 删除标志
     * @return 菜单数量
     */
    long countByMenuTypeAndStatusAndDelFlag(String menuType, Integer status, Integer delFlag);

    /**
     * 查询最大排序号
     * @param parentId 父菜单ID
     * @param delFlag 删除标志
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(m.orderNum), 0) FROM Menu m WHERE m.parentId = :parentId AND m.delFlag = :delFlag")
    Integer findMaxOrderNumByParentId(@Param("parentId") Long parentId, @Param("delFlag") Integer delFlag);

    /**
     * 根据权限标识查询菜单ID列表
     * @param perms 权限标识
     * @return 菜单ID列表
     */
    @Query("SELECT m.id FROM Menu m WHERE m.perms = :perms AND m.status = 0 AND m.delFlag = 0")
    List<Long> findMenuIdsByPermission(@Param("perms") String perms);
}
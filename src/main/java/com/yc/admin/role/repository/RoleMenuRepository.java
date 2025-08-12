package com.yc.admin.role.repository;

import com.yc.admin.role.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色菜单关联表数据访问接口
 * 
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据角色ID查询角色菜单关联列表
     * @param roleId 角色ID
     * @return 角色菜单关联列表
     */
    List<RoleMenu> findByRoleId(Long roleId);

    /**
     * 根据菜单ID查询角色菜单关联列表
     * @param menuId 菜单ID
     * @return 角色菜单关联列表
     */
    List<RoleMenu> findByMenuId(Long menuId);

    /**
     * 根据角色ID和菜单ID查询角色菜单关联
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 角色菜单关联对象
     */
    RoleMenu findByRoleIdAndMenuId(Long roleId, Long menuId);

    /**
     * 根据角色ID列表查询角色菜单关联列表
     * @param roleIds 角色ID列表
     * @return 角色菜单关联列表
     */
    List<RoleMenu> findByRoleIdIn(List<Long> roleIds);

    /**
     * 根据菜单ID列表查询角色菜单关联列表
     * @param menuIds 菜单ID列表
     * @return 角色菜单关联列表
     */
    List<RoleMenu> findByMenuIdIn(List<Long> menuIds);

    // ==================== 存在性检查方法 ====================

    /**
     * 检查角色是否拥有指定菜单权限
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return true：存在，false：不存在
     */
    boolean existsByRoleIdAndMenuId(Long roleId, Long menuId);

    /**
     * 检查角色是否拥有任意菜单权限
     * @param roleId 角色ID
     * @return true：存在，false：不存在
     */
    boolean existsByRoleId(Long roleId);

    /**
     * 检查菜单是否被任意角色使用
     * @param menuId 菜单ID
     * @return true：存在，false：不存在
     */
    boolean existsByMenuId(Long menuId);

    // ==================== 统计查询方法 ====================

    /**
     * 统计角色的菜单数量
     * @param roleId 角色ID
     * @return 菜单数量
     */
    long countByRoleId(Long roleId);

    /**
     * 统计菜单的角色数量
     * @param menuId 菜单ID
     * @return 角色数量
     */
    long countByMenuId(Long menuId);

    /**
     * 统计指定菜单列表的角色数量
     * @param menuIds 菜单ID列表
     * @return 角色数量
     */
    @Query("SELECT COUNT(DISTINCT rm.roleId) FROM RoleMenu rm WHERE rm.menuId IN :menuIds")
    long countDistinctRolesByMenuIdIn(@Param("menuIds") List<Long> menuIds);

    // ==================== 自定义查询方法 ====================

    /**
     * 根据角色ID查询菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Query("SELECT rm.menuId FROM RoleMenu rm WHERE rm.roleId = :roleId")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID查询角色ID列表
     * @param menuId 菜单ID
     * @return 角色ID列表
     */
    @Query("SELECT rm.roleId FROM RoleMenu rm WHERE rm.menuId = :menuId")
    List<Long> findRoleIdsByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据角色ID列表查询菜单ID列表
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm WHERE rm.roleId IN :roleIds")
    List<Long> findDistinctMenuIdsByRoleIdIn(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据菜单ID列表查询角色ID列表
     * @param menuIds 菜单ID列表
     * @return 角色ID列表
     */
    @Query("SELECT DISTINCT rm.roleId FROM RoleMenu rm WHERE rm.menuId IN :menuIds")
    List<Long> findDistinctRoleIdsByMenuIdIn(@Param("menuIds") List<Long> menuIds);

    /**
     * 根据用户ID查询菜单ID列表（通过用户角色关联）
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN UserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId")
    List<Long> findMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID列表查询菜单ID列表（通过用户角色关联）
     * @param userIds 用户ID列表
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN UserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId IN :userIds")
    List<Long> findMenuIdsByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * 根据角色权限字符串查询菜单ID列表
     * @param roleKeys 角色权限字符串列表
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN Role r ON rm.roleId = r.id " +
           "WHERE r.roleKey IN :roleKeys AND r.status = '0'")
    List<Long> findMenuIdsByRoleKeys(@Param("roleKeys") List<String> roleKeys);

    /**
     * 根据权限标识查询菜单ID列表
     * @param permission 权限标识
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN Menu m ON rm.menuId = m.id " +
           "WHERE m.perms = :permission AND m.status = '0'")
    List<Long> findMenuIdsByPermission(@Param("permission") String permission);

    // ==================== 删除方法 ====================

    /**
     * 根据角色ID删除角色菜单关联
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE rm.roleId = :roleId")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID删除角色菜单关联
     * @param menuId 菜单ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE rm.menuId = :menuId")
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据角色ID和菜单ID删除角色菜单关联
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE rm.roleId = :roleId AND rm.menuId = :menuId")
    int deleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 根据角色ID列表删除角色菜单关联
     * @param roleIds 角色ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE rm.roleId IN :roleIds")
    int deleteByRoleIdIn(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据菜单ID列表删除角色菜单关联
     * @param menuIds 菜单ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE rm.menuId IN :menuIds")
    int deleteByMenuIdIn(@Param("menuIds") List<Long> menuIds);

    /**
     * 根据角色ID和菜单ID列表删除角色菜单关联
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE rm.roleId = :roleId AND rm.menuId IN :menuIds")
    int deleteByRoleIdAndMenuIdIn(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 批量删除角色菜单关联
     * @param roleMenus 角色菜单关联列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleMenu rm WHERE (rm.roleId, rm.menuId) IN :roleMenus")
    int deleteBatch(@Param("roleMenus") List<Object[]> roleMenus);
}
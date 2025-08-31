package com.yc.admin.system.role.repository;

import com.yc.admin.system.role.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 * 
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色权限字符串查询角色（未删除）
     * @param roleKey 角色权限字符串
     * @param delFlag 删除标志
     * @return 角色信息
     */
    Optional<Role> findByRoleKeyAndDelFlag(String roleKey, Integer delFlag);

    /**
     * 根据角色名称查询角色（未删除）
     * @param roleName 角色名称
     * @param delFlag 删除标志
     * @return 角色信息
     */
    Optional<Role> findByRoleNameAndDelFlag(String roleName, Integer delFlag);

    /**
     * 查询所有未删除的角色，按排序号升序
     * @param delFlag 删除标志
     * @return 角色列表
     */
    List<Role> findByDelFlagOrderByRoleSortAsc(Integer delFlag);

    /**
     * 分页查询未删除的角色，按排序号升序
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByDelFlagOrderByRoleSortAsc(Integer delFlag, Pageable pageable);

    /**
     * 根据状态分页查询角色
     * @param status 角色状态
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByStatusAndDelFlagOrderByRoleSortAsc(String status, Integer delFlag, Pageable pageable);

    /**
     * 统计正常状态的角色数量
     * @param status 角色状态
     * @param delFlag 删除标志
     * @return 角色数量
     */
    long countByStatusAndDelFlag(String status, Integer delFlag);

    /**
     * 复合条件查询角色
     * @param roleName 角色名称关键字
     * @param roleKey 角色权限字符串关键字
     * @param status 角色状态
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:roleName IS NULL OR r.roleName LIKE %:roleName%) AND " +
           "(:roleKey IS NULL OR r.roleKey LIKE %:roleKey%) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "r.delFlag = :delFlag " +
           "ORDER BY r.roleSort ASC")
    Page<Role> findByConditions(@Param("roleName") String roleName,
                               @Param("roleKey") String roleKey,
                               @Param("status") String status,
                               @Param("delFlag") Integer delFlag,
                               Pageable pageable);

    /**
     * 查询指定排序号范围内的角色数量
     * @param roleSort 排序号
     * @param delFlag 删除标志
     * @return 角色数量
     */
    long countByRoleSortAndDelFlag(Integer roleSort, Integer delFlag);

    /**
     * 查询大于指定排序号的角色
     * @param roleSort 排序号
     * @param delFlag 删除标志
     * @return 角色列表
     */
    List<Role> findByRoleSortGreaterThanAndDelFlagOrderByRoleSortAsc(Integer roleSort, Integer delFlag);

    /**
     * 根据数据权限范围查询角色
     * @param dataScope 数据权限范围
     * @param delFlag 删除标志
     * @return 角色列表
     */
    List<Role> findByDataScopeAndDelFlag(String dataScope, Integer delFlag);

    /**
     * 查询所有正常状态的角色（用于下拉选择）
     * @param status 角色状态
     * @param delFlag 删除标志
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = :status AND r.delFlag = :delFlag ORDER BY r.roleSort ASC")
    List<Role> findAllForSelect(@Param("status") String status, @Param("delFlag") Integer delFlag);

    /**
     * 检查角色权限字符串是否已存在（排除指定ID）
     * @param roleKey 角色权限字符串
     * @param id 排除的角色ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleKey = :roleKey AND r.id != :id AND r.delFlag = :delFlag")
    boolean existsByRoleKeyAndIdNotAndDelFlag(@Param("roleKey") String roleKey, @Param("id") Long id, @Param("delFlag") Integer delFlag);

    /**
     * 检查角色名称是否已存在（排除指定ID）
     * @param roleName 角色名称
     * @param id 排除的角色ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleName = :roleName AND r.id != :id AND r.delFlag = :delFlag")
    boolean existsByRoleNameAndIdNotAndDelFlag(@Param("roleName") String roleName, @Param("id") Long id, @Param("delFlag") Integer delFlag);

    /**
     * 根据角色ID列表查询角色列表（未删除）
     * @param roleIds 角色ID列表
     * @param delFlag 删除标志
     * @return 角色列表
     */
    List<Role> findByIdInAndDelFlag(List<Long> roleIds, Integer delFlag);

    /**
     * 统计未删除的角色数量
     * @param delFlag 删除标志
     * @return 角色数量
     */
    long countByDelFlag(Integer delFlag);

    /**
     * 根据角色权限字符串查询菜单ID列表
     * @param roleKeys 角色权限字符串列表
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN Role r ON rm.roleId = r.id " +
           "WHERE r.roleKey IN :roleKeys AND r.status = '0' AND r.delFlag = 0")
    List<Long> findMenuIdsByRoleKeys(@Param("roleKeys") List<String> roleKeys);

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r " +
           "INNER JOIN UserRole ur ON r.id = ur.roleId " +
           "WHERE ur.userId = :userId AND r.status = '0' AND r.delFlag = 0")
    List<Role> findByUserId(@Param("userId") Long userId);

    // ==================== 角色层级相关查询方法 ====================

    /**
     * 根据父角色ID查询子角色列表
     * @param parentId 父角色ID
     * @param delFlag 删除标志
     * @return 子角色列表
     */
    List<Role> findByParentIdAndDelFlag(Long parentId, Integer delFlag);

    /**
     * 查询所有根角色（没有父角色的角色）
     * @param delFlag 删除标志
     * @return 根角色列表
     */
    List<Role> findByParentIdIsNullAndDelFlag(Integer delFlag);

    /**
     * 递归查询指定角色的所有子角色
     * @param parentId 父角色ID
     * @param delFlag 删除标志
     * @return 所有子角色列表（包括子角色的子角色）
     */
    @Query(value = "WITH RECURSIVE role_hierarchy AS (" +
                   "  SELECT id, role_name, role_key, parent_id, role_sort, status, del_flag " +
                   "  FROM sys_role " +
                   "  WHERE parent_id = :parentId AND del_flag = :delFlag " +
                   "  UNION ALL " +
                   "  SELECT r.id, r.role_name, r.role_key, r.parent_id, r.role_sort, r.status, r.del_flag " +
                   "  FROM sys_role r " +
                   "  INNER JOIN role_hierarchy rh ON r.parent_id = rh.id " +
                   "  WHERE r.del_flag = :delFlag" +
                   ") " +
                   "SELECT * FROM role_hierarchy ORDER BY role_sort ASC", nativeQuery = true)
    List<Role> findAllChildRoles(@Param("parentId") Long parentId, @Param("delFlag") Integer delFlag);

    /**
     * 递归查询指定角色的所有祖先角色
     * @param roleId 角色ID
     * @param delFlag 删除标志
     * @return 祖先角色列表（从直接父角色到根角色）
     */
    @Query(value = "WITH RECURSIVE role_ancestors AS (" +
                   "  SELECT p.id, p.role_name, p.role_key, p.parent_id, p.role_sort, p.status, p.del_flag " +
                   "  FROM sys_role r " +
                   "  INNER JOIN sys_role p ON r.parent_id = p.id " +
                   "  WHERE r.id = :roleId AND r.del_flag = :delFlag AND p.del_flag = :delFlag " +
                   "  UNION ALL " +
                   "  SELECT p.id, p.role_name, p.role_key, p.parent_id, p.role_sort, p.status, p.del_flag " +
                   "  FROM sys_role p " +
                   "  INNER JOIN role_ancestors ra ON p.id = ra.parent_id " +
                   "  WHERE p.del_flag = :delFlag" +
                   ") " +
                   "SELECT * FROM role_ancestors ORDER BY role_sort ASC", nativeQuery = true)
    List<Role> findAncestorRoles(@Param("roleId") Long roleId, @Param("delFlag") Integer delFlag);

    /**
     * 检查角色是否存在循环引用
     * @param roleId 角色ID
     * @param parentId 要设置的父角色ID
     * @param delFlag 删除标志
     * @return 是否存在循环引用
     */
    @Query(value = "WITH RECURSIVE role_check AS (" +
                   "  SELECT id, parent_id " +
                   "  FROM sys_role " +
                   "  WHERE id = :parentId AND del_flag = :delFlag " +
                   "  UNION ALL " +
                   "  SELECT r.id, r.parent_id " +
                   "  FROM sys_role r " +
                   "  INNER JOIN role_check rc ON r.parent_id = rc.id " +
                   "  WHERE r.del_flag = :delFlag" +
                   ") " +
                   "SELECT COUNT(*) > 0 FROM role_check WHERE id = :roleId", nativeQuery = true)
    boolean wouldCreateCycle(@Param("roleId") Long roleId, @Param("parentId") Long parentId, @Param("delFlag") Integer delFlag);

    /**
     * 统计指定角色的子角色数量
     * @param parentId 父角色ID
     * @param delFlag 删除标志
     * @return 子角色数量
     */
    long countByParentIdAndDelFlag(Long parentId, Integer delFlag);

    /**
     * 查询指定层级深度的角色
     * @param depth 层级深度（0为根角色）
     * @param delFlag 删除标志
     * @return 指定层级的角色列表
     */
    @Query(value = "WITH RECURSIVE role_depth AS (" +
                   "  SELECT id, role_name, role_key, parent_id, role_sort, status, del_flag, 0 as depth " +
                   "  FROM sys_role " +
                   "  WHERE parent_id IS NULL AND del_flag = :delFlag " +
                   "  UNION ALL " +
                   "  SELECT r.id, r.role_name, r.role_key, r.parent_id, r.role_sort, r.status, r.del_flag, rd.depth + 1 " +
                   "  FROM sys_role r " +
                   "  INNER JOIN role_depth rd ON r.parent_id = rd.id " +
                   "  WHERE r.del_flag = :delFlag" +
                   ") " +
                   "SELECT * FROM role_depth WHERE depth = :depth ORDER BY role_sort ASC", nativeQuery = true)
    List<Role> findByDepth(@Param("depth") Integer depth, @Param("delFlag") Integer delFlag);
}
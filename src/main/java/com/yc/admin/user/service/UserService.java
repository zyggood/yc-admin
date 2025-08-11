package com.yc.admin.user.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.user.entity.User;
import com.yc.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 用户领域服务
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== 查询操作 ====================

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    public Optional<User> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted());
    }

    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户信息
     */
    public Optional<User> findByUserName(String userName) {
        if (!StringUtils.hasText(userName)) {
            return Optional.empty();
        }
        return userRepository.findByUserNameAndDelFlag(userName, 0);
    }

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    public Optional<User> findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return Optional.empty();
        }
        return userRepository.findByEmailAndDelFlag(email, 0);
    }

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    public Optional<User> findByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return Optional.empty();
        }
        return userRepository.findByPhoneAndDelFlag(phone, 0);
    }

    /**
     * 分页查询用户列表
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByDelFlagOrderByCreateTimeDesc(0, pageable);
    }

    /**
     * 根据状态分页查询用户列表
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    public Page<User> findByStatus(String status, Pageable pageable) {
        if (!StringUtils.hasText(status)) {
            return findAll(pageable);
        }
        return userRepository.findByStatusAndDelFlagOrderByCreateTimeDesc(status, 0, pageable);
    }

    /**
     * 复合条件查询用户
     * @param userName 用户名关键字
     * @param nickName 昵称关键字
     * @param phone 手机号关键字
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    public Page<User> findByConditions(String userName, String nickName, String phone, String status, Pageable pageable) {
        return userRepository.findByConditions(
                StringUtils.hasText(userName) ? userName : null,
                StringUtils.hasText(nickName) ? nickName : null,
                StringUtils.hasText(phone) ? phone : null,
                StringUtils.hasText(status) ? status : null,
                0,
                pageable
        );
    }

    /**
     * 统计正常状态的用户数量
     * @return 用户数量
     */
    public long countNormalUsers() {
        return userRepository.countByStatusAndDelFlag(User.Status.NORMAL, 0);
    }

    /**
     * 查询所有正常用户（用于导出）
     * @return 用户列表
     */
    public List<User> findAllForExport() {
        return userRepository.findByDelFlagOrderByCreateTimeDesc(0);
    }

    // ==================== 创建操作 ====================

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建的用户
     */
    @Transactional
    public User createUser(User user) {
        log.info("开始创建用户: {}", user.getUserName());
        
        // 参数校验
        validateUserForCreate(user);
        
        // 检查用户名是否已存在
        if (userRepository.findByUserNameAndDelFlag(user.getUserName(), 0).isPresent()) {
            throw new BusinessException("用户名已存在: " + user.getUserName());
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) && 
            userRepository.findByEmailAndDelFlag(user.getEmail(), 0).isPresent()) {
            throw new BusinessException("邮箱已存在: " + user.getEmail());
        }
        
        // 检查手机号是否已存在
        if (StringUtils.hasText(user.getPhone()) && 
            userRepository.findByPhoneAndDelFlag(user.getPhone(), 0).isPresent()) {
            throw new BusinessException("手机号已存在: " + user.getPhone());
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus(User.Status.NORMAL);
        }
        if (!StringUtils.hasText(user.getSex())) {
            user.setSex(User.Sex.UNKNOWN);
        }
        if (user.getAvatar() == null) {
            user.setAvatar("");
        }
        
        User savedUser = userRepository.save(user);
        log.info("用户创建成功: {}, ID: {}", savedUser.getUserName(), savedUser.getId());
        
        return savedUser;
    }

    // ==================== 更新操作 ====================

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param updateUser 更新的用户信息
     * @return 更新后的用户
     */
    @Transactional
    public User updateUser(Long userId, User updateUser) {
        log.info("开始更新用户: {}", userId);
        
        // 查找现有用户
        User existingUser = findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
        // 参数校验
        validateUserForUpdate(updateUser);
        
        // 检查用户名是否已被其他用户使用
        if (StringUtils.hasText(updateUser.getUserName()) && 
            !updateUser.getUserName().equals(existingUser.getUserName()) &&
            userRepository.existsByUserNameAndIdNotAndDelFlag(updateUser.getUserName(), userId, 0)) {
            throw new BusinessException("用户名已存在: " + updateUser.getUserName());
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(updateUser.getEmail()) && 
            !updateUser.getEmail().equals(existingUser.getEmail()) &&
            userRepository.existsByEmailAndIdNotAndDelFlag(updateUser.getEmail(), userId, 0)) {
            throw new BusinessException("邮箱已存在: " + updateUser.getEmail());
        }
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(updateUser.getPhone()) && 
            !updateUser.getPhone().equals(existingUser.getPhone()) &&
            userRepository.existsByPhoneAndIdNotAndDelFlag(updateUser.getPhone(), userId, 0)) {
            throw new BusinessException("手机号已存在: " + updateUser.getPhone());
        }
        
        // 更新字段
        if (StringUtils.hasText(updateUser.getUserName())) {
            existingUser.setUserName(updateUser.getUserName());
        }
        if (StringUtils.hasText(updateUser.getNickName())) {
            existingUser.setNickName(updateUser.getNickName());
        }
        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }
        if (updateUser.getPhone() != null) {
            existingUser.setPhone(updateUser.getPhone());
        }
        if (StringUtils.hasText(updateUser.getSex())) {
            existingUser.setSex(updateUser.getSex());
        }
        if (updateUser.getAvatar() != null) {
            existingUser.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getRemark() != null) {
            existingUser.setRemark(updateUser.getRemark());
        }
        
        User savedUser = userRepository.save(existingUser);
        log.info("用户更新成功: {}", savedUser.getUserName());
        
        return savedUser;
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态
     * @return 更新后的用户
     */
    @Transactional
    public User updateUserStatus(Long userId, String status) {
        log.info("开始更新用户状态: {}, 新状态: {}", userId, status);
        
        User user = findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
        if (!User.Status.NORMAL.equals(status) && !User.Status.DISABLED.equals(status)) {
            throw new BusinessException("无效的用户状态: " + status);
        }
        
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        
        log.info("用户状态更新成功: {}, 状态: {}", savedUser.getUserName(), savedUser.getStatusDesc());
        return savedUser;
    }

    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Transactional
    public int batchUpdateStatus(List<Long> userIds, String status) {
        log.info("开始批量更新用户状态: {}, 新状态: {}", userIds, status);
        
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        
        if (!User.Status.NORMAL.equals(status) && !User.Status.DISABLED.equals(status)) {
            throw new BusinessException("无效的用户状态: " + status);
        }
        
        int updatedCount = userRepository.updateStatusByIds(userIds, status);
        log.info("批量更新用户状态完成，更新记录数: {}", updatedCount);
        
        return updatedCount;
    }

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码（明文）
     * @return 更新的记录数
     */
    @Transactional
    public int resetPassword(Long userId, String newPassword) {
        log.info("开始重置用户密码: {}", userId);
        
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        
        // 检查用户是否存在
        findById(userId).orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        int updatedCount = userRepository.resetPassword(userId, encodedPassword);
        log.info("用户密码重置成功: {}", userId);
        
        return updatedCount;
    }

    // ==================== 删除操作 ====================

    /**
     * 逻辑删除用户
     * @param userId 用户ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("开始删除用户: {}", userId);
        
        User user = findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
        user.markDeleted();
        userRepository.save(user);
        
        log.info("用户删除成功: {}", user.getUserName());
    }

    /**
     * 批量逻辑删除用户
     * @param userIds 用户ID列表
     * @return 删除的记录数
     */
    @Transactional
    public int batchDeleteUsers(List<Long> userIds) {
        log.info("开始批量删除用户: {}", userIds);
        
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        
        int deletedCount = userRepository.deleteByIds(userIds);
        log.info("批量删除用户完成，删除记录数: {}", deletedCount);
        
        return deletedCount;
    }

    // ==================== 业务校验方法 ====================

    /**
     * 校验用户创建参数
     * @param user 用户信息
     */
    private void validateUserForCreate(User user) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }
        
        if (!StringUtils.hasText(user.getUserName())) {
            throw new BusinessException("用户名不能为空");
        }
        
        if (!StringUtils.hasText(user.getNickName())) {
            throw new BusinessException("用户昵称不能为空");
        }
        
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        
        if (user.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
    }

    /**
     * 校验用户更新参数
     * @param user 用户信息
     */
    private void validateUserForUpdate(User user) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }
        
        if (StringUtils.hasText(user.getUserName()) && user.getUserName().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        if (StringUtils.hasText(user.getNickName()) && user.getNickName().trim().isEmpty()) {
            throw new BusinessException("用户昵称不能为空");
        }
    }

    /**
     * 验证密码是否正确
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 检查用户名是否可用
     * @param userName 用户名
     * @param excludeUserId 排除的用户ID（用于更新时检查）
     * @return 是否可用
     */
    public boolean isUserNameAvailable(String userName, Long excludeUserId) {
        if (!StringUtils.hasText(userName)) {
            return false;
        }
        
        if (excludeUserId == null) {
            return !userRepository.findByUserNameAndDelFlag(userName, 0).isPresent();
        } else {
            return !userRepository.existsByUserNameAndIdNotAndDelFlag(userName, excludeUserId, 0);
        }
    }

    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @param excludeUserId 排除的用户ID（用于更新时检查）
     * @return 是否可用
     */
    public boolean isEmailAvailable(String email, Long excludeUserId) {
        if (!StringUtils.hasText(email)) {
            return true; // 邮箱可以为空
        }
        
        if (excludeUserId == null) {
            return !userRepository.findByEmailAndDelFlag(email, 0).isPresent();
        } else {
            return !userRepository.existsByEmailAndIdNotAndDelFlag(email, excludeUserId, 0);
        }
    }

    /**
     * 检查手机号是否可用
     * @param phone 手机号
     * @param excludeUserId 排除的用户ID（用于更新时检查）
     * @return 是否可用
     */
    public boolean isPhoneAvailable(String phone, Long excludeUserId) {
        if (!StringUtils.hasText(phone)) {
            return true; // 手机号可以为空
        }
        
        if (excludeUserId == null) {
            return !userRepository.findByPhoneAndDelFlag(phone, 0).isPresent();
        } else {
            return !userRepository.existsByPhoneAndIdNotAndDelFlag(phone, excludeUserId, 0);
        }
    }
}
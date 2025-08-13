package com.yc.admin.system.user.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.dto.UserDTO;
import com.yc.admin.system.user.dto.UserDTOConverter;
import com.yc.admin.system.user.repository.UserRepository;
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
    private final UserDTOConverter userDTOConverter;

    // ==================== 查询操作 ====================

    /**
     * 根据ID查询用户DTO
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserDTO findById(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        return userDTOConverter.toDTO(user);
    }

    /**
     * 根据ID查询用户实体（内部使用）
     * @param userId 用户ID
     * @return 用户实体
     */
    public Optional<User> findEntityById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId)
                .filter(u -> !u.isDeleted());
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    public Optional<User> findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return Optional.empty();
        }
        return userRepository.findByUserNameAndDelFlag(username, 0);
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
    public Page<UserDTO> findAll(Pageable pageable) {
        Page<User> userPage = userRepository.findByDelFlagOrderByCreateTimeDesc(0, pageable);
        return userDTOConverter.toDTOPage(userPage);
    }

    /**
     * 根据状态分页查询用户列表
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    public Page<UserDTO> findByStatus(String status, Pageable pageable) {
        if (!StringUtils.hasText(status)) {
            return findAll(pageable);
        }
        Page<User> userPage = userRepository.findByStatusAndDelFlagOrderByCreateTimeDesc(status, 0, pageable);
        return userDTOConverter.toDTOPage(userPage);
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
    public Page<UserDTO> findByConditions(String userName, String nickName, String phone, String status, Pageable pageable) {
        Page<User> userPage = userRepository.findByConditions(
                StringUtils.hasText(userName) ? userName : null,
                StringUtils.hasText(nickName) ? nickName : null,
                StringUtils.hasText(phone) ? phone : null,
                StringUtils.hasText(status) ? status : null,
                0,
                pageable
        );
        return userDTOConverter.toDTOPage(userPage);
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
    public List<UserDTO> findAllForExport() {
        List<User> users = userRepository.findByDelFlagOrderByCreateTimeDesc(0);
        return userDTOConverter.toDTOList(users);
    }

    // ==================== 创建操作 ====================

    /**
     * 创建用户
     * @param createDTO 用户创建信息
     * @return 创建的用户
     */
    @Transactional
    public UserDTO createUser(UserDTO.CreateDTO createDTO) {
        User user = userDTOConverter.toEntity(createDTO);
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
        
        return userDTOConverter.toDTO(savedUser);
    }

    // ==================== 更新操作 ====================

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param updateDTO 更新的用户信息
     * @return 更新后的用户
     */
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO.UpdateDTO updateDTO) {
        log.info("开始更新用户: {}", userId);
        
        // 查找现有用户
        User existingUser = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
        // 检查用户名是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getUserName()) && 
            !updateDTO.getUserName().equals(existingUser.getUserName()) &&
            userRepository.existsByUserNameAndIdNotAndDelFlag(updateDTO.getUserName(), userId, 0)) {
            throw new BusinessException("用户名已存在: " + updateDTO.getUserName());
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getEmail()) && 
            !updateDTO.getEmail().equals(existingUser.getEmail()) &&
            userRepository.existsByEmailAndIdNotAndDelFlag(updateDTO.getEmail(), userId, 0)) {
            throw new BusinessException("邮箱已存在: " + updateDTO.getEmail());
        }
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getPhone()) && 
            !updateDTO.getPhone().equals(existingUser.getPhone()) &&
            userRepository.existsByPhoneAndIdNotAndDelFlag(updateDTO.getPhone(), userId, 0)) {
            throw new BusinessException("手机号已存在: " + updateDTO.getPhone());
        }
        
        // 更新字段
        userDTOConverter.updateEntity(existingUser, updateDTO);
        
        User savedUser = userRepository.save(existingUser);
        log.info("用户更新成功: {}, ID: {}", savedUser.getUserName(), savedUser.getId());
        
        return userDTOConverter.toDTO(savedUser);
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态
     * @return 更新后的用户
     */
    @Transactional
    public UserDTO updateUserStatus(Long userId, String status) {
        log.info("开始更新用户状态: {}, 新状态: {}", userId, status);
        
        User existingUser = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
        existingUser.setStatus(status);
        User savedUser = userRepository.save(existingUser);
        
        log.info("用户状态更新成功: {}, ID: {}", savedUser.getUserName(), savedUser.getId());
        return userDTOConverter.toDTO(savedUser);
    }

    // ==================== 内部方法 ====================
    
    private void validateUserForCreate(User user) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }
        if (!StringUtils.hasText(user.getUserName())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getUserName().length() < 2 || user.getUserName().length() > 30) {
            throw new BusinessException("用户名长度必须在2-30个字符之间");
        }
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) {
            throw new BusinessException("密码长度必须在6-20个字符之间");
        }
        if (StringUtils.hasText(user.getEmail()) && !isValidEmail(user.getEmail())) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (StringUtils.hasText(user.getPhone()) && !isValidPhone(user.getPhone())) {
            throw new BusinessException("手机号格式不正确");
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private boolean isValidPhone(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
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
        findEntityById(userId).orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        
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
        
        User user = findEntityById(userId)
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
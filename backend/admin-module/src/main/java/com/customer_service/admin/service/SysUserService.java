package com.customer_service.admin.service;

import com.customer_service.shared.entity.SysRole;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.repository.SysRoleRepository;
import com.customer_service.shared.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 分页查询用户列表
     */
    public Page<SysUser> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (keyword != null && !keyword.isEmpty()) {
            return sysUserRepository.findAll((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")), pageable);
        }
        return sysUserRepository.findAll(pageable);
    }

    /**
     * 根据ID获取用户
     */
    public Optional<SysUser> getUserById(Long id) {
        return sysUserRepository.findById(id);
    }

    /**
     * 创建用户
     */
    @Transactional
    public SysUser createUser(String username, String password, String email, String phone, Long roleId) {
        if (sysUserRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus("active");

        if (roleId != null) {
            SysRole role = sysRoleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("角色不存在"));
            user.setRole(role);
        }

        return sysUserRepository.save(user);
    }

    /**
     * 更新用户基本信息
     */
    @Transactional
    public SysUser updateUser(Long id, String email, String phone, Long roleId, String status) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (status != null) {
            user.setStatus(status);
        }
        if (roleId != null) {
            SysRole role = sysRoleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("角色不存在"));
            user.setRole(role);
        }

        return sysUserRepository.save(user);
    }

    /**
     * 修改用户密码
     */
    @Transactional
    public void changePassword(Long id, String newPassword) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 不允许删除 admin 用户
        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("不能删除管理员账号");
        }

        sysUserRepository.deleteById(id);
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public SysUser toggleStatus(Long id) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("不能禁用管理员账号");
        }

        user.setStatus("active".equals(user.getStatus()) ? "disabled" : "active");
        return sysUserRepository.save(user);
    }
}

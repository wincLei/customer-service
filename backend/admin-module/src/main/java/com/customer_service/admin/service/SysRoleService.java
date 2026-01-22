package com.customer_service.admin.service;

import com.customer_service.shared.entity.SysRole;
import com.customer_service.shared.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleRepository sysRoleRepository;

    /**
     * 获取所有角色（不分页，用于下拉选择）
     */
    public List<SysRole> getAllRoles() {
        return sysRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * 分页查询角色列表
     */
    public Page<SysRole> getRoles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        if (keyword != null && !keyword.isEmpty()) {
            return sysRoleRepository.findAll((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%")), pageable);
        }
        return sysRoleRepository.findAll(pageable);
    }

    /**
     * 根据ID获取角色
     */
    public Optional<SysRole> getRoleById(Long id) {
        return sysRoleRepository.findById(id);
    }

    /**
     * 创建角色
     */
    @Transactional
    public SysRole createRole(String code, String name, String description, String permissions) {
        if (sysRoleRepository.findByCode(code).isPresent()) {
            throw new RuntimeException("角色代码已存在");
        }

        SysRole role = new SysRole();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions != null ? permissions : "[]");
        role.setIsSystem(false);

        return sysRoleRepository.save(role);
    }

    /**
     * 更新角色
     */
    @Transactional
    public SysRole updateRole(Long id, String name, String description, String permissions) {
        SysRole role = sysRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 系统角色不允许修改 code
        if (name != null) {
            role.setName(name);
        }
        if (description != null) {
            role.setDescription(description);
        }
        if (permissions != null) {
            role.setPermissions(permissions);
        }

        return sysRoleRepository.save(role);
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = sysRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 系统角色不允许删除
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new RuntimeException("系统内置角色不能删除");
        }

        sysRoleRepository.deleteById(id);
    }
}

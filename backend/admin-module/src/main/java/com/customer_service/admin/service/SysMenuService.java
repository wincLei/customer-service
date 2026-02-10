package com.customer_service.admin.service;

import com.customer_service.shared.entity.SysMenu;
import com.customer_service.shared.repository.SysMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.customer_service.shared.util.I18nUtil;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuRepository menuRepository;

    /**
     * 获取所有菜单（树形结构）
     */
    public List<Map<String, Object>> getMenuTree() {
        List<SysMenu> allMenus = menuRepository.findAllOrdered();
        return buildTree(allMenus, null);
    }

    /**
     * 获取启用的菜单（树形结构）
     */
    public List<Map<String, Object>> getEnabledMenuTree() {
        List<SysMenu> enabledMenus = menuRepository.findAllEnabledOrdered();
        return buildTree(enabledMenus, null);
    }

    /**
     * 构建菜单树
     */
    private List<Map<String, Object>> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> Objects.equals(menu.getParentId(), parentId))
                .map(menu -> {
                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("id", menu.getId());
                    node.put("code", menu.getCode());
                    node.put("name", menu.getName());
                    node.put("type", menu.getType());
                    node.put("parentId", menu.getParentId());
                    node.put("path", menu.getPath());
                    node.put("icon", menu.getIcon());
                    node.put("sortOrder", menu.getSortOrder());
                    node.put("isEnabled", menu.getIsEnabled());
                    node.put("description", menu.getDescription());
                    node.put("createdAt", menu.getCreatedAt());
                    node.put("updatedAt", menu.getUpdatedAt());

                    List<Map<String, Object>> children = buildTree(menus, menu.getId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取所有菜单（平铺列表）
     */
    public List<SysMenu> getAllMenus() {
        return menuRepository.findAllOrdered();
    }

    /**
     * 根据ID获取菜单
     */
    public Optional<SysMenu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }

    /**
     * 根据编码获取菜单
     */
    public Optional<SysMenu> getMenuByCode(String code) {
        return menuRepository.findByCode(code);
    }

    /**
     * 创建菜单
     */
    @Transactional
    public SysMenu createMenu(SysMenu menu) {
        if (menuRepository.existsByCode(menu.getCode())) {
            throw new RuntimeException(I18nUtil.getMessage("menu.code.exists"));
        }

        // 验证父级菜单
        if (menu.getParentId() != null) {
            menuRepository.findById(menu.getParentId())
                    .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("menu.parent.not.found")));
        }

        return menuRepository.save(menu);
    }

    /**
     * 更新菜单
     */
    @Transactional
    public SysMenu updateMenu(Long id, SysMenu menuData) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("menu.not.found")));

        // 检查编码是否被其他菜单使用
        if (!menu.getCode().equals(menuData.getCode()) && menuRepository.existsByCode(menuData.getCode())) {
            throw new RuntimeException(I18nUtil.getMessage("menu.code.used"));
        }

        // 验证父级菜单（不能将自己设置为自己的父级）
        if (menuData.getParentId() != null) {
            if (menuData.getParentId().equals(id)) {
                throw new RuntimeException(I18nUtil.getMessage("menu.cannot.set.self.parent"));
            }
            // 检查是否形成循环
            if (isDescendant(id, menuData.getParentId())) {
                throw new RuntimeException(I18nUtil.getMessage("menu.cannot.set.descendant.parent"));
            }
            menuRepository.findById(menuData.getParentId())
                    .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("menu.parent.not.found")));
        }

        menu.setCode(menuData.getCode());
        menu.setName(menuData.getName());
        menu.setType(menuData.getType());
        menu.setParentId(menuData.getParentId());
        menu.setPath(menuData.getPath());
        menu.setIcon(menuData.getIcon());
        menu.setSortOrder(menuData.getSortOrder());
        menu.setIsEnabled(menuData.getIsEnabled());
        menu.setDescription(menuData.getDescription());

        return menuRepository.save(menu);
    }

    /**
     * 检查targetId是否是parentId的后代
     */
    private boolean isDescendant(Long parentId, Long targetId) {
        List<SysMenu> children = menuRepository.findByParentIdOrderBySortOrderAsc(parentId);
        for (SysMenu child : children) {
            if (child.getId().equals(targetId)) {
                return true;
            }
            if (isDescendant(child.getId(), targetId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除菜单
     */
    @Transactional
    public void deleteMenu(Long id) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("menu.not.found")));

        // 检查是否有子菜单
        List<SysMenu> children = menuRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new RuntimeException(I18nUtil.getMessage("menu.delete.children.first"));
        }

        menuRepository.delete(menu);
    }

    /**
     * 启用/禁用菜单
     */
    @Transactional
    public SysMenu toggleMenuStatus(Long id) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("menu.not.found")));
        menu.setIsEnabled(!menu.getIsEnabled());
        return menuRepository.save(menu);
    }

    /**
     * 根据菜单编码列表获取菜单
     */
    public List<SysMenu> getMenusByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return menuRepository.findByCodeIn(codes);
    }

    /**
     * 获取所有菜单编码
     */
    public List<String> getAllMenuCodes() {
        return menuRepository.findAllEnabledOrdered().stream()
                .map(SysMenu::getCode)
                .collect(Collectors.toList());
    }
}

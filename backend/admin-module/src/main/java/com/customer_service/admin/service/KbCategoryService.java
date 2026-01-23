package com.customer_service.admin.service;

import com.customer_service.admin.repository.KbCategoryRepository;
import com.customer_service.shared.entity.KbCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库分类服务
 */
@Service
@RequiredArgsConstructor
public class KbCategoryService {

    private final KbCategoryRepository kbCategoryRepository;

    /**
     * 获取项目下的所有分类（扁平列表）
     */
    public List<KbCategory> getAllCategories(Long projectId) {
        return kbCategoryRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    /**
     * 获取项目下的分类树
     */
    public List<CategoryTreeNode> getCategoryTree(Long projectId) {
        List<KbCategory> allCategories = getAllCategories(projectId);
        return buildTree(allCategories);
    }

    /**
     * 构建分类树
     */
    private List<CategoryTreeNode> buildTree(List<KbCategory> categories) {
        // 按父级分组
        Map<Long, List<KbCategory>> parentMap = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        // 构建根节点
        List<CategoryTreeNode> rootNodes = new ArrayList<>();
        List<KbCategory> rootCategories = parentMap.getOrDefault(0L, new ArrayList<>());

        for (KbCategory category : rootCategories) {
            CategoryTreeNode node = new CategoryTreeNode(category);
            node.setChildren(buildChildNodes(category.getId(), parentMap));
            node.setArticleCount(kbCategoryRepository.countArticlesByCategory(category.getId()));
            rootNodes.add(node);
        }

        return rootNodes;
    }

    /**
     * 递归构建子节点
     */
    private List<CategoryTreeNode> buildChildNodes(Long parentId, Map<Long, List<KbCategory>> parentMap) {
        List<KbCategory> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        List<CategoryTreeNode> childNodes = new ArrayList<>();

        for (KbCategory category : children) {
            CategoryTreeNode node = new CategoryTreeNode(category);
            node.setChildren(buildChildNodes(category.getId(), parentMap));
            node.setArticleCount(kbCategoryRepository.countArticlesByCategory(category.getId()));
            childNodes.add(node);
        }

        return childNodes;
    }

    /**
     * 根据ID获取分类
     */
    public KbCategory getCategoryById(Long id) {
        return kbCategoryRepository.findById(id).orElse(null);
    }

    /**
     * 创建分类
     */
    @Transactional
    public KbCategory createCategory(KbCategory category) {
        // 检查名称是否重复
        if (kbCategoryRepository.existsByProjectIdAndName(category.getProjectId(), category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        // 设置默认排序值
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        return kbCategoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    @Transactional
    public KbCategory updateCategory(Long id, KbCategory category) {
        KbCategory existing = kbCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        // 检查名称是否重复（排除自己）
        if (kbCategoryRepository.existsByProjectIdAndNameAndIdNot(existing.getProjectId(), category.getName(), id)) {
            throw new RuntimeException("分类名称已存在");
        }

        existing.setName(category.getName());
        existing.setParentId(category.getParentId());
        existing.setSortOrder(category.getSortOrder());

        return kbCategoryRepository.save(existing);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        List<KbCategory> children = kbCategoryRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }

        // 检查是否有文章
        long articleCount = kbCategoryRepository.countArticlesByCategory(id);
        if (articleCount > 0) {
            throw new RuntimeException("该分类下存在文章，无法删除");
        }

        kbCategoryRepository.deleteById(id);
    }

    /**
     * 分类树节点
     */
    @lombok.Data
    public static class CategoryTreeNode {
        private Long id;
        private Long projectId;
        private Long parentId;
        private String name;
        private Integer sortOrder;
        private Long articleCount;
        private List<CategoryTreeNode> children;

        public CategoryTreeNode(KbCategory category) {
            this.id = category.getId();
            this.projectId = category.getProjectId();
            this.parentId = category.getParentId();
            this.name = category.getName();
            this.sortOrder = category.getSortOrder();
            this.children = new ArrayList<>();
        }
    }
}

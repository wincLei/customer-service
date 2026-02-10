package com.customer_service.admin.service;

import com.customer_service.admin.repository.CustomerTagRepository;
import com.customer_service.admin.repository.UserTagRelationRepository;
import com.customer_service.shared.entity.CustomerTag;
import com.customer_service.shared.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.customer_service.shared.util.I18nUtil;

/**
 * 客户标签管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerTagService {

    private final CustomerTagRepository customerTagRepository;
    private final UserTagRelationRepository userTagRelationRepository;
    private final UserProjectRepository userProjectRepository;

    /**
     * 获取项目下的所有标签
     */
    public List<CustomerTag> getTagsByProject(Long projectId) {
        return customerTagRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    /**
     * 获取用户有权限访问的项目的所有标签
     */
    public List<CustomerTag> getTagsByProjects(List<Long> projectIds) {
        return customerTagRepository.findByProjectIdInOrderBySortOrderAsc(projectIds);
    }

    /**
     * 获取标签详情
     */
    public CustomerTag getTagById(Long id) {
        return customerTagRepository.findById(id).orElse(null);
    }

    /**
     * 创建标签
     */
    @Transactional
    public CustomerTag createTag(Long projectId, String name, String color, String description, Integer sortOrder,
            Long createdBy) {
        // 检查名称是否已存在
        if (customerTagRepository.existsByProjectIdAndName(projectId, name)) {
            throw new RuntimeException(I18nUtil.getMessage("tag.name.exists"));
        }

        CustomerTag tag = CustomerTag.builder()
                .projectId(projectId)
                .name(name)
                .color(color != null ? color : "#409EFF")
                .description(description)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .createdBy(createdBy)
                .build();

        return customerTagRepository.save(tag);
    }

    /**
     * 更新标签
     */
    @Transactional
    public CustomerTag updateTag(Long id, String name, String color, String description, Integer sortOrder) {
        CustomerTag tag = customerTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("tag.not.found")));

        // 检查名称是否与其他标签重复
        if (name != null && !name.equals(tag.getName())) {
            if (customerTagRepository.existsByProjectIdAndName(tag.getProjectId(), name)) {
                throw new RuntimeException(I18nUtil.getMessage("tag.name.exists"));
            }
            tag.setName(name);
        }

        if (color != null) {
            tag.setColor(color);
        }
        if (description != null) {
            tag.setDescription(description);
        }
        if (sortOrder != null) {
            tag.setSortOrder(sortOrder);
        }

        return customerTagRepository.save(tag);
    }

    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id) {
        CustomerTag tag = customerTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("tag.not.found")));

        // 删除所有关联关系
        // 由于有外键级联删除，这里不需要手动删除

        customerTagRepository.delete(tag);
        log.info("删除标签: {} (ID: {})", tag.getName(), id);
    }

    /**
     * 获取标签关联的用户数量
     */
    public long getTagUserCount(Long tagId) {
        return customerTagRepository.countUsersByTagId(tagId);
    }

    /**
     * 统计项目下的标签数量
     */
    public long countTagsByProject(Long projectId) {
        return customerTagRepository.countByProjectId(projectId);
    }
}

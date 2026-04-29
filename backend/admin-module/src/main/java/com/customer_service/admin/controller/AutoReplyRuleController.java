package com.customer_service.admin.controller;

import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.AutoReplyRule;
import com.customer_service.shared.repository.AutoReplyRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自动回复规则管理接口（Admin端）
 */
@RestController
@RequestMapping("/api/admin/auto-reply")
@RequiredArgsConstructor
public class AutoReplyRuleController {

    private final AutoReplyRuleRepository autoReplyRuleRepository;

    /**
     * 获取指定项目的所有规则
     */
    @GetMapping("/list/{projectId}")
    public ApiResponse<List<AutoReplyRule>> list(@PathVariable Long projectId) {
        return ApiResponse.success(
                autoReplyRuleRepository.findByProjectIdOrderByPriorityAsc(projectId));
    }

    /**
     * 创建规则
     */
    @PostMapping
    public ApiResponse<AutoReplyRule> create(@RequestBody AutoReplyRule rule) {
        if (rule.getRuleName() == null || rule.getRuleName().isBlank()) {
            return ApiResponse.fail(400, "规则名称不能为空");
        }
        if (rule.getKeywords() == null || rule.getKeywords().isBlank()) {
            return ApiResponse.fail(400, "关键词不能为空");
        }
        if (rule.getReplyContent() == null || rule.getReplyContent().isBlank()) {
            return ApiResponse.fail(400, "回复内容不能为空");
        }
        if (rule.getProjectId() == null) {
            return ApiResponse.fail(400, "项目ID不能为空");
        }
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        if (rule.getPriority() == null) {
            rule.setPriority(100);
        }
        return ApiResponse.success(autoReplyRuleRepository.save(rule));
    }

    /**
     * 更新规则
     */
    @PutMapping("/{id}")
    public ApiResponse<AutoReplyRule> update(@PathVariable Long id, @RequestBody AutoReplyRule rule) {
        return autoReplyRuleRepository.findById(id).map(existing -> {
            if (rule.getRuleName() != null)
                existing.setRuleName(rule.getRuleName());
            if (rule.getKeywords() != null)
                existing.setKeywords(rule.getKeywords());
            if (rule.getReplyContent() != null)
                existing.setReplyContent(rule.getReplyContent());
            if (rule.getEnabled() != null)
                existing.setEnabled(rule.getEnabled());
            if (rule.getPriority() != null)
                existing.setPriority(rule.getPriority());
            return ApiResponse.success(autoReplyRuleRepository.save(existing));
        }).orElse(ApiResponse.fail(404, "规则不存在"));
    }

    /**
     * 启用/禁用规则
     */
    @PatchMapping("/{id}/toggle")
    public ApiResponse<AutoReplyRule> toggle(@PathVariable Long id) {
        return autoReplyRuleRepository.findById(id).map(existing -> {
            existing.setEnabled(!existing.getEnabled());
            return ApiResponse.success(autoReplyRuleRepository.save(existing));
        }).orElse(ApiResponse.fail(404, "规则不存在"));
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!autoReplyRuleRepository.existsById(id)) {
            return ApiResponse.fail(404, "规则不存在");
        }
        autoReplyRuleRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}

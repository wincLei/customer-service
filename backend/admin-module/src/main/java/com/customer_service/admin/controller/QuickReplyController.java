package com.customer_service.admin.controller;

import com.customer_service.admin.repository.QuickReplyRepository;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.QuickReply;
import com.customer_service.shared.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 快捷回复控制器
 * 提供客服工作台的快捷回复功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/quick-replies")
@RequiredArgsConstructor
@RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
public class QuickReplyController {

    private final QuickReplyRepository quickReplyRepository;

    /**
     * 获取项目的快捷回复列表
     * 返回公共快捷回复（creatorId 为空）和当前用户创建的快捷回复
     * 
     * @param projectIds 项目ID列表（逗号分隔）
     */
    @GetMapping
    public ApiResponse<List<QuickReply>> getQuickReplies(@RequestParam("projectIds") List<Long> projectIds) {
        log.info("Get quick replies for projects: {}", projectIds);
        List<QuickReply> replies = quickReplyRepository.findByProjectIdIn(projectIds);
        return ApiResponse.success(replies);
    }

    /**
     * 创建快捷回复
     */
    @PostMapping
    public ApiResponse<QuickReply> createQuickReply(@RequestBody QuickReply quickReply) {
        log.info("Create quick reply for project: {}", quickReply.getProjectId());
        QuickReply saved = quickReplyRepository.save(quickReply);
        return ApiResponse.success(saved);
    }

    /**
     * 更新快捷回复
     */
    @PutMapping("/{id}")
    public ApiResponse<QuickReply> updateQuickReply(@PathVariable Long id, @RequestBody QuickReply quickReply) {
        log.info("Update quick reply: {}", id);
        return quickReplyRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(quickReply.getTitle());
                    existing.setContent(quickReply.getContent());
                    existing.setCategory(quickReply.getCategory());
                    return ApiResponse.success(quickReplyRepository.save(existing));
                })
                .orElse(ApiResponse.error(I18nUtil.getMessage("quick.reply.not.found")));
    }

    /**
     * 删除快捷回复
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuickReply(@PathVariable Long id) {
        log.info("Delete quick reply: {}", id);
        if (quickReplyRepository.existsById(id)) {
            quickReplyRepository.deleteById(id);
            return ApiResponse.success(null);
        }
        return ApiResponse.error(I18nUtil.getMessage("quick.reply.not.found"));
    }
}

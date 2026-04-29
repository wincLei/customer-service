package com.customer_service.portal.controller;

import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.AutoReplyRule;
import com.customer_service.shared.repository.AutoReplyRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公开自动回复规则接口 - 无需认证
 * 供前端工单提交表单做关键词匹配提示使用，仅返回关键词和回复内容
 */
@Slf4j
@RestController
@RequestMapping("/api/pub/auto-reply")
@RequiredArgsConstructor
public class PublicAutoReplyController {

    private final AutoReplyRuleRepository autoReplyRuleRepository;

    /**
     * 获取项目下已启用的自动回复规则（仅关键词+回复内容）
     */
    @GetMapping("/rules")
    public ApiResponse<List<Map<String, String>>> getRules(@RequestParam Long projectId) {
        log.debug("Public auto-reply rules for project {}", projectId);

        List<AutoReplyRule> rules = autoReplyRuleRepository
                .findByProjectIdAndEnabledTrueOrderByPriorityAsc(projectId);

        List<Map<String, String>> result = rules.stream()
                .map(rule -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("keywords", rule.getKeywords());
                    map.put("replyContent", rule.getReplyContent());
                    return map;
                })
                .collect(Collectors.toList());

        return ApiResponse.success(result);
    }
}

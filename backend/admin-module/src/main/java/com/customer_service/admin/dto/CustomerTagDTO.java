package com.customer_service.admin.dto;

import com.customer_service.shared.entity.CustomerTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户标签 DTO
 * 用于 API 返回，避免 Hibernate 懒加载代理序列化问题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTagDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String color;
    private String description;
    private Integer sortOrder;

    /**
     * 从实体转换为 DTO
     */
    public static CustomerTagDTO fromEntity(CustomerTag tag) {
        if (tag == null) {
            return null;
        }
        return CustomerTagDTO.builder()
                .id(tag.getId())
                .projectId(tag.getProjectId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .sortOrder(tag.getSortOrder())
                .build();
    }
}

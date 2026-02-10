package com.customer_service.admin.service;

import com.customer_service.shared.entity.Project;
import com.customer_service.shared.entity.UserProject;
import com.customer_service.shared.repository.ProjectRepository;
import com.customer_service.shared.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.customer_service.shared.util.I18nUtil;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有项目
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * 根据用户ID获取关联的项目列表
     */
    public List<Project> getProjectsByUserId(Long userId) {
        List<UserProject> userProjects = userProjectRepository.findByUserId(userId);
        return userProjects.stream()
                .map(UserProject::getProject)
                .collect(Collectors.toList());
    }

    /**
     * 分页搜索项目
     */
    public Page<Project> searchProjects(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 根据ID获取项目
     */
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * 根据AppKey获取项目
     */
    public Optional<Project> getProjectByAppKey(String appKey) {
        return projectRepository.findByAppKey(appKey);
    }

    /**
     * 创建项目
     */
    @Transactional
    public Project createProject(String name, String description, String welcomeMessage) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setAppKey(generateAppKey());
        project.setAppSecret(generateAppSecret());

        // 设置配置信息（包括欢迎语）
        ObjectNode config = objectMapper.createObjectNode();
        config.put("welcomeMessage",
                welcomeMessage != null ? welcomeMessage : I18nUtil.getMessage("portal.welcome.message"));
        project.setConfig(config);

        return projectRepository.save(project);
    }

    /**
     * 更新项目
     */
    @Transactional
    public Project updateProject(Long id, String name, String description, String welcomeMessage) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("project.not.found")));
        project.setName(name);
        project.setDescription(description);

        // 更新配置信息（保留其他配置，只更新欢迎语）
        ObjectNode config;
        if (project.getConfig() != null && project.getConfig().isObject()) {
            config = (ObjectNode) project.getConfig();
        } else {
            config = objectMapper.createObjectNode();
        }
        config.put("welcomeMessage", welcomeMessage != null ? welcomeMessage : "");
        project.setConfig(config);

        return projectRepository.save(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException(I18nUtil.getMessage("project.not.found"));
        }
        projectRepository.deleteById(id);
    }

    /**
     * 重新生成AppSecret
     */
    @Transactional
    public Project regenerateAppSecret(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("project.not.found")));
        project.setAppSecret(generateAppSecret());
        return projectRepository.save(project);
    }

    /**
     * 生成AppKey (32位随机字符串)
     */
    private String generateAppKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 生成AppSecret (64位随机字符串)
     */
    private String generateAppSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

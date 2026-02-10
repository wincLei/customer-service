package com.customer_service.admin.controller;

import com.customer_service.shared.annotation.Public;
import com.customer_service.shared.constant.AppDefaults;
import com.customer_service.shared.constant.UserStatus;
import com.customer_service.shared.context.UserContextHolder;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.SysRole;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.repository.SysRoleRepository;
import com.customer_service.shared.repository.SysUserRepository;
import com.customer_service.shared.repository.UserProjectRepository;
import com.customer_service.shared.util.I18nUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Public // 标记整个控制器为公开接口
public class AuthController {

    private final StringRedisTemplate redisTemplate;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final UserProjectRepository userProjectRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        // 强制验证验证码
        if (request.getCaptchaKey() == null || request.getCaptchaKey().isEmpty()) {
            return ApiResponse.error(I18nUtil.getMessage("auth.captcha.required"));
        }

        if (request.getCaptcha() == null || request.getCaptcha().isEmpty()) {
            return ApiResponse.error(I18nUtil.getMessage("auth.captcha.answer.required"));
        }

        try {
            String savedAnswer = redisTemplate.opsForValue()
                    .get(AppDefaults.REDIS_CAPTCHA_PREFIX + request.getCaptchaKey());
            if (savedAnswer == null) {
                return ApiResponse.error(I18nUtil.getMessage("auth.captcha.expired"));
            }
            if (!savedAnswer.equals(request.getCaptcha())) {
                return ApiResponse.error(I18nUtil.getMessage("auth.captcha.error"));
            }
            // 验证成功后删除验证码
            redisTemplate.delete(AppDefaults.REDIS_CAPTCHA_PREFIX + request.getCaptchaKey());
        } catch (Exception e) {
            log.error("Redis验证码验证失败: {}", e.getMessage(), e);
            return ApiResponse.error(I18nUtil.getMessage("auth.redis.error"));
        }

        // 验证用户名和密码 - 从sys_users表查询
        Optional<SysUser> userOptional = sysUserRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            log.warn("用户不存在: {}", request.getUsername());
            return ApiResponse.error(I18nUtil.getMessage("auth.login.failed"));
        }

        SysUser sysUser = userOptional.get();

        // 检查账号状态
        if (!UserStatus.ACTIVE.equals(sysUser.getStatus())) {
            log.warn("账号已禁用: {}", request.getUsername());
            return ApiResponse.error(I18nUtil.getMessage("auth.account.disabled"));
        }

        // 验证密码（使用BCrypt）
        if (!passwordEncoder.matches(request.getPassword(), sysUser.getPasswordHash())) {
            log.warn("密码错误: {}", request.getUsername());
            return ApiResponse.error(I18nUtil.getMessage("auth.login.failed"));
        }

        // 更新最后登录时间
        sysUser.setLastLoginAt(LocalDateTime.now());
        sysUserRepository.save(sysUser);

        String roleCode = sysUser.getRoleCode();
        log.info("用户登录成功: {} (ID: {}, Role: {})", sysUser.getUsername(), sysUser.getId(), roleCode);

        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 获取角色权限信息
        List<String> permissions = new ArrayList<>();
        Map<String, Object> permissionsMap = new HashMap<>();
        Optional<SysRole> roleOpt = sysRoleRepository.findByCode(roleCode);
        if (roleOpt.isPresent() && roleOpt.get().getPermissions() != null) {
            try {
                permissionsMap = objectMapper.readValue(roleOpt.get().getPermissions(),
                        new TypeReference<Map<String, Object>>() {
                        });
                if (permissionsMap.get("menus") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> menus = (List<String>) permissionsMap.get("menus");
                    permissions.addAll(menus);
                }
                if (permissionsMap.get("actions") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> actions = (List<String>) permissionsMap.get("actions");
                    permissions.addAll(actions);
                }
            } catch (Exception e) {
                log.error("解析角色权限失败", e);
            }
        }

        // 将用户信息存储到Redis，用于后续请求验证（24小时过期）
        // 获取用户关联的项目ID列表
        List<Long> projectIds = userProjectRepository.findProjectIdsByUserId(sysUser.getId());

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userId", sysUser.getId());
        tokenData.put("username", sysUser.getUsername());
        tokenData.put("roleCode", roleCode);
        tokenData.put("permissions", permissions);
        tokenData.put("projectIds", projectIds);
        try {
            String tokenJson = objectMapper.writeValueAsString(tokenData);
            redisTemplate.opsForValue().set(AppDefaults.REDIS_TOKEN_PREFIX + token, tokenJson,
                    AppDefaults.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("存储token失败", e);
        }

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        Map<String, Object> user = new HashMap<>();
        user.put("id", sysUser.getId());
        user.put("username", sysUser.getUsername());
        user.put("email", sysUser.getEmail());
        user.put("role", roleCode);
        user.put("permissions", permissionsMap);
        user.put("projectIds", projectIds); // 添加关联的项目ID列表
        user.put("avatar", sysUser.getAvatar() != null ? sysUser.getAvatar()
                : "https://api.dicebear.com/7.x/avataaars/svg?seed=" + sysUser.getUsername());
        data.put("user", user);

        return ApiResponse.success(data);
    }

    @GetMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 从Redis删除token
        if (authHeader != null && authHeader.startsWith(AppDefaults.BEARER_PREFIX)) {
            String token = authHeader.substring(AppDefaults.BEARER_PREFIX.length());
            redisTemplate.delete(AppDefaults.REDIS_TOKEN_PREFIX + token);
            log.info("用户登出，token已删除");
        }
        return ApiResponse.success("Logout successful");
    }

    /**
     * 获取当前用户信息（用于前端刷新权限）
     */
    @GetMapping("/me")
    public ApiResponse<?> getCurrentUser() {
        var context = UserContextHolder.getContext();
        if (context == null) {
            return ApiResponse.error(401, I18nUtil.getMessage("auth.not.logged.in"));
        }

        Optional<SysUser> userOpt = sysUserRepository.findById(context.getUserId());
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, I18nUtil.getMessage("auth.user.not.found"));
        }

        SysUser sysUser = userOpt.get();
        String roleCode = sysUser.getRoleCode();

        // 获取最新的角色权限
        Map<String, Object> permissionsMap = new HashMap<>();
        Optional<SysRole> roleOpt = sysRoleRepository.findByCode(roleCode);
        if (roleOpt.isPresent() && roleOpt.get().getPermissions() != null) {
            try {
                permissionsMap = objectMapper.readValue(roleOpt.get().getPermissions(),
                        new TypeReference<Map<String, Object>>() {
                        });
            } catch (Exception e) {
                log.error("解析角色权限失败", e);
            }
        }

        Map<String, Object> user = new HashMap<>();
        user.put("id", sysUser.getId());
        user.put("username", sysUser.getUsername());
        user.put("email", sysUser.getEmail());
        user.put("role", roleCode);
        user.put("permissions", permissionsMap);
        user.put("avatar", sysUser.getAvatar() != null ? sysUser.getAvatar()
                : "https://api.dicebear.com/7.x/avataaars/svg?seed=" + sysUser.getUsername());

        return ApiResponse.success(user);
    }

    @GetMapping("/generate-hash")
    public ApiResponse<?> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, Object> data = new HashMap<>();
        data.put("password", password);
        data.put("hash", hash);
        data.put("verify", passwordEncoder.matches(password, hash));
        return ApiResponse.success(data);
    }

    @GetMapping("/captcha")
    public ApiResponse<?> getCaptcha() {
        try {
            // 生成随机数学验证码
            Random random = new Random();
            int num1 = random.nextInt(20) + 1; // 1-20
            int num2 = random.nextInt(20) + 1; // 1-20
            String[] operators = { "+", "-", "×" };
            String operator = operators[random.nextInt(operators.length)];

            int answer;
            String question;

            switch (operator) {
                case "+":
                    answer = num1 + num2;
                    question = num1 + " + " + num2 + " = ?";
                    break;
                case "-":
                    // 确保减法结果为正数
                    if (num1 < num2) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                    answer = num1 - num2;
                    question = num1 + " - " + num2 + " = ?";
                    break;
                case "×":
                    // 使用较小的数字进行乘法
                    num1 = random.nextInt(10) + 1; // 1-10
                    num2 = random.nextInt(10) + 1; // 1-10
                    answer = num1 * num2;
                    question = num1 + " × " + num2 + " = ?";
                    break;
                default:
                    answer = num1 + num2;
                    question = num1 + " + " + num2 + " = ?";
            }

            // 生成唯一key
            String key = UUID.randomUUID().toString().replace("-", "");

            // 保存答案到Redis，5分钟过期
            redisTemplate.opsForValue().set(AppDefaults.REDIS_CAPTCHA_PREFIX + key, String.valueOf(answer),
                    AppDefaults.CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.info("验证码生成成功: key={}, question={}", key, question);

            Map<String, Object> data = new HashMap<>();
            data.put("key", key);
            data.put("question", question);

            return ApiResponse.success(data);
        } catch (Exception e) {
            log.error("生成验证码失败: {}", e.getMessage(), e);
            return ApiResponse.error(I18nUtil.getMessage("auth.captcha.redis.error"));
        }
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String captcha;
        private String captchaKey;
    }
}

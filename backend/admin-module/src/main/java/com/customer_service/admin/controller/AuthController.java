package com.customer_service.admin.controller;

import com.customer_service.shared.dto.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StringRedisTemplate redisTemplate;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        log.info("Agent login attempt: {}", request.getUsername());

        // 强制验证验证码
        if (request.getCaptchaKey() == null || request.getCaptchaKey().isEmpty()) {
            return ApiResponse.error("请先获取验证码");
        }

        if (request.getCaptcha() == null || request.getCaptcha().isEmpty()) {
            return ApiResponse.error("请输入验证码答案");
        }

        try {
            String savedAnswer = redisTemplate.opsForValue().get("captcha:" + request.getCaptchaKey());
            if (savedAnswer == null) {
                return ApiResponse.error("验证码已过期，请刷新后重试");
            }
            if (!savedAnswer.equals(request.getCaptcha())) {
                return ApiResponse.error("验证码错误");
            }
            // 验证成功后删除验证码
            redisTemplate.delete("captcha:" + request.getCaptchaKey());
        } catch (Exception e) {
            log.error("Redis验证码验证失败: {}", e.getMessage(), e);
            return ApiResponse.error("Redis服务异常，请稍后重试");
        }

        // 验证用户名和密码（简单示例）
        if (!"admin".equals(request.getUsername()) || !"admin123".equals(request.getPassword())) {
            return ApiResponse.error("用户名或密码错误");
        }

        // 生成token（实际应该使用JWT）
        String token = UUID.randomUUID().toString().replace("-", "");

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        Map<String, Object> user = new HashMap<>();
        user.put("id", "1");
        user.put("username", request.getUsername());
        user.put("email", "admin@example.com");
        user.put("role", "admin");
        user.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=admin");
        data.put("user", user);

        return ApiResponse.success(data);
    }

    @GetMapping("/logout")
    public ApiResponse<?> logout() {
        return ApiResponse.success("Logout successful");
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
            redisTemplate.opsForValue().set("captcha:" + key, String.valueOf(answer), 5, TimeUnit.MINUTES);
            log.info("验证码生成成功: key={}, question={}", key, question);

            Map<String, Object> data = new HashMap<>();
            data.put("key", key);
            data.put("question", question);

            return ApiResponse.success(data);
        } catch (Exception e) {
            log.error("生成验证码失败: {}", e.getMessage(), e);
            return ApiResponse.error("Redis服务异常，无法生成验证码，请稍后重试");
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

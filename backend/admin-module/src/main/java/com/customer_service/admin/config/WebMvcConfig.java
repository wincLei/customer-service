package com.customer_service.admin.config;

import com.customer_service.admin.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/admin/**") // 拦截所有admin接口
                .excludePathPatterns(
                        "/api/admin/auth/login", // 登录接口
                        "/api/admin/auth/captcha", // 验证码接口
                        "/api/admin/auth/logout", // 登出接口
                        "/api/admin/auth/generate-hash" // 密码生成工具（开发用）
                );
    }
}

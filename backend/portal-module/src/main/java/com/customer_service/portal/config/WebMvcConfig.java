package com.customer_service.portal.config;

import com.customer_service.portal.security.PortalTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置拦截器和其他 Web 相关设置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PortalTokenInterceptor portalTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(portalTokenInterceptor)
                // 拦截 portal 相关接口
                .addPathPatterns("/api/portal/**")
                // 排除不需要验证的接口
                .excludePathPatterns(
                        "/api/portal/user/init", // 用户初始化接口（用于获取 token）
                        "/api/pub/**", // 公开配置接口
                        "/internal/**" // 内部回调接口
                );
    }
}

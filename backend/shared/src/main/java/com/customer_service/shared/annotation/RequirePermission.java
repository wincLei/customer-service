package com.customer_service.shared.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 * 用于标记需要特定权限才能访问的接口
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限列表，满足其中任意一个即可访问
     */
    String[] value() default {};

    /**
     * 需要的角色列表，满足其中任意一个即可访问
     */
    String[] roles() default {};

    /**
     * 是否需要登录（默认需要）
     */
    boolean requireLogin() default true;
}

package com.customer_service.shared.annotation;

import java.lang.annotation.*;

/**
 * 公开接口注解
 * 标记不需要认证的公开接口
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Public {
}

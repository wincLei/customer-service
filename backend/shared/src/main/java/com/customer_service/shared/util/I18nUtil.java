package com.customer_service.shared.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Internationalization utility class.
 * Provides static access to localized messages.
 */
@Component
public class I18nUtil {

    private static MessageSource messageSource;

    public I18nUtil(MessageSource messageSource) {
        I18nUtil.messageSource = messageSource;
    }

    /**
     * Get localized message by key using current request locale.
     */
    public static String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message by key with arguments.
     */
    public static String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message by key with specific locale.
     */
    public static String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, key, locale);
    }
}

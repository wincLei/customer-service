package com.customer_service.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具测试类
 * 用于根据明文密码生成 BCrypt 哈希字符串，便于插入数据库
 * 
 * 运行方式：
 * 1. 在 IDE 中直接运行 main 方法
 * 2. 或使用命令: mvn -pl admin-module -Dtest=PasswordHashGeneratorTest test
 */
public class PasswordHashGeneratorTest {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        // 在这里修改要生成哈希的明文密码
        String[] passwords = {
                "admin123",
                "123456",
                "password",
                "test123"
        };

        System.out.println("===========================================");
        System.out.println("      BCrypt 密码哈希生成器");
        System.out.println("===========================================\n");

        for (String password : passwords) {
            String hash = generateHash(password);
            System.out.println("明文密码: " + password);
            System.out.println("BCrypt哈希: " + hash);
            System.out.println("验证结果: " + verifyPassword(password, hash));
            System.out.println("-------------------------------------------");
        }

        // 交互式生成（如果需要自定义密码）
        System.out.println("\n自定义密码生成示例:");
        String customPassword = "myCustomPassword";
        System.out.println("明文: " + customPassword);
        System.out.println("哈希: " + generateHash(customPassword));
    }

    /**
     * 根据明文密码生成 BCrypt 哈希
     * 
     * @param plainPassword 明文密码
     * @return BCrypt 哈希字符串
     */
    public static String generateHash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    /**
     * 验证明文密码与哈希是否匹配
     * 
     * @param plainPassword 明文密码
     * @param hash          BCrypt 哈希
     * @return 是否匹配
     */
    public static boolean verifyPassword(String plainPassword, String hash) {
        return encoder.matches(plainPassword, hash);
    }

    /**
     * 生成指定密码的哈希并打印
     * 
     * @param password 明文密码
     */
    public static void printHash(String password) {
        String hash = generateHash(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Verify: " + verifyPassword(password, hash));
    }
}

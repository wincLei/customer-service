package com.customer_service.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具
 * 
 * 使用方法（在项目根目录下运行）:
 * 
 * 1. 先编译项目:
 * cd backend && mvn compile -pl admin-module -am
 * 
 * 2. 运行工具:
 * cd backend/admin-module
 * mvn exec:java
 * -Dexec.mainClass="com.customer_service.admin.util.PasswordHashGenerator"
 * -Dexec.args="你的密码"
 * 
 * 或者直接在 IDE 中运行 main 方法
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 如果提供了命令行参数，使用参数作为密码
        if (args.length > 0) {
            String password = args[0];
            String hash = encoder.encode(password);
            System.out.println("===========================================");
            System.out.println("明文密码: " + password);
            System.out.println("BCrypt哈希: " + hash);
            System.out.println("===========================================");
            return;
        }

        // 默认生成几个常用密码的哈希
        String[] passwords = {
                "admin123",
                "123456",
                "password",
                "agent123"
        };

        System.out.println("===========================================");
        System.out.println("      BCrypt 密码哈希生成器");
        System.out.println("===========================================\n");

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("明文密码: " + password);
            System.out.println("BCrypt哈希: " + hash);
            System.out.println("验证结果: " + encoder.matches(password, hash));
            System.out.println("-------------------------------------------");
        }

        System.out.println("\n提示: 可以通过命令行参数指定密码:");
        System.out.println(
                "mvn exec:java -Dexec.mainClass=\"com.customer_service.admin.util.PasswordHashGenerator\" -Dexec.args=\"你的密码\"");
    }
}

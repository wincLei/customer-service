package com.customer_service.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        
        // 生成多个哈希来验证
        for (int i = 0; i < 3; i++) {
            String hash = encoder.encode(password);
            System.out.println("Hash " + (i+1) + ": " + hash);
            System.out.println("Verify: " + encoder.matches(password, hash));
            System.out.println();
        }
        
        // 测试已有的哈希
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1PVHwz9JXPZJgN3mYPJPSPaeKS8vGgS";
        System.out.println("Testing existing hash: " + existingHash);
        System.out.println("Matches 'admin123': " + encoder.matches("admin123", existingHash));
    }
}

#!/bin/bash
# 密码哈希生成脚本
# 使用方法: ./bin/generate-password-hash.sh [password]
# 如果不提供参数，默认生成几个常用密码的哈希

cd "$(dirname "$0")/.."

PASSWORD="${1:-}"

echo "=== BCrypt 密码哈希生成器 ==="
echo ""

# 检查是否安装了 Python 和 bcrypt
if python3 -c "import bcrypt" 2>/dev/null; then
    if [ -n "$PASSWORD" ]; then
        python3 << EOF
import bcrypt
password = "$PASSWORD".encode('utf-8')
salt = bcrypt.gensalt()
hashed = bcrypt.hashpw(password, salt)
print(f"明文密码: $PASSWORD")
print(f"BCrypt哈希: {hashed.decode('utf-8')}")
EOF
    else
        python3 << 'EOF'
import bcrypt

passwords = ["admin123", "123456", "password", "agent123"]
print("生成常用密码的 BCrypt 哈希:\n")
for pwd in passwords:
    password = pwd.encode('utf-8')
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password, salt)
    print(f"明文密码: {pwd}")
    print(f"BCrypt哈希: {hashed.decode('utf-8')}")
    print("-------------------------------------------")

print("\n提示: 可以指定密码生成哈希:")
print("  ./bin/generate-password-hash.sh 你的密码")
EOF
    fi
else
    echo "需要安装 bcrypt 库，请运行: pip3 install bcrypt"
    echo ""
    echo "或者使用 htpasswd 生成（注意这不是 BCrypt）:"
    if command -v htpasswd &> /dev/null; then
        if [ -n "$PASSWORD" ]; then
            htpasswd -nbBC 10 "" "$PASSWORD" | tr -d ':\n'
            echo ""
        fi
    fi
fi


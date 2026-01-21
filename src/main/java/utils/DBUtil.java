package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Base64;

public class DBUtil {
    // 数据库配置
    private static final String DB_NAME = "gym_system";
    // 基础连接（不带数据库名，用于创建数据库）
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    // 完整连接（带数据库名，用于正常操作）
    private static final String FULL_URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    // 注意：Mac 本地安装 MySQL 后默认密码通常为空，或者你自己设置的密码。
    // 如果你在 Mac 上运行报错 Access Denied，请检查这里的密码是否正确。
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    // 静态块加载驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ 无法加载 MySQL 驱动，请检查依赖 (pom.xml) 是否包含 mysql-connector-j");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接（智能版）
     * 如果数据库不存在，会自动尝试创建并初始化
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(FULL_URL, USER, PASSWORD);
        } catch (SQLException e) {
            // 错误码 1049 代表 "Unknown database" (数据库不存在)
            if (e.getErrorCode() == 1049) {
                System.out.println("⚠️ 检测到数据库 '" + DB_NAME + "' 不存在，开始自动初始化...");
                initDatabase();
                // 初始化完成后，再次尝试连接
                return DriverManager.getConnection(FULL_URL, USER, PASSWORD);
            } else {
                // 其他错误（如密码错、服务没启动）直接抛出
                throw e;
            }
        }
    }

    /**
     * 初始化数据库：建库 + 执行 SQL 脚本
     */
    private static void initDatabase() {
        // 使用不带库名的 URL 连接，专门用来执行 CREATE DATABASE
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. 创建数据库
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4");
            stmt.executeUpdate("USE " + DB_NAME);
            System.out.println("✅ 数据库 " + DB_NAME + " 创建成功！");

            // 2. 执行 SQL 脚本
            runSqlScript(stmt);

        } catch (Exception e) {
            System.err.println("❌ 数据库初始化失败！请检查 MySQL 服务是否启动，以及账号密码是否匹配。");
            e.printStackTrace();
        }
    }

    /**
     * 读取 resources/init.sql 并执行
     */
    private static void runSqlScript(Statement stmt) throws Exception {
        // 从类路径读取文件 (兼容 Windows 和 Mac)
        InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("init.sql");
        if (is == null) {
            throw new RuntimeException("❌ 未找到 init.sql 文件！请确保它在 src/main/resources 目录下。");
        }

        System.out.println("📂 正在导入数据脚本...");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            StringBuilder sql = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("/*") || line.startsWith("*")) {
                    continue;
                }

                sql.append(line);

                // 以分号结尾表示一句 SQL 结束
                if (line.endsWith(";")) {
                    try {
                        stmt.execute(sql.toString());
                    } catch (SQLException e) {
                        // 忽略表已存在等非致命错误
                        System.out.println("⚠️ SQL 执行跳过: " + e.getMessage());
                    }
                    sql.setLength(0); // 清空缓冲区
                } else {
                    sql.append(" ");
                }
            }
            System.out.println("✅ 数据库表结构及数据导入完成！");
        }
    }

    /**
     * 测试连接
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ 数据库连接测试通过");
            }
        } catch (SQLException e) {
            System.err.println("❌ 连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 密码哈希 (保持不变)
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
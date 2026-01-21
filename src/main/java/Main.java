import Ui.LoginUi;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import utils.DBUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // 1. 设置 UI 属性 (Mac 风格)
        System.setProperty("flatlaf.useWindowDecorations", "true");
        System.setProperty("flatlaf.menuBarEmbedded", "true");

        try {
            FlatMacLightLaf.setup();
            System.out.println("✅ 皮肤加载成功");
        } catch (Exception e) {
            System.err.println("❌ 皮肤加载失败");
            e.printStackTrace();
        }

        // 2. 优化字体
        optimizeFont();

        // 3. 【关键修改】启动前同步检查数据库
        // 这会触发 DBUtil 的自动创建逻辑。如果报错，直接弹窗提示，避免程序静默崩溃。
        System.out.println("正在检查数据库环境...");
        try {
            DBUtil.getConnection();
            System.out.println("✅ 环境检查完毕，启动系统。");
        } catch (SQLException e) {
            String os = System.getProperty("os.name");
            String msg = "无法连接数据库！\n错误信息: " + e.getMessage();
            if (os.toLowerCase().contains("mac") && e.getMessage().contains("Access denied")) {
                msg += "\n\n提示：Mac 上 MySQL 默认密码可能不是 123456，请去 DBUtil.java 修改。";
            }
            JOptionPane.showMessageDialog(null, msg, "数据库错误", JOptionPane.ERROR_MESSAGE);
            // 数据库连不上，程序通常无法继续运行，这里可以选择退出
            // System.exit(1);
        }

        // 4. 启动登录界面
        SwingUtilities.invokeLater(() -> {
            new LoginUi().LoginJFrame();
        });
    }

    private static void optimizeFont() {
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 14);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
            }
        }
    }
}
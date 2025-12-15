package utils;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class StyleUtils {

    // ... (配色方案保持不变) ...
    public static final Color COLOR_PRIMARY   = new Color(64, 158, 255);
    public static final Color COLOR_SUCCESS   = new Color(103, 194, 58);
    public static final Color COLOR_WARNING   = new Color(230, 162, 60);
    public static final Color COLOR_DANGER    = new Color(245, 108, 108);
    public static final Color COLOR_INFO      = new Color(144, 147, 153);
    public static final Color COLOR_TEXT_MAIN = new Color(48, 49, 51);
    public static final Color COLOR_BG        = new Color(245, 247, 250);
    public static final Color COLOR_WHITE     = Color.WHITE;

    // ==================== 核心修改：改为逻辑字体 ====================
    // 只有改为 Font.SANS_SERIF，Java 才会自动去系统里找能显示 Emoji 的字体
    public static final String FONT_NAME     = Font.SANS_SERIF;

    public static final Font FONT_TITLE_BIG  = new Font(FONT_NAME, Font.BOLD, 24);
    public static final Font FONT_TITLE      = new Font(FONT_NAME, Font.BOLD, 18);
    public static final Font FONT_NORMAL     = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font FONT_BOLD       = new Font(FONT_NAME, Font.BOLD, 14);

    public static void initGlobalTheme() {
        try {
            FlatLightLaf.setup();

            // 强制覆盖所有组件字体
            UIManager.put("defaultFont", FONT_NORMAL);
            UIManager.put("Button.font", FONT_BOLD);
            UIManager.put("Label.font", FONT_NORMAL);
            UIManager.put("TextField.font", FONT_NORMAL);
            UIManager.put("TableHeader.font", FONT_BOLD);

            // 加上这句以防万一
            UIManager.put("TitlePane.font", FONT_NORMAL);

            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ... (styleButton, styleTextField, styleTable 方法保持不变) ...
    public static void styleButton(JButton btn, Color bgColor) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_NORMAL);
        field.setForeground(COLOR_TEXT_MAIN);
        field.setMargin(new Insets(5, 5, 5, 5));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(FONT_NORMAL);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(242, 242, 242));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }
}
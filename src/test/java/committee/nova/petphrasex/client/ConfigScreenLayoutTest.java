package committee.nova.petphrasex.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigScreenLayoutTest {
    private static final int ROWS = 6;

    /** 常見 GUI 邏輯解析度（含 GUI Scale 1~4 與寬高比變化） */
    private static final int[][] SCREEN_SIZES = {
            {320, 240}, {427, 240}, {480, 270}, {640, 360}, {854, 480},
            {560, 320}, {960, 540}, {384, 216}, {1280, 720}, {1920, 1080},
            {320, 200}, {400, 300}, {500, 260}, {256, 160}
    };

    @Test
    void everyRowAndWidgetStaysInsideTheScreen() {
        for (int[] size : SCREEN_SIZES) {
            int w = size[0];
            int h = size[1];
            ConfigScreenLayout layout = ConfigScreenLayout.compute(w, h, ROWS);

            for (int i = 0; i < ROWS; i++) {
                int widgetY = layout.widgetY(i);
                int widgetBottom = widgetY + layout.fieldHeight;
                String at = w + "x" + h + " row " + i;

                assertTrue(layout.fieldX >= 0, at + ": fieldX < 0");
                assertTrue(layout.fieldX + layout.fieldWidth <= w, at + ": 输入框超出右边界 (" + (layout.fieldX + layout.fieldWidth) + " > " + w + ")");
                assertTrue(widgetY >= ConfigScreenLayout.HEADER_HEIGHT, at + ": 控件压到页首");
                assertTrue(widgetBottom <= h, at + ": 控件超出下边界");
                assertTrue(layout.labelY(i) >= ConfigScreenLayout.HEADER_HEIGHT, at + ": 标签压到页首");
                assertTrue(layout.labelY(i) + ConfigScreenLayout.LABEL_LINE_HEIGHT <= h, at + ": 标签超出下边界");
                assertTrue(layout.fieldHeight > 0, at + ": 输入框高度非正");
            }

            // 内容区不应与页尾按钮区重叠（GUI 逻辑高度 >= 130 时）
            if (h >= 130) {
                assertTrue(layout.contentBottom() <= layout.buttonY(),
                        w + "x" + h + ": 内容区压到按钮 (" + layout.contentBottom() + " > " + layout.buttonY() + ")");
            }

            int btnW = layout.buttonWidth();
            int start = layout.buttonStartX();
            assertTrue(start >= 0, w + "x" + h + ": 按钮起点 < 0");
            assertTrue(start + btnW * 2 + layout.buttonGap() <= w, w + "x" + h + ": 按钮超出右边界");
            assertTrue(layout.buttonY() + layout.buttonHeight() <= h, w + "x" + h + ": 按钮超出下边界");
        }
    }

    @Test
    void rowLayoutIsNonOverlapping() {
        for (int[] size : SCREEN_SIZES) {
            ConfigScreenLayout layout = ConfigScreenLayout.compute(size[0], size[1], ROWS);
            for (int i = 1; i < ROWS; i++) {
                assertTrue(layout.rowY(i) >= layout.rowY(i - 1) + layout.fieldHeight,
                        size[0] + "x" + size[1] + ": 第 " + i + " 行与上一行重叠");
            }
        }
    }

    @Test
    void tallScreensUseStackedLabelsAndShortOnesSwitchToInline() {
        ConfigScreenLayout tall = ConfigScreenLayout.compute(854, 480, ROWS);
        assertFalse(tall.inlineLabels, "高解析度应使用标签在上、输入框在下的版面");

        ConfigScreenLayout shortScreen = ConfigScreenLayout.compute(427, 240, ROWS);
        assertTrue(shortScreen.inlineLabels, "240 高的画面应切换为紧凑版面");

        // 两种版面的输入框高度都必须是可点击的合理值
        assertTrue(tall.fieldHeight >= 16, "stacked fieldHeight 过小: " + tall.fieldHeight);
        assertTrue(shortScreen.fieldHeight >= 12, "inline fieldHeight 过小: " + shortScreen.fieldHeight);
    }

    @Test
    void widerScreensGetWiderCappedFields() {
        ConfigScreenLayout small = ConfigScreenLayout.compute(427, 240, ROWS);
        ConfigScreenLayout large = ConfigScreenLayout.compute(1920, 1080, ROWS);

        assertTrue(large.fieldWidth >= small.fieldWidth, "大画面输入框不应比小画面窄");
        assertTrue(large.fieldWidth <= 240, "输入框宽度应有上限: " + large.fieldWidth);
        assertEquals(0, (large.screenWidth - large.fieldWidth) / 2 - large.fieldX, "stacked 版面输入框应水平居中");
    }
}

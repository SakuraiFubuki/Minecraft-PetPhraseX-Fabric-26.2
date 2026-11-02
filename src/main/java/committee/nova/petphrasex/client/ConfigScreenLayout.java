package committee.nova.petphrasex.client;

/**
 * 設定画面的版面計算。刻意不引用任何 Minecraft 類別，方便以單元測試驗證各種視窗尺寸下的版面
 * 是否都落在畫面內、且不與頁首/頁尾重疊。
 */
public final class ConfigScreenLayout {
    public static final int HEADER_HEIGHT = 26;
    public static final int FOOTER_HEIGHT = 34;
    public static final int LABEL_LINE_HEIGHT = 9;
    /** 行高低於此值時改用「標籤在左、輸入框在右」的緊湊版面 */
    public static final int STACKED_MIN_ROW = 34;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;

    public final int screenWidth;
    public final int screenHeight;
    public final int rowCount;
    public final int rowHeight;
    public final int fieldHeight;
    public final boolean inlineLabels;
    public final int labelX;
    public final int labelWidth;
    public final int fieldX;
    public final int fieldWidth;

    private ConfigScreenLayout(int screenWidth, int screenHeight, int rowCount, int rowHeight, int fieldHeight,
                               boolean inlineLabels, int labelX, int labelWidth, int fieldX, int fieldWidth) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.rowCount = rowCount;
        this.rowHeight = rowHeight;
        this.fieldHeight = fieldHeight;
        this.inlineLabels = inlineLabels;
        this.labelX = labelX;
        this.labelWidth = labelWidth;
        this.fieldX = fieldX;
        this.fieldWidth = fieldWidth;
    }

    public static ConfigScreenLayout compute(int screenWidth, int screenHeight, int rowCount) {
        int available = Math.max(1, screenHeight - HEADER_HEIGHT - FOOTER_HEIGHT);
        int rowHeight = Math.max(10, available / Math.max(1, rowCount));
        boolean inline = rowHeight < STACKED_MIN_ROW;
        int fieldHeight = inline ? Math.max(8, rowHeight - 4) : 20;

        int labelX;
        int labelWidth;
        int fieldX;
        int fieldWidth;
        if (inline) {
            labelX = 6;
            labelWidth = Math.max(30, Math.min(160, (int) (screenWidth * 0.40) - labelX));
            fieldX = labelX + labelWidth + 6;
            fieldWidth = Math.max(40, Math.min(240, screenWidth - fieldX - 6));
        } else {
            labelX = 0;
            labelWidth = Math.max(40, screenWidth - 16);
            fieldWidth = Math.max(60, Math.min(240, screenWidth - 40));
            fieldX = Math.max(0, (screenWidth - fieldWidth) / 2);
        }

        return new ConfigScreenLayout(screenWidth, screenHeight, rowCount, rowHeight, fieldHeight,
                inline, labelX, labelWidth, fieldX, fieldWidth);
    }

    public int rowY(int index) {
        return HEADER_HEIGHT + index * rowHeight;
    }

    public int widgetY(int index) {
        int rowTop = rowY(index);
        return this.inlineLabels
                ? rowTop + Math.max(0, (this.rowHeight - this.fieldHeight) / 2)
                : rowTop + 12;
    }

    public int labelY(int index) {
        int rowTop = rowY(index);
        return this.inlineLabels
                ? rowTop + Math.max(0, (this.rowHeight - LABEL_LINE_HEIGHT) / 2)
                : rowTop + 2;
    }

    public int contentBottom() {
        return HEADER_HEIGHT + this.rowCount * this.rowHeight;
    }

    public int buttonWidth() {
        return Math.max(40, Math.min(120, (this.screenWidth - 24 - BUTTON_GAP) / 2));
    }

    public int buttonHeight() {
        return BUTTON_HEIGHT;
    }

    public int buttonY() {
        return this.screenHeight - BUTTON_HEIGHT - 6;
    }

    public int buttonStartX() {
        return Math.max(0, (this.screenWidth - (buttonWidth() * 2 + BUTTON_GAP)) / 2);
    }

    public int buttonGap() {
        return BUTTON_GAP;
    }
}

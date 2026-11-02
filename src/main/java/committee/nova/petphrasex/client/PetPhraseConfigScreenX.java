package committee.nova.petphrasex.client;

import committee.nova.petphrasex.config.PetPhraseConfigX;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 設定画面。版面全部由 {@link ConfigScreenLayout} 依視窗大小計算：視窗縮放或 GUI Scale 變更時
 * init() 會重跑並重新排版；高度不足時自動切換成緊湊版面，不會有控件掉出畫面。
 * 文字一律走 {@link PetPhraseLang}（本模組不依賴 Fabric API，模組語系不會被原版載入）。
 */
public class PetPhraseConfigScreenX extends Screen {
    private static final int ROW_COUNT = 6;

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();

    private EditBox ignoreMarkField;
    private EditBox prefixField;
    private EditBox suffixField;
    private EditBox sPrefixField;
    private EditBox sSuffixField;
    private boolean removeIgnoreMark;

    private ConfigScreenLayout layout = ConfigScreenLayout.compute(320, 240, ROW_COUNT);

    private static final class Row {
        final Component label;
        final int labelY;
        final boolean centered;

        Row(Component label, int labelY, boolean centered) {
            this.label = label;
            this.labelY = labelY;
            this.centered = centered;
        }
    }

    public PetPhraseConfigScreenX(Screen parent) {
        super(PetPhraseLang.text("screen.petphrasex.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.rows.clear();
        this.layout = ConfigScreenLayout.compute(this.width, this.height, ROW_COUNT);

        PetPhraseConfigX config = PetPhraseConfigX.get();
        this.removeIgnoreMark = config.removeIgnoreMark;

        this.ignoreMarkField = createField(config.ignoreMark, "screen.petphrasex.config.ignore_mark");
        addRow(PetPhraseLang.text("screen.petphrasex.config.ignore_mark.desc"), this.ignoreMarkField);

        Button toggle = Button.builder(
                        this.getRemoveIgnoreMarkButtonText(this.removeIgnoreMark),
                        button -> {
                            this.removeIgnoreMark = !this.removeIgnoreMark;
                            button.setMessage(this.getRemoveIgnoreMarkButtonText(this.removeIgnoreMark));
                        })
                .bounds(this.layout.fieldX, 0, this.layout.fieldWidth, this.layout.fieldHeight)
                .build();
        addRow(PetPhraseLang.text("screen.petphrasex.config.remove_ignore_mark.desc"), toggle);

        this.prefixField = createField(config.prefix, "screen.petphrasex.config.prefix");
        addRow(PetPhraseLang.text("screen.petphrasex.config.prefix.desc"), this.prefixField);

        this.suffixField = createField(config.suffix, "screen.petphrasex.config.suffix");
        addRow(PetPhraseLang.text("screen.petphrasex.config.suffix.desc"), this.suffixField);

        this.sPrefixField = createField(config.sentencePrefix, "screen.petphrasex.config.sentence_prefix");
        addRow(PetPhraseLang.text("screen.petphrasex.config.sentence_prefix.desc"), this.sPrefixField);

        this.sSuffixField = createField(config.sentenceSuffix, "screen.petphrasex.config.sentence_suffix");
        addRow(PetPhraseLang.text("screen.petphrasex.config.sentence_suffix.desc"), this.sSuffixField);

        int buttonWidth = this.layout.buttonWidth();
        int buttonHeight = this.layout.buttonHeight();
        int buttonY = this.layout.buttonY();
        int startX = this.layout.buttonStartX();
        int gap = this.layout.buttonGap();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.save())
                .bounds(startX, buttonY, buttonWidth, buttonHeight).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(startX + buttonWidth + gap, buttonY, buttonWidth, buttonHeight).build());
    }

    private EditBox createField(String value, String narrationKey) {
        EditBox box = new EditBox(this.font, this.layout.fieldX, 0, this.layout.fieldWidth, this.layout.fieldHeight,
                PetPhraseLang.text(narrationKey));
        box.setValue(value);
        return box;
    }

    private void addRow(Component label, AbstractWidget widget) {
        int index = this.rows.size();
        widget.setX(this.layout.fieldX);
        widget.setY(this.layout.widgetY(index));
        widget.setWidth(this.layout.fieldWidth);
        widget.setHeight(this.layout.fieldHeight);
        this.addRenderableWidget(widget);
        this.rows.add(new Row(label, this.layout.labelY(index), !this.layout.inlineLabels));
    }

    private void save() {
        PetPhraseConfigX config = PetPhraseConfigX.get();
        config.ignoreMark = this.ignoreMarkField.getValue();
        config.removeIgnoreMark = this.removeIgnoreMark;
        config.prefix = this.prefixField.getValue();
        config.suffix = this.suffixField.getValue();
        config.sentencePrefix = this.sPrefixField.getValue();
        config.sentenceSuffix = this.sSuffixField.getValue();
        PetPhraseConfigX.save();

        this.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int titleY = Math.max(4, (ConfigScreenLayout.HEADER_HEIGHT - ConfigScreenLayout.LABEL_LINE_HEIGHT) / 2);
        // 26.x 的文字颜色是 ARGB：alpha 为 0 时 text() 会直接跳过绘制，必须带 0xFF 前缀
        graphics.text(this.font, this.title.getVisualOrderText(),
                Math.max(4, (this.width - this.font.width(this.title)) / 2), titleY, 0xFFFFFFFF, true);

        int labelColor = 0xFFA0A0A0;
        for (Row row : this.rows) {
            if (row.centered) {
                int x = Math.max(4, (this.width - this.font.width(row.label)) / 2);
                graphics.text(this.font, row.label, x, row.labelY, labelColor, true);
            } else {
                // 緊湊版面：標籤只取一行，超出欄寬時截斷並補省略號，避免壓到輸入框
                int maxWidth = Math.max(16, this.layout.labelWidth);
                String text = row.label.getString();
                if (this.font.width(text) > maxWidth) {
                    String ellipsis = "…";
                    text = this.font.plainSubstrByWidth(text, maxWidth - this.font.width(ellipsis)) + ellipsis;
                }
                graphics.text(this.font, Component.literal(text), this.layout.labelX, row.labelY, labelColor, true);
            }
        }
    }

    private Component getRemoveIgnoreMarkButtonText(boolean removeIgnoreMark) {
        return PetPhraseLang.text("screen.petphrasex.config.remove_ignore_mark.button",
                I18n.get(removeIgnoreMark ? "options.on" : "options.off"));
    }

    @Override
    public void onClose() {
        // 26.2 起屏幕切换从 Minecraft.setScreen 移到 Gui.setScreen
        if (this.minecraft != null) this.minecraft.gui.setScreen(this.parent);
    }
}

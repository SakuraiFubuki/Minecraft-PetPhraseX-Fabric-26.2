package committee.nova.petphrasex.mixin;

import committee.nova.petphrasex.config.PetPhraseConfigX;
import committee.nova.petphrasex.util.StringUtil;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @ModifyVariable(
            method = "handleChatInput",
            at = @At("HEAD"),
            argsOnly = true
    )
    private String modifyMsg(String chatMessage) {
        PetPhraseConfigX.reloadIfChanged();
        PetPhraseConfigX config = PetPhraseConfigX.get();
        if (!config.enableClientPhrase) return chatMessage;

        // 客户端口癖一律由本地设定决定（含单人存档）；服务端口癖只在专用伺服器生效，
        // 两条路径互斥，不会重复加词
        return StringUtil.processMessage(
                chatMessage,
                config.ignoreMark,
                config.removeIgnoreMark,
                config.prefix,
                config.suffix,
                config.sentencePrefix,
                config.sentenceSuffix
        );
    }
}

package committee.nova.petphrasex.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * 自帶的語系解析。
 * <p>
 * 本模組刻意不依賴 Fabric API，而模組資源包（assets/&lt;id&gt;/lang/*.json）正是由 Fabric API 的
 * fabric-resource-loader-v0 注入的；缺少它時 {@code I18n.get()} 查不到模組的 key，畫面會顯示原始 key。
 * 因此這裡直接從模組 jar 讀取語系檔並自行查找，找不到的 key 再退回原版翻譯（例如 options.on）。
 */
public final class PetPhraseLang {
    private static final String MOD_ID = "petphrasex";
    private static final String FALLBACK_LANG = "en_us";
    private static final String LANG_DIR = "assets/" + MOD_ID + "/lang/";
    private static final Type TABLE_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final Gson GSON = new Gson();

    private static Map<String, String> table = Map.of();
    private static String loadedFor = "";

    private PetPhraseLang() {
    }

    /** 取得翻譯後的文字；查不到時退回原版翻譯，最後才顯示 key 本身。 */
    public static String get(String key, Object... args) {
        String raw = table().get(key);
        if (raw == null) {
            raw = I18n.get(key);
        }
        if (raw == null || raw.equals(key)) {
            return key;
        }
        return args.length == 0 ? raw : String.format(raw, args);
    }

    /** 取得翻譯後的文字元件。 */
    public static Component text(String key, Object... args) {
        return Component.literal(get(key, args));
    }

    private static Map<String, String> table() {
        String code = currentLanguage();
        if (!code.equals(loadedFor)) {
            table = load(code);
            loadedFor = code;
        }
        return table;
    }

    private static String currentLanguage() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.getLanguageManager() != null) {
                String selected = minecraft.getLanguageManager().getSelected();
                if (selected != null && !selected.isBlank()) return selected;
            }
        } catch (Throwable ignored) {
            // 語言管理員尚未就緒時退回英文
        }
        return FALLBACK_LANG;
    }

    private static Map<String, String> load(String code) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (container.isEmpty()) return Map.of();

        for (String candidate : new String[]{code, FALLBACK_LANG}) {
            Optional<Path> path = container.get().findPath(LANG_DIR + candidate + ".json");
            if (path.isEmpty()) continue;
            try {
                Map<String, String> parsed = GSON.fromJson(Files.readString(path.get(), StandardCharsets.UTF_8), TABLE_TYPE);
                if (parsed != null && !parsed.isEmpty()) return parsed;
            } catch (Exception e) {
                System.err.println("[PetPhraseX] 無法讀取語系檔 " + candidate + ": " + e);
            }
        }
        return Map.of();
    }
}

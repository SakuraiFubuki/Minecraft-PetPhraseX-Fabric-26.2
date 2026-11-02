package committee.nova.petphrasex.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PetPhraseConfigX {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("petphrasex.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile PetPhraseConfigX INSTANCE;

    // 上一次成功读取到的档案状态，用于判断设定档是否被外部修改
    private static volatile long lastModified = Long.MIN_VALUE;
    private static volatile long lastSize = -1L;

    // 進階配置項
    public String ignoreMark = "#";
    public boolean removeIgnoreMark = true;
    public String prefix = "";
    public String suffix = " nya~; 喵~";
    public String sentencePrefix = "";
    public String sentenceSuffix = "";
    public boolean enableClientPhrase = true;
    // 伺服器端口癖预设关闭：专用伺服器需明确改为 true 才会对所有玩家套用
    public boolean enableServerPhrase = false;
    public ServerConditionRule[] serverConditionRules = new ServerConditionRule[]{
            ServerConditionRule.disabled("raining", "", " wet nya~"),
            ServerConditionRule.disabled("night", "", " good night nya~"),
            ServerConditionRule.disabled("low_health", "", " help nya~")
    };

    public static class ServerConditionRule {
        public boolean enabled = false;
        public String condition = "always";
        public String prefix = "";
        public String suffix = "";
        public String sentencePrefix = "";
        public String sentenceSuffix = "";

        public ServerConditionRule() {
        }

        private static ServerConditionRule disabled(String condition, String prefix, String suffix) {
            ServerConditionRule rule = new ServerConditionRule();
            rule.condition = condition;
            rule.prefix = prefix;
            rule.suffix = suffix;
            return rule;
        }
    }

    // 获取实例
    public static PetPhraseConfigX get() {
        PetPhraseConfigX instance = INSTANCE;
        if (instance == null) {
            synchronized (PetPhraseConfigX.class) {
                if (INSTANCE == null) {
                    load();
                }
                instance = INSTANCE;
            }
        }
        return instance;
    }

    // 加载配置
    public static synchronized void load() {
        if (!Files.exists(CONFIG_PATH)) {
            INSTANCE = new PetPhraseConfigX();
            save();
            return;
        }

        try {
            PetPhraseConfigX parsed = GSON.fromJson(Files.readString(CONFIG_PATH), PetPhraseConfigX.class);
            // 空档案或内容为 null 时 fromJson 回传 null，此处退回预设值，避免后续 NPE
            INSTANCE = parsed != null ? parsed : new PetPhraseConfigX();
        } catch (Exception e) {
            // IOException（读取失败）与 JsonSyntaxException（格式错误）都走这里：
            // 保留使用者原档不覆盖，改用预设值继续运作
            System.err.println("[PetPhraseX] 无法读取设定档 " + CONFIG_PATH + "，暂用预设值（原档未覆盖）: " + e);
            INSTANCE = new PetPhraseConfigX();
        }
        rememberFileState();
    }

    // 设定档被外部修改时重新加载（无需重启）；未修改时几乎零开销
    public static void reloadIfChanged() {
        if (!Files.exists(CONFIG_PATH)) return;
        try {
            long modified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            long size = Files.size(CONFIG_PATH);
            if (modified == lastModified && size == lastSize) return;
            load();
        } catch (IOException ignored) {
            // 档案正在被写入时可能短暂读不到，下次再试
        }
    }

    // 保存配置
    public static synchronized void save() {
        PetPhraseConfigX instance = INSTANCE;
        if (instance == null) return;
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
            rememberFileState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rememberFileState() {
        try {
            lastModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            lastSize = Files.size(CONFIG_PATH);
        } catch (IOException ignored) {
            lastModified = Long.MIN_VALUE;
            lastSize = -1L;
        }
    }
}

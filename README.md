# PetPhraseX

A lightweight client-side mod that automatically adds customizable "pet phrases" (like "nya~") to your chat messages. 
一款轻量级的用户端模组，能为您发出的聊天讯息自动加上自定义的「口癖」（例如 " nya~"）。

[![GitHub](https://img.shields.io/badge/GitHub-Minecraft--PetPhraseX--Fabric--26.2-181717?logo=github)](https://github.com/SakuraiFubuki/Minecraft-PetPhraseX-Fabric-26.2)

*[<img src="https://www.mcmod.cn/static/public/images/logo.png?v=4" height="16" alt="MCMod Logo"> 最大的MInecraft中文MOD百科](https://www.mcmod.cn/class/16747.html)*

## ✨ 最新功能 / Features

### 基础功能

以下功能属于 PetPhraseX 的日常使用能力，一般用户端即可使用，不强依赖 `Fabric API`：

* **🎲 随机口癖**
  同一个栏位可用分号（`;`）分隔多个候选内容，送出讯息时会随机抽取一个使用；若要输入真正的分号，可用 `;;` 转义。
* **✂️ 整句与短句双层加工**
  可分别设定整段讯息的 `prefix` / `suffix`，也可再针对短句设定 `sentencePrefix` / `sentenceSuffix`。
* **🚫 忽略标记**
  预设为 `#`。当讯息以忽略标记开头时，该讯息不会套用任何口癖，送出时也会自动移除标记本身（由 `removeIgnoreMark` 控制是否移除）。
* **⚙️ 游戏内设定**
  支援直接在游戏内调整 `ignoreMark`、`prefix`、`suffix`、`sentencePrefix`、`sentenceSuffix` 五个栏位（Fabric 需搭配 ModMenu 打开设定画面）。

### 服务端额外功能(不保证可用性)

以下功能属于 Fabric 伺服器的额外能力。本分支（26.2）的实现情况如下：

* **🛡️ 伺服器条件式口癖处理 — 已实现**
  服务端透过 Mixin 介入聊天广播链路，在玩家送出讯息时套用服务端口癖逻辑，并支援依条件规则（天气、昼夜、血量、维度等）切换不同口癖。
* **👮 管理员强制指定单一玩家口癖（`/ppx force`）— 本分支未实现**
  本分支没有注册任何指令，也没有跨端同步，因此不支援由管理员强制指定单一口癖。
* **🔄 玩家切回自己的本地设定 — 本分支未实现**
  需要客户端/服务端同步与设定画面开关，本分支未包含。

> ⚠️ 服务端口癖**预设关闭**（`enableServerPhrase = false`），需要管理員手动改为 `true` 才会对玩家套用。
> 开启前请注意：若玩家端也安装了本模组，客户端与服务端会各加一次口癖，导致重复（服务端无法得知对方是否装模组）。
> 若要「只用客户端口癖」，保持此项为 `false` 即可。

## 📦 目前支援的版本 / Supported Versions

* **Fabric**: `26.2`（Java `25`，Fabric Loader `>= 0.19.3`）
* **NeoForge**: 本分支不提供

## ⚙️ 设定档 / Config

设定档位置：

* 客户端：`.minecraft/config/petphrasex.json`
* 服务端：`<伺服器目录>/config/petphrasex.json`

修改后**无需重启**：下一次发送/接收聊天时会自动侦测档案变更并热重载。若设定档格式错误或内容为空，模组会改用预设值继续运作，且**不会覆盖**你原来的档案，方便修正后再次自动生效。

| 栏位 | 型别 | 预设值 | 说明 |
| --- | --- | --- | --- |
| `ignoreMark` | string | `#` | 忽略标记，可用 `;` 分隔多个候选，`;;` 转义 |
| `removeIgnoreMark` | boolean | `true` | 命中忽略标记时是否把标记从讯息中移除 |
| `prefix` | string | `""` | 整段讯息前缀 |
| `suffix` | string | ` nya~; 喵~` | 整段讯息后缀（两个候选随机取一） |
| `sentencePrefix` | string | `""` | 每个短句前缀 |
| `sentenceSuffix` | string | `""` | 每个短句后缀 |
| `enableClientPhrase` | boolean | `true` | 是否启用客户端口癖（单人存档时由服务端逻辑处理） |
| `enableServerPhrase` | boolean | `false` | 是否启用服务端口癖处理（服务端需手动改为 `true`） |
| `serverConditionRules` | array | 3 条（皆 `enabled: false`） | 服务端条件规则，见下 |

`serverConditionRules` 每一项的栏位：

```json
{
  "enabled": false,
  "condition": "raining",
  "prefix": "",
  "suffix": " wet nya~",
  "sentencePrefix": "",
  "sentenceSuffix": ""
}
```

可用 `condition`：`always`、`raining`（`rain`）、`thundering`（`thunder`）、`day`、`night`、`low_health`（血量 ≤ 6）、`underwater`、`in_water`、`on_fire`（`burning`）、`sneaking`（`crouching`）、`sprinting`、`in_overworld`（`overworld`）、`in_nether`（`nether`）、`in_end`（`end`）。

规则依阵列顺序匹配，命中第一条启用的规则即套用该条口癖；全部未命中时套用全局 `prefix` / `suffix`。

## 🕹️ 指令用法 / Commands

本分支**未注册任何指令**（`/ppx` 与 `/petphrasex` 尚未实现）。服务端口癖目前只能透过编辑 `config/petphrasex.json` 后重启伺服器调整。

## 🧩 Fabric API 说明

本分支**不依赖 `Fabric API`**，仅依赖 Fabric Loader 与 Minecraft 本体，模组可独立载入。

若后续要实作 `/ppx` 强制口癖指令与跨端同步，才需要引入 `Fabric API`（`fabric-command-api-v2`、`fabric-networking-api-v1`、`fabric-lifecycle-events-v1`）。

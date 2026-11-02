package committee.nova.petphrasex.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilTest {
    @Test
    void removesOnlyMatchedIgnoreMarkWhenMultipleMarksAreConfigured() {
        assertEquals("baritone", StringUtil.processMessage("!baritone", "#;!", true, "", "", "", ""));
    }

    @Test
    void prefersLongestMatchingIgnoreMark() {
        assertEquals("baritone", StringUtil.processMessage("##baritone", "#;##", true, "", "", "", ""));
    }

    @Test
    void keepsMatchedIgnoreMarkWhenStripIsDisabled() {
        assertEquals("!baritone", StringUtil.processMessage("!baritone", "#;!", false, "", "", "", ""));
    }

    // 以下为「口癖真的会加上」的核心验证，对应客户端/服务端共用的处理流程
    @Test
    void appendsSuffixAfterLastTextCharacter() {
        assertEquals("hello nya~", StringUtil.processMessage("hello", "#", true, "", " nya~", "", ""));
    }

    @Test
    void insertsSuffixBeforeTrailingPunctuation() {
        assertEquals("hello nya~!", StringUtil.processMessage("hello!", "#", true, "", " nya~", "", ""));
    }

    @Test
    void prependsPrefix() {
        assertEquals("[admin] hello", StringUtil.processMessage("hello", "#", true, "[admin] ", "", "", ""));
    }

    @Test
    void wrapsEachSentenceWhenSentenceAffixesSet() {
        assertEquals("a hi b a there b", StringUtil.processMessage("hi there", "#", true, "", "", "a ", " b"));
    }

    @Test
    void ignoreMarkDisablesPhraseEntirely() {
        assertEquals("hello", StringUtil.processMessage("#hello", "#", true, "", " nya~", "", ""));
    }

    @Test
    void emptyMessageStaysEmpty() {
        assertEquals("", StringUtil.processMessage("", "#", true, "", " nya~", "", ""));
    }

    @Test
    void randomSuffixAlwaysPicksOneOfTheCandidates() {
        for (int i = 0; i < 50; i++) {
            String out = StringUtil.processMessage("hi", "#", true, "", " a; b", "", "");
            assertTrue("hi a".equals(out) || "hi b".equals(out), "unexpected output: " + out);
        }
    }

    @Test
    void escapedSemicolonIsKeptLiteral() {
        assertEquals("hi;there", StringUtil.processMessage("hi", "#", true, "", ";;there", "", ""));
    }
}

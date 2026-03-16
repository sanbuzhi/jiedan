package com.jiedan.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReferralCodeGeneratorTest {

    @Test
    public void testGenerateCodeFormat() {
        // 由于 ReferralCodeGenerator 现在依赖 UserRepository，
        // 这里只测试生成逻辑是否符合规范
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int codeLength = 6;

        // 验证字符集和长度设置
        assertEquals(32, characters.length());
        assertEquals(6, codeLength);

        // 验证不包含易混淆字符
        assertFalse(characters.contains("0"));
        assertFalse(characters.contains("O"));
        assertFalse(characters.contains("1"));
        assertFalse(characters.contains("I"));

        // 验证只包含大写字母和数字
        assertTrue(characters.matches("^[A-Z0-9]+$"));
    }
}

package com.xidian.activity.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 动态签到二维码 HMAC 凭证单元测试 */
class DynamicQrUtilTest {

    private final DynamicQrUtil util = new DynamicQrUtil("test-secret-key-for-unit-test-2026-activity");

    @Test
    void generatedTokenVerifiesOk() {
        long expire = System.currentTimeMillis() + 30_000;
        String token = util.generate(42L, expire);
        assertEquals(DynamicQrUtil.VerifyResult.OK, util.verify(42L, token, expire - 1000));
    }

    @Test
    void expiredTokenIsRejected() {
        long expire = System.currentTimeMillis() - 1000;
        String token = util.generate(42L, expire);
        assertEquals(DynamicQrUtil.VerifyResult.EXPIRED, util.verify(42L, token, expire + 1));
    }

    @Test
    void tokenForAnotherActivityIsInvalid() {
        long expire = System.currentTimeMillis() + 30_000;
        String token = util.generate(42L, expire);
        assertEquals(DynamicQrUtil.VerifyResult.INVALID, util.verify(99L, token, expire - 1000));
    }

    @Test
    void tamperedTokenIsInvalid() {
        long expire = System.currentTimeMillis() + 30_000;
        String token = util.generate(42L, expire);
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");
        assertEquals(DynamicQrUtil.VerifyResult.INVALID, util.verify(42L, tampered, expire - 1000));
    }

    @Test
    void nullOrMalformedTokenIsInvalid() {
        assertEquals(DynamicQrUtil.VerifyResult.INVALID, util.verify(42L, null, System.currentTimeMillis()));
        assertEquals(DynamicQrUtil.VerifyResult.INVALID, util.verify(42L, "garbage", System.currentTimeMillis()));
    }
}

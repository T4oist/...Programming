package com.xidian.activity.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 动态签到二维码凭证工具（无状态 HMAC 方案）：
 * 凭证格式 = activityId:expireAtMillis:HMAC(activityId:expireAtMillis, secret)
 * 服务端不存储凭证，验证时重算签名即可；有效期由生成时的 expireAt 决定
 */
@Component
public class DynamicQrUtil {

    /** 凭证校验结果 */
    public enum VerifyResult { OK, INVALID, EXPIRED }

    private final byte[] secretBytes;

    public DynamicQrUtil(@Value("${activity.checkin.qr-secret}") String secret) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    /** 生成动态凭证 */
    public String generate(long activityId, long expireAtMillis) {
        String payload = activityId + ":" + expireAtMillis;
        return payload + ":" + hmac(payload);
    }

    /**
     * 校验凭证：签名正确、活动号匹配、未过期
     */
    public VerifyResult verify(long activityId, String token, long nowMillis) {
        if (token == null || token.isBlank()) {
            return VerifyResult.INVALID;
        }
        String[] parts = token.split(":");
        if (parts.length != 3) {
            return VerifyResult.INVALID;
        }
        long tokenActivityId;
        long expireAt;
        try {
            tokenActivityId = Long.parseLong(parts[0]);
            expireAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return VerifyResult.INVALID;
        }
        // 活动不匹配或签名错误 → 入口无效（恒定时间比较，避免时序侧信道）
        byte[] expected = hmac(parts[0] + ":" + parts[1]).getBytes(StandardCharsets.UTF_8);
        byte[] actual = parts[2].getBytes(StandardCharsets.UTF_8);
        if (tokenActivityId != activityId || !MessageDigest.isEqual(expected, actual)) {
            return VerifyResult.INVALID;
        }
        // 已过期 → 提示重新扫码
        return nowMillis <= expireAt ? VerifyResult.OK : VerifyResult.EXPIRED;
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }
}

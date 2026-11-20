package com.xidian.activity.service;

import com.xidian.activity.common.BusinessException;
import com.xidian.activity.common.ErrorCode;
import com.xidian.activity.dto.CheckinRequest;
import com.xidian.activity.dto.CheckinTokenResponse;
import com.xidian.activity.entity.Activity;
import com.xidian.activity.entity.CheckIn;
import com.xidian.activity.entity.Enrollment;
import com.xidian.activity.mapper.CheckInMapper;
import com.xidian.activity.mapper.EnrollmentMapper;
import com.xidian.activity.util.DynamicQrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 签到服务：
 * - 动态二维码凭证签发（管理端签到码展示页每 30 秒拉取）
 * - 扫码签到核验（活动有效 → 时间窗 → 已报名 → 动态凭证 → 唯一签到）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInMapper checkInMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final ActivityService activityService;
    private final DynamicQrUtil dynamicQrUtil;

    @Value("${activity.checkin.qr-ttl-seconds}")
    private int qrTtlSeconds;

    @Value("${activity.checkin.open-before-minutes}")
    private int openBeforeMinutes;

    @Value("${activity.checkin.public-base-url}")
    private String publicBaseUrl;

    /**
     * 签发动态签到凭证：活动须已发布、未删除
     */
    public CheckinTokenResponse currentToken(long activityId) {
        Activity activity = activityService.getExisting(activityId);
        long expireAt = System.currentTimeMillis() + qrTtlSeconds * 1000L;
        String token = dynamicQrUtil.generate(activityId, expireAt);
        String checkinUrl = publicBaseUrl + "/checkin/" + activityId
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        return CheckinTokenResponse.builder()
                .activityId(activityId)
                .token(token)
                .expireAt(expireAt)
                .refreshSeconds(qrTtlSeconds)
                .checkinUrl(checkinUrl)
                .title(activity.getTitle())
                .build();
    }

    /**
     * 扫码签到：所有签到必须携带由活动二维码签发的动态凭证，禁止静态旁路
     * 校验顺序：活动有效 → 动态凭证 → 签到时间窗 → 报名记录 → 唯一签到记录
     * 凭证先于时间窗校验，可避免用过期/伪造凭证探测签到窗口状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkin(CheckinRequest req) {
        long activityId = req.getActivityId();
        String phone = req.getPhone() == null ? "" : req.getPhone().trim();

        // ---- 1. 活动有效 ----
        Activity activity = activityService.getExisting(activityId);
        if (!Boolean.TRUE.equals(activity.getPublished())) {
            throw new BusinessException(ErrorCode.SIGNUP_NOT_AVAILABLE);
        }

        // ---- 2. 动态凭证核验（强制，无凭证一律拒绝） ----
        DynamicQrUtil.VerifyResult vr =
                dynamicQrUtil.verify(activityId, req.getToken(), System.currentTimeMillis());
        switch (vr) {
            case INVALID -> throw new BusinessException(ErrorCode.CHECKIN_TOKEN_INVALID);
            case EXPIRED -> throw new BusinessException(ErrorCode.CHECKIN_TOKEN_EXPIRED);
            case OK -> { /* 通过 */ }
        }

        // ---- 3. 签到时间窗：活动开始前 N 分钟 至 活动结束 ----
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime().minusMinutes(openBeforeMinutes))
                || now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_OPEN);
        }

        // ---- 4. 报名记录核验 ----
        Enrollment enrollment = enrollmentMapper.selectByActivityAndPhone(activityId, phone);
        if (enrollment == null) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_ENROLLED);
        }

        // ---- 5. 写入唯一签到记录 ----
        Map<String, Object> result = new HashMap<>();
        try {
            CheckIn checkIn = new CheckIn();
            checkIn.setEnrollmentId(enrollment.getId());
            checkIn.setMethod("dynamic");
            checkInMapper.insert(checkIn);
            result.put("checkedIn", true);
            result.put("duplicated", false);
            result.put("method", checkIn.getMethod());
            result.put("message", "签到成功");
            log.info("签到成功: activityId={}, enrollmentId={}", activityId, enrollment.getId());
        } catch (DuplicateKeyException e) {
            // enrollment_id 唯一约束命中 → 已签到，不重复计数
            result.put("checkedIn", true);
            result.put("duplicated", true);
            result.put("message", "已签到，请勿重复签到");
        }
        return result;
    }
}

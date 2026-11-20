package com.xidian.activity.service;

import com.xidian.activity.common.BusinessException;
import com.xidian.activity.common.ErrorCode;
import com.xidian.activity.dto.EnrollmentRequest;
import com.xidian.activity.entity.Activity;
import com.xidian.activity.entity.College;
import com.xidian.activity.entity.Enrollment;
import com.xidian.activity.mapper.ActivityMapper;
import com.xidian.activity.mapper.CollegeMapper;
import com.xidian.activity.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 报名服务：所有校验在服务端事务内完成，
 * 通过「悲观锁(FOR UPDATE) + 数据库唯一约束」双重保证并发下的名额与防重复
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final ActivityMapper activityMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final CollegeMapper collegeMapper;

    /**
     * 参与人报名：
     * 1. 信息格式校验（姓名/手机号/学院）
     * 2. 事务内锁定活动行，核验活动有效性与名额
     * 3. (activity_id, phone) 唯一约束兜底防重复报名
     */
    @Transactional(rollbackFor = Exception.class)
    public void signup(long activityId, EnrollmentRequest req) {
        // ---- 1. 输入格式校验（不读库，不建立 read view） ----
        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.length() < 2 || name.length() > 20) {
            throw new BusinessException(ErrorCode.SIGNUP_NAME_INVALID);
        }
        String phone = req.getPhone() == null ? "" : req.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.SIGNUP_PHONE_INVALID);
        }

        // ---- 2. 先把活动行锁住：SELECT ... FOR UPDATE 为当前读，不建立 read view ----
        // 同一活动的并发报名在此串行化；必须先于任何快照读，否则 RR 下计数会复用旧快照导致超卖
        Activity activity = activityMapper.selectByIdForUpdate(activityId);
        if (activity == null || Boolean.TRUE.equals(activity.getDeleted()) || !Boolean.TRUE.equals(activity.getPublished())) {
            throw new BusinessException(ErrorCode.SIGNUP_NOT_AVAILABLE);
        }
        LocalDateTime now = LocalDateTime.now(); // 以服务器时间为准，不信任浏览器时间
        if (!now.isBefore(activity.getSignupDeadline()) || !now.isBefore(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.SIGNUP_ENDED);
        }

        // ---- 3. 学院校验 + 当前读计数（都在拿到行锁之后，read view 反映最新提交） ----
        College college = collegeMapper.selectById(req.getCollegeId());
        if (college == null || !Boolean.TRUE.equals(college.getEnabled())) {
            throw new BusinessException(ErrorCode.SIGNUP_COLLEGE_INVALID);
        }
        long enrolled = enrollmentMapper.countForUpdate(activityId); // SELECT COUNT(*) ... FOR SHARE 当前读
        if (enrolled >= activity.getCapacity()) {
            throw new BusinessException(ErrorCode.SIGNUP_FULL); // 满额不改变生命周期状态
        }

        // ---- 4. 写入报名（唯一约束兜底防重复） ----
        Enrollment enrollment = new Enrollment();
        enrollment.setActivityId(activityId);
        enrollment.setName(name);
        enrollment.setPhone(phone);
        enrollment.setCollegeId(req.getCollegeId());
        try {
            enrollmentMapper.insert(enrollment);
        } catch (DuplicateKeyException e) {
            // (activity_id, phone) 命中唯一约束 → 重复报名
            throw new BusinessException(ErrorCode.SIGNUP_DUPLICATE);
        }
        log.info("报名成功: activityId={}, phone={}", activityId, maskPhone(phone));
    }

    /** 日志脱敏 */
    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

package com.xidian.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xidian.activity.common.ActivityStatus;
import com.xidian.activity.common.BusinessException;
import com.xidian.activity.common.ErrorCode;
import com.xidian.activity.dto.ActivityDetail;
import com.xidian.activity.dto.ActivityListItem;
import com.xidian.activity.dto.ActivityListQuery;
import com.xidian.activity.dto.ActivityRequest;
import com.xidian.activity.dto.EnrollmentListItem;
import com.xidian.activity.entity.Activity;
import com.xidian.activity.mapper.ActivityMapper;
import com.xidian.activity.mapper.EnrollmentMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动生命周期服务：创建、编辑、删除（逻辑删除）、发布、查询、统计
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final OperationLogService operationLogService;

    @Value("${activity.checkin.public-base-url}")
    private String publicBaseUrl;

    // ------------------------------------------------------------------
    // 创建
    // ------------------------------------------------------------------
    public Long create(ActivityRequest req, long adminId, HttpServletRequest request) {
        req.validateTime();
        Activity activity = new Activity();
        copyFields(req, activity);
        activity.setPublished(false); // 新建活动状态初始化为“未发布”
        activity.setCreatedBy(adminId);
        activityMapper.insert(activity);
        operationLogService.record(adminId, activity.getId(), "CREATE",
                "创建活动「" + activity.getTitle() + "」", request);
        return activity.getId();
    }

    // ------------------------------------------------------------------
    // 编辑：任意状态可编辑；保留活动标识与报名、签到记录
    // ------------------------------------------------------------------
    public void update(long id, ActivityRequest req, long adminId, HttpServletRequest request) {
        Activity activity = getExisting(id);
        req.validateTime();
        long enrolled = countEnrolled(id);
        // 人数上限不得小于已报名人数
        if (req.getCapacity() < enrolled) {
            throw new BusinessException(ErrorCode.ACTIVITY_CAPACITY_LESS_THAN_ENROLLED);
        }
        copyFields(req, activity);
        int rows = activityMapper.updateById(activity); // @Version 乐观锁
        if (rows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作冲突，请刷新后重试");
        }
        operationLogService.record(adminId, id, "UPDATE",
                "编辑活动「" + activity.getTitle() + "」", request);
    }

    // ------------------------------------------------------------------
    // 删除：逻辑删除，公开入口立即失效，历史数据保留
    // ------------------------------------------------------------------
    public void delete(long id, long adminId, HttpServletRequest request) {
        Activity activity = getExisting(id);
        activityMapper.deleteLogically(id);
        operationLogService.record(adminId, id, "DELETE",
                "删除活动「" + activity.getTitle() + "」（逻辑删除）", request);
    }

    // ------------------------------------------------------------------
    // 发布 / 重新生成宣传页
    // ------------------------------------------------------------------
    public ActivityDetail publish(long id, long adminId, HttpServletRequest request) {
        Activity activity = getExisting(id);
        LocalDateTime now = LocalDateTime.now();
        // 首次发布须满足报名截止时间和活动结束时间均未到达
        if (now.isAfter(activity.getSignupDeadline()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.ACTIVITY_PUBLISH_NOT_ALLOWED);
        }
        boolean firstPublish = activity.getFirstPublishedAt() == null;
        activity.setPublished(true);
        if (firstPublish) {
            activity.setFirstPublishedAt(now); // 重新生成宣传页不重置首次发布时间
        }
        int rows = activityMapper.updateById(activity); // @Version 乐观锁
        if (rows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作冲突，请刷新后重试");
        }
        operationLogService.record(adminId, id, "PUBLISH",
                (firstPublish ? "首次发布" : "重新生成宣传页") + "：「" + activity.getTitle() + "」", request);
        return detail(id);
    }

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------
    public IPage<ActivityListItem> list(ActivityListQuery query) {
        Page<ActivityListItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        return activityMapper.selectPageWithStatus(page, query);
    }

    /** 管理端详情 */
    public ActivityDetail detail(long id) {
        Activity activity = getExisting(id);
        return toDetail(activity, true);
    }

    /** 公开宣传页详情：仅已发布且未删除的活动可见，不泄露后台信息 */
    public ActivityDetail publicDetail(long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getDeleted() || !activity.getPublished()) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND, "活动不存在或暂不可访问");
        }
        return toDetail(activity, false);
    }

    /** 报名名单（含签到状态） */
    public List<EnrollmentListItem> enrollments(long id) {
        getExisting(id);
        return enrollmentMapper.selectListWithCheckin(id);
    }

    /** 报名 / 签到统计 */
    public Map<String, Object> stats(long id) {
        Activity activity = getExisting(id);
        long enrolled = countEnrolled(id);
        long checked = countChecked(id);
        Map<String, Object> stats = new HashMap<>();
        stats.put("capacity", activity.getCapacity());
        stats.put("enrolledCount", enrolled);
        stats.put("checkedCount", checked);
        // 无人报名时签到率为 0%
        stats.put("checkinRate", enrolled == 0 ? "0%"
                : String.format("%.1f%%", checked * 100.0 / enrolled));
        return stats;
    }

    public long countEnrolled(long activityId) {
        return enrollmentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xidian.activity.entity.Enrollment>()
                        .eq("activity_id", activityId)).longValue();
    }

    public long countChecked(long activityId) {
        return enrollmentMapper.countChecked(activityId);
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------
    public Activity getExisting(long id) {
        Activity activity = activityMapper.selectById(id); // @TableLogic 自动过滤已删除
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    private void copyFields(ActivityRequest req, Activity activity) {
        activity.setTitle(req.getTitle().trim());
        activity.setType(req.getType());
        activity.setIntro(req.getIntro());
        activity.setLocation(req.getLocation().trim());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setSignupDeadline(req.getSignupDeadline());
        activity.setCapacity(req.getCapacity());
    }

    private ActivityDetail toDetail(Activity activity, boolean forAdmin) {
        ActivityDetail d = new ActivityDetail();
        d.setId(activity.getId());
        d.setTitle(activity.getTitle());
        d.setType(activity.getType());
        d.setIntro(activity.getIntro());
        d.setLocation(activity.getLocation());
        d.setStartTime(activity.getStartTime());
        d.setEndTime(activity.getEndTime());
        d.setSignupDeadline(activity.getSignupDeadline());
        d.setCapacity(activity.getCapacity());
        d.setPublished(activity.getPublished());
        d.setFirstPublishedAt(activity.getFirstPublishedAt());
        d.setCreatedAt(activity.getCreatedAt());
        d.setUpdatedAt(activity.getUpdatedAt());
        d.setEnrolledCount(countEnrolled(activity.getId()));
        ActivityStatus status = ActivityStatus.of(activity.getPublished(),
                activity.getSignupDeadline(), activity.getEndTime(), LocalDateTime.now());
        d.setStatus(status.name());
        d.setStatusLabel(status.getLabel());
        LocalDateTime now = LocalDateTime.now();
        d.setSignupAvailable(activity.getPublished()
                && now.isBefore(activity.getSignupDeadline())
                && now.isBefore(activity.getEndTime()));
        if (forAdmin) {
            // 报名地址保持稳定：/a/{id}，编辑、重新生成均不变
            d.setShareUrl(publicBaseUrl + "/a/" + activity.getId());
            d.setCheckinUrl(publicBaseUrl + "/checkin/" + activity.getId());
        }
        return d;
    }
}

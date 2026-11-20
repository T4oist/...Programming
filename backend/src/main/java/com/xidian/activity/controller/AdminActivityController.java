package com.xidian.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xidian.activity.common.Result;
import com.xidian.activity.config.JwtInterceptor;
import com.xidian.activity.dto.ActivityDetail;
import com.xidian.activity.dto.ActivityListItem;
import com.xidian.activity.dto.ActivityListQuery;
import com.xidian.activity.dto.ActivityRequest;
import com.xidian.activity.dto.CheckinTokenResponse;
import com.xidian.activity.dto.EnrollmentListItem;
import com.xidian.activity.service.ActivityService;
import com.xidian.activity.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端活动接口（需登录）：活动全生命周期管理
 */
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;
    private final CheckInService checkInService;

    private long adminId(HttpServletRequest request) {
        return (long) request.getAttribute(JwtInterceptor.ATTR_ADMIN_ID);
    }

    /** 活动列表：多条件筛选（类型/状态/开始时间范围/关键字） */
    @GetMapping
    public Result<IPage<ActivityListItem>> list(@Valid ActivityListQuery query) {
        return Result.ok(activityService.list(query));
    }

    /** 活动创建 */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ActivityRequest req,
                               HttpServletRequest request) {
        return Result.ok(activityService.create(req, adminId(request), request));
    }

    /** 活动详情 */
    @GetMapping("/{id}")
    public Result<ActivityDetail> detail(@PathVariable Long id) {
        return Result.ok(activityService.detail(id));
    }

    /** 活动编辑（任意状态；保留活动标识与报名、签到记录） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest req,
                               HttpServletRequest request) {
        activityService.update(id, req, adminId(request), request);
        return Result.ok();
    }

    /** 活动删除（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        activityService.delete(id, adminId(request), request);
        return Result.ok();
    }

    /** 活动发布 / 重新生成宣传页（自动生成宣传页与二维码，报名地址稳定） */
    @PostMapping("/{id}/publish")
    public Result<ActivityDetail> publish(@PathVariable Long id, HttpServletRequest request) {
        return Result.ok(activityService.publish(id, adminId(request), request));
    }

    /** 报名名单（含签到状态） */
    @GetMapping("/{id}/enrollments")
    public Result<List<EnrollmentListItem>> enrollments(@PathVariable Long id) {
        return Result.ok(activityService.enrollments(id));
    }

    /** 报名 / 签到统计 */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return Result.ok(activityService.stats(id));
    }

    /** 动态签到码凭证（签到码展示页每 30 秒拉取刷新） */
    @GetMapping("/{id}/checkin-token")
    public Result<CheckinTokenResponse> checkinToken(@PathVariable Long id) {
        return Result.ok(checkInService.currentToken(id));
    }
}

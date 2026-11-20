package com.xidian.activity.controller;

import com.xidian.activity.common.Result;
import com.xidian.activity.dto.ActivityDetail;
import com.xidian.activity.dto.CheckinRequest;
import com.xidian.activity.dto.EnrollmentRequest;
import com.xidian.activity.entity.College;
import com.xidian.activity.mapper.CollegeMapper;
import com.xidian.activity.service.CheckInService;
import com.xidian.activity.service.EnrollmentService;
import com.xidian.activity.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 公开接口（免登录）：学院列表、宣传页、报名、签到
 * 公开响应不包含报名名单等后台信息
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CollegeMapper collegeMapper;
    private final ActivityService activityService;
    private final EnrollmentService enrollmentService;
    private final CheckInService checkInService;

    /** 启用的学院列表（报名下拉选择） */
    @GetMapping("/colleges")
    public Result<List<College>> colleges() {
        return Result.ok(collegeMapper.selectEnabled());
    }

    /** 活动宣传页数据：仅已发布且未删除的活动可见 */
    @GetMapping("/activities/{id}")
    public Result<ActivityDetail> activity(@PathVariable Long id) {
        return Result.ok(activityService.publicDetail(id));
    }

    /** 报名：校验活动有效性、输入格式、重复报名、人数上限 */
    @PostMapping("/activities/{id}/signup")
    public Result<Void> signup(@PathVariable Long id, @Valid @RequestBody EnrollmentRequest req) {
        enrollmentService.signup(id, req);
        return Result.ok();
    }

    /** 扫码签到：校验活动、时间窗、报名记录与动态凭证 */
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@Valid @RequestBody CheckinRequest req) {
        return Result.ok(checkInService.checkin(req));
    }
}

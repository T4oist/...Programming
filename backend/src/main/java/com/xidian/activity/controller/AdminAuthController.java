package com.xidian.activity.controller;

import com.xidian.activity.common.Result;
import com.xidian.activity.config.JwtInterceptor;
import com.xidian.activity.dto.LoginRequest;
import com.xidian.activity.dto.LoginResponse;
import com.xidian.activity.dto.PasswordChangeRequest;
import com.xidian.activity.dto.ProfileUpdateRequest;
import com.xidian.activity.entity.Admin;
import com.xidian.activity.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员账号接口：登录 / 个人信息 / 修改密码
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    /** 管理员登录（免拦截） */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                       HttpServletRequest request) {
        return Result.ok(adminService.login(req, request));
    }

    /** 查看个人信息 */
    @GetMapping("/profile")
    public Result<Admin> profile(HttpServletRequest request) {
        long adminId = (long) request.getAttribute(JwtInterceptor.ATTR_ADMIN_ID);
        return Result.ok(adminService.getById(adminId));
    }

    /** 修改个人信息 */
    @PutMapping("/profile")
    public Result<Admin> updateProfile(@Valid @RequestBody ProfileUpdateRequest req,
                                       HttpServletRequest request) {
        long adminId = (long) request.getAttribute(JwtInterceptor.ATTR_ADMIN_ID);
        return Result.ok(adminService.updateProfile(adminId, req));
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req,
                                       HttpServletRequest request) {
        long adminId = (long) request.getAttribute(JwtInterceptor.ATTR_ADMIN_ID);
        adminService.changePassword(adminId, req, request);
        return Result.ok();
    }
}

package com.xidian.activity.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xidian.activity.common.BusinessException;
import com.xidian.activity.common.ErrorCode;
import com.xidian.activity.config.JwtUtil;
import com.xidian.activity.dto.LoginRequest;
import com.xidian.activity.dto.LoginResponse;
import com.xidian.activity.dto.PasswordChangeRequest;
import com.xidian.activity.dto.ProfileUpdateRequest;
import com.xidian.activity.entity.Admin;
import com.xidian.activity.mapper.AdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员账号服务：登录认证、个人信息维护、修改密码
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;
    private final OperationLogService operationLogService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest req, HttpServletRequest request) {
        Admin admin = adminMapper.selectOne(
                Wrappers.<Admin>lambdaQuery().eq(Admin::getUsername, req.getUsername()));
        // 密码统一走 BCrypt 校验；账号不存在与密码错误返回同一提示，避免账号枚举
        if (admin == null || !passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        operationLogService.record(admin.getId(), null, "LOGIN",
                "管理员 " + admin.getUsername() + " 登录系统", request);
        return LoginResponse.builder()
                .token(jwtUtil.issue(admin.getId()))
                .id(admin.getId())
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .phone(admin.getPhone())
                .build();
    }

    public Admin getById(long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号不存在");
        }
        return admin;
    }

    public Admin updateProfile(long adminId, ProfileUpdateRequest req) {
        Admin admin = getById(adminId);
        admin.setRealName(req.getRealName());
        admin.setPhone(req.getPhone());
        adminMapper.updateById(admin);
        return getById(adminId);
    }

    public void changePassword(long adminId, PasswordChangeRequest req, HttpServletRequest request) {
        Admin admin = getById(adminId);
        if (!passwordEncoder.matches(req.getOldPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_WRONG);
        }
        // 新密码仅以 BCrypt 哈希存储
        admin.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        adminMapper.updateById(admin);
        operationLogService.record(adminId, null, "PASSWORD",
                "管理员 " + admin.getUsername() + " 修改了密码", request);
    }
}

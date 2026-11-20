package com.xidian.activity.service;

import com.xidian.activity.entity.OperationLog;
import com.xidian.activity.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务：管理员关键操作留痕（创建/发布/编辑/删除/登录等）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public void record(Long adminId, Long activityId, String action, String detail,
                       HttpServletRequest request) {
        try {
            OperationLog op = new OperationLog();
            op.setAdminId(adminId);
            op.setActivityId(activityId);
            op.setAction(action);
            op.setDetail(detail);
            op.setIp(request != null ? request.getRemoteAddr() : null);
            operationLogMapper.insert(op);
        } catch (Exception e) {
            // 日志失败不影响主流程
            log.warn("操作日志写入失败: {}", e.getMessage());
        }
    }
}

-- =============================================================
-- 高校学生活动全流程管理系统 建库建表脚本 (MySQL 8.0+)
-- 设计说明：所有表均满足第三范式（3NF）
--   - 非主属性完全函数依赖于主键，无部分依赖与传递依赖；
--   - 学院名称、活动类型等可枚举信息独立成表，报名表仅存外键；
--   - 报名人数、签到率等统计值不落库，由业务统计实时计算。
-- =============================================================

DROP DATABASE IF EXISTS activity_system;
CREATE DATABASE activity_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE activity_system;

-- -------------------------------------------------------------
-- 1. 管理员表 admin：系统唯一需要登录的正式用户，账号由初始化创建
-- -------------------------------------------------------------
CREATE TABLE admin (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员编号',
    username      VARCHAR(50)  NOT NULL                COMMENT '登录账号',
    password_hash VARCHAR(100) NOT NULL                COMMENT '密码哈希(BCrypt)',
    real_name     VARCHAR(50)  NOT NULL                COMMENT '姓名',
    phone         VARCHAR(11)  NULL                    COMMENT '手机号',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE = InnoDB COMMENT = '管理员表';

-- -------------------------------------------------------------
-- 2. 学院字典表 college：报名时校验“学院属于启用列表”
-- -------------------------------------------------------------
CREATE TABLE college (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '学院编号',
    name       VARCHAR(50) NOT NULL                COMMENT '学院名称',
    enabled    TINYINT(1)  NOT NULL DEFAULT 1      COMMENT '是否启用：1启用 0停用',
    sort_order INT         NOT NULL DEFAULT 0      COMMENT '显示顺序',
    PRIMARY KEY (id),
    UNIQUE KEY uk_college_name (name)
) ENGINE = InnoDB COMMENT = '学院字典表';

-- -------------------------------------------------------------
-- 3. 活动表 activity：活动的生命周期数据
--    展示状态由系统按时间计算，不在库中冗余存储：
--    未发布(published=0) / 报名中(已发布且未到截止) /
--    已截止(过报名截止时间但未结束) / 已结束(过结束时间)
-- -------------------------------------------------------------
CREATE TABLE activity (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动编号',
    title              VARCHAR(100) NOT NULL                COMMENT '活动名称',
    type               VARCHAR(20)  NOT NULL                COMMENT '活动类型：讲座/宣讲会/就业指导/其他',
    intro              TEXT         NULL                    COMMENT '活动简介',
    location           VARCHAR(100) NOT NULL                COMMENT '活动地点',
    start_time         DATETIME     NOT NULL                COMMENT '活动开始时间',
    end_time           DATETIME     NOT NULL                COMMENT '活动结束时间',
    signup_deadline    DATETIME     NOT NULL                COMMENT '报名截止时间',
    capacity           INT          NOT NULL                COMMENT '人数上限',
    published          TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '是否已发布：0未发布 1已发布',
    first_published_at DATETIME     NULL                    COMMENT '首次发布时间(重新生成不重置)',
    deleted            TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除：0正常 1已删除',
    deleted_at         DATETIME     NULL                    COMMENT '删除时间',
    created_by         BIGINT       NOT NULL                COMMENT '创建人(管理员编号)',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version            INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    CONSTRAINT fk_activity_admin FOREIGN KEY (created_by) REFERENCES admin (id),
    CONSTRAINT chk_activity_time CHECK (start_time < end_time
                                        AND signup_deadline <= start_time),
    CONSTRAINT chk_activity_capacity CHECK (capacity > 0),
    -- 列表按逻辑删除过滤 + 创建时间倒序分页
    KEY idx_activity_del_created (deleted, created_at DESC),
    -- 状态实时计算依赖发布时间与报名截止时间
    KEY idx_activity_pub_deadline (published, signup_deadline),
    KEY idx_activity_type (type)
) ENGINE = InnoDB COMMENT = '活动表';

-- -------------------------------------------------------------
-- 4. 报名表 enrollment：一条记录代表一名参与人对一个活动的报名
--    同一活动下「姓名相同」或「手机号相同」均视为重复报名，
--    由 (activity_id, name) 与 (activity_id, phone) 两个唯一约束兜底
-- -------------------------------------------------------------
CREATE TABLE enrollment (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '报名编号',
    activity_id BIGINT      NOT NULL                COMMENT '活动编号',
    name        VARCHAR(20) NOT NULL                COMMENT '参与人姓名(2~20字符)',
    phone       VARCHAR(11) NOT NULL                COMMENT '报名手机号(大陆11位,存储前已标准化)',
    college_id  BIGINT      NOT NULL                COMMENT '学院编号',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_enrollment_activity_phone (activity_id, phone),
    UNIQUE KEY uk_enrollment_activity_name  (activity_id, name),
    CONSTRAINT fk_enrollment_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT fk_enrollment_college  FOREIGN KEY (college_id)  REFERENCES college (id)
    -- 两个唯一索引分别覆盖按活动+手机号、按活动+姓名查询与防重
) ENGINE = InnoDB COMMENT = '报名表';

-- -------------------------------------------------------------
-- 5. 签到表 check_in：一条报名最多一条有效签到（enrollment_id 唯一）
--    method 记录静态/动态方式；动态二维码凭证由服务端 HMAC
--    签名即时校验，不在库中存储
-- -------------------------------------------------------------
CREATE TABLE check_in (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '签到编号',
    enrollment_id BIGINT      NOT NULL                COMMENT '报名编号',
    method        VARCHAR(10) NOT NULL DEFAULT 'static' COMMENT '签到方式：static静态 dynamic动态',
    checked_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_checkin_enrollment (enrollment_id),
    CONSTRAINT fk_checkin_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollment (id)
) ENGINE = InnoDB COMMENT = '签到表';

-- -------------------------------------------------------------
-- 6. 操作日志表 operation_log：记录管理员的创建/发布/编辑/删除等
--    关键操作，支持过程追溯
-- -------------------------------------------------------------
CREATE TABLE operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志编号',
    admin_id    BIGINT       NOT NULL                COMMENT '操作人(管理员编号)',
    activity_id BIGINT       NULL                    COMMENT '相关活动编号(可空)',
    action      VARCHAR(30)  NOT NULL                COMMENT '操作类型：LOGIN/CREATE/PUBLISH/UPDATE/DELETE...',
    detail      VARCHAR(500) NULL                    COMMENT '操作详情',
    ip          VARCHAR(45)  NULL                    COMMENT '操作来源IP',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_oplog_admin   FOREIGN KEY (admin_id)    REFERENCES admin (id),
    CONSTRAINT fk_oplog_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    KEY idx_oplog_admin_created (admin_id, created_at DESC),
    KEY idx_oplog_activity (activity_id)
) ENGINE = InnoDB COMMENT = '操作日志表';

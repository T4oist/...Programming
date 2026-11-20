# 高校学生活动全流程管理系统

程序设计综合实训（2026）项目 —— 面向讲座、宣讲会、就业指导会等高校学生活动的 Web 应用，
覆盖活动的 **创建 → 查询 → 发布（宣传页+报名二维码）→ 报名 → 签到 → 统计** 全流程。

## 技术栈

| 层次 | 技术 |
|---|---|
| 前端 | Vue 3 + Vite + Element Plus + Vue Router + Axios |
| 后端 | Spring Boot 3 + MyBatis-Plus + Spring Validation |
| 数据库 | MySQL 8.0（InnoDB，utf8mb4，满足第三范式） |
| 认证 | JWT（管理员登录），参与人免注册 |
| 安全 | BCrypt 密码哈希、动态二维码 HMAC 凭证、乐观锁+数据库唯一约束防重复报名 |

## 目录结构

```
...Programming
├── backend                          # Spring Boot 后端
│   └── src/main/
│       ├── java/com/xidian/activity/
│       │   ├── ActivitySystemApplication.java   # 启动类
│       │   ├── common/    # 统一返回、业务异常、错误码、枚举
│       │   ├── config/    # MyBatis-Plus、CORS、JWT 拦截器
│       │   ├── controller/# 管理端 + 公开接口
│       │   ├── dto/       # 请求/响应对象
│       │   ├── entity/    # 数据库实体
│       │   ├── mapper/    # MyBatis-Plus Mapper
│       │   └── service/   # 业务逻辑（含事务与并发控制）
│       └── resources/
│           ├── application.yml
│           └── db/        # schema.sql / data.sql
└── frontend                         # Vue 3 前端
    └── src/
        ├── api/          # Axios 封装
        ├── router/       # 路由（管理端需登录，公开页免登录）
        └── views/        # 页面
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p < backend/src/main/resources/db/data.sql
```

> 也可在脚本前手动加 `DROP DATABASE IF EXISTS activity_system;`（schema.sql 已含）。
> 初始管理员账号：`admin / admin123`。

### 2. 配置本地私密信息

```bash
cd backend
cp application-local.yml.example application-local.yml
# 编辑 application-local.yml，填入本机 MySQL 密码；如需局域网访问，改 public-base-url 为本机 IP
```

> `application-local.yml` 已被 Git 忽略且不会打包进 jar，密钥/密码不会泄露到仓库。
> 也可不建该文件，直接设环境变量 `DB_PASSWORD`、`JWT_SECRET`、`QR_SECRET`、`PUBLIC_BASE_URL`。

### 3. 启动后端（默认 8080 端口）

```bash
cd backend
./mvnw spring-boot:run      # 无需本机安装 Maven，wrapper 会自动下载
# Windows 用 mvnw.cmd spring-boot:run
```

### 4. 启动前端（默认 5173 端口，已配置代理到 8080）

```bash
cd frontend
npm ci                       # 用 lock 文件安装，保证版本可复现
npm run dev
```

浏览器访问 <http://localhost:5173>：
- 管理端：`/login` 登录后进入活动管理；
- 公开宣传页（免登录）：`/a/{activityId}`，报名页 `/a/{activityId}/signup`；
- 现场签到页：`/checkin/{activityId}`，动态签到码展示页（管理端）`/admin/checkin/{activityId}`。

## 核心业务规则

- **活动状态**由系统按服务器时间实时计算：未发布 / 报名中 / 已截止 / 已结束，不在库中冗余存储；
- **报名校验**（服务端事务内）：活动有效（已发布、未删除、未截止、未结束）→ 信息格式（姓名 2~20 字符、手机号 11 位、学院须在启用列表）→ `(activity_id, phone)` 数据库唯一约束防重复 → 悲观/乐观锁并发控制名额上限；
- **编辑约束**：人数上限不得小于已报名人数；编辑保留活动标识与报名、签到记录，报名地址不变；
- **删除**为逻辑删除，公开入口立即失效，历史数据保留；
- **动态二维码签到**（选做）：签到码每 30 秒刷新，二维码内容含 HMAC 签名凭证，服务端校验活动标识、用途、有效期，过期即拒绝；一条报名最多产生一条有效签到记录；
- 所有管理端关键操作写入 `operation_log`，记录操作人与时间。

## 生产 / 局域网部署要点

开发与演示（localhost）开箱即用，但**换到非本机域名/IP 时需注意两点**，否则扫码签到会失效：

0. 局域网（同一 Wi-Fi）演示：`vite.config.js` 已配置 `host: true`，启动前端后让手机/其他电脑访问 `http://<电脑局域网IP>:5173`（`ipconfig` 查看 WLAN 的 IPv4）。**管理端也请用该地址打开**，页面上的分享链接和二维码会按当前访问地址自动生成；若管理员用 localhost 打开，页面会出现警告提示。
1. 后端 `public-base-url` 建议设为参与人可访问的前端地址（环境变量 `PUBLIC_BASE_URL`，例如 `http://192.168.1.10:5173`）；前端分享链接/二维码已按当前访问地址自动生成，此项主要影响接口返回的绝对地址。
2. 生产用 nginx 把 `/api` 反向代理到后端 8080，前端构建产物 `dist/` 由 nginx 静态托管：

```nginx
location /api/ { proxy_pass http://127.0.0.1:8080; proxy_set_header Host $host; }
location / { try_files $uri $uri/ /index.html; }   # Vue history 路由回退
```

3. 生产务必通过环境变量注入强随机密钥：`JWT_SECRET`、`QR_SECRET`（各 ≥32 字节）、`DB_PASSWORD`； SQL 日志默认关闭，需要时设 `SQL_LOG_IMPL`。
4. 前端如需连接非同源后端，设 `VITE_API_BASE_URL` 环境变量（如 `https://api.example.com/api`）。
5. `npm run preview` 已配置与开发相同的 `/api` 代理，可直接预览构建产物。

## 团队分工（三人小组）

原则：按端到端功能块切分，公共部分（数据库、接口契约、报告）归组长单点负责，避免多人改同一文件。

| 成员 | 角色 | 负责模块 | 主要交付物 |
|---|---|---|---|
| A | 后端架构 / 组长 | 项目骨架、数据库、JWT 认证、活动管理、接口契约与联调统筹 | `pom.xml`、`schema.sql`、认证配置、活动模块、报告整合 |
| B | 后端业务 | 报名（事务+并发）、签到（动态二维码验签）、操作日志、公开接口 | 报名/签到 Service、PublicController |
| C | 前端 / 文档 | 管理端全部页面、公开三页、Axios/路由、测试与报告排版 | `frontend/`、测试用例、报告与答辩材料 |

协作约定：接口契约先行（见 `frontend/src/api/modules.js`）→ 各在 `feature-a/b/c` 分支开发 → 每日合并到 `main` 并联调一次 → 报告由 A 统一整合。

> 弹性调整：前端吃力可把 3 个公开页分给 A；后端强者可让 B 接管活动管理；基础薄弱者可负责测试章节与公开页。

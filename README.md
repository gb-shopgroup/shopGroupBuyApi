# ShopGroupBuyApi — 店小团团购系统后端

> 面向社区团购场景的微服务后端项目，提供商品团购、订单交易、支付分账、团长自提点管理等核心能力。

## 目录

1. [项目简介](#一项目简介)
2. [技术栈](#二技术栈)
3. [系统架构](#三系统架构)
4. [模块说明](#四模块说明)
5. [数据库设计](#五数据库设计)
6. [核心业务流程](#六核心业务流程)
7. [快速启动](#七快速启动)
8. [接口文档](#八接口文档)

---

## 一、项目简介

本项目是**店小团**（AI 店小团）社区团购系统的后端服务，采用 Spring Cloud 微服务架构，为微信小程序端（团员端）、团长端、管理后台提供统一的 API 服务。

系统核心角色与能力：

- **团员（Member）**：浏览团购活动、下单购买、申请退款、查看订单；
- **团长（Leader）**：管理店铺与商品（含 SKU/规格/包装）、发起团购活动、订单核销、退款审批、提现、数据汇总；
- **员工（Staff）**：协助团长管理自提点订单核销与数据统计；
- **平台运营（Admin）**：团长/商品/订单/报表的全局管理。

项目整体以 Spring Cloud Gateway 作为统一入口，网关按路径前缀将请求转发至各业务服务；各服务独立访问 MySQL（库名 `group_purchase`）与 Redis；支付对接**易宝支付**，图片资源存储于**华为云 OBS**，用户身份基于**微信小程序**体系（openid / 手机号）。

---

## 二、技术栈

| 分类 | 技术 | 版本 |
| --- | --- | --- |
| 语言 | Java | 8 |
| 基础框架 | Spring Boot | 2.7.18 |
| 微服务 | Spring Cloud | 2021.0.5 |
| 微服务组件 | Spring Cloud Alibaba（BOM 版本管理） | 2021.0.5.0 |
| API 网关 | Spring Cloud Gateway | 随 Spring Cloud |
| ORM | MyBatis-Plus | 3.x |
| 数据库 | MySQL | 8.0（库名 `group_purchase`） |
| 连接池 | Druid | — |
| 缓存 | Redis（Lettuce） | — |
| 构建工具 | Maven | — |
| 支付 | 易宝支付（Yeepay） | — |
| 对象存储 | 华为云 OBS | — |
| 二维码/小程序码 | 微信小程序码（`getwxacodeunlimit`） | — |

Maven 依赖仓库使用阿里云镜像（`https://maven.aliyun.com/repository/public`）。

---

## 三、系统架构

### 3.1 架构总览

```
                微信小程序（团员端 / 团长端）          管理后台 Web
                          │                                │
                          └──────────────┬─────────────────┘
                                         ▼
                              ┌───────────────────┐
                              │  Spring Cloud     │
                              │  Gateway (:8080)  │  统一入口 / 跨域 / 路由
                              └───────────────────┘
                                         │
        ┌───────────────┬───────────────┼──────────────────┬───────────────┐
        ▼               ▼               ▼                  ▼               ▼
 ┌────────────┐  ┌────────────┐  ┌────────────┐   ┌────────────┐   ┌────────────┐
 │ gb-group-  │  │ gb-group-  │  │ gb-group-  │   │ gb-group-  │   │ gb-group-  │
 │ admin      │  │ user       │  │ goods      │   │ order      │   │ task       │
 │ (后台管理)  │  │ (用户/团长) │  │ (商品/团购) │   │ (订单/支付) │   │ (定时任务)  │
 └────────────┘  └────────────┘  └────────────┘   └────────────┘   └────────────┘
        │               │               │                │                │
        └───────────────┴───────┬───────┴────────────────┴────────────────┘
                                ▼
                    ┌──────────────────────┐
                    │ MySQL (group_purchase) │  Redis       华为云 OBS / 易宝支付
                    └───────────────────────┘
```

> 各服务之间通过 Maven 模块相互依赖（如 order 依赖 goods、user 等），并通过公共模块 `gb-group-common` 复用统一能力。

### 3.2 网关路由

网关（`gb-group-gateway`，端口 `8080`）按路径前缀转发（与网关 `application.yml` 路由配置一致）：

| 路由前缀 | 目标服务 | 目标地址 | 说明 |
| --- | --- | --- | --- |
| `/admin/**` | gb-group-admin | `http://127.0.0.1:9015` | 平台管理后台 |
| `/user/**` | gb-group-user | `http://127.0.0.1:9011` | 用户 / 团长 / 员工 |
| `/order/**` | gb-group-order | `http://127.0.0.1:9013` | 订单 / 支付 / 分账 |
| `/goods/**` | gb-group-goods | `http://127.0.0.1:9012` | 商品 / 团购 |
| `/task/**` | gb-group-task | `http://127.0.0.1:9014` | 定时任务 / 运维接口 |

### 3.3 服务端口一览

| 模块 | 应用名 | 实际配置端口 | 备注 |
| --- | --- | --- | --- |
| gb-group-gateway | spring-groupbuy-gateway | 8080 | API 网关（统一入口） |
| gb-group-admin | spring-groupbuy-admin | 9015 | 平台管理后台服务 |
| gb-group-user | spring-gbshop-user | 9011 | 用户 / 团长 / 员工服务 |
| gb-group-order | spring-gbshop-order | 9013 | 订单 / 支付 / 分账服务 |
| gb-group-goods | spring-gbshop-goods | 9012 | 商品 / 团购服务 |
| gb-group-task | spring-gbshop-task | 9014 | 定时任务服务 |
| gb-group-common | — | — | 公共依赖模块（无端口） |

---

## 四、模块说明

### 4.1 gb-group-common — 公共模块

被所有服务依赖，提供统一基础设施，包括：

- 统一响应结果封装（`Result` / `ResultCode` 等）；
- 全局异常处理与业务异常定义；
- 通用实体基类、工具类；
- 易宝支付 SDK 公共配置（`config/yop_sdk_config_default.json`）；数据库、Redis、图片上传、支付回调等环境配置由各服务自身的 `application-{dev,test,prod}.yml` 维护。

### 4.2 gb-group-gateway — API 网关

- 基于 Spring Cloud Gateway 实现统一入口、路由转发、跨域配置（CORS）；
- 生产环境通过 `https://api.shopgroup.com.cn` 对外暴露，反向代理至网关。

### 4.3 gb-group-user — 用户 / 团长 / 员工服务

覆盖：会员登录注册（微信 openid / 手机号）、用户信息、团长店铺管理、自提点管理、员工管理、黑名单、消息、图片上传、文章与轮播图等。**46 个接口**。

### 4.4 gb-group-goods — 商品 / 团购服务

覆盖：商品（分类、图片、包装、规格、规格值、SKU、库存）、团长商品管理、团购活动查询等。**22 个接口**。

### 4.5 gb-group-order — 订单 / 支付 / 分账服务

覆盖：下单、订单查询、核销（整单 / 部分核销）、退款（申请 / 审批 / 回调）、微信发货、易宝支付发起与回调、团长数据汇总、报表查询，以及团长端「我的团员」（含团购查看埋点，数据表 `gb_group_view_log`）、团长端「对账单」（按时间范围 + 按商品 / 按订单维度统计）等。**47 个接口**。

### 4.6 gb-group-admin — 平台管理后台服务

覆盖：后台登录（图形验证码）、团长管理、商品管理、团购管理、订单管理、会员管理、报表管理等。**29 个接口**。

### 4.7 gb-group-task — 定时任务 / 运维服务

覆盖：微信发货、订单分账、订单查询、退款状态同步、提现、自动收货、库存恢复等运维与补偿类接口。**8 个接口**。

---

## 五、数据库设计

### 5.1 连接信息（prod 环境示例）

| 项 | 配置 |
| --- | --- |
| MySQL（dev/test） | `localhost:3306`，库名 `group_purchase`（test 环境部分模块为 `group_purchase1`），账号 `root` |
| MySQL（prod） | `192.168.0.122:3306`，库名 `group_purchase`，账号 `group_buy` |
| Redis 地址 | `192.168.0.114:6379`，DB `1` |
| 图片存储 | 华为云 OBS：`shopgroup.obs.cn-north-9.myhuaweicloud.com`（公有）/ 私有访问经 `https://api.shopgroup.com.cn/image/access` |
| 支付回调 | `https://api.shopgroup.com.cn/group/order/notify` |

> 建表脚本见 **`sql/group_purchase.sql`**（36 张表，从开发库 `group_purchase` 直接导出，仅含表结构）；后续新增的表单独建脚本，位于 `sql/` 下（如 `sql/gb_group_view_log.sql`），需在对应环境单独执行。

### 5.2 核心表清单（共 35 张）

| 表名 | 说明 |
| --- | --- |
| `gb_article_info` | 文章信息表 |
| `gb_focus_info` | 轮播图信息表 |
| `gb_goods_category_info` | 商品分类信息表 |
| `gb_goods_image_info` | 商品图片表 |
| `gb_goods_info` | 商品信息表 |
| `gb_goods_package_info` | 商品包装信息表 |
| `gb_goods_restock_info` | 商品库存补货表 |
| `gb_goods_sku_info` | 商品 SKU 信息表 |
| `gb_goods_spec_info` | 商品规格信息表 |
| `gb_goods_spec_value` | 商品规格值表 |
| `gb_group_activity_goods` | 团购商品信息表 |
| `gb_group_activity_info` | 团购活动信息表 |
| `gb_group_category_info` | 团购分类信息表 |
| `gb_group_view_log` | 团购查看记录表（用户浏览团购详情埋点，团长端"我的团员"查看次数/动态数据源） |
| `gb_image_library_info` | 图片库信息表 |
| `gb_member_address_info` | 用户/会员地址表 |
| `gb_member_blacklist` | 黑名单信息表 |
| `gb_member_info` | 用户/会员信息表 |
| `gb_member_message_info` | 用户/会员消息表 |
| `gb_order_business_info` | 订单收款账户信息表 |
| `gb_order_commission_info` | 订单分账/佣金信息表 |
| `gb_order_goods_info` | 订单商品信息表 |
| `gb_order_goods_refund_record` | 订单商品退款记录表 |
| `gb_order_info` | 订单信息表 |
| `gb_org_business_info` | 团长分账户信息表 |
| `gb_org_cash_bank_info` | 银行信息表 |
| `gb_org_cash_bank_no` | 银行卡信息表 |
| `gb_org_cash_info` | 团长提现信息表 |
| `gb_org_leader_info` | 团长信息表 |
| `gb_org_message_info` | 团长消息表 |
| `gb_org_point_info` | 自提点信息表 |
| `gb_org_shop_info` | 团长店铺信息表 |
| `gb_org_staff_info` | 员工信息表 |
| `gb_sys_config_info` | 系统配置信息表 |
| `gb_sys_user_info` | 系统用户信息表 |

> 业务域分布：**商品域**（8 表）、**团购域**（4 表，含查看埋点表 `gb_group_view_log`）、**用户域**（4 表）、**订单域**（5 表）、**团长/商户域**（9 表）、**内容域**（3 表）、**系统域**（2 表），合计 35 张。

---

## 六、核心业务流程

1. **用户登录**：小程序 code → 换取 openid / 手机号 → 自动登录 / 注册，后续请求携带 `token`。
2. **团长开店**：团长维护店铺信息 → 添加商品（分类/图片/包装/规格/SKU/库存）→ 发起团购活动。
3. **用户参团**：首页查看团购分类与列表 → 进入团购详情 → 下单 → 发起易宝支付 → 支付回调更新订单。
4. **订单履约**：用户到自提点出示订单码 → 团长/员工扫码核销（支持部分核销）→ 微信发货（快递场景）→ 自动收货。
5. **退款流程**：用户申请退款 → 团长审批（通过/拒绝）→ 退款回调同步状态。
6. **资金结算**：支付后按分账/佣金配置自动拆账到团长收款账户 → 团长提现（绑定银行卡）。

---

## 七、快速启动

### 7.1 环境要求

- JDK 8
- Maven 3.6+
- MySQL 8.0（执行 `sql/group_purchase.sql` 建表脚本，库名 `group_purchase`）
- Redis

### 7.2 编译构建

```bash
# 全量编译（跳过测试）
mvn clean install -DskipTests
```

> 根 `pom.xml` 已配置 `spring-boot-maven-plugin` 默认跳过 repackage，保证各模块能互相以普通 jar 依赖；需要可执行 jar 时对目标模块执行 `mvn spring-boot:repackage`。

### 7.3 启动服务（按依赖顺序）

```bash
# 1. 网关
mvn -pl gb-group-gateway spring-boot:run
# 2. 后台管理
mvn -pl gb-group-admin spring-boot:run
# 3. 用户服务
mvn -pl gb-group-user spring-boot:run
# 4. 商品服务
mvn -pl gb-group-goods spring-boot:run
# 5. 订单服务
mvn -pl gb-group-order spring-boot:run
# 6. 定时任务服务（可选）
mvn -pl gb-group-task spring-boot:run
```

启动后统一通过网关访问：`http://localhost:8080/<前缀>/<路径>`。

### 7.4 环境配置说明

各服务自带 `application-{dev,test,prod}.yml`，通过 `application.yml` 的 `spring.profiles.active` 切换环境；数据库、Redis、图片上传（本地 / 华为云 OBS）、易宝支付回调等配置均在各环境文件中维护。易宝支付 SDK 公共配置位于 `gb-group-common/src/main/resources/config/yop_sdk_config_default.json`。

---

## 八、接口文档

### 8.1 公共说明

- **Base URL**：网关地址 `http://localhost:8080`（生产环境 `https://api.shopgroup.com.cn`），接口路径以 `/admin`、`/user`、`/order`、`/goods`、`/task` 开头，由网关路由到对应服务。
- **请求头公共参数**：`token`（用户登录令牌）、`lid`（团长 id）、`sid`（员工 id），实际以各接口校验为准。
- **入参说明**：Query 参数挂在 URL 上（`?key=value`），Header 参数放在请求头，Body 为 JSON 请求体对象。Query/Header 参数「必填」默认「是」（`@RequestParam` 默认必填），标注 `required=false` 的为非必填；Body 对象各字段的必填取自字段校验注解（`@NotNull` 等），未注解时以实际逻辑为准。
- **分页约定**：列表类接口一般通过 `page`（页码，从 1 开始）/ `pageSize`（每页条数）分页，配套 `count` 接口获取总数。
- **出参说明**：统一返回 `JsonResult`（`code` 状态码：200=成功 / 300=失败 / 400=无权限 / 500=错误；`msg` 提示信息；`data` 业务数据）。`data` 字段表中嵌套对象字段以 `→` 前缀递进展开，字段「说明」列取自实体 / DTO 源码注释。
- **常用参数速查**：

| 参数 | 含义 |
| --- | --- |
| `token` | 用户登录令牌 |
| `lid` / `leaderId` | 团长 ID |
| `sid` / `staffId` | 员工 ID |
| `memberId` / `aId` | 会员（用户）ID |
| `shopId` | 店铺 ID |
| `goodsId` / `id`（商品接口） | 商品 ID |
| `gid` / `groupId` | 团购活动 ID |
| `catId` / `cat` | 团购分类 ID |
| `pid` / `point` | 提货点（自提点）ID |
| `orderNo` / `orderId` | 订单编号 / 订单 ID |
| `openid` | 微信 openid（用户唯一标识） |
| `code` | 微信授权 code（临时凭证） |
| `mobile` | 手机号 |
| `page` / `pageSize` | 页码（从 1 开始）/ 每页条数 |
| `start` / `end` | 开始时间 / 结束时间（如 yyyy-MM-dd） |

- **接口数量统计**：user 46 个、order 47 个、goods 22 个、admin 29 个、task 8 个，合计 **152 个**。
- **详细版接口文档**（含每个接口的完整入参 / 出参字段说明，参数含义、必填、嵌套字段均已细化）见根目录 **`API接口文档.md`**，可通过 `python3 generate_api_doc.py` 扫描各模块 `*Controller.java` 重新生成。下方为接口总览清单。

### 8.2 接口清单

> 功能说明优先取源码方法注释；*斜体* 为脚本依据路径与出入参自动推断（仅供参考），字段级说明见 [`API接口文档.md`](API接口文档.md)。

#### 8.2.1 gb-group-user（用户/团长/员工）— 46 个接口

**ImageSourceController**（`cn.com.shopgroup.user.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/user/image/upload/avatar` | *上传头像文件* | 无 |
| 2 | POST | `/user/image/upload/goods` | *上传商品文件* | 无 |
| 3 | POST | `/user/image/upload/banner` | *上传轮播图文件* | 无 |
| 4 | GET | `/user/image/access/{*file}` | *访问图片（静态资源）* | **file** (String) |

**BlackController**（`cn.com.shopgroup.user.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/group/black` | *查询团购黑名单* | **lid** (Long) |

**GroupPointController**（`cn.com.shopgroup.user.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/group/point` | *查询团购自提点* | **id** (Long) |

**ShowsPageController**（`cn.com.shopgroup.user.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/article/info` | *查询文章详情* | **id** (Long) |
| 2 | GET | `/user/focus` | *查询关注* | 无 |

**LeaderBlackController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/member/mobile` | *查询团长会员手机号* | **mobile** (String) |
| 2 | POST | `/user/leader/add/black` | *提交（团长黑名单）* | **memberId** (Long) |
| 3 | GET | `/user/leader/black/mobile` | *查询团长黑名单手机号* | **mobile** (String) |
| 4 | POST | `/user/leader/member/black/remove` | *删除团长会员黑名单* | **memberId** (Long) |
| 5 | GET | `/user/leader/black/list` | *查询团长黑名单列表* | **page** (int); **pageSize** (int) |
| 6 | GET | `/user/leader/black/count` | *查询团长黑名单总数* | 无 |

**LeaderBusinessController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/business/list` | 查询当前团长收款账户列表（含Redis累计额度） | 无 |
| 2 | POST | `/user/leader/business/add` | 添加团长收款账户（校验证件号码唯一，返回新增账户ID） | Body: **request** (BusinessRequest, JSON) |
| 3 | POST | `/user/leader/business/edit` | 修改收款账户（已审核通过的账户不允许修改） | Body: **request** (BusinessRequest, JSON) |
| 4 | POST | `/user/leader/business/close` | 关闭/启用收款账户（禁用或启用商户收款） | Body: **request** (OCBusinessRequest, JSON) |

**LeaderPointController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/point/list` | *查询团长自提点列表* | **name** (String) |
| 2 | GET | `/user/leader/point/addGroup/list` | *查询团长自提点团购列表* | 无 |
| 3 | POST | `/user/leader/point/add` | *新增团长自提点* | Body: **request** (PointRequest, JSON) |
| 4 | POST | `/user/leader/point/edit` | *修改团长自提点* | Body: **request** (PointRequest, JSON) |
| 5 | GET | `/user/leader/point/close` | *启用/关闭团长自提点* | **id** (Long) |
| 6 | GET | `/user/leader/point/info` | *查询团长自提点详情* | **pointId** (Long) |
| 7 | GET | `/user/leader/point/ercode` | *查询团长自提点二维码* | **id** (Long) |

**LeaderShopController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/shop/info` | 查看店铺信息 | 无 |
| 2 | POST | `/user/leader/shop/save` | *保存团长店铺* | Body: **request** (ShopRequest, JSON) |
| 3 | POST | `/user/leader/shop/update` | *修改团长店铺* | Body: **request** (ShopErCodeRequest, JSON) |
| 4 | GET | `/user/leader/getGroup/shop` | *查询团长团购店铺* | **leaderId** (Long) |
| 5 | POST | `/user/leader/shop/makeQrCode` | *生成二维码（团长店铺）* | **shopId** (Long) |

**MessageController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/message/list` | *查询团长消息列表* | **type** (Integer); **page** (int); **pageSize** (int) |
| 2 | GET | `/user/leader/message/count` | *查询团长消息总数* | **type** (Integer) |
| 3 | GET | `/user/leader/message/unread` | *查询团长消息未读* | **type** (Integer) |
| 4 | GET | `/user/leader/message/read` | *标记已读（团长消息）* | **id** (Long) |

**StaffController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/staff/list` | *查询团长员工列表* | 无 |
| 2 | POST | `/user/leader/staff/add` | *新增团长员工* | Body: **request** (StaffRequest, JSON) |
| 3 | POST | `/user/leader/staff/edit` | *修改团长员工* | Body: **request** (StaffRequest, JSON) |
| 4 | POST | `/user/leader/staff/close` | *启用/关闭团长员工* | **id** (Long) |
| 5 | POST | `/user/leader/staff/remove` | *删除团长员工* | **staffId** (Long) |

**LoginController**（`cn.com.shopgroup.user.controller.member`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/phone` | *查询手机号* | **code** (String) |
| 2 | GET | `/user/openid` | *查询openid* | **code** (String) |
| 3 | GET | `/user/login` | *登录* | **openid** (String) |
| 4 | POST | `/user/reg` | *注册* | Body: **request** (MemberRequest, JSON) |
| 5 | POST | `/user/logout` | *退出登录* | 无 |

**MemberController**（`cn.com.shopgroup.user.controller.member`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/member/info` | *查询会员详情* | 无 |
| 2 | GET | `/user/member/isleader` | *查询会员是否为团长* | 无 |

#### 8.2.2 gb-group-order（订单/退款/分账）— 47 个接口

**OrderBusinessController**（`cn.com.shopgroup.order.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/orderbusiness/list` | *查询订单收款账户列表* | **page** (int); **pageSize** (int) |
| 2 | GET | `/order/orderbusiness/count` | *查询订单收款账户总数* | 无 |

**GroupOrderController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/group/add` | *新增团购* | Body: **request** (OrderRequest, JSON) |

**MemberGroupController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/group/groupActivity/cat` | *查询团购活动分类* | 无 |
| 2 | POST | `/order/member/groupActivity/list` | *查询会员团购活动列表* | Body: **request** (MemberGroupActListRequest, JSON) |
| 3 | GET | `/order/group/groupActivity/info` | *查询团购活动详情* | **groupId** (Long) |
| 4 | POST | `/order/group/groupActivity/view` | *查看（团购活动）* | Body: **request** (MemberGroupViewRequest, JSON) |
| 5 | GET | `/order/group/groupActivity/shop` | *查询团购活动店铺* | **leaderId** (Long) |
| 6 | GET | `/order/group/groupActivity/logs` | *查询团购活动日志* | **groupId** (Long) |
| 7 | GET | `/order/group/groupActivity/logs2` | *查询团购活动日志* | **id** (Long) |
| 8 | GET | `/order/group/order/records` | 真实跟团记录：基于支付成功订单数据 | **groupId** (Long); **limit** (Integer) |

**MemberOrderController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/group/order/list` | *查询团购订单列表* | Body: **request** (MemberOrderListRequest, JSON) |
| 2 | POST | `/order/group/order/applyRefundList` | *查询团购订单退款申请列表* | Body: **request** (MemberOrderRefundListRequest, JSON) |
| 3 | GET | `/order/group/order/count` | *查询团购订单总数* | **pid** (Long) |
| 4 | GET | `/order/group/order/info` | *查询团购订单详情* | **orderNo** (String) |
| 5 | GET | `/order/group/order/makeErcode` | *生成二维码（团购订单）* | **orderNo** (String) |
| 6 | GET | `/order/group/order/receipt` | 用户扫码核销-整单核销<br>核销后订单状态：已有售后(5)保持不变；无售后则置已收货(3)并记录收货时间 | **orderNo** (String); **point** (Long) |
| 7 | POST | `/order/group/order/part/receipt` | 用户扫码核销-部分核销（可多次核销）<br>核销后订单状态：已有售后(5)保持不变；无售后且完全核销置已收货(3)+收货时间，未完全核销置部分收货(2) | Body: **request** (MemberOrderReceiptRequest, JSON) |
| 8 | POST | `/order/group/order/apply/refund` | *退款（团购订单）* | Body: **refundApplyRequest** (OrderRefundApplyRequest, JSON) |
| 9 | GET | `/order/group/order/refund/reasonList` | *查询团购订单退款原因列表* | 无 |
| 10 | GET | `/order/group/order/refund/recodes` | *查询团购订单退款记录* | **orderNo** (String) |
| 11 | GET | `/order/group/order/notAllReceiptList` | 查询团购订单未全部提货列表<br>用户id+团长id查询（shopId仅用于解析团长id），已支付且状态1待收货/2部分收货/5售后，须存在未核销商品（购买数&gt;已核销数）；状态5时商品行还须购买数&gt;（已核销数+退款数）；返回订单并回填商品信息 | **shopId** (Long) |
| 12 | POST | `/order/group/order/confirmShipping` | *确认发货（团购订单）* | **orderNo** (String) |
| 13 | POST | `/order/group/order/applyRefund/orderInfo` | *查询团购订单退款申请订单详情* | Body: **request** (MemberOrderRefundRequest, JSON) |

**WxOrderController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/group/wx/order` | *查询团购微信订单* | **orderNo** (String) |

**LeaderBillController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/bill/list` | 团长端-对账单 | Body: **request** (LeaderBillListRequest, JSON) |

**LeaderGroupActivityController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/leader/activity/list` | 团长端-团购活动查询  提供团长端活动下拉/选择所需的接口；活动主数据在商品域(goods), 本控制器通过 goods 模块的 {@link GbGroupActivityInfoService} 跨模块调用，避免在 order 模块重复建表/写 SQL。 | 无 |

**LeaderMemberController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/myMember/list` | 我的团员列表（支持手机号/昵称搜索，分页） | Body: **request** (LeaderMemberListRequest, JSON) |
| 2 | GET | `/order/leader/myMember/detail` | 团员详情（消费/退款/跟团次数/查看次数/动态） | **memberId** (Long) |

**OrderController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/order/list` | *查询团长订单列表* | Body: **request** (LeaderOrderListRequest, JSON) |
| 2 | POST | `/order/leader/apply/refundList` | *查询团长退款列表* | Body: **request** (LeaderOrderApplyRefundRequest, JSON) |
| 3 | GET | `/order/leader/order/count` | *查询团长订单总数* | **gid** (Long); **pid** (Long) |
| 4 | GET | `/order/leader/order/status` | *查询团长订单状态* | **gid** (Long); **pid** (Long) |
| 5 | POST | `/order/leader/order/scanQRCode` | *扫码核销（团长订单）* | Body: **request** (ScanQRCodeRequest, JSON) |
| 6 | GET | `/order/leader/order/query` | *查询团长订单* | **orderNo** (String) |
| 7 | POST | `/order/leader/order/writeOff` | *核销（团长订单）* | **orderNo** (String); **pid** (Long) |
| 8 | POST | `/order/leader/order/partWriteOff` | *部分核销（团长订单）* | Body: **request** (OrderVerifyRequest, JSON) |
| 9 | GET | `/order/leader/order/send` | *发送（团长订单）* | **orderNo** (String) |
| 10 | GET | `/order/leader/home/show/orders` | *查询团长首页展示订单* | **groupId** (Long); **pointId** (Long) |
| 11 | GET | `/order/leader/home/order/goodsSummary` | *查询团长首页订单商品* | **groupId** (Long); **pointId** (Long); **keyword** (String); **page** (Integer); **pageSize** (Integer) |
| 12 | POST | `/order/get/groupActivity/totalOrder` | *提交（团购活动汇总订单）* | **groupId** (Long) |

**OrderRefundController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/leader/refund/count` | *查询团长退款总数* | **gid** (Long); **pid** (Long) |
| 2 | POST | `/order/leader/refund/applyList` | 查询团长退款列表（待审核申请）<br>口径：订单状态5售后 + 存在待审核(apply_refund=1)商品行，"先筛选再分页"，total 与列表一致；每单回填该笔申请的整笔待审核商品行；商品行数量以"申请退中数量"(gb_order_goods_info.apply_refund_num)为准 | Body: **request** (LeaderRefundApplyListRequest, JSON) |
| 3 | POST | `/order/leader/refund/approve` | *审核（团长退款）* | Body: **approveRequest** (OrderApproveRequest, JSON) |

**OrderRefundNotifyController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/refund/notify` | *回调（团长退款）* | 无 |

**OrderPaymentController**（`cn.com.shopgroup.order.controller.payment`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/payment/order/pay` | *支付（支付订单）* | **orderNo** (String); **openid** (String) |
| 2 | POST | `/order/payment/order/notify` | *回调（支付订单）* | 无 |

#### 8.2.3 gb-group-goods（商品/团购）— 22 个接口

**GroupGoodsController**（`cn.com.shopgroup.goods.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/goods/group/goods/list` | *查询团购商品列表* | **lid** (Long); **groupId** (Long) |
| 2 | GET | `/goods/group/goods/stock` | *查询团购商品库存* | **gid** (String) |

**LeaderGoodsManageController**（`cn.com.shopgroup.goods.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/goods/get/goods/cat` | *查询商品分类* | 无 |
| 2 | GET | `/goods/leader/goods/online` | *查询团长商品上架* | 无 |
| 3 | GET | `/goods/leader/goods/list` | *查询团长商品列表* | **cat** (Long); **keyword** (String); **page** (int); **pageSize** (int) |
| 4 | GET | `/goods/leader/goods/count` | *查询团长商品总数* | **cat** (Long); **keyword** (String) |
| 5 | GET | `/goods/leader/goods/info` | *查询团长商品详情* | **id** (Long) |
| 6 | POST | `/goods/leader/goods/addGoods` | *新增商品团长商品* | Body: **request** (LeaderAddGoodsRequest, JSON) |
| 7 | POST | `/goods/leader/goods/edit` | *修改团长商品* | Body: **request** (LeaderGoodsRequest, JSON) |
| 8 | GET | `/goods/leader/goods/close` | *启用/关闭团长商品* | **id** (Long) |
| 9 | GET | `/goods/leader/goods/sku/spec` | *查询团长商品SKU规格* | **id** (Long) |
| 10 | POST | `/goods/leader/goods/sku/save` | *保存团长商品SKU* | Body: **requestList** (List<LeaderSkuRequest>, JSON) |

**LeaderGroupManageController**（`cn.com.shopgroup.goods.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/goods/Leader/get/groupActivity/list` | 团长端-查询所有团购活动列表<br>返回每个团购活动的订单汇总数据 `groupSummaryResponse`（实际收入、退款金额、跟团人数）, 由 LeaderGroupSummaryService 批量聚合 gb_order_info 得出; 已取消订单不计入。 | Body: **request** (LeaderGroupListRequest, JSON) |
| 2 | GET | `/goods/Leader/get/groupActivity/count` | *查询团长团购活动总数* | **cat** (Long); **name** (String); **status** (Integer) |
| 3 | GET | `/goods/Leader/groupActivity/tag/list` | *查询团长团购活动标签列表* | 无 |
| 4 | POST | `/goods/Leader/groupActivity/add` | *新增团长团购活动* | Body: **request** (GroupActRequest, JSON) |
| 5 | GET | `/goods/Leader/get/groupActivity/info` | 查询团长团购活动详情<br>返回跟团统计 `genTuanResponse` 与跟团记录 `followRecords`（真实订单数据：手机号/姓名/头像/购买时间/购买商品/数量，已支付未取消，按购买时间倒序取最新50条） | **groupId** (Long) |
| 6 | POST | `/goods/Leader/groupActivity/edit` | *修改团长团购活动* | Body: **request** (GroupActRequest, JSON) |
| 7 | POST | `/goods/Leader/groupActivity/close` | *启用/关闭团长团购活动* | **groupId** (Long) |
| 8 | GET | `/goods/Leader/get/groupActivity/cat` | *查询团长团购活动分类* | 无 |
| 9 | POST | `/goods/Leader/share/groupActivity/poster` | *提交（团长分享团购活动海报）* | **groupId** (Long) |
| 10 | POST | `/goods/Leader/share/groupActivity/make/poster` | *提交（团长分享团购活动海报）* | **groupId** (Long) |

#### 8.2.4 gb-group-admin（后台管理）— 29 个接口

**AdminBusinessController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/business/list` | *查询收款账户列表* | **id** (Long); **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/business/count` | *查询收款账户总数* | 无 |
| 3 | GET | `/admin/business/info` | *查询收款账户详情* | **id** (Long) |
| 4 | POST | `/admin/business/add` | *新增收款账户* | Body: **request** (LeaderBusinessRequest, JSON) |

**AdminGoodsController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/goods/list` | *查询商品列表* | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/goods/count` | *查询商品总数* | 无 |
| 3 | GET | `/admin/goods/info` | *查询商品详情* | **id** (Long) |
| 4 | GET | `/admin/goods/img` | *查询商品图片* | **id** (Long) |

**AdminGroupController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/group/list` | *查询团购列表* | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/group/count` | *查询团购总数* | 无 |
| 3 | GET | `/admin/group/info` | *查询团购详情* | **id** (Long) |

**AdminGroupTagController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/tag/list` | *查询标签列表* | **keyword** (String); **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/tag/count` | *查询标签总数* | **keyword** (String) |
| 3 | GET | `/admin/tag/info` | *查询标签详情* | **tagId** (Long) |
| 4 | POST | `/admin/tag/add` | *新增标签* | Body: **tag** (GbGroupTag, JSON) |
| 5 | POST | `/admin/tag/edit` | *修改标签* | Body: **tag** (GbGroupTag, JSON) |
| 6 | POST | `/admin/tag/delete` | *删除标签* | **tagId** (Long) |

**AdminLeaderController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/leader/list` | *查询团长列表* | **mobile** (String); **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/leader/count` | *查询团长总数* | 无 |
| 3 | POST | `/admin/leader/add` | *新增团长* | Body: **request** (LeaderRequest, JSON) |
| 4 | GET | `/admin/leader/select` | *查询团长* | 无 |

**AdminLoginController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/login/kaptcha` | *查询验证码* | 无 |
| 2 | POST | `/admin/login/submit` | *提交* | Body: **request** (LoginRequest, JSON) |

**AdminMemberController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/member/list` | *查询会员列表* | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/member/count` | *查询会员总数* | 无 |

**AdminOrderBusinessController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/orderbusiness/list` | *查询订单收款账户列表* | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/orderbusiness/count` | *查询订单收款账户总数* | 无 |

**AdminOrderController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/order/list` | *查询订单列表* | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/order/count` | *查询订单总数* | 无 |

#### 8.2.5 gb-group-task（定时任务）— 8 个接口

**TaskController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/task/order/send` | *发送（订单）* | **orderNo** (String) |
| 2 | GET | `/task/order/divide` | *分账（订单）* | **orderNo** (String) |
| 3 | GET | `/task/order/query` | *查询订单* | **orderNo** (String) |
| 4 | GET | `/task/order/refund` | *退款（订单）* | **orderNo** (String) |
| 5 | GET | `/task/order/cash` | *查询订单提现* | **id** (int); **val** (int) |
| 6 | GET | `/task/order/verify` | *核销（订单）* | 无 |
| 7 | GET | `/task/order/backstock` | *回库（订单）* | 无 |
| 8 | GET | `/task/test/user` | *查询测试用户* | **aId** (Long) |


## 附：文件结构

```
ShopGroupBuyApi/
├── pom.xml                    # 父 POM（依赖版本管理）
├── sql/                       # 数据库脚本目录
│   ├── group_purchase.sql     # 建表语句（50 张表，从 MySQL 直接导出）
│   └── gb_group_view_log.sql  # 团购查看埋点表（新增, 需单独执行）
├── generate_api_doc.py        # 接口文档自动生成脚本（同步更新 README 接口清单）
├── API接口文档.md              # 接口文档（自动生成）
├── README.md                  # 项目说明文档（本文档）
├── logs/                      # 运行日志
├── gb-group-common/           # 公共模块
├── gb-group-gateway/          # API 网关
├── gb-group-user/             # 用户/团长/员工服务
├── gb-group-goods/            # 商品/团购服务
├── gb-group-order/            # 订单/支付/分账服务
├── gb-group-admin/            # 平台管理后台服务
└── gb-group-task/             # 定时任务服务
```

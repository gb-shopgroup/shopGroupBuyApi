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
| 二维码 | ZXing | — |

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

覆盖：会员登录注册（微信 openid / 手机号）、用户信息、团长店铺管理、自提点管理、员工管理、黑名单、消息、图片上传、文章与轮播图等。**44 个接口**。

### 4.4 gb-group-goods — 商品 / 团购服务

覆盖：商品（分类、图片、包装、规格、规格值、SKU、库存）、团长商品管理、团购活动查询等。**21 个接口**。

### 4.5 gb-group-order — 订单 / 支付 / 分账服务

覆盖：下单、订单查询、核销（整单 / 部分核销）、退款（申请 / 审批 / 回调）、微信发货、易宝支付发起与回调、团长数据汇总、报表查询，以及团长端「我的团员」（含团购查看埋点，数据表 `gb_group_view_log`）等。**45 个接口**。

### 4.6 gb-group-admin — 平台管理后台服务

覆盖：后台登录（图形验证码）、团长管理、商品管理、团购管理、订单管理、会员管理、报表管理等。**25 个接口**。

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

> 建表脚本见 **`sql/group_purchase.sql`**（50 张表，从开发库 `group_purchase` 直接导出，仅含表结构）；后续新增的表单独建脚本，位于 `sql/` 下（如 `sql/gb_group_view_log.sql`），需在对应环境单独执行。

### 5.2 核心表清单（共 51 张）

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
| `gb_goods_stock_log` | 商品库存日志表 |
| `gb_group_activity_goods` | 团购商品信息表 |
| `gb_group_activity_info` | 团购活动信息表 |
| `gb_group_category_info` | 团购分类信息表 |
| `gb_group_collection_activity` | 团购合集活动信息表 |
| `gb_group_collection_info` | 团购合集信息表 |
| `gb_group_region_info` | 团购区域表 |
| `gb_group_sales_info` | 团购帮卖信息表 |
| `gb_group_sales_price` | 团购帮卖价格表 |
| `gb_group_sales_set` | 团购帮卖设置表 |
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
| `gb_org_point_region` | 自提点区域信息表 |
| `gb_org_point_staff` | 自提点员工信息表 |
| `gb_org_shop_info` | 团长店铺信息表 |
| `gb_org_staff_info` | 员工信息表 |
| `gb_region_area_info` | 行政区域信息表 |
| `gb_region_map_info` | 地图区域信息表 |
| `gb_report_business_info` | 团长分账统计报表 |
| `gb_report_goods_info` | 团长商品统计报表 |
| `gb_report_point_goods` | 团长自提点商品统计报表 |
| `gb_report_point_info` | 团长自提点统计报表 |
| `gb_report_sales_info` | 团长销售统计报表 |
| `gb_sys_config_info` | 系统配置信息表 |
| `gb_sys_user_info` | 系统用户信息表 |

> 业务域分布：**商品域**（9 表）、**团购域**（10 表，含新增 `gb_group_view_log` 查看埋点表）、**用户域**（4 表）、**订单域**（5 表）、**团长/商户域**（11 表）、**内容域**（3 表）、**区域域**（2 表）、**报表域**（5 表）、**系统域**（2 表），合计 51 张。

---

## 六、核心业务流程

1. **用户登录**：小程序 code → 换取 openid / 手机号 → 自动登录 / 注册，后续请求携带 `token`。
2. **团长开店**：团长维护店铺信息 → 添加商品（分类/图片/包装/规格/SKU/库存）→ 发起团购活动。
3. **用户参团**：首页查看团购分类与列表 → 进入团购详情 → 下单 → 发起易宝支付 → 支付回调更新订单。
4. **订单履约**：用户到自提点出示订单码 → 团长/员工扫码核销（支持部分核销）→ 微信发货（快递场景）→ 自动收货。
5. **退款流程**：用户申请退款 → 团长审批（通过/拒绝）→ 退款回调同步状态。
6. **资金结算**：支付后按分账/佣金配置拆账 → 团长提现（绑定银行卡）→ 后台报表统计。

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

- **接口数量统计**：user 44 个、order 45 个、goods 21 个、admin 25 个、task 8 个，合计 **143 个**。
- **详细版接口文档**（含每个接口的完整入参 / 出参字段说明，参数含义、必填、嵌套字段均已细化）见根目录 **`API接口文档.md`**，可通过 `python3 generate_api_doc.py` 扫描各模块 `*Controller.java` 重新生成。下方为接口总览清单。

### 8.2 接口清单

#### 8.2.1 gb-group-user（用户/团长/员工）— 44 个接口

**ImageSourceController**（`cn.com.shopgroup.user.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/user/image/upload/avatar` | 上传用户头像 | 无 |
| 2 | POST | `/user/image/upload/goods` | 上传商品图片(尺寸400*400) | 无 |
| 3 | POST | `/user/image/upload/banner` | 上传店铺banner(尺寸750*250) | 无 |
| 4 | GET | `/user/image/access/{*file}` | 查看私有图片 | **file** (String) |

**BlackController**（`cn.com.shopgroup.user.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/group/black` | 登录查询是否是（leaderId）团长下的黑名单 | **lid** (Long) |

**GroupPointController**（`cn.com.shopgroup.user.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/group/point` | 自提点列表 | **id** (Long) |

**ShowsPageController**（`cn.com.shopgroup.user.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/article/info` | 文章详情 | **id** (Long) |
| 2 | GET | `/user/focus` | 轮播图列表 | 无 |

**LeaderBlackController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/member/mobile` | 根据手机号查询用户 | **mobile** (String) |
| 2 | POST | `/user/leader/add/black` | 加入黑名单 | **memberId** (Long) |
| 3 | GET | `/user/leader/black/mobile` | 根据手机号查询黑名单用户 | **mobile** (String) |
| 4 | POST | `/user/leader/member/black/remove` | 解除黑名单 | **memberId** (Long) |
| 5 | GET | `/user/leader/black/list` | 黑名单列表 | **page** (int); **pageSize** (int) |
| 6 | GET | `/user/leader/black/count` | 黑名单总数 | 无 |

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
| 1 | GET | `/user/leader/point/list` | 查询提货点列表 | 无 |
| 2 | POST | `/user/leader/point/add` | 添加提货点 | Body: **request** (PointRequest, JSON) |
| 3 | POST | `/user/leader/point/edit` | 修改提货点 | Body: **request** (PointRequest, JSON) |
| 4 | GET | `/user/leader/point/close` | 关闭提货点 | **id** (Long) |
| 5 | GET | `/user/leader/point/ercode` | 提货点二维码 | **id** (Long) |

**LeaderShopController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/shop/info` | 查看店铺信息 | 无 |
| 2 | POST | `/user/leader/shop/save` | 修改店铺信息 | Body: **request** (ShopRequest, JSON) |
| 3 | POST | `/user/leader/shop/update` | 更新店铺码图片地址（保存店铺二维码上传后的访问URL） | Body: **request** (ShopErCodeRequest, JSON) |
| 4 | GET | `/user/leader/getGroup/shop` | 通过leaderId团长店铺详情 | **leaderId** (Long) |
| 5 | POST | `/user/leader/shop/makeQrCode` | 团长-我的店铺二维码,上传到服务器返回URL | **shopId** (Long) |

**MessageController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/message/list` | 消息列表(团长查看所有消息) | **type** (Integer); **page** (int); **pageSize** (int) |
| 2 | GET | `/user/leader/message/count` | 消息列表总数量(团长查看所有消息) | **type** (Integer) |
| 3 | GET | `/user/leader/message/unread` | 未读消息总数量(团长查看所有消息) | **type** (Integer) |
| 4 | GET | `/user/leader/message/read` | 阅读消息 | **id** (Long) |

**StaffController**（`cn.com.shopgroup.user.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/leader/staff/list` | 查询当前团长员工列表(不含超级团长) | 无 |
| 2 | POST | `/user/leader/staff/add` | 添加员工(校验用户存在且未绑定, 绑定提货点) | Body: **request** (StaffRequest, JSON) |
| 3 | POST | `/user/leader/staff/edit` | 修改员工信息(支持更换员工及提货点) | Body: **request** (StaffRequest, JSON) |
| 4 | POST | `/user/leader/staff/close` | 关闭/启用员工账号(不能操作自己) | **id** (Long) |
| 5 | POST | `/user/leader/staff/remove` | 删除员工(不能删除自己) | **staffId** (Long) |

**LoginController**（`cn.com.shopgroup.user.controller.member`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/phone` | 根据code获取手机号 | **code** (String) |
| 2 | GET | `/user/openid` | 根据code获取openid | **code** (String) |
| 3 | GET | `/user/login` | openid自动登录 | **openid** (String) |
| 4 | POST | `/user/reg` | 注册新用户 | Body: **request** (MemberRequest, JSON) |
| 5 | POST | `/user/logout` | 退出登录(小程序调用): 清除服务端登录态, 小程序端需同步删除本地token | 无 |

**MemberController**（`cn.com.shopgroup.user.controller.member`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/user/member/info` | 根据 token 获取用户信息 | 无 |
| 2 | GET | `/user/member/isleader` | 是否团长身份 | 无 |

#### 8.2.2 gb-group-order（订单/退款/分账）— 45 个接口

**OrderBusinessController**（`cn.com.shopgroup.order.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/orderbusiness/list` | 分页查询订单列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/order/orderbusiness/count` | 查询订单总数 | 无 |

**ReportController**（`cn.com.shopgroup.order.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/leader/report/business/list` | 分账汇总列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/order/leader/report/business/count` | 分账汇总数量 | 无 |

**GroupOrderController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/group/add` | 用户下单 | Body: **request** (OrderRequest, JSON) |

**MemberGroupController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/group/groupActivity/cat` | 团购分类列表（首页） | 无 |
| 2 | POST | `/order/group/get/groupActivity/list` | 用户首页-查询所有团购活动列表 | Body: **request** (MemberGroupListRequest, JSON) |
| 3 | GET | `/order/group/groupActivity/count` | 团购数量（首页） | **leaderId** (Long); **catId** (Long) |
| 4 | GET | `/order/group/groupActivity/info` | 团购详情(团长分享页面)---用户首页：团长更多好货也用 | **groupId** (Long) |
| 5 | POST | `/order/group/groupActivity/view` | 用户查看团购详情-显式埋点上报(分享等场景前端调用; 首页进入详情会自动埋点, 可不上报) | Body: **request** (MemberGroupViewRequest, JSON) |
| 6 | GET | `/order/group/groupActivity/shop` | 团长店铺详情 | **leaderId** (Long) |
| 7 | GET | `/order/group/groupActivity/logs` | 晚上时间只生成固定跟团记录（锁定在19:00-20:00生成的订单）。 | **groupId** (Long) |
| 8 | GET | `/order/group/groupActivity/logs2` | 团购记录(跟团记录), 滚动部分 | **id** (Long) |
| 9 | GET | `/order/group/order/records` | 真实跟团记录：基于支付成功订单数据 | **groupId** (Long); **limit** (Integer) |

**MemberOrderController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/group/order/list` | 用户订单列表（按订单状态/商品名称筛选, 分页查询） | Body: **request** (MemberOrderListRequest, JSON) |
| 2 | POST | `/order/group/order/applyRefundList` | 用户售后订单列表（按订单状态筛选, 分页查询） | Body: **request** (MemberOrderRefundListRequest, JSON) |
| 3 | GET | `/order/group/order/count` | 用户订单数量 | **pid** (Long) |
| 4 | GET | `/order/group/order/info` | 用户订单详情 | **orderNo** (String) |
| 5 | GET | `/order/group/order/makeErcode` | 用户订单小程序码(微信小程序码, 扫码进入C端小程序对应订单页面) | **orderNo** (String) |
| 6 | GET | `/order/group/order/receipt` | 用户订单收货 | **orderNo** (String); **point** (Long) |
| 7 | POST | `/order/group/order/apply/refund` | 用户申请订单退款 | Body: **refundApplyRequest** (OrderRefundApplyRequest, JSON) |
| 8 | GET | `/order/group/order/refund/reasonList` | 用户退款原因下拉列表(申请退款时"选择退款原因") | 无 |
| 9 | GET | `/order/group/order/refund/recodes` | 申请售后记录查询 | **orderNo** (String) |
| 10 | GET | `/order/group/order/notAllReceiptList` | 用户端-查询还有商品未全部收货的订单列表(该用户在该团长/店铺下已支付, 且存在商品行收货数量小于购买数量的订单) | **shopId** (Long) |
| 11 | POST | `/order/group/order/confirmShipping` | 用户点击确认收货组件后调用接口，更新订单已经操作按钮 | **orderNo** (String) |

**WxOrderController**（`cn.com.shopgroup.order.controller.group`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/group/wx/order` | 查询微信订单发货状态（查询订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款；(6) 资金待结算） | **orderNo** (String) |

**LeaderMemberController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/myMember/list` | 我的团员列表（支持手机号/昵称搜索，分页） | Body: **request** (LeaderMemberListRequest, JSON) |
| 2 | GET | `/order/leader/myMember/detail` | 团员详情（消费/退款/跟团次数/查看次数/动态） | **memberId** (Long) |

**OrderController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/order/list` | 团长订单列表（按团活动/订单状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询） | Body: **request** (LeaderOrderListRequest, JSON) |
| 2 | POST | `/order/leader/apply/refundList` | 团长售后订单列表（按团活动/审核状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询） | Body: **request** (LeaderOrderApplyRefundRequest, JSON) |
| 3 | GET | `/order/leader/order/count` | 查询订单总数(待核销), 考虑提货点 | **gid** (Long); **pid** (Long) |
| 4 | GET | `/order/leader/order/status` | 查询订单状态数量 | **gid** (Long); **pid** (Long) |
| 5 | POST | `/order/leader/order/scanQRCode` | 团长扫用户订单码接口 | Body: **request** (ScanQRCodeRequest, JSON) |
| 6 | GET | `/order/leader/order/query` | 根据订单号查询订单 | **orderNo** (String) |
| 7 | POST | `/order/leader/order/writeOff` | 核销（整单核销） | **orderNo** (String); **pid** (Long) |
| 8 | POST | `/order/leader/order/partWriteOff` | 部分核销订单 | Body: **request** (OrderVerifyRequest, JSON) |
| 9 | GET | `/order/leader/order/send` | 团长端-查询微信发货 | **orderNo** (String) |
| 10 | GET | `/order/leader/home/show/orders` | 团长首页订单汇总(head部分): 返回有效订单总数/订单总金额/退款总金额; pointId 传 0 或不传表示不区分提货点, 传具体值则按提货点过滤 | **pointId** (Long) |
| 11 | GET | `/order/leader/home/order/goodsSummary` | 团长端订单-商品统计: 返回商品种类总数/待核销总件数 + 每个商品的件数统计(含已核销/未核销), 支持商品名称搜索与分页 | **pointId** (Long); **keyword** (String); **page** (Integer); **pageSize** (Integer) |
| 12 | POST | `/order/get/groupActivity/totalOrder` | 根据团购活动id统计订单数（实时统计，团长端有需求时使用） | **groupId** (Long) |

**OrderRefundController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/leader/refund/count` | 退款订单数量 | **gid** (Long); **pid** (Long) |
| 2 | POST | `/order/leader/refund/approve` | 售后订单审核（同意/不同意） | Body: **approveRequest** (OrderApproveRequest, JSON) |

**OrderRefundNotifyController**（`cn.com.shopgroup.order.controller.leader`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/order/leader/refund/notify` | 退款结果回调通知(占位接口) | 无 |

**OrderPaymentController**（`cn.com.shopgroup.order.controller.payment`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/order/payment/order/pay` | 发起支付 | **orderNo** (String); **openid** (String) |
| 2 | POST | `/order/payment/order/notify` | 支付回调 | 无 |

#### 8.2.3 gb-group-goods（商品/团购）— 21 个接口

**GroupGoodsController**（`cn.com.shopgroup.goods.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/goods/group/goods/list` | 团购商品列表(包装, 规格, sku) | **lid** (Long); **groupId** (Long) |
| 2 | GET | `/goods/group/goods/stock` | 查询商品库存, 后期增加缓存 | **gid** (String) |

**LeaderGoodsManageController**（`cn.com.shopgroup.goods.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/goods/get/goods/cat` | 商品分类列表 | 无 |
| 2 | GET | `/goods/leader/goods/online` | 查询所有审核通过且未关闭的商品, 添加团购时使用 | 无 |
| 3 | GET | `/goods/leader/goods/list` | 分页查询团长下的所有商品(团长控制台-商品管理), 支持分类+商品名称关键字筛选 | **cat** (Long); **keyword** (String); **page** (int); **pageSize** (int) |
| 4 | GET | `/goods/leader/goods/count` | 查询团长下的所有商品数量, 支持分类+商品名称关键字筛选 | **cat** (Long); **keyword** (String) |
| 5 | GET | `/goods/leader/goods/info` | 商品信息查询: 商品基本信息 + 分类名 + 图片 + 规格(含规格值) + SKU | **id** (Long) |
| 6 | POST | `/goods/leader/goods/addGoods` | 添加商品 | Body: **request** (LeaderAddGoodsRequest, JSON) |
| 7 | POST | `/goods/leader/goods/edit` | 修改商品, 如果该商品正在团购中, 则不允许修改 | Body: **request** (LeaderGoodsRequest, JSON) |
| 8 | GET | `/goods/leader/goods/close` | 关闭商品(上下架), 如果该商品正在团购中, 则不允许操作 | **id** (Long) |
| 9 | GET | `/goods/leader/goods/sku/spec` | 根据规格罗列所有SKU, 包括已经存在的sku信息 | **id** (Long) |
| 10 | POST | `/goods/leader/goods/sku/save` | 注: 前端不再单独调用此接口, SKU已随添加/修改商品接口(addGoods/edit)一并处理, 此处保留兼容 | Body: **requestList** (List<LeaderSkuRequest>, JSON) |

**LeaderGroupManageController**（`cn.com.shopgroup.goods.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/goods/Leader/get/groupActivity/list` | 查询所有团购活动列表 | Body: **request** (LeaderGroupListRequest, JSON) |
| 2 | GET | `/goods/Leader/get/groupActivity/count` | 查询所有团购活动总数(筛选条件与列表接口一致, 保证分页总页数正确) | **cat** (Long); **name** (String); **status** (Integer) |
| 3 | POST | `/goods/Leader/groupActivity/add` | 添加团购活动 | Body: **request** (GroupActRequest, JSON) |
| 4 | GET | `/goods/Leader/get/groupActivity/info` | 查询团购信息, 还要查询商品列表(价格以团购商品表冗余的团购价为准) | **groupId** (Long) |
| 5 | POST | `/goods/Leader/groupActivity/edit` | 修改团购活动, 团购进行中, 不允许修改 | Body: **request** (GroupActRequest, JSON) |
| 6 | POST | `/goods/Leader/groupActivity/close` | 这样就不需要修改的时候同步Redis缓存了, 只需要关闭修改完, 打开上线的时候更新一次即可 | **groupId** (Long) |
| 7 | GET | `/goods/Leader/get/groupActivity/cat` | 团购分类列表 | 无 |
| 8 | POST | `/goods/Leader/share/groupActivity/poster` | 分享团购海报生成1 | **groupId** (Long) |
| 9 | POST | `/goods/Leader/share/groupActivity/make/poster` | 分享团购活动海报（带有logo的海报） | **groupId** (Long) |

#### 8.2.4 gb-group-admin（后台管理）— 25 个接口

**AdminBusinessController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/business/list` | 分页查询团长收款账户列表(含累计额度) | **id** (Long); **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/business/count` | 查询收款账户总数 | 无 |
| 3 | GET | `/admin/business/info` | 查询收款账户详情 | **id** (Long) |
| 4 | POST | `/admin/business/add` | 添加团长收款账户(商户编号需唯一) | Body: **request** (LeaderBusinessRequest, JSON) |

**AdminGoodsController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/goods/list` | 分页查询商品列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/goods/count` | 查询商品总数 | 无 |
| 3 | GET | `/admin/goods/info` | 查询商品详情 | **id** (Long) |
| 4 | GET | `/admin/goods/img` | 查询商品缩略图列表(最多3张) | **id** (Long) |

**AdminGroupController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/group/list` | 分页查询团购活动列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/group/count` | 查询团购活动总数 | 无 |
| 3 | GET | `/admin/group/info` | 查询团购活动详情 | **id** (Long) |

**AdminLeaderController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/leader/list` | 分页查询团长列表(可按手机号筛选) | **mobile** (String); **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/leader/count` | 查询团长总数 | 无 |
| 3 | POST | `/admin/leader/add` | 添加团长(校验手机号/商户编号, 同步创建员工/店铺/收款账户) | Body: **request** (LeaderRequest, JSON) |
| 4 | GET | `/admin/leader/select` | 团长下拉选项列表(id/名称) | 无 |

**AdminLoginController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/login/kaptcha` | 获取后台登录图形验证码(Base64图片, 5分钟有效) | 无 |
| 2 | POST | `/admin/login/submit` | 后台登录(验证码+账号密码, 返回token) | Body: **request** (LoginRequest, JSON) |

**AdminMemberController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/member/list` | 分页查询会员列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/member/count` | 查询会员总数 | 无 |

**AdminOrderBusinessController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/orderbusiness/list` | 分页查询订单列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/orderbusiness/count` | 查询订单总数 | 无 |

**AdminOrderController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/order/list` | 分页查询订单列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/order/count` | 查询订单总数 | 无 |

**AdminReportController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/admin/report/list` | 分页查询订单列表 | **page** (int); **pageSize** (int) |
| 2 | GET | `/admin/report/count` | 查询订单总数 | 无 |

#### 8.2.5 gb-group-task（定时任务）— 8 个接口

**TaskController**（`cn.com.shopgroup.controller`）

| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/task/order/send` | 微信订单发货 | **orderNo** (String) |
| 2 | GET | `/task/order/divide` | 订单分账 | **orderNo** (String) |
| 3 | GET | `/task/order/query` | 查询订单 | **orderNo** (String) |
| 4 | GET | `/task/order/refund` | 同步原始订单表和商户订单表的退款状态 | **orderNo** (String) |
| 5 | GET | `/task/order/cash` | 提现 | **id** (int); **val** (int) |
| 6 | GET | `/task/order/verify` | 自动收货 | 无 |
| 7 | GET | `/task/order/backstock` | 超时未支付订单自动取消并恢复库存(手动触发, 与定时任务同一套逻辑) | 无 |
| 8 | GET | `/task/test/user` | 查询文章信息(联调测试接口) | **aId** (Long) |


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

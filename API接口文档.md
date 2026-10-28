# ShopGroupBuyApi 接口文档（详细版）

> 自动生成时间：2026-10-29 00:13:09

> 生成方式：扫描各模块 `*Controller.java` 源码（`python3 generate_api_doc.py` 可重新生成）

> 请求头公共参数：`token`（用户登录令牌）、`lid`（团长id）、`sid`（员工id），实际以各接口校验为准

> 参数约定：Query/Header 参数「必填」默认「是」（`@RequestParam` 默认必填，标注 `required=false` 则为「否」）；Body 请求对象各字段的「必填」取自字段校验注解（`@NotNull` 等），未注解时以实际逻辑为准

> 分页约定：列表类接口一般通过 `page`（页码，从 1 开始）/ `pageSize`（每页条数）分页，配套 `count` 接口获取总数

> 统一出参：所有接口返回 `JsonResult`（`code` 状态码 / `msg` 提示信息 / `data` 业务数据），`data` 字段说明见各接口；嵌套对象字段以 `→` 前缀递进展开

> 金额约定：接口返回的金额字段统一为 `Double`，单位「元」（最多 2 位小数）；字段名含 `fee` / `amount` / `price` / `money` / `balance` 的均为金额。数据库实体（`model` 包）内部仍以「分」存储，由 Response 层经 `MoneyUtil.centToYuan` 转换后返回，接口不暴露「分」

> 功能说明：优先取源码方法注释（Javadoc 或紧邻方法的 `//` 行注释）；源码无注释时由脚本依据路径、入参与出参自动推断（显示为 *斜体*，仅供参考）；入参「位置」为 Query 参数 / Body（`@RequestBody` 对象）/ 请求头

## 金额字段变更记录

> 接口金额字段已统一为「元」（`Double`）。下表为历史调整项：字段名保持不变，但数值语义或类型可能变化（如原「分」现值「元」，相差 100 倍），接入方需同步调整解析逻辑。

| 日期 | 接口 | 方式 | 受影响字段 | 变更 |
| --- | --- | --- | --- | --- |
| 2026-09-16 | `/order/orderbusiness/list` | GET | orderFee、receivedFee、busFee、serviceFee、otherFee | 分 → 元 |
| 2026-09-16 | `/admin/orderbusiness/list` | GET | orderFee、receivedFee、busFee、serviceFee、otherFee | 分 → 元 |
| 2026-09-16 | `/admin/order/list` | GET | payFee、refundFee | 分 → 元 |
| 2026-09-16 | `/order/group/order/refund/recodes` | GET | refundFee | 原固定返回 `null`，现按「分 → 元」返回 |
| 2026-09-16 | `/order/leader/myMember/list` | POST | consumeAmount | `String` → `Double`（元） |
| 2026-09-16 | `/order/leader/myMember/detail` | GET | consumeAmount、refundAmount | `String` → `Double`（元） |

## 接口行为变更记录

> 接口字段、返回语义（非金额类）的行为变更记录，接入方需关注。

| 日期 | 接口 | 方式 | 变更 |
| --- | --- | --- | --- |
| 2026-10-02 | `/order/group/order/list` | POST | status 不传（查全部）时不再返回已取消(6)的订单；需查看已取消订单须显式传 status=6 |
| 2026-10-02 | `/order/leader/order/list` | POST | 同上（status 不传时过滤已取消订单, 显式传 6 可查） |
| 2026-10-03 | `/admin/business/list` | GET | busBalance 数据源由 Redis 月度缓存改为 gb_leader_merchant_monthly_amount 表，恒为当月累计收款（元）；原 Redis 缓存跨月清零/30 天过期导致的归零问题不再出现 |
| 2026-10-27 | `/task/order/send` | GET | 微信发货录入的商品描述（goodsName）由团购名称改为：该订单涉及的所有商品名称按商品行顺序去重后逗号拼接；订单无商品行时仍兜底使用团购名称 |
| 2026-10-27 | `/order/leader/order/send` | GET | 同上（团长端手动发货场景，微信发货商品描述取订单商品名称拼接） |
| 2026-10-27 | `/order/group/order/confirmShipping` | POST | 同上（确认发货 / 部分核销触发的微信发货，商品描述取订单商品名称拼接） |
| 2026-10-27 | `/order/leader/order/writeOff` | POST | 同上（核销时补调用微信发货，商品描述取订单商品名称拼接） |
| 2026-10-27 | `/order/payment/order/notify` | POST | 同上（支付回调延迟 10 分钟触发的微信发货，商品描述取订单商品名称拼接） |
| 2026-10-27 | `/goods/leader/activity/goods/close` | GET | 团购活动内商品上下架：切换状态改为按团购商品行自身 is_close 取反（修复原逻辑取商品表状态导致恒为下架、无法重新上架）；商品表已下架返回 300204、活动不存在或不属于当前团长返回 300203，未实际更新时不再返回成功（原为 500 空指针） |
| 2026-10-28 | `/goods/group/goods/list` | GET | 团购活动内被团长下架的商品（团购商品行 is_close=1）不再返回；团长在团购活动内重新上架后，上下架接口会清除该团购的商品列表缓存，C端即时恢复展示并可购买 |
| 2026-10-28 | `/order/member/groupActivity/list` | POST | 团购活动列表回填的商品同步过滤团购内已下架商品（与 C端团购商品列表口径一致，含商品表已下线商品） |
| 2026-10-28 | `/order/group/add` | POST | 下单新增团购活动内上下架校验：该商品在当前团购活动内被下架（或不属于该团购）时拒绝下单，返回「xx已下架, 无法购买」（原仅校验商品表状态，用户端缓存的旧列表可绕过） |
| 2026-10-28 | `/goods/Leader/get/groupActivity/list` | POST | 团长端团购活动列表的商品改为返回全部状态（含团购活动内已下架、商品表已下线的商品），便于团长查看已下架商品并重新上架；团购详情 /goods/Leader/get/groupActivity/info 同步补齐已下线商品的库存/单位 |
| 2026-10-29 | `/order/member/groupActivity/list` | POST | viewCount（查看人数）展示规则调整：真实查看人数 ≥ 跟团人数×1.6 时展示真实查看人数，否则展示跟团人数×1.6（向下取整）；原规则为「查看人数不低于订单数时取查看人数，否则取订单数」 |
| 2026-10-29 | `/order/group/groupActivity/info` | GET | 同上：团购详情 viewCount 与列表接口统一为「≥ 跟团人数×1.6 展示真实值，否则展示 1.6 倍」 |
| 2026-10-29 | `/order/group/groupActivity/logs` | GET | 跟团记录调整：① 相邻两条记录时间间隔保证大于 30 秒（虚拟记录按 31~180 秒随机生成时间）；② 跟团数 > 10 且存在真实订单时，真实订单记录与虚拟记录合并后随机打乱展示 |
| 2026-10-29 | `/order/group/groupActivity/logs2` | GET | 滚动跟团记录的时间由固定「刚刚」改为按时间戳生成（相邻间隔随机 31~180 秒，保证 >30 秒） |

## 近期变更

> 以下为最近 2 条与接口定义相关的提交（来源 `git log`, 由 `generate_api_doc.py` 自动生成）；
> 完整变更请查阅 git 提交记录。

| 日期 | 提交 | 摘要 | 涉及文件 |
| --- | --- | --- | --- |
| 2026-10-28 | `c3228c0` | 二期开发 | ApplyLeaderController.java, LeaderGoodsManageController.java, LeaderGroupManageController.java, LeaderUpdateSkuStockRequest.java, MemberGroupController.java |
| 2026-10-27 | `620883e` | 二期开发 | ApplyLeaderController.java, ApplyLeaderRequest.java, GroupActGoodsResponse.java, LeaderGoodsManageController.java, LeaderGroupManageController.java 等 |

## 目录

1. [**gb-group-user**（用户/团长/员工）](#1-gb-group-user用户团长员工) — 53 个接口
2. [**gb-group-order**（订单/退款/分账）](#2-gb-group-order订单退款分账) — 49 个接口
3. [**gb-group-goods**（商品/团购）](#3-gb-group-goods商品团购) — 25 个接口
4. [**gb-group-admin**（后台管理）](#4-gb-group-admin后台管理) — 33 个接口
5. [**gb-group-task**（定时任务）](#5-gb-group-task定时任务) — 8 个接口

---

## 接口总览索引

> 共 168 个接口，按下表序号定位到下方各模块接口详情；「功能说明」列取自源码方法注释，超过 60 字在本表中截断显示（完整说明见各接口详情的「功能说明」），列值为 `—` 表示源码无方法注释，可按入参 / 出参字段推断用途。

| 序号 | 模块 | 方式 | 路径 | 功能说明 |
| --- | --- | --- | --- | --- |
| [1](#1-post-userimageuploadavatar) | user | POST | `/user/image/upload/avatar` | 上传用户头像 |
| [2](#2-post-userimageuploadgoods) | user | POST | `/user/image/upload/goods` | 上传商品图片(尺寸400*400) |
| [3](#3-post-userimageuploadbanner) | user | POST | `/user/image/upload/banner` | 上传店铺banner(尺寸750*250) |
| [4](#4-get-userimageaccessfile) | user | GET | `/user/image/access/{*file}` | 查看私有图片 |
| [5](#5-get-usergroupblack) | user | GET | `/user/group/black` | 登录查询是否是（leaderId）团长下的黑名单 |
| [6](#6-get-usergrouppoint) | user | GET | `/user/group/point` | 自提点列表 |
| [7](#7-get-userarticleinfo) | user | GET | `/user/article/info` | 文章详情 |
| [8](#8-get-userfocus) | user | GET | `/user/focus` | 轮播图列表 |
| [9](#9-get-userleadermembermobile) | user | GET | `/user/leader/member/mobile` | 根据手机号查询用户 |
| [10](#10-post-userleaderaddblack) | user | POST | `/user/leader/add/black` | 加入黑名单 |
| [11](#11-get-userleaderblackmobile) | user | GET | `/user/leader/black/mobile` | 根据手机号查询黑名单用户 |
| [12](#12-post-userleadermemberblackremove) | user | POST | `/user/leader/member/black/remove` | 解除黑名单 |
| [13](#13-get-userleaderblacklist) | user | GET | `/user/leader/black/list` | 黑名单列表 |
| [14](#14-get-userleaderblackcount) | user | GET | `/user/leader/black/count` | 黑名单总数 |
| [15](#15-get-userleaderbusinesslist) | user | GET | `/user/leader/business/list` | 查询当前团长收款账户列表（含Redis累计额度） |
| [16](#16-post-userleaderbusinessadd) | user | POST | `/user/leader/business/add` | 添加团长收款账户（校验证件号码唯一，返回新增账户ID） |
| [17](#17-post-userleaderbusinessedit) | user | POST | `/user/leader/business/edit` | 修改收款账户（已审核通过的账户不允许修改） |
| [18](#18-post-userleaderbusinessclose) | user | POST | `/user/leader/business/close` | 关闭/启用收款账户（禁用或启用商户收款） |
| [19](#19-get-userleaderpointlist) | user | GET | `/user/leader/point/list` | 团长端-查询提货点列表 |
| [20](#20-get-userleaderpointaddgrouplist) | user | GET | `/user/leader/point/addGroup/list` | 添加团购活动时，调用查询提货点列表 |
| [21](#21-post-userleaderpointadd) | user | POST | `/user/leader/point/add` | 添加提货点 |
| [22](#22-post-userleaderpointedit) | user | POST | `/user/leader/point/edit` | 修改提货点 |
| [23](#23-get-userleaderpointclose) | user | GET | `/user/leader/point/close` | 作废/恢复 提货点 |
| [24](#24-get-userleaderpointinfo) | user | GET | `/user/leader/point/info` | 查询提货点详情 |
| [25](#25-get-userleaderpointercode) | user | GET | `/user/leader/point/ercode` | 提货点二维码 |
| [26](#26-get-userleadershopinfo) | user | GET | `/user/leader/shop/info` | 查看店铺信息 |
| [27](#27-post-userleadershopsave) | user | POST | `/user/leader/shop/save` | 修改/保存-店铺信息 |
| [28](#28-post-userleadershopupdate) | user | POST | `/user/leader/shop/update` | 更新店铺码图片地址（保存店铺二维码上传后的访问URL） |
| [29](#29-get-userleadergetgroupshop) | user | GET | `/user/leader/getGroup/shop` | 通过leaderId团长店铺详情 |
| [30](#30-post-userleadershopmakeqrcode) | user | POST | `/user/leader/shop/makeQrCode` | 团长-我的店铺小程序码(微信小程序码,page=pages/order/index,scene=shopId=xx),上… |
| [31](#31-get-userleadermessagelist) | user | GET | `/user/leader/message/list` | 消息列表(团长查看所有消息) |
| [32](#32-get-userleadermessagecount) | user | GET | `/user/leader/message/count` | 消息列表总数量(团长查看所有消息) |
| [33](#33-get-userleadermessageunread) | user | GET | `/user/leader/message/unread` | 未读消息总数量(团长查看所有消息) |
| [34](#34-get-userleadermessageread) | user | GET | `/user/leader/message/read` | 阅读消息 |
| [35](#35-get-userleaderstafflist) | user | GET | `/user/leader/staff/list` | 查询当前团长员工列表(不含超级团长) |
| [36](#36-post-userleaderstaffadd) | user | POST | `/user/leader/staff/add` | 添加员工(校验用户存在且未绑定, 绑定提货点) |
| [37](#37-post-userleaderstaffedit) | user | POST | `/user/leader/staff/edit` | 修改员工信息(支持更换员工及提货点) |
| [38](#38-post-userleaderstaffclose) | user | POST | `/user/leader/staff/close` | 关闭/启用员工账号(不能操作自己) |
| [39](#39-post-userleaderstaffremove) | user | POST | `/user/leader/staff/remove` | 删除员工(不能删除自己) |
| [40](#40-post-userapplybecomeleader) | user | POST | `/user/apply/become/leader` | 申请团长(会员端提交申请记录) |
| [41](#41-get-userphone) | user | GET | `/user/phone` | 根据code获取手机号 |
| [42](#42-get-useropenid) | user | GET | `/user/openid` | 根据code获取openid |
| [43](#43-get-userlogin) | user | GET | `/user/login` | openid自动登录 |
| [44](#44-post-userreg) | user | POST | `/user/reg` | 注册新用户 |
| [45](#45-post-userlogout) | user | POST | `/user/logout` | 退出登录(小程序调用): 清除服务端登录态, 小程序端需同步删除本地token |
| [46](#46-post-usermemberadviseadd) | user | POST | `/user/member/advise/add` | 提交建议反馈 |
| [47](#47-post-usermemberadviseedit) | user | POST | `/user/member/advise/edit` | 修改建议反馈 |
| [48](#48-post-usermemberadvisedel) | user | POST | `/user/member/advise/del` | 删除建议反馈 |
| [49](#49-get-usermemberadviseinfo) | user | GET | `/user/member/advise/info` | 建议反馈详情 |
| [50](#50-get-usermemberadviselist) | user | GET | `/user/member/advise/list` | 我的建议反馈列表 |
| [51](#51-get-usermemberinfo) | user | GET | `/user/member/info` | 根据 token 获取用户信息 |
| [52](#52-get-usermemberisleader) | user | GET | `/user/member/isleader` | 是否团长身份 |
| [53](#53-post-usermemberswapgetinfo) | user | POST | `/user/member/swap/getInfo` | 登录用户获取最新团长id |
| [54](#54-get-orderorderbusinesslist) | order | GET | `/order/orderbusiness/list` | 分页查询订单列表 |
| [55](#55-get-orderorderbusinesscount) | order | GET | `/order/orderbusiness/count` | 查询订单总数 |
| [56](#56-post-ordergroupadd) | order | POST | `/order/group/add` | 用户下单: 下单前校验商品/SKU库存与限购(限购商品按「当前用户 + 当前团购活动 + 该商品」维度校验, 该团购活动… |
| [57](#57-get-ordergroupgroupactivitycat) | order | GET | `/order/group/groupActivity/cat` | 团购分类列表（首页） |
| [58](#58-post-ordermembergroupactivitylist) | order | POST | `/order/member/groupActivity/list` | 用户查询所有在线的团购活动列表[新用户未绑定团长时leaderId=0]: 仅返回未下线(isClose=0)且当前时间… |
| [59](#59-get-ordergroupgroupactivityinfo) | order | GET | `/order/group/groupActivity/info` | 团购数量（首页） 团购详情(团长分享页面)---用户首页：团长更多好货也用 |
| [60](#60-post-ordergroupgroupactivityview) | order | POST | `/order/group/groupActivity/view` | 用户查看团购详情-显式埋点上报(分享等场景前端调用; 首页进入详情会自动埋点, 可不上报) |
| [61](#61-get-ordergroupgroupactivityshop) | order | GET | `/order/group/groupActivity/shop` | 团长店铺详情 |
| [62](#62-get-ordergroupgroupactivitylogs) | order | GET | `/order/group/groupActivity/logs` | 团购记录(跟团记录) 早上6点到晚上8点之间，每个小时生成一批跟团记录（固定部分+滚动部分）， 固定部分打开页面就显示，… |
| [63](#63-get-ordergroupgroupactivitylogs2) | order | GET | `/order/group/groupActivity/logs2` | 团购记录(跟团记录), 滚动部分 |
| [64](#64-get-ordergrouporderrecords) | order | GET | `/order/group/order/records` | 真实跟团记录：基于支付成功订单数据 |
| [65](#65-post-ordergrouporderlist) | order | POST | `/order/group/order/list` | 用户订单列表（按订单状态/商品名称筛选, 分页查询） |
| [66](#66-post-ordergrouporderapplyrefundlist) | order | POST | `/order/group/order/applyRefundList` | 用户售后订单列表(支持按商品名称/售后审核状态筛选; 同一订单的商品可能处于不同售后状态, 按(订单,审核状态)拆分为多… |
| [67](#67-get-ordergroupordercount) | order | GET | `/order/group/order/count` | 用户订单数量 |
| [68](#68-get-ordergrouporderinfo) | order | GET | `/order/group/order/info` | 用户订单详情 |
| [69](#69-get-ordergroupordermakeercode) | order | GET | `/order/group/order/makeErcode` | 用户订单小程序码(微信小程序码, 扫码进入C端小程序对应订单页面) |
| [70](#70-get-ordergrouporderreceipt) | order | GET | `/order/group/order/receipt` | 用户扫码核销-整单核销(GET): 核销订单全部剩余可核销商品(购买数-已核销-已退) |
| [71](#71-post-ordergrouporderpartreceipt) | order | POST | `/order/group/order/part/receipt` | 用户扫码核销-整单/部分核销(POST): goodsList 为空 = 整单核销(等同 GET /group/orde… |
| [72](#72-post-ordergrouporderapplyrefund) | order | POST | `/order/group/order/apply/refund` | 用户申请订单退款。refundFlag: 1=退款(退"待收货"部分, 可退量=购买数-收货数-已申请退款数), 2=退… |
| [73](#73-get-ordergrouporderrefundreasonlist) | order | GET | `/order/group/order/refund/reasonList` | 用户退款原因下拉列表(申请退款时"选择退款原因") |
| [74](#74-get-ordergrouporderrefundrecodes) | order | GET | `/order/group/order/refund/recodes` | 售后记录查询 |
| [75](#75-get-ordergroupordernotallreceiptlist) | order | GET | `/order/group/order/notAllReceiptList` | 用户端-查询还有商品未全部收货的订单列表(用户id+团长id查询, 状态1待收货/2部分收货/5售后; 5售后时商品行还… |
| [76](#76-post-ordergrouporderconfirmshipping) | order | POST | `/order/group/order/confirmShipping` | 用户点击确认收货组件后调用接口，更新订单已经操作按钮 |
| [77](#77-post-ordergrouporderapplyrefundorderinfo) | order | POST | `/order/group/order/applyRefund/orderInfo` | 用户点击申请退货后调用接口 |
| [78](#78-get-ordergroupwxorder) | order | GET | `/order/group/wx/order` | 查询微信订单发货状态（查询订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退… |
| [79](#79-post-orderleaderbilllist) | order | POST | `/order/leader/bill/list` | 团长端-对账单 |
| [80](#80-get-orderleaderactivitylist) | order | GET | `/order/leader/activity/list` | 团长端-团购活动查询  提供团长端活动下拉/选择所需的接口；活动主数据在商品域(goods), 本控制器通过 goods… |
| [81](#81-post-orderleadermymemberlist) | order | POST | `/order/leader/myMember/list` | 我的团员列表（支持手机号/昵称搜索，分页） |
| [82](#82-get-orderleadermymemberdetail) | order | GET | `/order/leader/myMember/detail` | 团员详情（消费/退款/跟团次数/查看次数/动态） |
| [83](#83-post-orderleaderorderlist) | order | POST | `/order/leader/order/list` | 团长订单列表（按团活动/订单状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询） 团购详情调用必须传groupI… |
| [84](#84-post-orderleaderapplyrefundlist) | order | POST | `/order/leader/apply/refundList` | 团长售后订单列表（按团活动/审核状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询） |
| [85](#85-get-orderleaderordercount) | order | GET | `/order/leader/order/count` | 查询订单总数(待核销), 考虑提货点 |
| [86](#86-get-orderleaderorderstatus) | order | GET | `/order/leader/order/status` | 查询订单状态数量 |
| [87](#87-post-orderleaderorderscanqrcode) | order | POST | `/order/leader/order/scanQRCode` | 团长扫用户订单码接口 |
| [88](#88-get-orderleaderorderquery) | order | GET | `/order/leader/order/query` | 根据订单号查询订单 |
| [89](#89-post-orderleaderorderwriteoff) | order | POST | `/order/leader/order/writeOff` | 核销（整单核销） |
| [90](#90-post-orderleaderorderpartwriteoff) | order | POST | `/order/leader/order/partWriteOff` | 部分核销订单 |
| [91](#91-get-orderleaderordersend) | order | GET | `/order/leader/order/send` | 团长端-查询微信发货 |
| [92](#92-get-orderleaderhomeshoworders) | order | GET | `/order/leader/home/show/orders` | 团长首页订单汇总(head部分): 返回有效订单总数/订单总金额/退款总金额;groupId必传， pointId 传… |
| [93](#93-get-orderleaderhomeordergoodssummary) | order | GET | `/order/leader/home/order/goodsSummary` | 团长端订单-商品统计: 返回订单商品总件数/待核销总件数 + 每个商品的总件数/待核销件数, 支持按团购活动groupI… |
| [94](#94-post-ordergetgroupactivitytotalorder) | order | POST | `/order/get/groupActivity/totalOrder` | 根据团购活动id统计订单数（实时统计，团长端有需求时使用） |
| [95](#95-get-orderleaderrefundcount) | order | GET | `/order/leader/refund/count` | 退款订单数量: 订单中存在商品发生过退款(部分退/整单全退, 审核同意)即计入 |
| [96](#96-post-orderleaderrefundapplylist) | order | POST | `/order/leader/refund/applyList` | ------暂时废弃 批量查询用户退款申请数据(团长端): 把用户申请退的数据按订单+商品行结构化展示, 只查"待审核"… |
| [97](#97-post-orderleaderapprovelist) | order | POST | `/order/leader/approve/list` | 团长端-批量退款列表: 查询指定团购下"存在待待核销商品申请的订单, 支持按商品名称模糊搜索, 分页返回订单+该笔申请的… |
| [98](#98-post-orderleadergetrefundcount) | order | POST | `/order/leader/get/refund/count` | 团长端-团购订单上面展示售后订单数 |
| [99](#99-post-orderleaderrefundapprove) | order | POST | `/order/leader/refund/approve` | 售后订单审核（同意/不同意）。status: 1=同意, 2=不同意; 每单一行key=订单号, value.refun… |
| [100](#100-post-orderleaderrefundnotify) | order | POST | `/order/leader/refund/notify` | 退款结果回调通知: 易宝退款最终结果异步通知(与支付回调一致, 为 RSA2048 数字信封加密的 form 报文) |
| [101](#101-get-orderpaymentorderpay) | order | GET | `/order/payment/order/pay` | 发起支付 |
| [102](#102-post-orderpaymentordernotify) | order | POST | `/order/payment/order/notify` | 支付回调 |
| [103](#103-get-goodsgroupgoodslist) | goods | GET | `/goods/group/goods/list` | 团购商品列表(包装, 规格, sku) |
| [104](#104-get-goodsgroupgoodsstock) | goods | GET | `/goods/group/goods/stock` | 查询商品库存, 后期增加缓存 |
| [105](#105-get-goodsgetgoodscat) | goods | GET | `/goods/get/goods/cat` | 商品分类列表 |
| [106](#106-get-goodsleadergoodsonline) | goods | GET | `/goods/leader/goods/online` | 查询所有审核通过且未关闭的商品, 添加团购时使用 |
| [107](#107-get-goodsleadergoodslist) | goods | GET | `/goods/leader/goods/list` | 分页查询团长下的所有商品(团长控制台-商品管理), 支持分类+商品名称关键字筛选 |
| [108](#108-get-goodsleadergoodscount) | goods | GET | `/goods/leader/goods/count` | 查询团长下的所有商品数量, 支持分类+商品名称关键字筛选 |
| [109](#109-get-goodsleadergoodsinfo) | goods | GET | `/goods/leader/goods/info` | 商品信息查询: 商品基本信息 + 分类名 + 图片 + 规格(含规格值) + SKU |
| [110](#110-post-goodsleadergoodsaddgoods) | goods | POST | `/goods/leader/goods/addGoods` | 添加商品 |
| [111](#111-post-goodsleadergoodsedit) | goods | POST | `/goods/leader/goods/edit` | 修改商品 |
| [112](#112-get-goodsleadergoodsclose) | goods | GET | `/goods/leader/goods/close` | 关闭商品(上下架), 如果该商品正在团购中, 则不允许操作 |
| [113](#113-get-goodsleadergoodsskuspec) | goods | GET | `/goods/leader/goods/sku/spec` | 根据规格罗列所有SKU, 包括已经存在的sku信息 |
| [114](#114-post-goodsleadergoodsskusave) | goods | POST | `/goods/leader/goods/sku/save` | 批量保存商品SKU(先删除后添加), 如果该商品正在团购中, 则不允许修改 注: 前端不再单独调用此接口, SKU已随添… |
| [115](#115-post-goodsleaderupdateskustock) | goods | POST | `/goods/leader/update/sku/stock` | 团长端修改商品库存(按SKU维度修改为指定库存, 商品主表库存按各SKU库存之和同步) |
| [116](#116-get-goodsleaderactivitygoodsclose) | goods | GET | `/goods/leader/activity/goods/close` | 团购活动详情中-商品上下架(按团购商品行自身 is_close 取反切换; 商品表已下架或活动不属于当前团长时抛错) |
| [117](#117-post-goodsleadergetgroupactivitylist) | goods | POST | `/goods/Leader/get/groupActivity/list` | 团长端-查询所有团购活动列表<br>返回每个团购活动的订单汇总数据 `groupSummaryResponse`（实际收… |
| [118](#118-get-goodsleadergetgroupactivitycount) | goods | GET | `/goods/Leader/get/groupActivity/count` | 查询所有团购活动总数(筛选条件与列表接口一致, 保证分页总页数正确) |
| [119](#119-get-goodsleadergroupactivitytaglist) | goods | GET | `/goods/Leader/groupActivity/tag/list` | 团购标签下拉列表(添加/编辑团购活动时选择标签) |
| [120](#120-post-goodsleadergroupactivityadd) | goods | POST | `/goods/Leader/groupActivity/add` | 添加团购活动 |
| [121](#121-get-goodsleadergetgroupactivityinfo) | goods | GET | `/goods/Leader/get/groupActivity/info` | 查询团购信息, 还要查询商品列表(价格以团购商品表冗余的团购价为准) 返回跟团统计genTuanResponse(团员人… |
| [122](#122-post-goodsleadergroupactivityedit) | goods | POST | `/goods/Leader/groupActivity/edit` | 修改团购活动, 团购进行中, 不允许修改 |
| [123](#123-post-goodsleadergroupactivityclose) | goods | POST | `/goods/Leader/groupActivity/close` | 关闭团购活动, 平台审核未通过, 不允许打开上线 打开上线的话, 需要清空Redis缓存, 任何修改团购和商品信息, 都… |
| [124](#124-get-goodsleadergetgroupactivitycat) | goods | GET | `/goods/Leader/get/groupActivity/cat` | 团购分类列表 |
| [125](#125-post-goodsleadersharegroupactivityposter) | goods | POST | `/goods/Leader/share/groupActivity/poster` | 分享团购海报生成1 |
| [126](#126-post-goodsleadersharegroupactivitymakeposter) | goods | POST | `/goods/Leader/share/groupActivity/make/poster` | 分享团购活动海报（带有logo的海报） |
| [127](#127-post-goodsleadersharegroupactivitygoodsposter) | goods | POST | `/goods/Leader/share/groupActivity/goods/poster` | 团购端-分享团购-商品-海报生成 |
| [128](#128-get-adminadviselist) | admin | GET | `/admin/advise/list` | 建议反馈列表 |
| [129](#129-get-adminadvisecount) | admin | GET | `/admin/advise/count` | 建议反馈总数 |
| [130](#130-get-adminadviseinfo) | admin | GET | `/admin/advise/info` | 建议反馈详情 |
| [131](#131-post-adminadvisereply) | admin | POST | `/admin/advise/reply` | 官方回复建议反馈 |
| [132](#132-get-adminbusinesslist) | admin | GET | `/admin/business/list` | 分页查询团长收款账户列表(含累计额度) |
| [133](#133-get-adminbusinesscount) | admin | GET | `/admin/business/count` | 查询收款账户总数 |
| [134](#134-get-adminbusinessinfo) | admin | GET | `/admin/business/info` | 查询收款账户详情 |
| [135](#135-post-adminbusinessadd) | admin | POST | `/admin/business/add` | 添加团长收款账户(商户编号需唯一) |
| [136](#136-get-admingoodslist) | admin | GET | `/admin/goods/list` | 分页查询商品列表(含规格) |
| [137](#137-get-admingoodscount) | admin | GET | `/admin/goods/count` | 查询商品总数 |
| [138](#138-get-admingoodsinfo) | admin | GET | `/admin/goods/info` | 查询商品详情(含规格) |
| [139](#139-get-admingoodsimg) | admin | GET | `/admin/goods/img` | 查询商品缩略图列表(最多3张) |
| [140](#140-get-admingrouplist) | admin | GET | `/admin/group/list` | 分页查询团购活动列表 |
| [141](#141-get-admingroupcount) | admin | GET | `/admin/group/count` | 查询团购活动总数 |
| [142](#142-get-admingroupinfo) | admin | GET | `/admin/group/info` | 查询团购活动详情 |
| [143](#143-get-admintaglist) | admin | GET | `/admin/tag/list` | 分页查询标签列表(含停用, 名称模糊) |
| [144](#144-get-admintagcount) | admin | GET | `/admin/tag/count` | 标签总数(与列表筛选条件一致, 供分页) |
| [145](#145-get-admintaginfo) | admin | GET | `/admin/tag/info` | 标签详情 |
| [146](#146-post-admintagadd) | admin | POST | `/admin/tag/add` | 新增标签 |
| [147](#147-post-admintagedit) | admin | POST | `/admin/tag/edit` | 编辑标签(可修改名称/颜色/排序/状态启停, 未传字段保留原值) |
| [148](#148-post-admintagdelete) | admin | POST | `/admin/tag/delete` | 删除标签(被团购活动使用中禁止删除, 可改为停用) |
| [149](#149-get-adminleaderlist) | admin | GET | `/admin/leader/list` | 分页查询团长列表(可按手机号筛选) |
| [150](#150-get-adminleadercount) | admin | GET | `/admin/leader/count` | 查询团长总数 |
| [151](#151-post-adminleaderadd) | admin | POST | `/admin/leader/add` | 添加团长(校验手机号/商户编号, 同步创建员工/店铺/收款账户) |
| [152](#152-get-adminleaderselect) | admin | GET | `/admin/leader/select` | 团长下拉选项列表(id/名称) |
| [153](#153-get-adminloginkaptcha) | admin | GET | `/admin/login/kaptcha` | 获取后台登录图形验证码(Base64图片, 5分钟有效) |
| [154](#154-post-adminloginsubmit) | admin | POST | `/admin/login/submit` | 后台登录(验证码+账号密码, 返回token) |
| [155](#155-get-adminmemberlist) | admin | GET | `/admin/member/list` | 分页查询会员列表 |
| [156](#156-get-adminmembercount) | admin | GET | `/admin/member/count` | 查询会员总数 |
| [157](#157-get-adminorderbusinesslist) | admin | GET | `/admin/orderbusiness/list` | 分页查询订单列表 |
| [158](#158-get-adminorderbusinesscount) | admin | GET | `/admin/orderbusiness/count` | 查询订单总数 |
| [159](#159-get-adminorderlist) | admin | GET | `/admin/order/list` | 分页查询订单列表 |
| [160](#160-get-adminordercount) | admin | GET | `/admin/order/count` | 查询订单总数 |
| [161](#161-get-taskordersend) | task | GET | `/task/order/send` | 微信订单发货 |
| [162](#162-get-taskorderdivide) | task | GET | `/task/order/divide` | 订单分账 |
| [163](#163-get-taskorderquery) | task | GET | `/task/order/query` | 查询订单 |
| [164](#164-get-taskorderrefund) | task | GET | `/task/order/refund` | 分账前部分退款，存在问题，实际到款金额-退款金额-平台服务费=商户分账金额。 分账后部分退款的话，没有问题，子商户本来就… |
| [165](#165-get-taskordercash) | task | GET | `/task/order/cash` | 提现 |
| [166](#166-get-taskorderverify) | task | GET | `/task/order/verify` | 自动完成收货(手动触发, 与定时任务同一套逻辑): 已分账核销、核销满7天仍未完成收货的订单自动完成 |
| [167](#167-get-taskorderbackstock) | task | GET | `/task/order/backstock` | 超时未支付订单自动取消并恢复库存(手动触发, 与定时任务同一套逻辑) |
| [168](#168-get-tasktestuser) | task | GET | `/task/test/user` | 查询文章信息(联调测试接口) |

---


## 1. gb-group-user（用户/团长/员工）


### ImageSourceController

> 类路径：`cn.com.shopgroup.user.controller.ImageSourceController`

> 接口数量：4

#### 1. POST `/user/image/upload/avatar`

**功能说明**：上传用户头像

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 2. POST `/user/image/upload/goods`

**功能说明**：上传商品图片(尺寸400*400)

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 3. POST `/user/image/upload/banner`

**功能说明**：上传店铺banner(尺寸750*250)

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 4. GET `/user/image/access/{*file}`

**功能说明**：查看私有图片

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| file | `String` | Query 参数 | 是 | 上传文件 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### BlackController

> 类路径：`cn.com.shopgroup.user.controller.group.BlackController`

> 接口数量：1

#### 1. GET `/user/group/black`

**功能说明**：登录查询是否是（leaderId）团长下的黑名单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| lid | `Long` | Query 参数 | 是 | 团长 ID（<=0 或 null 时表示不过滤）（变量名 leaderId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`boolean`（具体字段见下方表格） |

data 类型：`boolean`（基本类型，无子字段）


### GroupPointController

> 类路径：`cn.com.shopgroup.user.controller.group.GroupPointController`

> 接口数量：1

#### 1. GET `/user/group/point`

**功能说明**：自提点列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 leaderId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<PointResponse>`（具体字段见下方表格） |

data 类型：`List<PointResponse>`（数组，元素类型 `PointResponse`，字段说明见下）

**PointResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pointId | `Long` | 否 | 自提点id |
| leaderId | `Long` | 否 | 团长id,外键 |
| pointName | `String` | 否 | 自提点名称 |
| pointAddress | `String` | 否 | 详细地址 |
| pointImg | `String` | 否 | 门头照片 |
| longitude | `Double` | 否 | 经度,精度为10米级 |
| latitude | `Double` | 否 | 纬度,精度为10米级 |
| pointScope | `Integer` | 否 | 自提范围,单位：公里 |
| pointInfo | `String` | 否 | 自提说明,给c端用户看的 |
| pointErcode | `String` | 否 | 小程序二维码,给c端用户扫码使用 |
| person | `String` | 否 | 联系人 |
| phone | `String` | 否 | 联系电话 |
| isClose | `Byte` | 否 | 是否禁用 |



### ShowsPageController

> 类路径：`cn.com.shopgroup.user.controller.group.ShowsPageController`

> 接口数量：2

#### 1. GET `/user/article/info`

**功能说明**：文章详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 articleId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`ArticleResponse`（具体字段见下方表格） |

data 类型：`ArticleResponse`（字段说明见下）

**ArticleResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| title | `String` | 否 | — |
| content | `String` | 否 | — |


#### 2. GET `/user/focus`

**功能说明**：轮播图列表

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`、`List<FocusResponse>`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）
data 类型：`List<FocusResponse>`（数组，元素类型 `FocusResponse`，字段说明见下）

**FocusResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| title | `String` | 否 | — |
| url | `String` | 否 | — |
| link | `String` | 否 | — |



### LeaderBlackController

> 类路径：`cn.com.shopgroup.user.controller.leader.LeaderBlackController`

> 接口数量：6

#### 1. GET `/user/leader/member/mobile`

**功能说明**：根据手机号查询用户

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| mobile | `String` | Query 参数 | 是 | 手机号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderMemberResponse`（具体字段见下方表格） |

data 类型：`LeaderMemberResponse`（字段说明见下）

**LeaderMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 用户id |
| mobile | `String` | 否 | 手机号码 |
| nickname | `String` | 否 | 微信昵称 |
| avatar | `String` | 否 | 微信头像 |


#### 2. POST `/user/leader/add/black`

**功能说明**：加入黑名单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| memberId | `Long` | Query 参数 | 是 | 会员（用户）ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 3. GET `/user/leader/black/mobile`

**功能说明**：根据手机号查询黑名单用户

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| mobile | `String` | Query 参数 | 是 | 手机号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderMemberResponse`（具体字段见下方表格） |

data 类型：`LeaderMemberResponse`（字段说明见下）

**LeaderMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 用户id |
| mobile | `String` | 否 | 手机号码 |
| nickname | `String` | 否 | 微信昵称 |
| avatar | `String` | 否 | 微信头像 |


#### 4. POST `/user/leader/member/black/remove`

**功能说明**：解除黑名单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| memberId | `Long` | Query 参数 | 是 | 会员（用户）ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 5. GET `/user/leader/black/list`

**功能说明**：黑名单列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderMemberResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderMemberResponse>`（数组，元素类型 `LeaderMemberResponse`，字段说明见下）

**LeaderMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 用户id |
| mobile | `String` | 否 | 手机号码 |
| nickname | `String` | 否 | 微信昵称 |
| avatar | `String` | 否 | 微信头像 |


#### 6. GET `/user/leader/black/count`

**功能说明**：黑名单总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）


### LeaderBusinessController

> 类路径：`cn.com.shopgroup.user.controller.leader.LeaderBusinessController`

> 接口数量：4

#### 1. GET `/user/leader/business/list`

**功能说明**：查询当前团长收款账户列表（含Redis累计额度）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<BusinessResponse>`（具体字段见下方表格） |

data 类型：`List<BusinessResponse>`（数组，元素类型 `BusinessResponse`，字段说明见下）

**BusinessResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 账户id |
| type | `Byte` | 否 | 账户类型, 1=一般企业2=小微企业3=个体户4=个人 |
| name | `String` | 否 | 账户名称 |
| legal | `String` | 否 | 法人姓名 |
| no | `String` | 否 | 身份证号 |
| front | `String` | 否 | 身份证正面 |
| back | `String` | 否 | 身份证反面 |
| tax | `Integer` | 否 | 纳税额度,单位：万（收款提醒） |
| balance | `Double` | 否 | 余额 |
| isClose | `Byte` | 否 | 是否禁用 |
| isCheck | `Byte` | 否 | 是否审核 |


#### 2. POST `/user/leader/business/add`

**功能说明**：添加团长收款账户（校验证件号码唯一，返回新增账户ID）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `BusinessRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**BusinessRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 账户id |
| type | `Byte` | 是 | 账户类型, 1=一般企业2=小微企业3=个体户4=个人 |
| name | `String` | 是 | 账户名称 |
| legal | `String` | 是 | 法人姓名 |
| no | `String` | 否 | 身份证号 |
| front | `String` | 否 | 身份证正面 |
| back | `String` | 否 | 身份证反面 |
| tax | `Integer` | 否 | 纳税额度,单位：万（收款提醒） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 3. POST `/user/leader/business/edit`

**功能说明**：修改收款账户（已审核通过的账户不允许修改）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `BusinessRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**BusinessRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 账户id |
| type | `Byte` | 是 | 账户类型, 1=一般企业2=小微企业3=个体户4=个人 |
| name | `String` | 是 | 账户名称 |
| legal | `String` | 是 | 法人姓名 |
| no | `String` | 否 | 身份证号 |
| front | `String` | 否 | 身份证正面 |
| back | `String` | 否 | 身份证反面 |
| tax | `Integer` | 否 | 纳税额度,单位：万（收款提醒） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 4. POST `/user/leader/business/close`

**功能说明**：关闭/启用收款账户（禁用或启用商户收款）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `OCBusinessRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**OCBusinessRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| busId | `Long` | 是 | 账户id |
| status | `Integer` | 是 | 0 启用 1 禁用 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）


### LeaderPointController

> 类路径：`cn.com.shopgroup.user.controller.leader.LeaderPointController`

> 接口数量：7

#### 1. GET `/user/leader/point/list`

**功能说明**：团长端-查询提货点列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| name | `String` | Query 参数 | 否 | 名称 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`PointResponse`、`List<PointResponse>`（具体字段见下方表格） |

data 类型：`PointResponse`（字段说明见下）

**PointResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pointId | `Long` | 否 | 自提点id |
| leaderId | `Long` | 否 | 团长id,外键 |
| pointName | `String` | 否 | 自提点名称 |
| pointAddress | `String` | 否 | 详细地址 |
| pointImg | `String` | 否 | 门头照片 |
| longitude | `Double` | 否 | 经度,精度为10米级 |
| latitude | `Double` | 否 | 纬度,精度为10米级 |
| pointScope | `Integer` | 否 | 自提范围,单位：公里 |
| pointInfo | `String` | 否 | 自提说明,给c端用户看的 |
| pointErcode | `String` | 否 | 小程序二维码,给c端用户扫码使用 |
| person | `String` | 否 | 联系人 |
| phone | `String` | 否 | 联系电话 |
| isClose | `Byte` | 否 | 是否禁用 |

data 类型：`List<PointResponse>`（数组，元素类型 `PointResponse`，字段说明见下）


#### 2. GET `/user/leader/point/addGroup/list`

**功能说明**：添加团购活动时，调用查询提货点列表

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<PointResponse>`（具体字段见下方表格） |

data 类型：`List<PointResponse>`（数组，元素类型 `PointResponse`，字段说明见下）

**PointResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pointId | `Long` | 否 | 自提点id |
| leaderId | `Long` | 否 | 团长id,外键 |
| pointName | `String` | 否 | 自提点名称 |
| pointAddress | `String` | 否 | 详细地址 |
| pointImg | `String` | 否 | 门头照片 |
| longitude | `Double` | 否 | 经度,精度为10米级 |
| latitude | `Double` | 否 | 纬度,精度为10米级 |
| pointScope | `Integer` | 否 | 自提范围,单位：公里 |
| pointInfo | `String` | 否 | 自提说明,给c端用户看的 |
| pointErcode | `String` | 否 | 小程序二维码,给c端用户扫码使用 |
| person | `String` | 否 | 联系人 |
| phone | `String` | 否 | 联系电话 |
| isClose | `Byte` | 否 | 是否禁用 |


#### 3. POST `/user/leader/point/add`

**功能说明**：添加提货点

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `PointRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**PointRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 自提点Id |
| name | `String` | 是 | 自提点名称 |
| address | `String` | 是 | 详细地址 |
| img | `String` | 否 | 门头照片 |
| lon | `Double` | 否 | 经度,精度为10米级 |
| lat | `Double` | 否 | 纬度,精度为10米级 |
| scope | `Integer` | 否 | 自提范围,单位：公里 |
| info | `String` | 否 | 自提说明,给c端用户看的 |
| person | `String` | 否 | 联系人 |
| phone | `String` | 否 | 联系电话 |
| leaderId | `Long` | 否 | 团长id |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 4. POST `/user/leader/point/edit`

**功能说明**：修改提货点

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `PointRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**PointRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 自提点Id |
| name | `String` | 是 | 自提点名称 |
| address | `String` | 是 | 详细地址 |
| img | `String` | 否 | 门头照片 |
| lon | `Double` | 否 | 经度,精度为10米级 |
| lat | `Double` | 否 | 纬度,精度为10米级 |
| scope | `Integer` | 否 | 自提范围,单位：公里 |
| info | `String` | 否 | 自提说明,给c端用户看的 |
| person | `String` | 否 | 联系人 |
| phone | `String` | 否 | 联系电话 |
| leaderId | `Long` | 否 | 团长id |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 5. GET `/user/leader/point/close`

**功能说明**：作废/恢复 提货点

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 6. GET `/user/leader/point/info`

**功能说明**：查询提货点详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pointId | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`PointResponse`（具体字段见下方表格） |

data 类型：`PointResponse`（字段说明见下）

**PointResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pointId | `Long` | 否 | 自提点id |
| leaderId | `Long` | 否 | 团长id,外键 |
| pointName | `String` | 否 | 自提点名称 |
| pointAddress | `String` | 否 | 详细地址 |
| pointImg | `String` | 否 | 门头照片 |
| longitude | `Double` | 否 | 经度,精度为10米级 |
| latitude | `Double` | 否 | 纬度,精度为10米级 |
| pointScope | `Integer` | 否 | 自提范围,单位：公里 |
| pointInfo | `String` | 否 | 自提说明,给c端用户看的 |
| pointErcode | `String` | 否 | 小程序二维码,给c端用户扫码使用 |
| person | `String` | 否 | 联系人 |
| phone | `String` | 否 | 联系电话 |
| isClose | `Byte` | 否 | 是否禁用 |


#### 7. GET `/user/leader/point/ercode`

**功能说明**：提货点二维码

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### LeaderShopController

> 模块说明：团长端-店铺管理

> 类路径：`cn.com.shopgroup.user.controller.leader.LeaderShopController`

> 接口数量：5

#### 1. GET `/user/leader/shop/info`

**功能说明**：查看店铺信息

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，以接口实际返回为准）

#### 2. POST `/user/leader/shop/save`

**功能说明**：修改/保存-店铺信息

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `ShopRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**ShopRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| shopId | `Long` | 是 | 店铺id不能为空 |
| name | `String` | 是 | 门店名称不能为空 |
| shortName | `String` | 否 | 店铺简称 |
| mobile | `String` | 是 | 联系电话不能为空 |
| banner | `String` | 否 | 门店banner |
| shopInfo | `String` | 否 | 店铺信息介绍 |
| shopLogo | `String` | 否 | 店铺logo |
| shopCodeUrl | `String` | 否 | 店铺二维码图,用户扫码查看待核销订单使用 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 3. POST `/user/leader/shop/update`

**功能说明**：更新店铺码图片地址（保存店铺二维码上传后的访问URL）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `ShopErCodeRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**ShopErCodeRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| shopId | `Long` | 是 | 店铺id不能为空 |
| shopUrl | `String` | 是 | 门店url |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 4. GET `/user/leader/getGroup/shop`

**功能说明**：通过leaderId团长店铺详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| leaderId | `Long` | Query 参数 | 是 | 团长 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，以接口实际返回为准）

#### 5. POST `/user/leader/shop/makeQrCode`

**功能说明**：团长-我的店铺小程序码(微信小程序码,page=pages/order/index,scene=shopId=xx),上传到服务器返回URL

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| shopId | `Long` | Query 参数 | 是 | 店铺 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### MessageController

> 类路径：`cn.com.shopgroup.user.controller.leader.MessageController`

> 接口数量：4

#### 1. GET `/user/leader/message/list`

**功能说明**：消息列表(团长查看所有消息)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| type | `Integer` | Query 参数 | 是 | 类型（含义见各接口说明）（变量名 msgType） |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<MessageResponse>`（具体字段见下方表格） |

data 类型：`List<MessageResponse>`（数组，元素类型 `MessageResponse`，字段说明见下）

**MessageResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 消息id |
| type | `Byte` | 否 | 消息类型,1=系统消息2=内部消息3=业务消息 |
| content | `String` | 否 | 消息内容 |
| read | `Byte` | 否 | 是否阅读 |
| time | `String` | 否 | 添加时间 |


#### 2. GET `/user/leader/message/count`

**功能说明**：消息列表总数量(团长查看所有消息)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| type | `Integer` | Query 参数 | 是 | 类型（含义见各接口说明）（变量名 msgType） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/user/leader/message/unread`

**功能说明**：未读消息总数量(团长查看所有消息)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| type | `Integer` | Query 参数 | 是 | 类型（含义见各接口说明）（变量名 msgType） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 4. GET `/user/leader/message/read`

**功能说明**：阅读消息

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 msgId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）


### StaffController

> 类路径：`cn.com.shopgroup.user.controller.leader.StaffController`

> 接口数量：5

#### 1. GET `/user/leader/staff/list`

**功能说明**：查询当前团长员工列表(不含超级团长)

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<StaffResponse>`（具体字段见下方表格） |

data 类型：`List<StaffResponse>`（数组，元素类型 `StaffResponse`，字段说明见下）

**StaffResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 员工id,主键自增 |
| name | `String` | 否 | 员工姓名 |
| mobile | `String` | 否 | 手机号码 |
| remark | `String` | 否 | 员工备注 |
| auth | `String` | 否 | 权限设置(逗号间隔) |
| isClose | `Byte` | 否 | 是否禁用 |
| point | `String` | 否 | 所属提货点(逗号间隔) |


#### 2. POST `/user/leader/staff/add`

**功能说明**：添加员工(校验用户存在且未绑定, 绑定提货点)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `StaffRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**StaffRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 员工Id (修改时候使用) |
| name | `String` | 否 | 员工姓名 |
| mobile | `String` | 是 | 手机号码 |
| remark | `String` | 否 | 员工备注 |
| auth | `String` | 否 | 权限设置(逗号间隔) |
| point | `String` | 否 | 所属提货点(逗号间隔) |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 3. POST `/user/leader/staff/edit`

**功能说明**：修改员工信息(支持更换员工及提货点)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `StaffRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**StaffRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 员工Id (修改时候使用) |
| name | `String` | 否 | 员工姓名 |
| mobile | `String` | 是 | 手机号码 |
| remark | `String` | 否 | 员工备注 |
| auth | `String` | 否 | 权限设置(逗号间隔) |
| point | `String` | 否 | 所属提货点(逗号间隔) |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 4. POST `/user/leader/staff/close`

**功能说明**：关闭/启用员工账号(不能操作自己)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 staffId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 5. POST `/user/leader/staff/remove`

**功能说明**：删除员工(不能删除自己)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| staffId | `Long` | Query 参数 | 是 | 员工 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### ApplyLeaderController

> 类路径：`cn.com.shopgroup.user.controller.member.ApplyLeaderController`

> 接口数量：1

#### 1. POST `/user/apply/become/leader`

**功能说明**：申请团长(会员端提交申请记录)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `ApplyLeaderRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**ApplyLeaderRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | `String` | 是 | 姓名 |
| mobile | `String` | 是 | 手机号码 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）


### LoginController

> 类路径：`cn.com.shopgroup.user.controller.member.LoginController`

> 接口数量：5

#### 1. GET `/user/phone`

**功能说明**：根据code获取手机号

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| code | `String` | Query 参数 | 是 | 微信授权 code（临时凭证，用于换取手机号 / openid） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 2. GET `/user/openid`

**功能说明**：根据code获取openid

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| code | `String` | Query 参数 | 是 | 微信授权 code（临时凭证，用于换取手机号 / openid） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 3. GET `/user/login`

**功能说明**：openid自动登录

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| openid | `String` | Query 参数 | 是 | 微信 openid（用户唯一标识） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LoginMemberResponse`（具体字段见下方表格） |

data 类型：`LoginMemberResponse`（字段说明见下）

**LoginMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `int` | 否 | — |
| name | `String` | 否 | — |
| avatar | `String` | 否 | — |
| mobile | `String` | 否 | — |
| openid | `String` | 否 | — |
| token | `String` | 否 | — |
| leaderId | `Long` | 否 | — |


#### 4. POST `/user/reg`

**功能说明**：注册新用户

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | `String` | 是 | 姓名不能为空 |
| avatar | `String` | 是 | 头像不能为空 |
| mobile | `String` | 是 | 手机不能为空 |
| openid | `String` | 是 | openid不能为空 |
| leader | `Long` | 否 | 团长ID |
| map | `String` | 否 | 地图定位 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GbMemberInfo`、`LoginMemberResponse`（具体字段见下方表格） |

data 类型：`GbMemberInfo`（字段说明见下）

**GbMemberInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberId | `Long` | 否 | 用户id,主键自增 |
| mobile | `String` | 否 | 手机号码 |
| nickname | `String` | 否 | 微信昵称 |
| avatar | `String` | 否 | 微信头像 |
| openid | `String` | 否 | openid,唯一索引（自动登录） |
| mapId | `Long` | 否 | 地图定位id,外键 |
| mapName | `String` | 否 | 地图定位名称 |
| leaderId | `Long` | 否 | 团长id,外键/来源 |
| isolationId | `Integer` | 否 | 数据隔离id |
| ercode | `String` | 否 | 小程序二维码,给团长扫码使用 |
| isClose | `Byte` | 否 | 是否禁用 |
| addTime | `Integer` | 否 | 添加时间 |

data 类型：`LoginMemberResponse`（字段说明见下）

**LoginMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `int` | 否 | — |
| name | `String` | 否 | — |
| avatar | `String` | 否 | — |
| mobile | `String` | 否 | — |
| openid | `String` | 否 | — |
| token | `String` | 否 | — |
| leaderId | `Long` | 否 | — |


#### 5. POST `/user/logout`

**功能说明**：退出登录(小程序调用): 清除服务端登录态, 小程序端需同步删除本地token

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）


### MemberAdviseFeedbackController

> 模块说明：用户建议反馈

> 类路径：`cn.com.shopgroup.user.controller.member.MemberAdviseFeedbackController`

> 接口数量：5

#### 1. POST `/user/member/advise/add`

**功能说明**：提交建议反馈

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `AdviseFeedbackRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**AdviseFeedbackRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 反馈记录id(新增时不传, 修改/删除时必传) |
| title | `String` | 是 | 反馈标题 |
| content | `String` | 是 | 反馈内容 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 2. POST `/user/member/advise/edit`

**功能说明**：修改建议反馈

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `AdviseFeedbackRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**AdviseFeedbackRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 反馈记录id(新增时不传, 修改/删除时必传) |
| title | `String` | 是 | 反馈标题 |
| content | `String` | 是 | 反馈内容 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 3. POST `/user/member/advise/del`

**功能说明**：删除建议反馈

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `AdviseFeedbackRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**AdviseFeedbackRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 反馈记录id(新增时不传, 修改/删除时必传) |
| title | `String` | 是 | 反馈标题 |
| content | `String` | 是 | 反馈内容 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 4. GET `/user/member/advise/info`

**功能说明**：建议反馈详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`AdviseFeedbackResponse`（具体字段见下方表格） |

data 类型：`AdviseFeedbackResponse`（字段说明见下）

**AdviseFeedbackResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| TIME_PATTERN | `static final String` | 否 | 时间展示格式 |
| id | `Long` | 否 | — |
| title | `String` | 否 | — |
| content | `String` | 否 | — |
| memberId | `Long` | 否 | — |
| status | `Integer` | 否 | — |
| statusName | `String` | 否 | — |
| reply | `String` | 否 | — |
| addTime | `String` | 否 | — |
| updateTime | `String` | 否 | — |


#### 5. GET `/user/member/advise/list`

**功能说明**：我的建议反馈列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| status | `Integer` | Query 参数 | 否 | 状态（含义见各接口说明） |
| page | `Integer` | Query 参数 | 否 | 页码（从 1 开始） |
| pageSize | `Integer` | Query 参数 | 否 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Map<String, Object>`（具体字段见下方表格） |

data 类型：`Map<String, Object>`（基本类型，无子字段）


### MemberController

> 类路径：`cn.com.shopgroup.user.controller.member.MemberController`

> 接口数量：3

#### 1. GET `/user/member/info`

**功能说明**：根据 token 获取用户信息

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LoginMemberResponse`（具体字段见下方表格） |

data 类型：`LoginMemberResponse`（字段说明见下）

**LoginMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `int` | 否 | — |
| name | `String` | 否 | — |
| avatar | `String` | 否 | — |
| mobile | `String` | 否 | — |
| openid | `String` | 否 | — |
| token | `String` | 否 | — |
| leaderId | `Long` | 否 | — |


#### 2. GET `/user/member/isleader`

**功能说明**：是否团长身份

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderResponse`（具体字段见下方表格） |

data 类型：`LeaderResponse`（字段说明见下）

**LeaderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| lid | `String` | 否 | 团长id(加密) |
| sid | `String` | 否 | 员工id(加密) |
| shop | `String` | 否 | 店铺名称 |
| name | `String` | 否 | 员工姓名 |
| isSuper | `Boolean` | 否 | 是否超管 |
| auth | `String` | 否 | 权限列表(逗号间隔) |
| pointIds | `String` | 否 | 提货点列表(逗号间隔) |


#### 3. POST `/user/member/swap/getInfo`

**功能说明**：登录用户获取最新团长id

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberSwapLeaderRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberSwapLeaderRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| openid | `String` | 是 | openid |
| leaderId | `Long` | 是 | 团长ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LoginMemberResponse`（具体字段见下方表格） |

data 类型：`LoginMemberResponse`（字段说明见下）

**LoginMemberResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `int` | 否 | — |
| name | `String` | 否 | — |
| avatar | `String` | 否 | — |
| mobile | `String` | 否 | — |
| openid | `String` | 否 | — |
| token | `String` | 否 | — |
| leaderId | `Long` | 否 | — |



## 2. gb-group-order（订单/退款/分账）


### OrderBusinessController

> 类路径：`cn.com.shopgroup.order.controller.OrderBusinessController`

> 接口数量：2

#### 1. GET `/order/orderbusiness/list`

**功能说明**：分页查询订单列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderBusinessInfoResponse>`（具体字段见下方表格） |

data 类型：`List<OrderBusinessInfoResponse>`（数组，元素类型 `OrderBusinessInfoResponse`，字段说明见下）

**OrderBusinessInfoResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| Id | `Long` | 否 | id,主键 |
| orderNo | `String` | 否 | 订单号 |
| busId | `Long` | 否 | 账户id,外键 |
| merchantNo | `String` | 否 | 易宝商户编号,冗余 |
| leaderId | `Long` | 否 | 团长id,外键 |
| groupName | `String` | 否 | 团购名称,冗余 |
| orderSn | `String` | 否 | 订单编号,冗余 |
| isSend | `Byte` | 否 | 是否发货,微信发货 |
| transactionId | `String` | 否 | 微信单号,微信发货 |
| openid | `String` | 否 | openid,微信发货 |
| sendTime | `Integer` | 否 | 发货时间,微信发货 |
| isUnfreeze | `Byte` | 否 | 是否解冻,微信冻结 |
| unfreezeTime | `Integer` | 否 | 解冻时间,微信冻结 |
| isDivide | `Byte` | 否 | 是否分账 |
| divideStatus | `String` | 否 | 分账状态 |
| divideTime | `Integer` | 否 | 分账时间 |
| divideNo | `String` | 否 | 分账流水号 |
| orderFee | `Double` | 否 | 订单金额，单位元 |
| receivedFee | `Double` | 否 | 实到金额，单位元 |
| busFee | `Double` | 否 | 分账金额，单位元 |
| serviceFee | `Double` | 否 | 平台服务费，单位元 |
| otherFee | `Double` | 否 | 其他佣金，单位元 |
| commStatus | `Byte` | 否 | 数据状态: 0=已支付(支付回调),1=已发货(定时任务),2=已解冻(t+2),3=已分账(定时任务),4=已提现(用户申请),5=已退款(用户申请) |
| addTime | `Integer` | 否 | 添加时间(下单时间) |


#### 2. GET `/order/orderbusiness/count`

**功能说明**：查询订单总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）


### GroupOrderController

> 类路径：`cn.com.shopgroup.order.controller.group.GroupOrderController`

> 接口数量：1

#### 1. POST `/order/group/add`

**功能说明**：用户下单: 下单前校验商品/SKU库存与限购(限购商品按「当前用户 + 当前团购活动 + 该商品」维度校验, 该团购活动内已购买数量 + 本次购买数量不能超过限购数)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `OrderRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**OrderRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 是 | 团购不能为空 |
| pointId | `Long` | 是 | 提货点不能为空 |
| name | `String` | 是 | 姓名不能为空 |
| mobile | `String` | 是 | 电话不能为空 |
| addressId | `int` | 否 | 邮寄地址 |
| remark | `String` | 否 | 客户下单订单备注 |
| goods | `List<OrderGoodsRequest>` | 是 | 订单商品不能为空 |


**→OrderGoodsRequest 字段**（字段 `goods`（List<OrderGoodsRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品ID |
| num | `Integer` | 否 | 商品数量 |
| packId | `Long` | 否 | 包装信息 |
| packName | `String` | 否 | — |
| packNum | `Integer` | 否 | — |
| skuId | `Long` | 否 | sku信息 |
| skuids | `String` | 否 | — |
| skunames | `String` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### MemberGroupController

> 模块说明：用户首页-团购活动相关

> 类路径：`cn.com.shopgroup.order.controller.group.MemberGroupController`

> 接口数量：8

#### 1. GET `/order/group/groupActivity/cat`

**功能说明**：团购分类列表（首页）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupCategoryResponse>`（具体字段见下方表格） |

data 类型：`List<GroupCategoryResponse>`（数组，元素类型 `GroupCategoryResponse`，字段说明见下）

**GroupCategoryResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| name | `String` | 否 | — |


#### 2. POST `/order/member/groupActivity/list`

**功能说明**：用户查询所有在线的团购活动列表[新用户未绑定团长时leaderId=0]: 仅返回未下线(isClose=0)且当前时间处于开团时间窗内(已开团未结束)的在线活动; 支持团购名称模糊搜索(groupName, 非空时生效)与团购分类过滤(catId, 非空且大于0时生效); leaderId>0按团长过滤(排序值sortOrder升序置顶优先, 同级按活动id倒序, 分页在SQL层完成); leaderId=0时需传经纬度(longitude/latitude, 缺失返回空列表), 按活动所属团长过滤: 团长的所有未禁用自提点中, 只要有任意一个自提点到用户的距离小于等于该自提点设置的自提范围(point_scope, 单位公里), 该团长下的所有团购活动均对用户可见, 按活动id倒序(先按名称/分类过滤, 再距离过滤, 最后分页); page默认1, pageSize默认10最大20; 每个团购活动一并返回其商品列表(goods: 团购价取团购商品表冗余价, 库存/单位取自商品表, 批量查询, 无商品的团购返回空数组)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberGroupActListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberGroupActListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| leaderId | `Long` | 否 | 团长id(0=新用户未绑定团长, 此时必须传经纬度) |
| longitude | `Double` | 否 | 经度, 精度为10米级(leaderId=0时必填) |
| latitude | `Double` | 否 | 纬度, 精度为10米级(leaderId=0时必填) |
| groupName | `String` | 否 | 团购名称 |
| catId | `Long` | 否 | 分类id |
| page | `Integer` | 否 | 页码, 默认1 |
| pageSize | `Integer` | 否 | 每页条数, 默认10, 最大20 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<MemberHomeGroupActResponse>`（具体字段见下方表格） |

data 类型：`List<MemberHomeGroupActResponse>`（数组，元素类型 `MemberHomeGroupActResponse`，字段说明见下）

**MemberHomeGroupActResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 团购id |
| lid | `Long` | 否 | 团长id(分享使用) |
| cat | `Long` | 否 | 团购分类id |
| name | `String` | 否 | 团购名称 |
| pickup | `Byte` | 否 | 商品提货方式,1自提2邮递 |
| price | `Double` | 否 | 团购价格/最小价格 |
| price2 | `Double` | 否 | 团购价格/最大价格 |
| img | `String` | 否 | 团购图片 |
| img2 | `String` | 否 | 团购图片 |
| img3 | `String` | 否 | 团购图片 |
| info | `String` | 否 | 团购介绍 |
| virtual | `Integer` | 否 | 虚拟订单数量 |
| order | `Integer` | 否 | 实际订单数量 |
| viewCount | `Integer` | 否 | 当前团购查看次数(不去重, 总浏览人次, 与团购详情 viewCount 口径一致) |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |
| startTime | `Integer` | 否 | 开团时间 |
| endTime | `Integer` | 否 | 结束时间 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| tagId | `Long` | 否 | 团购标签id,0没有 |
| tagName | `String` | 否 | 团购标签名称 |
| goods | `List<GroupActGoodsResponse>` | 否 | 团购商品列表 |
| groupLogs | `List<GroupLogs>` | 否 | 滚动订单商品信息 |


**→GroupActGoodsResponse 字段**（字段 `goods`（List<GroupActGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| gid | `Long` | 否 | 商品id |
| gname | `String` | 否 | 商品名称 |
| gtype | `Byte` | 否 | 商品类型 |
| img | `String` | 否 | 商品图片 |
| price | `Double` | 否 | 团购价 |
| price2 | `Double` | 否 | 市场价 |
| stock | `String` | 否 | 库存(商品数量+单位, 如 100斤) |
| isClose | `Byte` | 否 | 后加 0 上架 1 下架 |
| specList | `List<GroupSpecResponse>` | 否 | 商品规格列表(包含规格值, 不含禁用规格/规格值); 调用方需按 goodsId 填充 |


**→→GroupSpecResponse 字段**（字段 `specList`（List<GroupSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<GroupSpecValResponse>` | 否 | — |


**→→→GroupSpecValResponse 字段**（字段 `valList`（List<GroupSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


**→GroupLogs 字段**（字段 `groupLogs`（List<GroupLogs>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| userAvatar | `String` | 否 | — |
| userMobile | `String` | 否 | — |
| userTime | `String` | 否 | — |
| userGoodsName | `String` | 否 | — |
| userGoodsNum | `String` | 否 | — |


#### 3. GET `/order/group/groupActivity/info`

**功能说明**：团购数量（首页） 团购详情(团长分享页面)---用户首页：团长更多好货也用

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GroupActivityResponse`（具体字段见下方表格） |

data 类型：`GroupActivityResponse`（字段说明见下）

**GroupActivityResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| name | `String` | 否 | — |
| img | `String` | 否 | — |
| img2 | `String` | 否 | — |
| img3 | `String` | 否 | — |
| price | `Double` | 否 | — |
| price2 | `Double` | 否 | — |
| price3 | `Double` | 否 | — |
| brief | `String` | 否 | — |
| lid | `Long` | 否 | 团长id |
| pickup | `Byte` | 否 | 商品提货方式：1自提2邮递 |
| num | `Integer` | 否 | 订单数量 |
| num2 | `Integer` | 否 | 虚拟数量 |
| viewCount | `Integer` | 否 | 当前团购查看次数(不去重, 总浏览人次, 实时统计, 不入详情缓存) |
| isClose | `Byte` | 否 | 是否关闭 |


#### 4. POST `/order/group/groupActivity/view`

**功能说明**：用户查看团购详情-显式埋点上报(分享等场景前端调用; 首页进入详情会自动埋点, 可不上报)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberGroupViewRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberGroupViewRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 否 | 团购id |
| source | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Boolean`（具体字段见下方表格） |

data 类型：`Boolean`（基本类型，无子字段）

#### 5. GET `/order/group/groupActivity/shop`

**功能说明**：团长店铺详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| leaderId | `Long` | Query 参数 | 是 | 团长 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`ShopResponse`（具体字段见下方表格） |

data 类型：`ShopResponse`（字段说明见下）

**ShopResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 店铺id |
| name | `String` | 否 | 店铺名称 |
| shortName | `String` | 否 | 店铺简称 |
| mobile | `String` | 否 | 联系电话 |
| banner | `String` | 否 | 店铺banner |
| shopCodeUrl | `String` | 否 | 店铺二维码图,用户扫码查看待核销订单使用 |
| shopInfo | `String` | 否 | 店铺信息介绍 |
| shopLogo | `String` | 否 | 店铺logo |


#### 6. GET `/order/group/groupActivity/logs`

**功能说明**：团购记录(跟团记录) 早上6点到晚上8点之间，每个小时生成一批跟团记录（固定部分+滚动部分）， 固定部分打开页面就显示，滚动部分每隔几秒显示一条； 晚上时间只生成固定跟团记录（锁定在19:00-20:00生成的订单）。

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupLogs>`（具体字段见下方表格） |

data 类型：`List<GroupLogs>`（数组，元素类型 `GroupLogs`，字段说明见下）

**GroupLogs 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| userAvatar | `String` | 否 | — |
| userMobile | `String` | 否 | — |
| userTime | `String` | 否 | — |
| userGoodsName | `String` | 否 | — |
| userGoodsNum | `String` | 否 | — |


#### 7. GET `/order/group/groupActivity/logs2`

**功能说明**：团购记录(跟团记录), 滚动部分

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 groupId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupLogs>`（具体字段见下方表格） |

data 类型：`List<GroupLogs>`（数组，元素类型 `GroupLogs`，字段说明见下）

**GroupLogs 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| userAvatar | `String` | 否 | — |
| userMobile | `String` | 否 | — |
| userTime | `String` | 否 | — |
| userGoodsName | `String` | 否 | — |
| userGoodsNum | `String` | 否 | — |


#### 8. GET `/order/group/order/records`

**功能说明**：真实跟团记录：基于支付成功订单数据

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 否 | 团购活动 ID（<=0 或 null 时表示不过滤） |
| limit | `Integer` | Query 参数 | 否 | 每页条数 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupOrderRecordResponse>`（具体字段见下方表格） |

data 类型：`List<GroupOrderRecordResponse>`（数组，元素类型 `GroupOrderRecordResponse`，字段说明见下）

**GroupOrderRecordResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberId | `Long` | 否 | — |
| mobile | `String` | 否 | — |
| avatar | `String` | 否 | — |
| nickname | `String` | 否 | — |
| time | `String` | 否 | — |
| goodsDesc | `String` | 否 | — |
| goodsNum | `Integer` | 否 | — |



### MemberOrderController

> 类路径：`cn.com.shopgroup.order.controller.group.MemberOrderController`

> 接口数量：13

#### 1. POST `/order/group/order/list`

**功能说明**：用户订单列表（按订单状态/商品名称筛选, 分页查询）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberOrderListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberOrderListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsName | `String` | 否 | 商品名称 |
| status | `Integer` | 否 | 订单状态[不传 全部(不含已取消) 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后(5 售后同时返回4 已退款的订单) 6 已取消(需显式传入, 才返回已取消订单)] |
| page | `Integer` | 否 | — |
| pageSize | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderResponse>`（具体字段见下方表格） |

data 类型：`List<OrderResponse>`（数组，元素类型 `OrderResponse`，字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 2. POST `/order/group/order/applyRefundList`

**功能说明**：用户售后订单列表(支持按商品名称/售后审核状态筛选; 同一订单的商品可能处于不同售后状态, 按(订单,审核状态)拆分为多条返回, 每条goods仅含该状态的商品, 行状态见goods[].applyRefund; 分页在订单维度)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberOrderRefundListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberOrderRefundListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| status | `Integer` | 否 | 售后审核状态:1 待审核 2 同意 3 不同意(不传=查询全部售后状态) |
| goodsName | `String` | 否 | 商品名称(模糊查询, 可选; 仅返回包含该商品售后行的记录) |
| page | `Integer` | 否 | 页码(从 1 开始, 不传默认 1) |
| pageSize | `Integer` | 否 | 每页条数(默认 10) |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderResponse>`（具体字段见下方表格） |

data 类型：`List<OrderResponse>`（数组，元素类型 `OrderResponse`，字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 3. GET `/order/group/order/count`

**功能说明**：用户订单数量

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pid | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤）（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 4. GET `/order/group/order/info`

**功能说明**：用户订单详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`OrderResponse`（具体字段见下方表格） |

data 类型：`OrderResponse`（字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 5. GET `/order/group/order/makeErcode`

**功能说明**：用户订单小程序码(微信小程序码, 扫码进入C端小程序对应订单页面)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 6. GET `/order/group/order/receipt`

**功能说明**：用户扫码核销-整单核销(GET): 核销订单全部剩余可核销商品(购买数-已核销-已退)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |
| point | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤）（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，以接口实际返回为准）

#### 7. POST `/order/group/order/part/receipt`

**功能说明**：用户扫码核销-整单/部分核销(POST): goodsList 为空 = 整单核销(等同 GET /group/order/receipt, 核销全部剩余可核销商品); goodsList 非空 = 部分核销(仅核销所选商品行, 一单可多次部分核销, 每行核销数量不超过"剩余可核销数(购买数-已核销-已退)"); 核销成功后统一落核销记录表(核销类型:2=用户扫码核销, 核销人=下单用户, 核销商品明细记录本次核销数量)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberOrderReceiptRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberOrderReceiptRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单号 |
| pointId | `Long` | 否 | 核销(实际领取)自提点id |
| goodsList | `List<OrderVerifyGoodsRequest>` | 否 | 核销商品列表(部分核销时传, 整单核销不传/传空): id=订单商品表id, num=本次核销数量 |


**→OrderVerifyGoodsRequest 字段**（字段 `goodsList`（List<OrderVerifyGoodsRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 订单商品表id |
| num | `Integer` | 否 | 收货数量 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，以接口实际返回为准）

#### 8. POST `/order/group/order/apply/refund`

**功能说明**：用户申请订单退款。refundFlag: 1=退款(退"待收货"部分, 可退量=购买数-收货数-已申请退款数), 2=退货退款(退"已收货"部分, 可退量=收货数-已申请退货退款数); 申请成功后: 订单状态置售后(5), 对应商品行退款/退货退款数量先占坑累计(可退量会相应扣减), 商品维度退款金额由 退款数量×商品单价 推算, 不单独落库; 订单主表refund_fee在申请/审核阶段都不维护, 待团长审核: 同意仅按本次申请金额发起退款(主表金额改由退款回调成功后按实际退款金额累加), 不同意=主表金额不变(天然回到申请前), 仅回退商品行本次申请的数量并把售后状态置不同意; 出参data为本次申请退款总金额(单位:元) 事务保证"订单置售后(5) + 商品行退款数量占坑 + 售后申请记录"三步一致, 避免中途异常导致订单已售后但商品行退款数量未维护(待核销统计虚高)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| refundApplyRequest | `OrderRefundApplyRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**OrderRefundApplyRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| refundFlag | `Integer` | 是 | 退款类型标识 1 退款 2 退货退款 |
| orderNo | `String` | 是 | 订单号不能为空 |
| refundGoodsMap | `Map<Long, OrderRefundGoodsRequest>` | 否 | map key Long订单商品ID |
| actionReason | `String` | 是 | 申请原因 |
| extraReason | `String` | 否 | 附加说明 |


**→OrderRefundGoodsRequest 字段**（字段 `refundGoodsMap`（Map<Long, OrderRefundGoodsRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderGoodsId | `Long` | 是 | 订单商品ID |
| refundNum | `Integer` | 是 | 商品数量 |
| refundAmount | `Double` | 是 | 退款金额 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Double`（具体字段见下方表格） |

data 类型：`Double`（基本类型，无子字段）

#### 9. GET `/order/group/order/refund/reasonList`

**功能说明**：用户退款原因下拉列表(申请退款时"选择退款原因")

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GbRefundReason>`（具体字段见下方表格） |

data 类型：`List<GbRefundReason>`（数组，元素类型 `GbRefundReason`，字段说明见下）

**GbRefundReason 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 原因id,主键自增 |
| reason | `String` | 否 | 退款原因文本 |
| sort | `Integer` | 否 | 排序,数值越小越靠前 |
| status | `Integer` | 否 | 状态,0=停用,1=启用 |
| addTime | `Integer` | 否 | 创建时间,秒级时间戳 |
| updateTime | `Integer` | 否 | 更新时间,秒级时间戳 |


#### 10. GET `/order/group/order/refund/recodes`

**功能说明**：售后记录查询

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`OrderMainRefundResponse`（具体字段见下方表格） |

data 类型：`OrderMainRefundResponse`（字段说明见下）

**OrderMainRefundResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderPrice | `Double` | 否 | 订单金额 |
| refundFee | `Double` | 否 | 退款金额（元） |
| leaderId | `Long` | 否 | 团长信息id |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


#### 11. GET `/order/group/order/notAllReceiptList`

**功能说明**：用户端-查询还有商品未全部收货的订单列表(用户id+团长id查询, 状态1待收货/2部分收货/5售后; 5售后时商品行还须满足 购买数>(已核销数+退款数); 订单下还存在未核销商品, 返回订单及商品信息)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| shopId | `Long` | Query 参数 | 是 | 店铺 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderResponse>`（具体字段见下方表格） |

data 类型：`List<OrderResponse>`（数组，元素类型 `OrderResponse`，字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 12. POST `/order/group/order/confirmShipping`

**功能说明**：用户点击确认收货组件后调用接口，更新订单已经操作按钮

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 13. POST `/order/group/order/applyRefund/orderInfo`

**功能说明**：用户点击申请退货后调用接口

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberOrderRefundRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberOrderRefundRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 是 | 订单号 |
| refundFlag | `Integer` | 是 | 退款类型标识 1 退款 2 退货退款 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`RefundOrderInfoResponse`（具体字段见下方表格） |

data 类型：`RefundOrderInfoResponse`（字段说明见下）

**RefundOrderInfoResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| refundGoods | `List<RefundOrderGoodsResponse>` | 否 | 订单可退款商品列表 |


**→RefundOrderGoodsResponse 字段**（字段 `refundGoods`（List<RefundOrderGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 订单商品id |
| goodsId | `Long` | 否 | 实体商品id |
| goodsName | `String` | 否 | 商品名 |
| goodsImg | `String` | 否 | 图片 |
| goodsPrice | `Double` | 否 | 图片 |
| goodsNum | `Integer` | 否 | 购买总数量 |
| refundGoodsNum | `Integer` | 否 | 可退数量 |
| goodsUnit | `String` | 否 | 单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |



### WxOrderController

> 类路径：`cn.com.shopgroup.order.controller.group.WxOrderController`

> 接口数量：1

#### 1. GET `/order/group/wx/order`

**功能说明**：查询微信订单发货状态（查询订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款；(6) 资金待结算）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Integer`（具体字段见下方表格） |

data 类型：`Integer`（基本类型，无子字段）


### LeaderBillController

> 类路径：`cn.com.shopgroup.order.controller.leader.LeaderBillController`

> 接口数量：1

#### 1. POST `/order/leader/bill/list`

**功能说明**：团长端-对账单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderBillListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderBillListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| type | `Integer` | 否 | 统计维度: 1=按商品(明细返回商品名称), 2=按订单(明细返回订单号), 不传默认1 |
| startDate | `String` | 否 | 开始日期 yyyy-MM-dd, 与结束日期成对传入, 未输入时间则不查询 |
| endDate | `String` | 否 | 结束日期 yyyy-MM-dd, 与开始日期成对传入, 未输入时间则不查询 |
| page | `Integer` | 否 | 页码，默认1 |
| pageSize | `Integer` | 否 | 每页数量，默认10，最大20 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderBillListResponse`（具体字段见下方表格） |

data 类型：`LeaderBillListResponse`（字段说明见下）

**LeaderBillListResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| type | `Integer` | 否 | 统计维度: 1=按商品, 2=按订单 |
| orderTotal | `Integer` | 否 | 有效订单数(已支付且未取消, 按下单时间归集) |
| amountTotal | `Double` | 否 | 订单总金额，单位元 |
| refundAmountTotal | `Double` | 否 | 退款总金额，单位元 |
| total | `Integer` | 否 | 明细总条数(分页用: 按商品=商品种类数, 按订单=有效订单数) |
| page | `Integer` | 否 | 页码 |
| pageSize | `Integer` | 否 | 每页数量 |
| list | `List<LeaderBillItemResponse>` | 否 | 对账明细列表: 按商品维度返回商品名称/订单金额/退款金额, 按订单维度返回订单号/订单金额/退款金额 |


**→LeaderBillItemResponse 字段**（字段 `list`（List<LeaderBillItemResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsId | `Long` | 否 | 商品id(按商品统计时返回) |
| goodsName | `String` | 否 | 商品名称(按商品统计时返回) |
| orderNo | `String` | 否 | 订单号(按订单统计时返回) |
| amount | `Double` | 否 | 订单金额，单位元(按订单统计=实付金额; 按商品统计=Σ商品单价×购买数量) |
| refundAmount | `Double` | 否 | 退款金额，单位元(按订单统计=退款成功累计; 按商品统计=Σ商品单价×退款数量, 含待审核不含被拒) |



### LeaderGroupActivityController

> 类路径：`cn.com.shopgroup.order.controller.leader.LeaderGroupActivityController`

> 接口数量：1

#### 1. GET `/order/leader/activity/list`

**功能说明**：团长端-团购活动查询  提供团长端活动下拉/选择所需的接口；活动主数据在商品域(goods), 本控制器通过 goods 模块的 {@link GbGroupActivityInfoService} 跨模块调用，避免在 order 模块重复建表/写 SQL。

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderGroupActivityResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderGroupActivityResponse>`（数组，元素类型 `LeaderGroupActivityResponse`，字段说明见下）

**LeaderGroupActivityResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 否 | 团购活动 ID |
| groupName | `String` | 否 | 团购活动名称 |
| startTime | `Integer` | 否 | 开团时间(秒级时间戳) |
| endTime | `Integer` | 否 | 结束时间(秒级时间戳) |
| orderTotal | `Integer` | 否 | 实际订单数量 |
| isClose | `Byte` | 否 | 是否关闭: 0 上线 1 下线 |



### LeaderMemberController

> 类路径：`cn.com.shopgroup.order.controller.leader.LeaderMemberController`

> 接口数量：2

#### 1. POST `/order/leader/myMember/list`

**功能说明**：我的团员列表（支持手机号/昵称搜索，分页）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderMemberListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderMemberListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | `String` | 否 | — |
| page | `Integer` | 否 | — |
| pageSize | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderMemberListResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderMemberListResponse>`（数组，元素类型 `LeaderMemberListResponse`，字段说明见下）

**LeaderMemberListResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberId | `Long` | 否 | — |
| mobile | `String` | 否 | — |
| nickname | `String` | 否 | — |
| avatar | `String` | 否 | — |
| lastTimeDesc | `String` | 否 | — |
| lastActionDesc | `String` | 否 | — |
| consumeAmount | `Double` | 否 | — |
| orderCount | `Integer` | 否 | — |
| viewCount | `Integer` | 否 | — |


#### 2. GET `/order/leader/myMember/detail`

**功能说明**：团员详情（消费/退款/跟团次数/查看次数/动态）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| memberId | `Long` | Query 参数 | 是 | 会员（用户）ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderMemberDetailResponse`（具体字段见下方表格） |

data 类型：`LeaderMemberDetailResponse`（字段说明见下）

**LeaderMemberDetailResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberId | `Long` | 否 | — |
| mobile | `String` | 否 | — |
| nickname | `String` | 否 | — |
| avatar | `String` | 否 | — |
| consumeAmount | `Double` | 否 | — |
| refundAmount | `Double` | 否 | — |
| orderCount | `Integer` | 否 | — |
| viewCount | `Integer` | 否 | — |
| dynamicList | `List<MemberDynamicGroup>` | 否 | — |


**→MemberDynamicGroup 字段**（字段 `dynamicList`（List<MemberDynamicGroup>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| date | `String` | 否 | — |
| items | `List<MemberDynamicItem>` | 否 | — |


**→→MemberDynamicItem 字段**（字段 `items`（List<MemberDynamicItem>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| time | `String` | 否 | — |
| action | `String` | 否 | — |
| content | `String` | 否 | — |



### OrderController

> 类路径：`cn.com.shopgroup.order.controller.leader.OrderController`

> 接口数量：12

#### 1. POST `/order/leader/order/list`

**功能说明**：团长订单列表（按团活动/订单状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询） 团购详情调用必须传groupId，团购订单按钮，全部传传0

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderOrderListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderOrderListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | `String` | 否 | 商品名称 或是手机号 |
| status | `Integer` | 否 | 订单状态[不传 全部(不含已取消) 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后(5 售后同时返回4 已退款的订单) 6 已取消(需显式传入, 才返回已取消订单)] |
| groupId | `Long` | 否 | 团活动Id |
| pointId | `Long` | 否 | 自提点id |
| startDate | `String` | 否 | 开始日期 yyyy-MM-dd, 与结束日期成对传入, |
| endDate | `String` | 否 | 结束日期 yyyy-MM-dd, 与开始日期成对传入 |
| page | `Integer` | 否 | — |
| pageSize | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderResponse>`（具体字段见下方表格） |

data 类型：`List<OrderResponse>`（数组，元素类型 `OrderResponse`，字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 2. POST `/order/leader/apply/refundList`

**功能说明**：团长售后订单列表（按团活动/审核状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderOrderApplyRefundRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderOrderApplyRefundRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | `String` | 否 | 商品名称 或是手机号 |
| applyStatus | `Integer` | 否 | 审核状态[不传 查询全部 1 待审核 2 同意 3 不同意] |
| groupId | `Long` | 否 | 团活动Id |
| pointId | `Long` | 否 | 先不考虑 |
| startDate | `String` | 否 | 开始日期 yyyy-MM-dd(按订单下单时间过滤, 取当天00:00:00), 未传则不限制开始时间 |
| endDate | `String` | 否 | 结束日期 yyyy-MM-dd(按订单下单时间过滤, 取当天23:59:59), 未传则不限制结束时间 |
| page | `Integer` | 否 | — |
| pageSize | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderResponse>`（具体字段见下方表格） |

data 类型：`List<OrderResponse>`（数组，元素类型 `OrderResponse`，字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 3. GET `/order/leader/order/count`

**功能说明**：查询订单总数(待核销), 考虑提货点

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| gid | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤）（变量名 groupId） |
| pid | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤）（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 4. GET `/order/leader/order/status`

**功能说明**：查询订单状态数量

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| gid | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤）（变量名 groupId） |
| pid | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤）（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`OrderStatusResponse`（具体字段见下方表格） |

data 类型：`OrderStatusResponse`（字段说明见下）

**OrderStatusResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| all | `Long` | 否 | 全部订单 |
| unpay | `Long` | 否 | 未支付 |
| unreceipt | `Long` | 否 | 待核销(待收货) |
| completed | `Long` | 否 | 已核销(已收货) |
| refunded | `Long` | 否 | 退款中(已退款, 拒绝退款) |


#### 5. POST `/order/leader/order/scanQRCode`

**功能说明**：团长扫用户订单码接口

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `ScanQRCodeRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**ScanQRCodeRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 是 | 订单号 |
| receiptCode | `String` | 是 | 核销码 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`OrderResponse`（具体字段见下方表格） |

data 类型：`OrderResponse`（字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 6. GET `/order/leader/order/query`

**功能说明**：根据订单号查询订单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`OrderResponse`（具体字段见下方表格） |

data 类型：`OrderResponse`（字段说明见下）

**OrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<OrderGoodsReponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |
| verifyRecords | `List<OrderVerifyRecordResponse>` | 否 | 核销记录(仅订单详情接口返回, 支持一单多次部分核销产生多条记录, 按核销时间正序; 列表接口不填充) |


**→OrderGoodsReponse 字段**（字段 `goods`（List<OrderGoodsReponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意 |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


**→OrderVerifyRecordResponse 字段**（字段 `verifyRecords`（List<OrderVerifyRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 核销记录id |
| verifyType | `Integer` | 否 | 核销类型: 0=团长后台核销, 2=用户扫码核销 |
| staffId | `Long` | 否 | 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId |
| staffName | `String` | 否 | 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称 |
| verifyPointId | `Long` | 否 | 核销(实际领取)自提点id |
| verifyPointName | `String` | 否 | 核销(实际领取)自提点名称 |
| addTime | `String` | 否 | 核销时间(格式化字符串) |
| verifyGoodsMsg | `List<Map<String, Object>>` | 否 | 核销商品明细(仅本次核销数量>0的商品行): 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数) |


#### 7. POST `/order/leader/order/writeOff`

**功能说明**：核销（整单核销）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |
| pid | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤）（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 8. POST `/order/leader/order/partWriteOff`

**功能说明**：部分核销订单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `OrderVerifyRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**OrderVerifyRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 是 | 订单号 |
| pid | `Long` | 是 | 提货点id |
| goodsMap | `Map<Long, OrderVerifyGoodsRequest>` | 否 | 商品收货数量映射: key=订单商品id, value=核销数量 |


**→OrderVerifyGoodsRequest 字段**（字段 `goodsMap`（Map<Long, OrderVerifyGoodsRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 订单商品表id |
| num | `Integer` | 否 | 收货数量 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 9. GET `/order/leader/order/send`

**功能说明**：团长端-查询微信发货

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 10. GET `/order/leader/home/show/orders`

**功能说明**：团长首页订单汇总(head部分): 返回有效订单总数/订单总金额/退款总金额;groupId必传， pointId 传 0 或不传表示不区分提货点, 传具体值则按提货点过滤

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |
| pointId | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderHomeShowDataResponse`（具体字段见下方表格） |

data 类型：`LeaderHomeShowDataResponse`（字段说明见下）

**LeaderHomeShowDataResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderTotal | `Integer` | 否 | 有效订单总数 |
| amountTotal | `Double` | 否 | 订单总金额 |
| refundAmountTotal | `Double` | 否 | 退款总金额 |


#### 11. GET `/order/leader/home/order/goodsSummary`

**功能说明**：团长端订单-商品统计: 返回订单商品总件数/待核销总件数 + 每个商品的总件数/待核销件数, 支持按团购活动groupId/自提点pointId/商品名称keyword过滤与分页

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |
| pointId | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤） |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |
| page | `Integer` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `Integer` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderHomeGoodsSummaryResponse`（具体字段见下方表格） |

data 类型：`LeaderHomeGoodsSummaryResponse`（字段说明见下）

**LeaderHomeGoodsSummaryResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsTotal | `Long` | 否 | — |
| unVerifyTotal | `Long` | 否 | — |
| page | `Integer` | 否 | 当前页码 |
| pageSize | `Integer` | 否 | 每页数量 |
| list | `List<SummaryOrderGoodsResponse>` | 否 | 商品统计列表 |


**→SummaryOrderGoodsResponse 字段**（字段 `list`（List<SummaryOrderGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品ID |
| name | `String` | 否 | 商品名称 |
| total | `Long` | 否 | 总数量 |
| num1 | `Long` | 否 | 已核销数量(商品行核销数量汇总) |
| num2 | `Long` | 否 | 待核销数量(商品数量-已核销数量-退款数量, 已退款部分不计入) |
| unit | `String` | 否 | 商品单位（如 斤/份/件） |
| skuNames | `String` | 否 | 商品规格(订单商品行 sku_names 去重拼接, 如 "500g,1斤装"; 无规格商品为 null) |


#### 12. POST `/order/get/groupActivity/totalOrder`

**功能说明**：根据团购活动id统计订单数（实时统计，团长端有需求时使用）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Integer`（具体字段见下方表格） |

data 类型：`Integer`（基本类型，无子字段）


### OrderRefundController

> 模块说明：团长端--退款管理

> 类路径：`cn.com.shopgroup.order.controller.leader.OrderRefundController`

> 接口数量：5

#### 1. GET `/order/leader/refund/count`

**功能说明**：退款订单数量: 订单中存在商品发生过退款(部分退/整单全退, 审核同意)即计入

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| gid | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤）（变量名 groupId） |
| pid | `Long` | Query 参数 | 是 | 提货点（自提点）ID（<=0 或 null 时表示不过滤）（变量名 pointId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 2. POST `/order/leader/refund/applyList`

**功能说明**：------暂时废弃 批量查询用户退款申请数据(团长端): 把用户申请退的数据按订单+商品行结构化展示, 只查"待审核"申请(售后订单 status=5 + 商品行 apply_refund=1), 关键字 keyword(商品名称/手机号) 过滤, 分页返回; 分页与总数共用同一口径(先由 SQL 过滤出"存在待审核商品行"的订单再分页, status 固定为售后5), 每单回填该笔申请的整笔待审核商品行(apply_refund=1, 不按 keyword 过滤商品行, 保证审核不遗漏商品)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderRefundApplyListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderRefundApplyListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | `String` | 否 | — |
| page | `Integer` | 否 | — |
| pageSize | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`LeaderRefundApplyListResponse`（具体字段见下方表格） |

data 类型：`LeaderRefundApplyListResponse`（字段说明见下）

**LeaderRefundApplyListResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| total | `Long` | 否 | 申请记录总数(不受分页影响) |
| page | `Integer` | 否 | 当前页码 |
| pageSize | `Integer` | 否 | 每页数量 |
| list | `List<LeaderRefundApplyResponse>` | 否 | 申请记录列表 |


**→LeaderRefundApplyResponse 字段**（字段 `list`（List<LeaderRefundApplyResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单号(审核回传 refundOrderGoodsMap 的 key) |
| addTime | `Integer` | 否 | 下单时间(时间戳秒) |
| orderStatus | `Integer` | 否 | 订单状态: 0待支付 1待收货 2部分收货 3已提货 4已退款 5售后 6已取消 |
| nickname | `String` | 否 | 会员昵称 |
| mobile | `String` | 否 | 会员手机号 |
| groupName | `String` | 否 | 团购活动名称 |
| pointName | `String` | 否 | 自提点名称 |
| payFee | `Double` | 否 | 订单实付金额(单位:元) |
| orderPrice | `Double` | 否 | 帮卖订单价格(单位:元) |
| refundFlag | `Integer` | 否 | ---------- 本次申请信息(来自最近一笔申请记录, 与审核端兜底口径一致) ---------- 退款类型: 1=退款(退待收货部分) 2=退货退款(退已收货部分)(审核回传 refundFlag) |
| applyReason | `String` | 否 | 申请原因 |
| extraReason | `String` | 否 | 补充原因 |
| applyTime | `Integer` | 否 | 申请时间(时间戳秒) |
| applyRefundAmount | `Double` | 否 | 本次申请退款总金额(单位:元, 申请记录落库值; 审核端发起退款优先按此金额) |
| refundGoodsMsg | `String` | 否 | 申请商品描述原文(如 "商品名-规格,申请退数量:2,退款金额:10.0;") |
| goods | `List<LeaderRefundApplyGoodsResponse>` | 否 | ---------- 申请商品行(仅匹配查询状态的商品行, 与审核 refundGoodsMap 对齐) ---------- |


**→→LeaderRefundApplyGoodsResponse 字段**（字段 `goods`（List<LeaderRefundApplyGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderGoodsId | `Long` | 否 | 订单商品行id(审核回传 refundGoodsMap 的 key) |
| goodsId | `Long` | 否 | 商品id |
| goodsName | `String` | 否 | 商品名称 |
| goodsImg | `String` | 否 | 商品图片 |
| goodsInfo | `String` | 否 | 规格信息(订单商品冗余的SKU名称) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsPrice | `Double` | 否 | 商品单价(单位:元) |
| goodsNum | `Integer` | 否 | 购买数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 行售后(审核)状态: 0=无 1=待审核 2=同意 3=不同意 |
| applyRefundNum | `Integer` | 否 | 申请退中数量(本次申请待审核数量, 用户申请时写入, 审核处理完成(同意/拒绝)后置0) |
| refundNum | `Integer` | 否 | 累计退款数量(退待收货部分, 申请占坑累计; 待审核行含本次申请, 历史已同意部分保留) |
| refundGoodsNum | `Integer` | 否 | 累计退货退款数量(退已收货部分, 申请占坑累计; 待审核行含本次申请, 历史已同意部分保留) |
| refundAmount | `Double` | 否 | 行退款金额(单位:元): 按 refundFlag 取对应占坑数量 x 商品单价推算 (商品维度退款金额不落库, 与审核端口径一致) |


#### 3. POST `/order/leader/approve/list`

**功能说明**：团长端-批量退款列表: 查询指定团购下"存在待待核销商品申请的订单, 支持按商品名称模糊搜索, 分页返回订单+该笔申请的待审核商品行, 供团长批量勾选后调 /leader/refund/approve 审核(同意=发起退款)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderApproveListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderApproveListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 是 | 请选择具体团购活动查询 |
| goodsName | `String` | 否 | — |
| page | `Integer` | 否 | — |
| pageSize | `Integer` | 否 | — |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderBatchOrderResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderBatchOrderResponse>`（数组，元素类型 `LeaderBatchOrderResponse`，字段说明见下）

**LeaderBatchOrderResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 否 | 订单信息 |
| orderTime | `String` | 否 | — |
| orderPrice | `Double` | 否 | — |
| leaderId | `Long` | 否 | 团长信息和店铺名称 |
| shopId | `Long` | 否 | — |
| shopName | `String` | 否 | — |
| groupId | `Long` | 否 | 团购信息 |
| groupName | `String` | 否 | — |
| goods | `List<LeaderBatchOrderGoodsResponse>` | 否 | 订单商品列表 |
| reason | `String` | 否 | 拒绝退款理由 |
| receiptTime | `Integer` | 否 | — |
| payno | `String` | 否 | 微信支付交易号 微信发货和收货都需要这个 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| receiptType | `Byte` | 否 | 收货方式：1=自提,2=邮寄 |
| trueName | `String` | 否 | — |
| telephone | `String` | 否 | — |
| pointId | `Long` | 否 | 自提点信息 |
| pointName | `String` | 否 | — |
| pointAddress | `String` | 否 | — |
| receiptCode | `String` | 否 | — |
| nickname | `String` | 否 | 下单用户信息（内部调用使用） |
| mobile | `String` | 否 | — |
| wxShipment | `Integer` | 否 | 微信发货是否已调用:0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记:0=未操作,1=已操作 |
| remark | `String` | 否 | 备注（下单用户天天填写） |


**→LeaderBatchOrderGoodsResponse 字段**（字段 `goods`（List<LeaderBatchOrderGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| goodsId | `Long` | 否 | — |
| goodsName | `String` | 否 | — |
| goodsImg | `String` | 否 | — |
| goodsPrice | `Double` | 否 | — |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 规格信息(普通商品=SKU名称, 称重商品=包装名称) |
| skuId | `Long` | 否 | SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格) |
| skuIds | `String` | 否 | — |


#### 4. POST `/order/leader/get/refund/count`

**功能说明**：团长端-团购订单上面展示售后订单数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 5. POST `/order/leader/refund/approve`

**功能说明**：售后订单审核（同意/不同意）。status: 1=同意, 2=不同意; 每单一行key=订单号, value.refundGoodsMap为本次申请的订单商品行(行内refundNum/refundAmount为本次申请值); 同意=按本次申请金额向易宝发起退款(支持部分退款)、保留申请时占坑的商品数量并把商品售后状态置同意, 同时维护订单主状态: 订单商品全部退完=已退款(4), 否则只要有未退完的=售后(5)(主表退款金额refund_fee不在此累加, 统一由退款回调成功后按实际退款金额累加; 商品维度退款金额以退款数量体现, 可由 退款数量×商品单价 推算); 不同意=主表退款金额不变(申请/审核阶段均未累加, 天然回到申请前), 按行回退商品退款/退货退款数量并把商品售后状态置不同意; 团长端旧版本未回传refundFlag/金额时后端按该订单最近一笔售后记录兜底

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| approveRequest | `OrderApproveRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**OrderApproveRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| refundOrderGoodsMap | `Map<String, OrderRefundInfoRequest>` | 否 | key String 是订单号 |
| status | `Integer` | 是 | 审核结果 1 同意 2 拒绝 |
| reason | `String` | 否 | 审核原因 |
| type | `Integer` | 否 | 同意来源 1 批量退款 |


**→OrderRefundInfoRequest 字段**（字段 `refundOrderGoodsMap`（Map<String, OrderRefundInfoRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | `String` | 是 | 退款订单号不能为空 |
| refundFlag | `Integer` | 否 | 退款类型标识: 1=退款(退待收货部分) 2=退货退款(退已收货部分); 申请时携带, 审核时回传; 旧版本客户端可能不传 |
| refundGoodsMap | `Map<Long, OrderRefundGoodsRequest>` | 否 | map key Long订单商品ID |


**→→OrderRefundGoodsRequest 字段**（字段 `refundGoodsMap`（Map<Long, OrderRefundGoodsRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderGoodsId | `Long` | 是 | 订单商品ID |
| refundNum | `Integer` | 是 | 商品数量 |
| refundAmount | `Double` | 是 | 退款金额 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### OrderRefundNotifyController

> 类路径：`cn.com.shopgroup.order.controller.leader.OrderRefundNotifyController`

> 接口数量：1

#### 1. POST `/order/leader/refund/notify`

**功能说明**：退款结果回调通知: 易宝退款最终结果异步通知(与支付回调一致, 为 RSA2048 数字信封加密的 form 报文)

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）


### OrderPaymentController

> 类路径：`cn.com.shopgroup.order.controller.payment.OrderPaymentController`

> 接口数量：2

#### 1. GET `/order/payment/order/pay`

**功能说明**：发起支付

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |
| openid | `String` | Query 参数 | 是 | 微信 openid（用户唯一标识） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，源码返回表达式：`JSONObject.parse(data)`，以接口实际返回为准）

#### 2. POST `/order/payment/order/notify`

**功能说明**：支付回调

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）


## 3. gb-group-goods（商品/团购）


### GroupGoodsController

> 类路径：`cn.com.shopgroup.goods.controller.GroupGoodsController`

> 接口数量：2

#### 1. GET `/goods/group/goods/list`

**功能说明**：团购商品列表(包装, 规格, sku)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| lid | `Long` | Query 参数 | 是 | 团长 ID（<=0 或 null 时表示不过滤）（变量名 leaderId） |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupGoodsResponse>`（具体字段见下方表格） |

data 类型：`List<GroupGoodsResponse>`（数组，元素类型 `GroupGoodsResponse`，字段说明见下）

**GroupGoodsResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品ID |
| name | `String` | 否 | 商品名称 |
| img | `String` | 否 | 商品主图 |
| price | `Double` | 否 | 商品价格 |
| price2 | `Double` | 否 | — |
| type | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| stock | `Byte` | 否 | 是否设置库存 |
| num | `Integer` | 否 | 库存数量 |
| unit | `String` | 否 | 商品单位 |
| limit | `Byte` | 否 | 是否限购 |
| quantity | `Integer` | 否 | 限购数量 |
| packs | `List<GroupPackageResponse>` | 否 | 商品包装列表 |
| specs | `List<GroupSpecResponse>` | 否 | 商品规格列表 |
| skus | `List<GroupSkuResponse>` | 否 | 商品sku价格和库存 |


**→GroupPackageResponse 字段**（字段 `packs`（List<GroupPackageResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| name | `String` | 否 | — |
| price | `Double` | 否 | — |


**→GroupSpecResponse 字段**（字段 `specs`（List<GroupSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<GroupSpecValResponse>` | 否 | — |


**→→GroupSpecValResponse 字段**（字段 `valList`（List<GroupSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


**→GroupSkuResponse 字段**（字段 `skus`（List<GroupSkuResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| ids | `String` | 否 | — |
| stock | `Integer` | 否 | — |
| price | `Double` | 否 | — |
| price2 | `Double` | 否 | — |


#### 2. GET `/goods/group/goods/stock`

**功能说明**：查询商品库存, 后期增加缓存

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| gid | `String` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤）（变量名 goodsIds） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Map<Long, Integer>`（具体字段见下方表格） |

data 类型：`Map<Long, Integer>`（基本类型，无子字段）


### LeaderGoodsManageController

> 类路径：`cn.com.shopgroup.goods.controller.LeaderGoodsManageController`

> 接口数量：12

#### 1. GET `/goods/get/goods/cat`

**功能说明**：商品分类列表

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GoodsCategoryResponse`（具体字段见下方表格） |

data 类型：`GoodsCategoryResponse`（字段说明见下）

**GoodsCategoryResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| name | `String` | 否 | — |


#### 2. GET `/goods/leader/goods/online`

**功能说明**：查询所有审核通过且未关闭的商品, 添加团购时使用

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderGoodsResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderGoodsResponse>`（数组，元素类型 `LeaderGoodsResponse`，字段说明见下）

**LeaderGoodsResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品id,主键自增 |
| cat | `Long` | 否 | 分类id,外键 |
| catName | `String` | 否 | 分类名称 |
| type | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| name | `String` | 否 | 商品名称 |
| img | `String` | 否 | 商品主图 |
| img2 | `String` | 否 | — |
| img3 | `String` | 否 | — |
| costPrice | `Double` | 否 | 进货价格 |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| isStock | `Byte` | 否 | 是否设置库存 |
| num | `Integer` | 否 | 商品库存,总库存 |
| isLimit | `Byte` | 否 | 是否设置限购 |
| num2 | `Integer` | 否 | 限购数量 |
| unit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| isClose | `Byte` | 否 | 是否禁用 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| specList | `List<LeaderSpecResponse>` | 否 | 商品规格列表(包含规格值) |


**→LeaderSpecResponse 字段**（字段 `specList`（List<LeaderSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<LeaderSpecValResponse>` | 否 | — |


**→→LeaderSpecValResponse 字段**（字段 `valList`（List<LeaderSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


#### 3. GET `/goods/leader/goods/list`

**功能说明**：分页查询团长下的所有商品(团长控制台-商品管理), 支持分类+商品名称关键字筛选

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| cat | `Long` | Query 参数 | 是 | 团购分类 ID（变量名 catId） |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderGoodsResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderGoodsResponse>`（数组，元素类型 `LeaderGoodsResponse`，字段说明见下）

**LeaderGoodsResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品id,主键自增 |
| cat | `Long` | 否 | 分类id,外键 |
| catName | `String` | 否 | 分类名称 |
| type | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| name | `String` | 否 | 商品名称 |
| img | `String` | 否 | 商品主图 |
| img2 | `String` | 否 | — |
| img3 | `String` | 否 | — |
| costPrice | `Double` | 否 | 进货价格 |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| isStock | `Byte` | 否 | 是否设置库存 |
| num | `Integer` | 否 | 商品库存,总库存 |
| isLimit | `Byte` | 否 | 是否设置限购 |
| num2 | `Integer` | 否 | 限购数量 |
| unit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| isClose | `Byte` | 否 | 是否禁用 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| specList | `List<LeaderSpecResponse>` | 否 | 商品规格列表(包含规格值) |


**→LeaderSpecResponse 字段**（字段 `specList`（List<LeaderSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<LeaderSpecValResponse>` | 否 | — |


**→→LeaderSpecValResponse 字段**（字段 `valList`（List<LeaderSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


#### 4. GET `/goods/leader/goods/count`

**功能说明**：查询团长下的所有商品数量, 支持分类+商品名称关键字筛选

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| cat | `Long` | Query 参数 | 是 | 团购分类 ID（变量名 catId） |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 5. GET `/goods/leader/goods/info`

**功能说明**：商品信息查询: 商品基本信息 + 分类名 + 图片 + 规格(含规格值) + SKU

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 goodsId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`、`GoodsDetailResponse`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）
data 类型：`GoodsDetailResponse`（字段说明见下）

**GoodsDetailResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goods | `LeaderGoodsResponse` | 否 | 商品信息(含规格/图片/分类名等) |
| skuList | `List<LeaderSkuResponse>` | 否 | 商品SKU列表 |


**→LeaderGoodsResponse 字段**（字段 `goods`（LeaderGoodsResponse））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品id,主键自增 |
| cat | `Long` | 否 | 分类id,外键 |
| catName | `String` | 否 | 分类名称 |
| type | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| name | `String` | 否 | 商品名称 |
| img | `String` | 否 | 商品主图 |
| img2 | `String` | 否 | — |
| img3 | `String` | 否 | — |
| costPrice | `Double` | 否 | 进货价格 |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| isStock | `Byte` | 否 | 是否设置库存 |
| num | `Integer` | 否 | 商品库存,总库存 |
| isLimit | `Byte` | 否 | 是否设置限购 |
| num2 | `Integer` | 否 | 限购数量 |
| unit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| isClose | `Byte` | 否 | 是否禁用 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| specList | `List<LeaderSpecResponse>` | 否 | 商品规格列表(包含规格值) |


**→→LeaderSpecResponse 字段**（字段 `specList`（List<LeaderSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<LeaderSpecValResponse>` | 否 | — |


**→→→LeaderSpecValResponse 字段**（字段 `valList`（List<LeaderSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


**→LeaderSkuResponse 字段**（字段 `skuList`（List<LeaderSkuResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | skuid |
| gid | `Long` | 否 | 商品id |
| ids | `String` | 否 | sku id, 规格1+规格2+...... |
| names | `String` | 否 | sku名称, 规格1+规格2+...... |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| num | `Integer` | 否 | 商品库存 |
| img | `String` | 否 | 商品图片 |
| isClose | `Byte` | 否 | 是否禁用 |


#### 6. POST `/goods/leader/goods/addGoods`

**功能说明**：添加商品

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderAddGoodsRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderAddGoodsRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| catId | `Long` | 是 | 分类id,外键 |
| type | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| name | `String` | 是 | 商品名称 |
| costPrice | `Double` | 否 | 进货价格 |
| salePrice | `Double` | 否 | 销售价格 |
| marketPrice | `Double` | 否 | 市场价格--划价 |
| isStock | `Byte` | 否 | 是否启用库存, 1=启用 |
| stockNum | `Integer` | 否 | 商品库存,总库存 |
| isLimit | `Byte` | 否 | 是否设置限购 |
| limitNum | `Integer` | 否 | 限购数量 |
| unit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| img | `String` | 是 | 商品图片 |
| img2 | `String` | 否 | — |
| img3 | `String` | 否 | — |
| addSpecList | `List<LeaderAddSpecRequest>` | 否 | 规格 |
| skuList | `List<LeaderSkuRequest>` | 否 | SKU列表(添加商品时随商品一并写入gb_goods_sku_info) |


**→LeaderAddSpecRequest 字段**（字段 `addSpecList`（List<LeaderAddSpecRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | 规格id |
| name | `String` | 是 | 规格名称 |
| price | `Byte` | 否 | 是否设置价格 |
| stock | `Byte` | 否 | 是否设置库存 |
| specValLists | `List<LeaderAddSpecValRequest>` | 否 | 规格值列表 |


**→→LeaderAddSpecValRequest 字段**（字段 `specValLists`（List<LeaderAddSpecValRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | 规格值id |
| sid | `Long` | 否 | 规格id |
| val | `String` | 是 | 规格值 |


**→LeaderSkuRequest 字段**（字段 `skuList`（List<LeaderSkuRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | skuid |
| gid | `Long` | 否 | 商品id |
| ids | `String` | 否 | skuids, 规格1+规格2+...... |
| names | `String` | 否 | sku名称, 规格1+规格2+...... |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| num | `Integer` | 否 | 商品库存 |
| img | `String` | 否 | 商品图片 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 7. POST `/goods/leader/goods/edit`

**功能说明**：修改商品

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderGoodsRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderGoodsRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 商品id,主键自增 |
| catId | `Long` | 是 | 分类id,外键 |
| type | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| name | `String` | 是 | 商品名称 |
| costPrice | `Double` | 否 | 进货价格 |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格--划价 |
| isStock | `Byte` | 否 | 是否启用库存, 1=启用 |
| stockNum | `Integer` | 否 | 商品库存,总库存 |
| isLimit | `Byte` | 否 | 是否设置限购 |
| limitNum | `Integer` | 否 | 限购数量 |
| unit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| img | `String` | 是 | 商品图片 |
| img2 | `String` | 否 | — |
| img3 | `String` | 否 | — |
| addSpecList | `List<LeaderAddSpecRequest>` | 否 | 规格 |
| skuList | `List<LeaderSkuRequest>` | 否 | SKU列表(修改商品时随商品信息一并重建gb_goods_sku_info) |


**→LeaderAddSpecRequest 字段**（字段 `addSpecList`（List<LeaderAddSpecRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | 规格id |
| name | `String` | 是 | 规格名称 |
| price | `Byte` | 否 | 是否设置价格 |
| stock | `Byte` | 否 | 是否设置库存 |
| specValLists | `List<LeaderAddSpecValRequest>` | 否 | 规格值列表 |


**→→LeaderAddSpecValRequest 字段**（字段 `specValLists`（List<LeaderAddSpecValRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | 规格值id |
| sid | `Long` | 否 | 规格id |
| val | `String` | 是 | 规格值 |


**→LeaderSkuRequest 字段**（字段 `skuList`（List<LeaderSkuRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | skuid |
| gid | `Long` | 否 | 商品id |
| ids | `String` | 否 | skuids, 规格1+规格2+...... |
| names | `String` | 否 | sku名称, 规格1+规格2+...... |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| num | `Integer` | 否 | 商品库存 |
| img | `String` | 否 | 商品图片 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 8. GET `/goods/leader/goods/close`

**功能说明**：关闭商品(上下架), 如果该商品正在团购中, 则不允许操作

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 goodsId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 9. GET `/goods/leader/goods/sku/spec`

**功能说明**：根据规格罗列所有SKU, 包括已经存在的sku信息

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 goodsId） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<LeaderSkuResponse>`（具体字段见下方表格） |

data 类型：`List<LeaderSkuResponse>`（数组，元素类型 `LeaderSkuResponse`，字段说明见下）

**LeaderSkuResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | skuid |
| gid | `Long` | 否 | 商品id |
| ids | `String` | 否 | sku id, 规格1+规格2+...... |
| names | `String` | 否 | sku名称, 规格1+规格2+...... |
| price | `Double` | 否 | 销售价格 |
| price2 | `Double` | 否 | 市场价格 |
| num | `Integer` | 否 | 商品库存 |
| img | `String` | 否 | 商品图片 |
| isClose | `Byte` | 否 | 是否禁用 |


#### 10. POST `/goods/leader/goods/sku/save`

**功能说明**：批量保存商品SKU(先删除后添加), 如果该商品正在团购中, 则不允许修改 注: 前端不再单独调用此接口, SKU已随添加/修改商品接口(addGoods/edit)一并处理, 此处保留兼容

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| requestList | `List<LeaderSkuRequest>` | Body | 是 | 请求体对象，字段说明见下方表格 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 11. POST `/goods/leader/update/sku/stock`

**功能说明**：团长端修改商品库存(按SKU维度修改为指定库存, 商品主表库存按各SKU库存之和同步)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderUpdateSkuStockRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderUpdateSkuStockRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsId | `Long` | 否 | 商品id |
| skuStockRequestList | `List<UpdateSkuStockRequest>` | 否 | skuId-info |


**→UpdateSkuStockRequest 字段**（字段 `skuStockRequestList`（List<UpdateSkuStockRequest>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| skuId | `Long` | 否 | skuId |
| num | `Integer` | 否 | 库存 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 12. GET `/goods/leader/activity/goods/close`

**功能说明**：团购活动详情中-商品上下架(按团购商品行自身 is_close 取反切换; 商品表已下架或活动不属于当前团长时抛错)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| goodsId | `Long` | Query 参数 | 是 | 商品 ID |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）


### LeaderGroupManageController

> 类路径：`cn.com.shopgroup.goods.controller.LeaderGroupManageController`

> 接口数量：11

#### 1. POST `/goods/Leader/get/groupActivity/list`

**功能说明**：团长端-查询所有团购活动列表<br>返回每个团购活动的订单汇总数据 `groupSummaryResponse`（实际收入、退款金额、跟团人数）, 由 LeaderGroupSummaryService 批量聚合 gb_order_info 得出; 已取消订单不计入。

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderGroupListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderGroupListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | `String` | 否 | 团购名称 |
| status | `Integer` | 否 | 状态：0 全部  1 活动中 2 未开始 3 已结束 |
| catId | `Long` | 是 | 分类id[分类id不能为空] |
| page | `Integer` | 否 | 页码 |
| pageSize | `Integer` | 否 | 每页条数 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupActResponse>`（具体字段见下方表格） |

data 类型：`List<GroupActResponse>`（数组，元素类型 `GroupActResponse`，字段说明见下）

**GroupActResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 团购id |
| lid | `Long` | 否 | 团长id(分享使用) |
| cat | `Long` | 否 | 团购分类id |
| name | `String` | 否 | 团购名称 |
| pickup | `Byte` | 否 | 商品提货方式,1自提2邮递 |
| price | `Double` | 否 | 团购价格/最小价格 |
| price2 | `Double` | 否 | 团购价格/最大价格 |
| img | `String` | 否 | 团购图片 |
| img2 | `String` | 否 | 团购图片 |
| img3 | `String` | 否 | 团购图片 |
| info | `String` | 否 | 团购介绍 |
| virtual | `Integer` | 否 | 虚拟订单数量 |
| tagId | `Long` | 否 | 团购标签id |
| tagName | `String` | 否 | 团购标签名称 |
| pointId | `Long` | 否 | 自提点id,0未选择 |
| order | `Integer` | 否 | 实际订单数量 |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |
| startTime | `Integer` | 否 | 开团时间 |
| endTime | `Integer` | 否 | 结束时间 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| goods | `List<GroupActGoodsResponse>` | 否 | 团购商品列表 |
| groupSummaryResponse | `LeaderGroupSummaryResponse` | 否 | 团长团购活动订单汇总数据 |
| genTuanResponse | `LeaderGroupGenTuanResponse` | 否 | — |
| followRecords | `List<LeaderGroupFollowRecordResponse>` | 否 | 跟团记录(真实订单数据: 手机号/姓名/头像/购买时间/购买商品/数量, 按购买时间倒序) |


**→GroupActGoodsResponse 字段**（字段 `goods`（List<GroupActGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| gid | `Long` | 否 | 商品id |
| gname | `String` | 否 | 商品名称 |
| gtype | `Byte` | 否 | 商品类型 |
| img | `String` | 否 | 商品图片 |
| price | `Double` | 否 | 团购价 |
| price2 | `Double` | 否 | 市场价 |
| stock | `String` | 否 | 库存(商品数量+单位, 如 100斤) |
| isClose | `Byte` | 否 | 后加 0 上架 1 下架 |
| specList | `List<GroupSpecResponse>` | 否 | 商品规格列表(包含规格值, 不含禁用规格/规格值); 调用方需按 goodsId 填充 |


**→→GroupSpecResponse 字段**（字段 `specList`（List<GroupSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<GroupSpecValResponse>` | 否 | — |


**→→→GroupSpecValResponse 字段**（字段 `valList`（List<GroupSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


**→LeaderGroupSummaryResponse 字段**（字段 `groupSummaryResponse`（LeaderGroupSummaryResponse））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| totalFee | `Double` | 否 | 实际收入（元） |
| refundFee | `Double` | 否 | 退款金额（元） |
| orderNum | `Integer` | 否 | 跟团人数 |


**→LeaderGroupGenTuanResponse 字段**（字段 `genTuanResponse`（LeaderGroupGenTuanResponse））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberNum | `Integer` | 否 | 成员 |
| orderNum | `Integer` | 否 | 跟团人次（订单数） |
| totalAmount | `Double` | 否 | 订单总金额（不管退的，支付总金额） |


**→LeaderGroupFollowRecordResponse 字段**（字段 `followRecords`（List<LeaderGroupFollowRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mobile | `String` | 否 | 下单用户手机号 |
| name | `String` | 否 | 下单用户姓名(昵称) |
| avatar | `String` | 否 | 下单用户头像 |
| buyTime | `String` | 否 | 购买时间(格式化 yyyy-MM-dd HH:mm:ss, 取支付时间) |
| goodsDesc | `String` | 否 | 购买商品描述(多件商品按 商品名/规格 拼接) |
| buyNum | `Integer` | 否 | 购买数量(订单商品数量合计) |


#### 2. GET `/goods/Leader/get/groupActivity/count`

**功能说明**：查询所有团购活动总数(筛选条件与列表接口一致, 保证分页总页数正确)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| cat | `Long` | Query 参数 | 否 | 团购分类 ID（变量名 catId） |
| name | `String` | Query 参数 | 否 | 名称 |
| status | `Integer` | Query 参数 | 否 | 状态（含义见各接口说明） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/goods/Leader/groupActivity/tag/list`

**功能说明**：团购标签下拉列表(添加/编辑团购活动时选择标签)

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，源码返回表达式：`tagService.getEnabledTagList()`，以接口实际返回为准）

#### 4. POST `/goods/Leader/groupActivity/add`

**功能说明**：添加团购活动

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `GroupActRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**GroupActRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 团购id |
| cat | `Long` | 否 | 团购分类id |
| pickup | `Byte` | 是 | 商品提货方式,1自提2邮递 |
| name | `String` | 是 | 团购名称 |
| info | `String` | 否 | 团购介绍 |
| virtual | `Integer` | 否 | 虚拟订单数量 |
| tagId | `Long` | 否 | 团购标签id,0=未选择 |
| pointId | `Long` | 否 | 自提点id,0未选择 |
| startTime | `Integer` | 是 | 活动开始时间 |
| endTime | `Integer` | 是 | 活动结束时间 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 5. GET `/goods/Leader/get/groupActivity/info`

**功能说明**：查询团购信息, 还要查询商品列表(价格以团购商品表冗余的团购价为准) 返回跟团统计genTuanResponse(团员人数memberNum及其下单数orderNum, 已支付、未取消、未退款的有效订单口径)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GroupActResponse`（具体字段见下方表格） |

data 类型：`GroupActResponse`（字段说明见下）

**GroupActResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 团购id |
| lid | `Long` | 否 | 团长id(分享使用) |
| cat | `Long` | 否 | 团购分类id |
| name | `String` | 否 | 团购名称 |
| pickup | `Byte` | 否 | 商品提货方式,1自提2邮递 |
| price | `Double` | 否 | 团购价格/最小价格 |
| price2 | `Double` | 否 | 团购价格/最大价格 |
| img | `String` | 否 | 团购图片 |
| img2 | `String` | 否 | 团购图片 |
| img3 | `String` | 否 | 团购图片 |
| info | `String` | 否 | 团购介绍 |
| virtual | `Integer` | 否 | 虚拟订单数量 |
| tagId | `Long` | 否 | 团购标签id |
| tagName | `String` | 否 | 团购标签名称 |
| pointId | `Long` | 否 | 自提点id,0未选择 |
| order | `Integer` | 否 | 实际订单数量 |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |
| startTime | `Integer` | 否 | 开团时间 |
| endTime | `Integer` | 否 | 结束时间 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| goods | `List<GroupActGoodsResponse>` | 否 | 团购商品列表 |
| groupSummaryResponse | `LeaderGroupSummaryResponse` | 否 | 团长团购活动订单汇总数据 |
| genTuanResponse | `LeaderGroupGenTuanResponse` | 否 | — |
| followRecords | `List<LeaderGroupFollowRecordResponse>` | 否 | 跟团记录(真实订单数据: 手机号/姓名/头像/购买时间/购买商品/数量, 按购买时间倒序) |


**→GroupActGoodsResponse 字段**（字段 `goods`（List<GroupActGoodsResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| gid | `Long` | 否 | 商品id |
| gname | `String` | 否 | 商品名称 |
| gtype | `Byte` | 否 | 商品类型 |
| img | `String` | 否 | 商品图片 |
| price | `Double` | 否 | 团购价 |
| price2 | `Double` | 否 | 市场价 |
| stock | `String` | 否 | 库存(商品数量+单位, 如 100斤) |
| isClose | `Byte` | 否 | 后加 0 上架 1 下架 |
| specList | `List<GroupSpecResponse>` | 否 | 商品规格列表(包含规格值, 不含禁用规格/规格值); 调用方需按 goodsId 填充 |


**→→GroupSpecResponse 字段**（字段 `specList`（List<GroupSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<GroupSpecValResponse>` | 否 | — |


**→→→GroupSpecValResponse 字段**（字段 `valList`（List<GroupSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


**→LeaderGroupSummaryResponse 字段**（字段 `groupSummaryResponse`（LeaderGroupSummaryResponse））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| totalFee | `Double` | 否 | 实际收入（元） |
| refundFee | `Double` | 否 | 退款金额（元） |
| orderNum | `Integer` | 否 | 跟团人数 |


**→LeaderGroupGenTuanResponse 字段**（字段 `genTuanResponse`（LeaderGroupGenTuanResponse））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberNum | `Integer` | 否 | 成员 |
| orderNum | `Integer` | 否 | 跟团人次（订单数） |
| totalAmount | `Double` | 否 | 订单总金额（不管退的，支付总金额） |


**→LeaderGroupFollowRecordResponse 字段**（字段 `followRecords`（List<LeaderGroupFollowRecordResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mobile | `String` | 否 | 下单用户手机号 |
| name | `String` | 否 | 下单用户姓名(昵称) |
| avatar | `String` | 否 | 下单用户头像 |
| buyTime | `String` | 否 | 购买时间(格式化 yyyy-MM-dd HH:mm:ss, 取支付时间) |
| goodsDesc | `String` | 否 | 购买商品描述(多件商品按 商品名/规格 拼接) |
| buyNum | `Integer` | 否 | 购买数量(订单商品数量合计) |


#### 6. POST `/goods/Leader/groupActivity/edit`

**功能说明**：修改团购活动, 团购进行中, 不允许修改

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `GroupActRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**GroupActRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | 团购id |
| cat | `Long` | 否 | 团购分类id |
| pickup | `Byte` | 是 | 商品提货方式,1自提2邮递 |
| name | `String` | 是 | 团购名称 |
| info | `String` | 否 | 团购介绍 |
| virtual | `Integer` | 否 | 虚拟订单数量 |
| tagId | `Long` | 否 | 团购标签id,0=未选择 |
| pointId | `Long` | 否 | 自提点id,0未选择 |
| startTime | `Integer` | 是 | 活动开始时间 |
| endTime | `Integer` | 是 | 活动结束时间 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 7. POST `/goods/Leader/groupActivity/close`

**功能说明**：关闭团购活动, 平台审核未通过, 不允许打开上线 打开上线的话, 需要清空Redis缓存, 任何修改团购和商品信息, 都必须是关闭状态 这样就不需要修改的时候同步Redis缓存了, 只需要关闭修改完, 打开上线的时候更新一次即可

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 8. GET `/goods/Leader/get/groupActivity/cat`

**功能说明**：团购分类列表

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GroupCatResponse>`（具体字段见下方表格） |

data 类型：`List<GroupCatResponse>`（数组，元素类型 `GroupCatResponse`，字段说明见下）

**GroupCatResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 否 | — |
| name | `String` | 否 | — |


#### 9. POST `/goods/Leader/share/groupActivity/poster`

**功能说明**：分享团购海报生成1

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 10. POST `/goods/Leader/share/groupActivity/make/poster`

**功能说明**：分享团购活动海报（带有logo的海报）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 11. POST `/goods/Leader/share/groupActivity/goods/poster`

**功能说明**：团购端-分享团购-商品-海报生成

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| groupId | `Long` | Query 参数 | 是 | 团购活动 ID（<=0 或 null 时表示不过滤） |
| goodsId | `Long` | Query 参数 | 是 | 商品 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


## 4. gb-group-admin（后台管理）


### AdminAdviseFeedbackController

> 模块说明：管理后台-用户建议反馈

> 类路径：`cn.com.shopgroup.controller.AdminAdviseFeedbackController`

> 接口数量：4

#### 1. GET `/admin/advise/list`

**功能说明**：建议反馈列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| status | `Integer` | Query 参数 | 否 | 状态（含义见各接口说明） |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |
| page | `Integer` | Query 参数 | 否 | 页码（从 1 开始） |
| pageSize | `Integer` | Query 参数 | 否 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`ArrayList`、`AdviseFeedbackResponse`（具体字段见下方表格） |

data 类型：`ArrayList`（基本类型，无子字段）
data 类型：`AdviseFeedbackResponse`（字段说明见下）

**AdviseFeedbackResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| TIME_PATTERN | `static final String` | 否 | 时间展示格式 |
| id | `Long` | 否 | — |
| title | `String` | 否 | — |
| content | `String` | 否 | — |
| memberId | `Long` | 否 | — |
| status | `Integer` | 否 | — |
| statusName | `String` | 否 | — |
| reply | `String` | 否 | — |
| addTime | `String` | 否 | — |
| updateTime | `String` | 否 | — |


#### 2. GET `/admin/advise/count`

**功能说明**：建议反馈总数

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| status | `Integer` | Query 参数 | 否 | 状态（含义见各接口说明） |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Long`（具体字段见下方表格） |

data 类型：`Long`（基本类型，无子字段）

#### 3. GET `/admin/advise/info`

**功能说明**：建议反馈详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`AdviseFeedbackResponse`（具体字段见下方表格） |

data 类型：`AdviseFeedbackResponse`（字段说明见下）

**AdviseFeedbackResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| TIME_PATTERN | `static final String` | 否 | 时间展示格式 |
| id | `Long` | 否 | — |
| title | `String` | 否 | — |
| content | `String` | 否 | — |
| memberId | `Long` | 否 | — |
| status | `Integer` | 否 | — |
| statusName | `String` | 否 | — |
| reply | `String` | 否 | — |
| addTime | `String` | 否 | — |
| updateTime | `String` | 否 | — |


#### 4. POST `/admin/advise/reply`

**功能说明**：官方回复建议反馈

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `AdviseReplyRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**AdviseReplyRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 是 | 反馈记录id |
| reply | `String` | 是 | 官方回复内容 |
| status | `Integer` | 否 | 处理状态: 0-待处理, 1-处理中, 2-已解决, 3-已关闭(不传表示保持原状态) |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### AdminBusinessController

> 类路径：`cn.com.shopgroup.controller.AdminBusinessController`

> 接口数量：4

#### 1. GET `/admin/business/list`

**功能说明**：分页查询团长收款账户列表(含累计额度)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID（变量名 leaderId） |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GbOrgBusinessInfo>`（具体字段见下方表格） |

data 类型：`List<GbOrgBusinessInfo>`（数组，元素类型 `GbOrgBusinessInfo`，字段说明见下）

**GbOrgBusinessInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| busId | `Long` | 否 | 账户id,主键自增 |
| leaderId | `Long` | 否 | 团长id,外键 |
| busType | `Byte` | 否 | 账户类型,1=一般企业2=小微企业3=个体户4=个人 |
| busName | `String` | 否 | 账户名称 |
| legalName | `String` | 否 | 法人姓名 |
| cidNo | `String` | 否 | 身份证号 |
| cidFront | `String` | 否 | 身份证正面 |
| cidBack | `String` | 否 | 身份证反面 |
| licenseNo | `String` | 否 | 营业执照号 |
| licenseFront | `String` | 否 | 营业执照正面 |
| licenseBack | `String` | 否 | 营业执照反面 |
| busBalance | `Double` | 否 | 账户余额 |
| limitAmount | `Integer` | 否 | 限制最高收款金额(万) |
| taxLimit | `Integer` | 否 | 纳税额度,单位：万（收款提醒） |
| isClose | `Byte` | 否 | 是否禁用 |
| isCheck | `Byte` | 否 | 是否审核 |
| checkRemark | `String` | 否 | 审核备注 |
| checkCustId | `String` | 否 | 商户支付ID(审核通过后给予) |
| addTime | `Integer` | 否 | 添加时间 |


#### 2. GET `/admin/business/count`

**功能说明**：查询收款账户总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/business/info`

**功能说明**：查询收款账户详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GbOrgBusinessInfo`（具体字段见下方表格） |

data 类型：`GbOrgBusinessInfo`（字段说明见下）

**GbOrgBusinessInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| busId | `Long` | 否 | 账户id,主键自增 |
| leaderId | `Long` | 否 | 团长id,外键 |
| busType | `Byte` | 否 | 账户类型,1=一般企业2=小微企业3=个体户4=个人 |
| busName | `String` | 否 | 账户名称 |
| legalName | `String` | 否 | 法人姓名 |
| cidNo | `String` | 否 | 身份证号 |
| cidFront | `String` | 否 | 身份证正面 |
| cidBack | `String` | 否 | 身份证反面 |
| licenseNo | `String` | 否 | 营业执照号 |
| licenseFront | `String` | 否 | 营业执照正面 |
| licenseBack | `String` | 否 | 营业执照反面 |
| busBalance | `Double` | 否 | 账户余额 |
| limitAmount | `Integer` | 否 | 限制最高收款金额(万) |
| taxLimit | `Integer` | 否 | 纳税额度,单位：万（收款提醒） |
| isClose | `Byte` | 否 | 是否禁用 |
| isCheck | `Byte` | 否 | 是否审核 |
| checkRemark | `String` | 否 | 审核备注 |
| checkCustId | `String` | 否 | 商户支付ID(审核通过后给予) |
| addTime | `Integer` | 否 | 添加时间 |


#### 4. POST `/admin/business/add`

**功能说明**：添加团长收款账户(商户编号需唯一)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderBusinessRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderBusinessRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| leaderId | `Long` | 否 | 团长ID |
| shopName | `String` | 是 | 请输入店铺名称 |
| shopCode | `String` | 是 | 请输入商户编号 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）


### AdminGoodsController

> 类路径：`cn.com.shopgroup.controller.AdminGoodsController`

> 接口数量：4

#### 1. GET `/admin/goods/list`

**功能说明**：分页查询商品列表(含规格)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<AdminGoodsResponse>`（具体字段见下方表格） |

data 类型：`List<AdminGoodsResponse>`（数组，元素类型 `AdminGoodsResponse`，字段说明见下）

**AdminGoodsResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsId | `Long` | 否 | 商品id,主键自增 |
| catId | `Long` | 否 | 分类id,外键 |
| leaderId | `Long` | 否 | 团长id,外键 |
| goodsType | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| goodsName | `String` | 否 | 商品名称 |
| goodsImg | `String` | 否 | 商品主图 |
| costPrice | `Double` | 否 | 进货价格 |
| salesPrice | `Double` | 否 | 销售价格 |
| marketPrice | `Double` | 否 | 市场价格 |
| isStock | `Byte` | 否 | 是否启用库存 |
| goodsNum | `Integer` | 否 | 商品库存 |
| isLimit | `Byte` | 否 | 是否启用限购 |
| limitNum | `Integer` | 否 | 限购数量 |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| isClose | `Byte` | 否 | 是否禁用 0 开启 1关闭 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| addTime | `Integer` | 否 | 添加时间 |
| specList | `List<LeaderSpecResponse>` | 否 | 商品规格列表(包含规格值, 不含禁用规格/规格值); 调用方需按 goodsId 填充 |


**→LeaderSpecResponse 字段**（字段 `specList`（List<LeaderSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<LeaderSpecValResponse>` | 否 | — |


**→→LeaderSpecValResponse 字段**（字段 `valList`（List<LeaderSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


#### 2. GET `/admin/goods/count`

**功能说明**：查询商品总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/goods/info`

**功能说明**：查询商品详情(含规格)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`AdminGoodsResponse`（具体字段见下方表格） |

data 类型：`AdminGoodsResponse`（字段说明见下）

**AdminGoodsResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsId | `Long` | 否 | 商品id,主键自增 |
| catId | `Long` | 否 | 分类id,外键 |
| leaderId | `Long` | 否 | 团长id,外键 |
| goodsType | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| goodsName | `String` | 否 | 商品名称 |
| goodsImg | `String` | 否 | 商品主图 |
| costPrice | `Double` | 否 | 进货价格 |
| salesPrice | `Double` | 否 | 销售价格 |
| marketPrice | `Double` | 否 | 市场价格 |
| isStock | `Byte` | 否 | 是否启用库存 |
| goodsNum | `Integer` | 否 | 商品库存 |
| isLimit | `Byte` | 否 | 是否启用限购 |
| limitNum | `Integer` | 否 | 限购数量 |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsInfo | `String` | 否 | 商品介绍 |
| isClose | `Byte` | 否 | 是否禁用 0 开启 1关闭 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
| addTime | `Integer` | 否 | 添加时间 |
| specList | `List<LeaderSpecResponse>` | 否 | 商品规格列表(包含规格值, 不含禁用规格/规格值); 调用方需按 goodsId 填充 |


**→LeaderSpecResponse 字段**（字段 `specList`（List<LeaderSpecResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| specId | `Long` | 否 | — |
| specName | `String` | 否 | — |
| valList | `List<LeaderSpecValResponse>` | 否 | — |


**→→LeaderSpecValResponse 字段**（字段 `valList`（List<LeaderSpecValResponse>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| valId | `Long` | 否 | — |
| valName | `String` | 否 | — |


#### 4. GET `/admin/goods/img`

**功能说明**：查询商品缩略图列表(最多3张)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<String>`（具体字段见下方表格） |

data 类型：`List<String>`（数组，元素为基本类型，无子字段）


### AdminGroupController

> 类路径：`cn.com.shopgroup.controller.AdminGroupController`

> 接口数量：3

#### 1. GET `/admin/group/list`

**功能说明**：分页查询团购活动列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GbGroupActivityInfo>`（具体字段见下方表格） |

data 类型：`List<GbGroupActivityInfo>`（数组，元素类型 `GbGroupActivityInfo`，字段说明见下）

**GbGroupActivityInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 否 | 团购id,主键自增 |
| catId | `Long` | 否 | 团购分类id,外键 |
| leaderId | `Long` | 否 | 团长id,外键 |
| isolationId | `Integer` | 否 | 数据隔离id |
| pickupStyle | `Byte` | 否 | 商品提货方式,1自提2邮递 |
| groupName | `String` | 否 | 团购名称 |
| groupImg | `String` | 否 | 团购主图 |
| groupImg2 | `String` | 否 | 团购主图 |
| groupImg3 | `String` | 否 | 团购主图 |
| groupPrice | `Double` | 否 | 团购价格/最小价格 |
| groupPrice2 | `Double` | 否 | 团购价格/最大价格 |
| marketPrice | `Double` | 否 | 市场价格/划线价格 |
| startTime | `Integer` | 否 | 开团时间 |
| endTime | `Integer` | 否 | 结束时间 |
| groupInfo | `String` | 否 | 团购介绍 |
| orderTotal | `Integer` | 否 | 实际订单数量 |
| virtualOrder | `Integer` | 否 | 虚拟订单数量 |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |
| sortOrder | `Integer` | 否 | 排列顺序,平台算法 |
| staffId | `Long` | 否 | 添加人员id |
| staffName | `String` | 否 | 添加人员姓名 |
| isCheck | `Byte` | 否 | 平台审核 0 待审核 1 通过 2 不通过 |
| checkRemark | `String` | 否 | 审核备注 |
| addTime | `Integer` | 否 | 添加时间 |
| tagId | `Long` | 否 | 团购标签id,0未选择 |
| tagName | `String` | 否 | 团购标签名称,冗余展示 |
| pointId | `Long` | 否 | 自提点id,0未选择 |
| lists | `List<GbGroupActivityGoods>` | 否 | 团购商品列表 不是订单表里面的字段哦 |


**→GbGroupActivityGoods 字段**（字段 `lists`（List<GbGroupActivityGoods>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 否 | 团购id,外键 |
| goodsId | `Long` | 否 | 商品id,外键 |
| goodsName | `String` | 否 | 商品名称,冗余 |
| goodsType | `Byte` | 否 | 商品类型,冗余 |
| groupImg | `String` | 否 | 商品主图,冗余 |
| groupPrice | `Double` | 否 | 团购价格 |
| marketPrice | `Double` | 否 | 市场价格 |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |


#### 2. GET `/admin/group/count`

**功能说明**：查询团购活动总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/group/info`

**功能说明**：查询团购活动详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `Long` | Query 参数 | 是 | 业务主键 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GbGroupActivityInfo`（具体字段见下方表格） |

data 类型：`GbGroupActivityInfo`（字段说明见下）

**GbGroupActivityInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 否 | 团购id,主键自增 |
| catId | `Long` | 否 | 团购分类id,外键 |
| leaderId | `Long` | 否 | 团长id,外键 |
| isolationId | `Integer` | 否 | 数据隔离id |
| pickupStyle | `Byte` | 否 | 商品提货方式,1自提2邮递 |
| groupName | `String` | 否 | 团购名称 |
| groupImg | `String` | 否 | 团购主图 |
| groupImg2 | `String` | 否 | 团购主图 |
| groupImg3 | `String` | 否 | 团购主图 |
| groupPrice | `Double` | 否 | 团购价格/最小价格 |
| groupPrice2 | `Double` | 否 | 团购价格/最大价格 |
| marketPrice | `Double` | 否 | 市场价格/划线价格 |
| startTime | `Integer` | 否 | 开团时间 |
| endTime | `Integer` | 否 | 结束时间 |
| groupInfo | `String` | 否 | 团购介绍 |
| orderTotal | `Integer` | 否 | 实际订单数量 |
| virtualOrder | `Integer` | 否 | 虚拟订单数量 |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |
| sortOrder | `Integer` | 否 | 排列顺序,平台算法 |
| staffId | `Long` | 否 | 添加人员id |
| staffName | `String` | 否 | 添加人员姓名 |
| isCheck | `Byte` | 否 | 平台审核 0 待审核 1 通过 2 不通过 |
| checkRemark | `String` | 否 | 审核备注 |
| addTime | `Integer` | 否 | 添加时间 |
| tagId | `Long` | 否 | 团购标签id,0未选择 |
| tagName | `String` | 否 | 团购标签名称,冗余展示 |
| pointId | `Long` | 否 | 自提点id,0未选择 |
| lists | `List<GbGroupActivityGoods>` | 否 | 团购商品列表 不是订单表里面的字段哦 |


**→GbGroupActivityGoods 字段**（字段 `lists`（List<GbGroupActivityGoods>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupId | `Long` | 否 | 团购id,外键 |
| goodsId | `Long` | 否 | 商品id,外键 |
| goodsName | `String` | 否 | 商品名称,冗余 |
| goodsType | `Byte` | 否 | 商品类型,冗余 |
| groupImg | `String` | 否 | 商品主图,冗余 |
| groupPrice | `Double` | 否 | 团购价格 |
| marketPrice | `Double` | 否 | 市场价格 |
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |



### AdminGroupTagController

> 类路径：`cn.com.shopgroup.controller.AdminGroupTagController`

> 接口数量：6

#### 1. GET `/admin/tag/list`

**功能说明**：分页查询标签列表(含停用, 名称模糊)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，源码返回表达式：`tagService.getAdminTagList(keyword, page, pageSize)`，以接口实际返回为准）

#### 2. GET `/admin/tag/count`

**功能说明**：标签总数(与列表筛选条件一致, 供分页)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| keyword | `String` | Query 参数 | 否 | 搜索关键字 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/tag/info`

**功能说明**：标签详情

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| tagId | `Long` | Query 参数 | 是 | tagID（含义以接口实际为准） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`GbGroupTag`（具体字段见下方表格） |

data 类型：`GbGroupTag`（字段说明见下）

**GbGroupTag 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| tagId | `Long` | 否 | 标签id,主键自增 |
| tagName | `String` | 否 | 标签名称 |
| tagColor | `String` | 否 | 标签颜色,展示用 |
| sortOrder | `Integer` | 否 | 排序,越小越靠前 |
| status | `Byte` | 否 | 状态:1启用0停用 |
| addTime | `Integer` | 否 | 添加时间 |


#### 4. POST `/admin/tag/add`

**功能说明**：新增标签

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| tag | `GbGroupTag` | Body | 是 | 请求体对象，字段说明见下方表格 |


**GbGroupTag 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| tagId | `Long` | 否 | 标签id,主键自增 |
| tagName | `String` | 否 | 标签名称 |
| tagColor | `String` | 否 | 标签颜色,展示用 |
| sortOrder | `Integer` | 否 | 排序,越小越靠前 |
| status | `Byte` | 否 | 状态:1启用0停用 |
| addTime | `Integer` | 否 | 添加时间 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 5. POST `/admin/tag/edit`

**功能说明**：编辑标签(可修改名称/颜色/排序/状态启停, 未传字段保留原值)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| tag | `GbGroupTag` | Body | 是 | 请求体对象，字段说明见下方表格 |


**GbGroupTag 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| tagId | `Long` | 否 | 标签id,主键自增 |
| tagName | `String` | 否 | 标签名称 |
| tagColor | `String` | 否 | 标签颜色,展示用 |
| sortOrder | `Integer` | 否 | 排序,越小越靠前 |
| status | `Byte` | 否 | 状态:1启用0停用 |
| addTime | `Integer` | 否 | 添加时间 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 6. POST `/admin/tag/delete`

**功能说明**：删除标签(被团购活动使用中禁止删除, 可改为停用)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| tagId | `Long` | Query 参数 | 是 | tagID（含义以接口实际为准） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）


### AdminLeaderController

> 类路径：`cn.com.shopgroup.controller.AdminLeaderController`

> 接口数量：4

#### 1. GET `/admin/leader/list`

**功能说明**：分页查询团长列表(可按手机号筛选)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| mobile | `String` | Query 参数 | 是 | 手机号 |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GbOrgLeaderInfo>`（具体字段见下方表格） |

data 类型：`List<GbOrgLeaderInfo>`（数组，元素类型 `GbOrgLeaderInfo`，字段说明见下）

**GbOrgLeaderInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| leaderId | `Long` | 否 | 团长id,主键自增 |
| regionId | `Long` | 否 | 区域id,外键 |
| regionName | `String` | 否 | 区域名称,冗余 |
| leaderType | `Byte` | 否 | 团长类型,1=平台团长2=独立团长 |
| isolationId | `Integer` | 否 | 数据隔离id,平台团长固定值,独立团长分配唯一值 |
| leaderName | `String` | 否 | 团长姓名 |
| mobile | `String` | 否 | 手机号码 |
| nickname | `String` | 否 | 微信昵称 |
| avatar | `String` | 否 | 微信头像 |
| openid | `String` | 否 | openid,唯一（自动登录） |
| startTime | `Integer` | 否 | 开始时间,使用有效期 |
| endTime | `Integer` | 否 | 结束时间,使用有效期 |
| leaderBalance | `Double` | 否 | 账户余额 |
| commission | `Byte` | 否 | 平台抽成,千分率 |
| cashType | `Integer` | 否 | 结算到账方式,默认0=支付时延迟到账型,1=核销时延迟到账型 |
| numLimit | `Integer` | 否 | 发团数量限制（状态：在线） |
| remark | `String` | 否 | 系统备注 |
| isClose | `Byte` | 否 | 是否禁用 |
| addTime | `Integer` | 否 | 添加时间 |


#### 2. GET `/admin/leader/count`

**功能说明**：查询团长总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. POST `/admin/leader/add`

**功能说明**：添加团长(校验手机号/商户编号, 同步创建员工/店铺/收款账户)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | `String` | 是 | 请输入姓名 |
| mobile | `String` | 是 | 请输入手机号 |
| shopName | `String` | 是 | 请输入店铺名称 |
| shopCode | `String` | 是 | 请输入商户编号 |
| commission | `Byte` | 是 | 最小是3 |
| cashType | `Integer` | 是 | 结算到账方式,默认0=支付时延迟到账型,1=核销时延迟到账型(不传默认0) |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 4. GET `/admin/leader/select`

**功能说明**：团长下拉选项列表(id/名称)

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OptionResponse>`（具体字段见下方表格） |

data 类型：`List<OptionResponse>`（数组，元素类型 `OptionResponse`，字段说明见下）

**OptionResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| value | `int` | 否 | — |
| label | `String` | 否 | — |



### AdminLoginController

> 类路径：`cn.com.shopgroup.controller.AdminLoginController`

> 接口数量：2

#### 1. GET `/admin/login/kaptcha`

**功能说明**：获取后台登录图形验证码(Base64图片, 5分钟有效)

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`Base64 字符串格式
        String`（具体字段见下方表格） |

data 类型：`Base64 字符串格式
        String`（基本类型，无子字段）

#### 2. POST `/admin/login/submit`

**功能说明**：后台登录(验证码+账号密码, 返回token)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LoginRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LoginRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | `String` | 是 | 请输入账号 |
| password | `String` | 是 | 请输入密码 |
| code | `String` | 是 | 请输入验证码 |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`UserResponse`（具体字段见下方表格） |

data 类型：`UserResponse`（字段说明见下）

**UserResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | `String` | 否 | — |
| avatar | `String` | 否 | — |
| token | `String` | 否 | — |



### AdminMemberController

> 类路径：`cn.com.shopgroup.controller.AdminMemberController`

> 接口数量：2

#### 1. GET `/admin/member/list`

**功能说明**：分页查询会员列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<GbMemberInfo>`（具体字段见下方表格） |

data 类型：`List<GbMemberInfo>`（数组，元素类型 `GbMemberInfo`，字段说明见下）

**GbMemberInfo 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| memberId | `Long` | 否 | 用户id,主键自增 |
| mobile | `String` | 否 | 手机号码 |
| nickname | `String` | 否 | 微信昵称 |
| avatar | `String` | 否 | 微信头像 |
| openid | `String` | 否 | openid,唯一索引（自动登录） |
| mapId | `Long` | 否 | 地图定位id,外键 |
| mapName | `String` | 否 | 地图定位名称 |
| leaderId | `Long` | 否 | 团长id,外键/来源 |
| isolationId | `Integer` | 否 | 数据隔离id |
| ercode | `String` | 否 | 小程序二维码,给团长扫码使用 |
| isClose | `Byte` | 否 | 是否禁用 |
| addTime | `Integer` | 否 | 添加时间 |


#### 2. GET `/admin/member/count`

**功能说明**：查询会员总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）


### AdminOrderBusinessController

> 类路径：`cn.com.shopgroup.controller.AdminOrderBusinessController`

> 接口数量：2

#### 1. GET `/admin/orderbusiness/list`

**功能说明**：分页查询订单列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderBusinessInfoResponse>`（具体字段见下方表格） |

data 类型：`List<OrderBusinessInfoResponse>`（数组，元素类型 `OrderBusinessInfoResponse`，字段说明见下）

**OrderBusinessInfoResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| Id | `Long` | 否 | id,主键 |
| orderNo | `String` | 否 | 订单号 |
| busId | `Long` | 否 | 账户id,外键 |
| merchantNo | `String` | 否 | 易宝商户编号,冗余 |
| leaderId | `Long` | 否 | 团长id,外键 |
| groupName | `String` | 否 | 团购名称,冗余 |
| orderSn | `String` | 否 | 订单编号,冗余 |
| isSend | `Byte` | 否 | 是否发货,微信发货 |
| transactionId | `String` | 否 | 微信单号,微信发货 |
| openid | `String` | 否 | openid,微信发货 |
| sendTime | `Integer` | 否 | 发货时间,微信发货 |
| isUnfreeze | `Byte` | 否 | 是否解冻,微信冻结 |
| unfreezeTime | `Integer` | 否 | 解冻时间,微信冻结 |
| isDivide | `Byte` | 否 | 是否分账 |
| divideStatus | `String` | 否 | 分账状态 |
| divideTime | `Integer` | 否 | 分账时间 |
| divideNo | `String` | 否 | 分账流水号 |
| orderFee | `Double` | 否 | 订单金额，单位元 |
| receivedFee | `Double` | 否 | 实到金额，单位元 |
| busFee | `Double` | 否 | 分账金额，单位元 |
| serviceFee | `Double` | 否 | 平台服务费，单位元 |
| otherFee | `Double` | 否 | 其他佣金，单位元 |
| commStatus | `Byte` | 否 | 数据状态: 0=已支付(支付回调),1=已发货(定时任务),2=已解冻(t+2),3=已分账(定时任务),4=已提现(用户申请),5=已退款(用户申请) |
| addTime | `Integer` | 否 | 添加时间(下单时间) |


#### 2. GET `/admin/orderbusiness/count`

**功能说明**：查询订单总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）


### AdminOrderController

> 类路径：`cn.com.shopgroup.controller.AdminOrderController`

> 接口数量：2

#### 1. GET `/admin/order/list`

**功能说明**：分页查询订单列表

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| page | `int` | Query 参数 | 是 | 页码（从 1 开始） |
| pageSize | `int` | Query 参数 | 是 | 每页条数（默认 10） |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`List<OrderInfoResponse>`（具体字段见下方表格） |

data 类型：`List<OrderInfoResponse>`（数组，元素类型 `OrderInfoResponse`，字段说明见下）

**OrderInfoResponse 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| Id | `Long` | 否 | 订单id,主键自增 |
| orderNo | `String` | 否 | 订单号 |
| memberId | `Long` | 否 | 用户id,外键 |
| groupId | `Long` | 否 | 团购id,外键 |
| leaderId | `Long` | 否 | 团长id,外键 |
| isolationId | `Integer` | 否 | 数据隔离id |
| shopId | `Long` | 否 | 店铺id,冗余 |
| shopName | `String` | 否 | 店铺名称,冗余 |
| mobile | `String` | 否 | 手机号码,冗余 |
| nickname | `String` | 否 | 微信昵称,冗余 |
| avatar | `String` | 否 | 微信头像,冗余 |
| openid | `String` | 否 | openid,冗余 |
| groupName | `String` | 否 | 团购名称,冗余 |
| groupPrice | `Double` | 否 | 团购价格,冗余，单位元 |
| orderPrice | `Double` | 否 | 订单价格,冗余（帮卖价格），单位元 |
| busId | `Long` | 否 | 收款账户id,外键 |
| merchantNo | `String` | 否 | 易宝商户编号,冗余 |
| status | `Integer` | 否 | 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消 |
| payFee | `Double` | 否 | 支付金额，单位元 |
| payTime | `Integer` | 否 | 支付时间 |
| payNo | `String` | 否 | 支付流水号,来自支付接口 |
| refundFee | `Double` | 否 | 退款金额，单位元 |
| refundTime | `Integer` | 否 | 退款时间 |
| refundNo | `String` | 否 | 退款流水号,来自退款接口 |
| refundStaff | `String` | 否 | 退款人员 |
| refundReason | `String` | 否 | 拒退理由 |
| receiptType | `Byte` | 否 | 收货方式,1=自提2=邮寄 |
| trueName | `String` | 否 | 收货姓名,自提和邮寄都需要 |
| telephone | `String` | 否 | 收货电话,自提和邮寄都需要 |
| receiptTime | `Integer` | 否 | 收货时间 |
| verifyTime | `Integer` | 否 | 核销时间 |
| pointId | `Long` | 否 | 自提点id,自提/外键 |
| pointName | `String` | 否 | 自提点名称,自提 |
| pointAddress | `String` | 否 | 详细地址,自提 |
| receiptCode | `String` | 否 | 核销码,自提 |
| staffId | `Long` | 否 | 核销人员id |
| staffName | `String` | 否 | 核销人员姓名 |
| pointId2 | `Long` | 否 | 自提点id,实际领取自提点 |
| pointName2 | `String` | 否 | 自提点名称,实际领取自提点 |
| remark | `String` | 否 | 订单备注,c端客户使用 |
| addTime | `Integer` | 否 | 下单时间 |
| updateTime | `Integer` | 否 | 更新时间 |
| wxShipment | `Integer` | 否 | 微信发货是否已调用,0=未调用,1=已调用 |
| clickConfirmFlag | `Integer` | 否 | 确认收货操作标记,0=未操作,1=已操作 |
| goodsInfoList | `List<GbOrderGoodsInfo>` | 否 | 订单商品列表(订单表以外的关联字段) |


**→GbOrderGoodsInfo 字段**（字段 `goodsInfoList`（List<GbOrderGoodsInfo>））

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| Id | `Long` | 否 | id,主键/外键 |
| orderNo | `String` | 否 | — |
| goodsId | `Long` | 否 | 商品id,外键 |
| goodsName | `String` | 否 | 商品名称 |
| goodsPrice | `Double` | 否 | 商品价格 |
| goodsNum | `Integer` | 否 | 商品数量 |
| receiptNum | `Integer` | 否 | 收货数量 |
| applyRefund | `Integer` | 否 | 售后（退款）状态 0 无 1 待审核 2 同意 3 不同意 |
| applyRefundNum | `Integer` | 否 | 申请退中数量(仅记录用户本次申请退款/退货退款的数量, 用户申请时写入, 审核处理完成(同意/拒绝)后置0) |
| refundGoodsNum | `Integer` | 否 | 退货退款数量(退已收货部分, 申请累计, 含待审核/已同意/不同意) |
| refundNum | `Integer` | 否 | 退款数量(退待收货部分, 申请累计, 含待审核/已同意/不同意) |
| goodsUnit | `String` | 否 | 商品单位 |
| goodsImg | `String` | 否 | 商品图片 |
| goodsType | `Byte` | 否 | 商品类型,1普通商品2称重商品 |
| packId | `Long` | 否 | 包装id,外键(可能不存在) |
| packName | `String` | 否 | 包装名称 |
| packNum | `Integer` | 否 | 包装数量 |
| skuIds | `String` | 否 | skuids, 规格1+规格2+...... |
| skuNames | `String` | 否 | sku名称, 规格1+规格2+...... |
| skuId | `Long` | 否 | skuid, 外键(可能不存在) |
| addTime | `Integer` | 否 | 下单时间 |
| updateTime | `Integer` | 否 | 更改时间 |


#### 2. GET `/admin/order/count`

**功能说明**：查询订单总数

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）


## 5. gb-group-task（定时任务）


### TaskController

> 类路径：`cn.com.shopgroup.controller.TaskController`

> 接口数量：8

#### 1. GET `/task/order/send`

**功能说明**：微信订单发货

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 2. GET `/task/order/divide`

**功能说明**：订单分账

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 3. GET `/task/order/query`

**功能说明**：查询订单

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 4. GET `/task/order/refund`

**功能说明**：分账前部分退款，存在问题，实际到款金额-退款金额-平台服务费=商户分账金额。 分账后部分退款的话，没有问题，子商户本来就自己承担平台服务费的差额。 退款(原始订单金额, 包括易宝手续费) 同步原始订单表和商户订单表的退款状态

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 5. GET `/task/order/cash`

**功能说明**：提现

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `int` | Query 参数 | 是 | 业务主键 ID |
| val | `int` | Query 参数 | 是 | 数值 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 6. GET `/task/order/verify`

**功能说明**：自动完成收货(手动触发, 与定时任务同一套逻辑): 已分账核销、核销满7天仍未完成收货的订单自动完成

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 7. GET `/task/order/backstock`

**功能说明**：超时未支付订单自动取消并恢复库存(手动触发, 与定时任务同一套逻辑)

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 8. GET `/task/test/user`

**功能说明**：查询文章信息(联调测试接口)

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| aId | `Long` | Query 参数 | 是 | 用户 ID |

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，源码返回表达式：`articleInfoService.getMiniArticleInfo(aId)`，以接口实际返回为准）

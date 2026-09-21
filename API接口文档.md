# ShopGroupBuyApi 接口文档（详细版）

> 自动生成时间：2026-09-19 15:20:28

> 生成方式：扫描各模块 `*Controller.java` 源码（`python3 generate_api_doc.py` 可重新生成）

> 请求头公共参数：`token`（用户登录令牌）、`lid`（团长id）、`sid`（员工id），实际以各接口校验为准

> 参数约定：Query/Header 参数「必填」默认「是」（`@RequestParam` 默认必填，标注 `required=false` 则为「否」）；Body 请求对象各字段的「必填」取自字段校验注解（`@NotNull` 等），未注解时以实际逻辑为准

> 分页约定：列表类接口一般通过 `page`（页码，从 1 开始）/ `pageSize`（每页条数）分页，配套 `count` 接口获取总数

> 统一出参：所有接口返回 `JsonResult`（`code` 状态码 / `msg` 提示信息 / `data` 业务数据），`data` 字段说明见各接口；嵌套对象字段以 `→` 前缀递进展开

> 金额约定：接口返回的金额字段统一为 `Double`，单位「元」（最多 2 位小数）；字段名含 `fee` / `amount` / `price` / `money` / `balance` 的均为金额。数据库实体（`model` 包）内部仍以「分」存储，由 Response 层经 `MoneyUtil.centToYuan` 转换后返回，接口不暴露「分」

> 功能说明：优先取源码方法注释；源码无注释时由脚本依据路径、入参与出参自动推断（表格中显示为 *斜体*，仅供参考）；入参「位置」为 Query 参数 / Body（`@RequestBody` 对象）/ 请求头

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

## 近期变更

> 以下为最近 15 条与接口定义相关的提交（来源 `git log`, 由 `generate_api_doc.py` 自动生成）；
> 完整变更请查阅 git 提交记录。

| 日期 | 提交 | 摘要 | 涉及文件 |
| --- | --- | --- | --- |
| 2026-09-19 | `20da595` | 核销记录 | MemberOrderController.java |
| 2026-09-19 | `523da65` | 核销记录 | LeaderRefundApplyGoodsResponse.java, LeaderRefundApplyListRequest.java, LeaderRefundApplyListResponse.java, LeaderRefundApplyResponse.java, LeaderShopController.java 等 |
| 2026-09-18 | `e803c05` | 退款 | LeaderGroupGenTuanResponse.java, MemberOrderController.java, OrderRefundController.java, OrderRefundNotifyController.java, OrderRefundRecordResponse.java |
| 2026-09-18 | `c385c3d` | 处理小程序码 | MemberOrderController.java |
| 2026-09-17 | `560d13f` | 商品/团购活动 | LeaderGoodsManageController.java, LeaderGroupManageController.java, MemberGroupController.java, MemberOrderController.java, OrderGoodsReponse.java 等 |
| 2026-09-17 | `be989fa` | 二维码 | AdminGoodsController.java, AdminGoodsResponse.java, GroupActGoodsResponse.java, LeaderGroupActivityController.java, LeaderGroupActivityResponse.java 等 |
| 2026-09-16 | `b8b257a` | redis分模块分区 | GroupGoodsController.java, LeaderGroupManageController.java, LeaderShopController.java |
| 2026-09-16 | `b3454cd` | 活动相关 | LeaderGroupManageController.java |
| 2026-09-16 | `1621b8b` | 活动相关 | AdminOrderBusinessController.java, AdminOrderController.java, GroupActResponse.java, GroupActivityResponse.java, LeaderGoodsManageController.java 等 |
| 2026-09-15 | `b144578` | 团购活动列表汇总数据 | GroupActResponse.java, LeaderGroupManageController.java, LeaderGroupSummaryResponse.java |
| 2026-09-15 | `0226057` | 团购新增。编辑 | LeaderGroupManageController.java |
| 2026-09-15 | `b4cde52` | 团购活动修改，下单15分钟不支付延迟取消 | LeaderGroupManageController.java, MemberGroupController.java, MemberOrderController.java |
| 2026-09-14 | `ddebc5e` | 限购逻辑 | LeaderGoodsManageController.java, OrderRefundController.java, OrderRefundNotifyController.java |
| 2026-09-13 | `013cbf8` | 限购逻辑 | GroupOrderController.java, LeaderGoodsManageController.java, LeaderGroupManageController.java, MemberGroupController.java, MemberOrderController.java 等 |
| 2026-09-13 | `6e944a0` | 优化 | GroupActGoodsResponse.java, GroupGoodsController.java, GroupOrderController.java, LeaderBillListRequest.java, LeaderGoodsManageController.java 等 |

## 目录

1. [**gb-group-user**（用户/团长/员工）](#1-gb-group-user用户团长员工) — 46 个接口
2. [**gb-group-order**（订单/退款/分账）](#2-gb-group-order订单退款分账) — 47 个接口
3. [**gb-group-goods**（商品/团购）](#3-gb-group-goods商品团购) — 22 个接口
4. [**gb-group-admin**（后台管理）](#4-gb-group-admin后台管理) — 29 个接口
5. [**gb-group-task**（定时任务）](#5-gb-group-task定时任务) — 8 个接口

---

## 接口总览索引

> 共 152 个接口，按下表序号定位到下方各模块接口详情；「功能说明」列为 `—` 表示源码无方法注释，可按入参 / 出参字段推断用途。

| 序号 | 模块 | 方式 | 路径 | 功能说明 |
| --- | --- | --- | --- | --- |
| [1](#1-post-userimageuploadavatar) | user | POST | `/user/image/upload/avatar` | *上传头像文件* |
| [2](#2-post-userimageuploadgoods) | user | POST | `/user/image/upload/goods` | *上传商品文件* |
| [3](#3-post-userimageuploadbanner) | user | POST | `/user/image/upload/banner` | *上传轮播图文件* |
| [4](#4-get-userimageaccessfile) | user | GET | `/user/image/access/{*file}` | *访问图片（静态资源）* |
| [5](#5-get-usergroupblack) | user | GET | `/user/group/black` | *查询团购黑名单* |
| [6](#6-get-usergrouppoint) | user | GET | `/user/group/point` | *查询团购自提点* |
| [7](#7-get-userarticleinfo) | user | GET | `/user/article/info` | *查询文章详情* |
| [8](#8-get-userfocus) | user | GET | `/user/focus` | *查询关注* |
| [9](#9-get-userleadermembermobile) | user | GET | `/user/leader/member/mobile` | *查询团长会员手机号* |
| [10](#10-post-userleaderaddblack) | user | POST | `/user/leader/add/black` | *提交（团长黑名单）* |
| [11](#11-get-userleaderblackmobile) | user | GET | `/user/leader/black/mobile` | *查询团长黑名单手机号* |
| [12](#12-post-userleadermemberblackremove) | user | POST | `/user/leader/member/black/remove` | *删除团长会员黑名单* |
| [13](#13-get-userleaderblacklist) | user | GET | `/user/leader/black/list` | *查询团长黑名单列表* |
| [14](#14-get-userleaderblackcount) | user | GET | `/user/leader/black/count` | *查询团长黑名单总数* |
| [15](#15-get-userleaderbusinesslist) | user | GET | `/user/leader/business/list` | 查询当前团长收款账户列表（含Redis累计额度） |
| [16](#16-post-userleaderbusinessadd) | user | POST | `/user/leader/business/add` | 添加团长收款账户（校验证件号码唯一，返回新增账户ID） |
| [17](#17-post-userleaderbusinessedit) | user | POST | `/user/leader/business/edit` | 修改收款账户（已审核通过的账户不允许修改） |
| [18](#18-post-userleaderbusinessclose) | user | POST | `/user/leader/business/close` | 关闭/启用收款账户（禁用或启用商户收款） |
| [19](#19-get-userleaderpointlist) | user | GET | `/user/leader/point/list` | *查询团长自提点列表*（全量含已作废，无搜索词走缓存） |
| [20](#20-get-userleaderpointaddgrouplist) | user | GET | `/user/leader/point/addGroup/list` | *查询团长自提点团购列表*（仅未作废，走缓存） |
| [21](#21-post-userleaderpointadd) | user | POST | `/user/leader/point/add` | *新增团长自提点*（成功后失效列表缓存） |
| [22](#22-post-userleaderpointedit) | user | POST | `/user/leader/point/edit` | *修改团长自提点*（成功后失效列表+详情缓存） |
| [23](#23-get-userleaderpointclose) | user | GET | `/user/leader/point/close` | *启用/关闭团长自提点*（作废/恢复切换，失效列表+详情缓存） |
| [24](#24-get-userleaderpointinfo) | user | GET | `/user/leader/point/info` | *查询团长自提点详情*（走缓存） |
| [25](#25-get-userleaderpointercode) | user | GET | `/user/leader/point/ercode` | *查询团长自提点二维码*（已生成直接返回，未生成则生成后失效详情缓存） |
| [26](#26-get-userleadershopinfo) | user | GET | `/user/leader/shop/info` | 查看店铺信息 |
| [27](#27-post-userleadershopsave) | user | POST | `/user/leader/shop/save` | *保存团长店铺* |
| [28](#28-post-userleadershopupdate) | user | POST | `/user/leader/shop/update` | *修改团长店铺* |
| [29](#29-get-userleadergetgroupshop) | user | GET | `/user/leader/getGroup/shop` | *查询团长团购店铺* |
| [30](#30-post-userleadershopmakeqrcode) | user | POST | `/user/leader/shop/makeQrCode` | *生成二维码（团长店铺）* |
| [31](#31-get-userleadermessagelist) | user | GET | `/user/leader/message/list` | *查询团长消息列表* |
| [32](#32-get-userleadermessagecount) | user | GET | `/user/leader/message/count` | *查询团长消息总数* |
| [33](#33-get-userleadermessageunread) | user | GET | `/user/leader/message/unread` | *查询团长消息未读* |
| [34](#34-get-userleadermessageread) | user | GET | `/user/leader/message/read` | *标记已读（团长消息）* |
| [35](#35-get-userleaderstafflist) | user | GET | `/user/leader/staff/list` | *查询团长员工列表* |
| [36](#36-post-userleaderstaffadd) | user | POST | `/user/leader/staff/add` | *新增团长员工* |
| [37](#37-post-userleaderstaffedit) | user | POST | `/user/leader/staff/edit` | *修改团长员工* |
| [38](#38-post-userleaderstaffclose) | user | POST | `/user/leader/staff/close` | *启用/关闭团长员工* |
| [39](#39-post-userleaderstaffremove) | user | POST | `/user/leader/staff/remove` | *删除团长员工* |
| [40](#40-get-userphone) | user | GET | `/user/phone` | *查询手机号* |
| [41](#41-get-useropenid) | user | GET | `/user/openid` | *查询openid* |
| [42](#42-get-userlogin) | user | GET | `/user/login` | *登录* |
| [43](#43-post-userreg) | user | POST | `/user/reg` | *注册* |
| [44](#44-post-userlogout) | user | POST | `/user/logout` | *退出登录* |
| [45](#45-get-usermemberinfo) | user | GET | `/user/member/info` | *查询会员详情* |
| [46](#46-get-usermemberisleader) | user | GET | `/user/member/isleader` | *查询会员是否为团长* |
| [47](#47-get-orderorderbusinesslist) | order | GET | `/order/orderbusiness/list` | *查询订单收款账户列表* |
| [48](#48-get-orderorderbusinesscount) | order | GET | `/order/orderbusiness/count` | *查询订单收款账户总数* |
| [49](#49-post-ordergroupadd) | order | POST | `/order/group/add` | *新增团购* |
| [50](#50-get-ordergroupgroupactivitycat) | order | GET | `/order/group/groupActivity/cat` | *查询团购活动分类* |
| [51](#51-post-ordermembergroupactivitylist) | order | POST | `/order/member/groupActivity/list` | *查询会员团购活动列表* |
| [52](#52-get-ordergroupgroupactivityinfo) | order | GET | `/order/group/groupActivity/info` | *查询团购活动详情* |
| [53](#53-post-ordergroupgroupactivityview) | order | POST | `/order/group/groupActivity/view` | *查看（团购活动）* |
| [54](#54-get-ordergroupgroupactivityshop) | order | GET | `/order/group/groupActivity/shop` | *查询团购活动店铺* |
| [55](#55-get-ordergroupgroupactivitylogs) | order | GET | `/order/group/groupActivity/logs` | *查询团购活动日志* |
| [56](#56-get-ordergroupgroupactivitylogs2) | order | GET | `/order/group/groupActivity/logs2` | *查询团购活动日志* |
| [57](#57-get-ordergrouporderrecords) | order | GET | `/order/group/order/records` | 真实跟团记录：基于支付成功订单数据 |
| [58](#58-post-ordergrouporderlist) | order | POST | `/order/group/order/list` | *查询团购订单列表* |
| [59](#59-post-ordergrouporderapplyrefundlist) | order | POST | `/order/group/order/applyRefundList` | *查询团购订单退款申请列表* |
| [60](#60-get-ordergroupordercount) | order | GET | `/order/group/order/count` | *查询团购订单总数* |
| [61](#61-get-ordergrouporderinfo) | order | GET | `/order/group/order/info` | *查询团购订单详情* |
| [62](#62-get-ordergroupordermakeercode) | order | GET | `/order/group/order/makeErcode` | *生成二维码（团购订单）* |
| [63](#63-get-ordergrouporderreceipt) | order | GET | `/order/group/order/receipt` | 用户扫码核销-整单核销（核销订单下全部剩余可核销商品）<br>核销后订单状态：已有售后(5)保持不变；无售后则置已收货(3)并记录收货时间 |
| [64](#64-post-ordergrouporderpartreceipt) | order | POST | `/order/group/order/part/receipt` | 用户扫码核销-部分核销（仅核销所选商品行, 可多次核销）<br>核销后订单状态：已有售后(5)保持不变；无售后且完全核销置已收货(3)+收货时间, 未完全核销置部分收货(2) |
| [65](#65-post-ordergrouporderapplyrefund) | order | POST | `/order/group/order/apply/refund` | *退款（团购订单）* |
| [66](#66-get-ordergrouporderrefundreasonlist) | order | GET | `/order/group/order/refund/reasonList` | *查询团购订单退款原因列表* |
| [67](#67-get-ordergrouporderrefundrecodes) | order | GET | `/order/group/order/refund/recodes` | *查询团购订单退款记录* |
| [68](#68-get-ordergroupordernotallreceiptlist) | order | GET | `/order/group/order/notAllReceiptList` | 查询团购订单未全部提货列表<br>用户id+团长id查询(shopId仅用于解析团长id并校验店铺), 已支付且状态1待收货/2部分收货/5售后, 须存在未核销商品(购买数>已核销数); 状态5时商品行还须 购买数>(已核销数+退款数); 返回订单并回填商品信息 |
| [69](#69-post-ordergrouporderconfirmshipping) | order | POST | `/order/group/order/confirmShipping` | *确认发货（团购订单）* |
| [70](#70-post-ordergrouporderapplyrefundorderinfo) | order | POST | `/order/group/order/applyRefund/orderInfo` | *查询团购订单退款申请订单详情* |
| [71](#71-get-ordergroupwxorder) | order | GET | `/order/group/wx/order` | *查询团购微信订单* |
| [72](#72-post-orderleaderbilllist) | order | POST | `/order/leader/bill/list` | 团长端-对账单 |
| [73](#73-get-orderleaderactivitylist) | order | GET | `/order/leader/activity/list` | 团长端-团购活动查询  提供团长端活动下拉/选择所需的接口；活动主数据在商品域(goods), 本控制器通过 goods 模块的 {@link GbGroupActivityInfoService} 跨模块调用，避免在 order 模块重复建表/写 SQL。 |
| [74](#74-post-orderleadermymemberlist) | order | POST | `/order/leader/myMember/list` | 我的团员列表（支持手机号/昵称搜索，分页） |
| [75](#75-get-orderleadermymemberdetail) | order | GET | `/order/leader/myMember/detail` | 团员详情（消费/退款/跟团次数/查看次数/动态） |
| [76](#76-post-orderleaderorderlist) | order | POST | `/order/leader/order/list` | *查询团长订单列表* |
| [77](#77-post-orderleaderapplyrefundlist) | order | POST | `/order/leader/apply/refundList` | *查询团长退款列表* |
| [78](#78-get-orderleaderordercount) | order | GET | `/order/leader/order/count` | *查询团长订单总数* |
| [79](#79-get-orderleaderorderstatus) | order | GET | `/order/leader/order/status` | *查询团长订单状态* |
| [80](#80-post-orderleaderorderscanqrcode) | order | POST | `/order/leader/order/scanQRCode` | *扫码核销（团长订单）* |
| [81](#81-get-orderleaderorderquery) | order | GET | `/order/leader/order/query` | *查询团长订单* |
| [82](#82-post-orderleaderorderwriteoff) | order | POST | `/order/leader/order/writeOff` | *核销（团长订单）* |
| [83](#83-post-orderleaderorderpartwriteoff) | order | POST | `/order/leader/order/partWriteOff` | *部分核销（团长订单）* |
| [84](#84-get-orderleaderordersend) | order | GET | `/order/leader/order/send` | *发送（团长订单）* |
| [85](#85-get-orderleaderhomeshoworders) | order | GET | `/order/leader/home/show/orders` | *查询团长首页展示订单* |
| [86](#86-get-orderleaderhomeordergoodssummary) | order | GET | `/order/leader/home/order/goodsSummary` | *查询团长首页订单商品* |
| [87](#87-post-ordergetgroupactivitytotalorder) | order | POST | `/order/get/groupActivity/totalOrder` | *提交（团购活动汇总订单）* |
| [88](#88-get-orderleaderrefundcount) | order | GET | `/order/leader/refund/count` | *查询团长退款总数* |
| [89](#89-post-orderleaderrefundapplylist) | order | POST | `/order/leader/refund/applyList` | 查询团长退款列表（待审核申请）<br>口径: 订单状态5售后+存在待审核(apply_refund=1)商品行, keyword匹配手机号/商品名; 先筛选再分页, total与列表一致, 每单回填该笔申请的整笔待审核商品行 |
| [90](#90-post-orderleaderrefundapprove) | order | POST | `/order/leader/refund/approve` | *审核（团长退款）* |
| [91](#91-post-orderleaderrefundnotify) | order | POST | `/order/leader/refund/notify` | *回调（团长退款）* |
| [92](#92-get-orderpaymentorderpay) | order | GET | `/order/payment/order/pay` | *支付（支付订单）* |
| [93](#93-post-orderpaymentordernotify) | order | POST | `/order/payment/order/notify` | *回调（支付订单）* |
| [94](#94-get-goodsgroupgoodslist) | goods | GET | `/goods/group/goods/list` | *查询团购商品列表* |
| [95](#95-get-goodsgroupgoodsstock) | goods | GET | `/goods/group/goods/stock` | *查询团购商品库存* |
| [96](#96-get-goodsgetgoodscat) | goods | GET | `/goods/get/goods/cat` | *查询商品分类* |
| [97](#97-get-goodsleadergoodsonline) | goods | GET | `/goods/leader/goods/online` | *查询团长商品上架* |
| [98](#98-get-goodsleadergoodslist) | goods | GET | `/goods/leader/goods/list` | *查询团长商品列表* |
| [99](#99-get-goodsleadergoodscount) | goods | GET | `/goods/leader/goods/count` | *查询团长商品总数* |
| [100](#100-get-goodsleadergoodsinfo) | goods | GET | `/goods/leader/goods/info` | *查询团长商品详情* |
| [101](#101-post-goodsleadergoodsaddgoods) | goods | POST | `/goods/leader/goods/addGoods` | *新增商品团长商品* |
| [102](#102-post-goodsleadergoodsedit) | goods | POST | `/goods/leader/goods/edit` | *修改团长商品* |
| [103](#103-get-goodsleadergoodsclose) | goods | GET | `/goods/leader/goods/close` | *启用/关闭团长商品* |
| [104](#104-get-goodsleadergoodsskuspec) | goods | GET | `/goods/leader/goods/sku/spec` | *查询团长商品SKU规格* |
| [105](#105-post-goodsleadergoodsskusave) | goods | POST | `/goods/leader/goods/sku/save` | *保存团长商品SKU* |
| [106](#106-post-goodsleadergetgroupactivitylist) | goods | POST | `/goods/Leader/get/groupActivity/list` | 团长端-查询所有团购活动列表<br>返回每个团购活动的订单汇总数据 `groupSummaryResponse`（实际收入、退款金额、跟团人数）, 由 LeaderGroupSummaryService 批量聚合 gb_order_info 得出; 已取消订单不计入。 |
| [107](#107-get-goodsleadergetgroupactivitycount) | goods | GET | `/goods/Leader/get/groupActivity/count` | *查询团长团购活动总数* |
| [108](#108-get-goodsleadergroupactivitytaglist) | goods | GET | `/goods/Leader/groupActivity/tag/list` | *查询团长团购活动标签列表* |
| [109](#109-post-goodsleadergroupactivityadd) | goods | POST | `/goods/Leader/groupActivity/add` | *新增团长团购活动* |
| [110](#110-get-goodsleadergetgroupactivityinfo) | goods | GET | `/goods/Leader/get/groupActivity/info` | 查询团长团购活动详情<br>返回跟团统计 `genTuanResponse` 与跟团记录 `followRecords`（真实订单数据: 手机号/姓名/头像/购买时间/购买商品/数量, 已支付未取消, 按购买时间倒序取最新50条） |
| [111](#111-post-goodsleadergroupactivityedit) | goods | POST | `/goods/Leader/groupActivity/edit` | *修改团长团购活动* |
| [112](#112-post-goodsleadergroupactivityclose) | goods | POST | `/goods/Leader/groupActivity/close` | *启用/关闭团长团购活动* |
| [113](#113-get-goodsleadergetgroupactivitycat) | goods | GET | `/goods/Leader/get/groupActivity/cat` | *查询团长团购活动分类* |
| [114](#114-post-goodsleadersharegroupactivityposter) | goods | POST | `/goods/Leader/share/groupActivity/poster` | *提交（团长分享团购活动海报）* |
| [115](#115-post-goodsleadersharegroupactivitymakeposter) | goods | POST | `/goods/Leader/share/groupActivity/make/poster` | *提交（团长分享团购活动海报）* |
| [116](#116-get-adminbusinesslist) | admin | GET | `/admin/business/list` | *查询收款账户列表* |
| [117](#117-get-adminbusinesscount) | admin | GET | `/admin/business/count` | *查询收款账户总数* |
| [118](#118-get-adminbusinessinfo) | admin | GET | `/admin/business/info` | *查询收款账户详情* |
| [119](#119-post-adminbusinessadd) | admin | POST | `/admin/business/add` | *新增收款账户* |
| [120](#120-get-admingoodslist) | admin | GET | `/admin/goods/list` | *查询商品列表* |
| [121](#121-get-admingoodscount) | admin | GET | `/admin/goods/count` | *查询商品总数* |
| [122](#122-get-admingoodsinfo) | admin | GET | `/admin/goods/info` | *查询商品详情* |
| [123](#123-get-admingoodsimg) | admin | GET | `/admin/goods/img` | *查询商品图片* |
| [124](#124-get-admingrouplist) | admin | GET | `/admin/group/list` | *查询团购列表* |
| [125](#125-get-admingroupcount) | admin | GET | `/admin/group/count` | *查询团购总数* |
| [126](#126-get-admingroupinfo) | admin | GET | `/admin/group/info` | *查询团购详情* |
| [127](#127-get-admintaglist) | admin | GET | `/admin/tag/list` | *查询标签列表* |
| [128](#128-get-admintagcount) | admin | GET | `/admin/tag/count` | *查询标签总数* |
| [129](#129-get-admintaginfo) | admin | GET | `/admin/tag/info` | *查询标签详情* |
| [130](#130-post-admintagadd) | admin | POST | `/admin/tag/add` | *新增标签* |
| [131](#131-post-admintagedit) | admin | POST | `/admin/tag/edit` | *修改标签* |
| [132](#132-post-admintagdelete) | admin | POST | `/admin/tag/delete` | *删除标签* |
| [133](#133-get-adminleaderlist) | admin | GET | `/admin/leader/list` | *查询团长列表* |
| [134](#134-get-adminleadercount) | admin | GET | `/admin/leader/count` | *查询团长总数* |
| [135](#135-post-adminleaderadd) | admin | POST | `/admin/leader/add` | *新增团长* |
| [136](#136-get-adminleaderselect) | admin | GET | `/admin/leader/select` | *查询团长* |
| [137](#137-get-adminloginkaptcha) | admin | GET | `/admin/login/kaptcha` | *查询验证码* |
| [138](#138-post-adminloginsubmit) | admin | POST | `/admin/login/submit` | *提交* |
| [139](#139-get-adminmemberlist) | admin | GET | `/admin/member/list` | *查询会员列表* |
| [140](#140-get-adminmembercount) | admin | GET | `/admin/member/count` | *查询会员总数* |
| [141](#141-get-adminorderbusinesslist) | admin | GET | `/admin/orderbusiness/list` | *查询订单收款账户列表* |
| [142](#142-get-adminorderbusinesscount) | admin | GET | `/admin/orderbusiness/count` | *查询订单收款账户总数* |
| [143](#143-get-adminorderlist) | admin | GET | `/admin/order/list` | *查询订单列表* |
| [144](#144-get-adminordercount) | admin | GET | `/admin/order/count` | *查询订单总数* |
| [145](#145-get-taskordersend) | task | GET | `/task/order/send` | *发送（订单）* |
| [146](#146-get-taskorderdivide) | task | GET | `/task/order/divide` | *分账（订单）* |
| [147](#147-get-taskorderquery) | task | GET | `/task/order/query` | *查询订单* |
| [148](#148-get-taskorderrefund) | task | GET | `/task/order/refund` | *退款（订单）* |
| [149](#149-get-taskordercash) | task | GET | `/task/order/cash` | *查询订单提现* |
| [150](#150-get-taskorderverify) | task | GET | `/task/order/verify` | *核销（订单）* |
| [151](#151-get-taskorderbackstock) | task | GET | `/task/order/backstock` | *回库（订单）* |
| [152](#152-get-tasktestuser) | task | GET | `/task/test/user` | *查询测试用户* |

---


## 1. gb-group-user（用户/团长/员工）


### ImageSourceController

> 类路径：`cn.com.shopgroup.user.controller.ImageSourceController`

> 接口数量：4

#### 1. POST `/user/image/upload/avatar`

**功能说明**：*上传头像文件*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 2. POST `/user/image/upload/goods`

**功能说明**：*上传商品文件*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 3. POST `/user/image/upload/banner`

**功能说明**：*上传轮播图文件*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`String`（具体字段见下方表格） |

data 类型：`String`（基本类型，无子字段）

#### 4. GET `/user/image/access/{*file}`

**功能说明**：*访问图片（静态资源）*（自动推断）

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

**功能说明**：*查询团购黑名单*（自动推断）

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

**功能说明**：*查询团购自提点*（自动推断）

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

**功能说明**：*查询文章详情*（自动推断）

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

**功能说明**：*查询关注*（自动推断）

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

**功能说明**：*查询团长会员手机号*（自动推断）

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

**功能说明**：*提交（团长黑名单）*（自动推断）

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

**功能说明**：*查询团长黑名单手机号*（自动推断）

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

**功能说明**：*删除团长会员黑名单*（自动推断）

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

**功能说明**：*查询团长黑名单列表*（自动推断）

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

**功能说明**：*查询团长黑名单总数*（自动推断）

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

**功能说明**：*查询团长自提点列表*（管理端全量列表，含已作废的自提点；不带搜索词时结果走 Redis 缓存，30 天，写操作后失效）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| name | `String` | Query 参数 | 否 | 名称模糊搜索；不传时返回全量列表并使用缓存，带搜索词时直接查库不缓存 |

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


#### 2. GET `/user/leader/point/addGroup/list`

**功能说明**：*查询团长自提点团购列表*（添加团购活动时选择自提点用；仅返回未作废（is_close=0）的自提点，按 pointId 升序，最多 20 条；结果走 Redis 缓存，写操作后失效）

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

**功能说明**：*新增团长自提点*（成功后失效 C 端与管理端的列表缓存；scope 不传默认 20 公里）

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

**功能说明**：*修改团长自提点*（仅能修改属于当前登录团长的自提点；成功后失效列表缓存与详情缓存）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `PointRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**PointRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | 是 | 自提点Id（更新定位条件，必传） |
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
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）

#### 5. GET `/user/leader/point/close`

**功能说明**：*启用/关闭团长自提点*（作废/恢复切换：已启用则作废，已作废则恢复；成功后失效列表缓存与详情缓存）

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

**功能说明**：*查询团长自提点详情*（结果走 Redis 缓存，30 天，编辑/作废/二维码生成后失效）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pointId | `Long` | Query 参数 | 是 | 提货点（自提点）ID（null 或 <=0 时返回参数错误） |

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

**功能说明**：*查询团长自提点二维码*（读取详情走缓存；若已生成过直接返回库中的二维码地址，否则调用微信接口生成小程序码并上传存储；自提点不存在时返回 300 数据不存在）

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

**功能说明**：*保存团长店铺*（自动推断）

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

**功能说明**：*修改团长店铺*（自动推断）

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

**功能说明**：*查询团长团购店铺*（自动推断）

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

**功能说明**：*生成二维码（团长店铺）*（自动推断）

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

**功能说明**：*查询团长消息列表*（自动推断）

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

**功能说明**：*查询团长消息总数*（自动推断）

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

**功能说明**：*查询团长消息未读*（自动推断）

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

**功能说明**：*标记已读（团长消息）*（自动推断）

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

**功能说明**：*查询团长员工列表*（自动推断）

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

**功能说明**：*新增团长员工*（自动推断）

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

**功能说明**：*修改团长员工*（自动推断）

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

**功能说明**：*启用/关闭团长员工*（自动推断）

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

**功能说明**：*删除团长员工*（自动推断）

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


### LoginController

> 类路径：`cn.com.shopgroup.user.controller.member.LoginController`

> 接口数量：5

#### 1. GET `/user/phone`

**功能说明**：*查询手机号*（自动推断）

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

**功能说明**：*查询openid*（自动推断）

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

**功能说明**：*登录*（自动推断）

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

**功能说明**：*注册*（自动推断）

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

**功能说明**：*退出登录*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：无（接口仅返回操作结果，data 为 null）


### MemberController

> 类路径：`cn.com.shopgroup.user.controller.member.MemberController`

> 接口数量：2

#### 1. GET `/user/member/info`

**功能说明**：*查询会员详情*（自动推断）

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

**功能说明**：*查询会员是否为团长*（自动推断）

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



## 2. gb-group-order（订单/退款/分账）


### OrderBusinessController

> 类路径：`cn.com.shopgroup.order.controller.OrderBusinessController`

> 接口数量：2

#### 1. GET `/order/orderbusiness/list`

**功能说明**：*查询订单收款账户列表*（自动推断）

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

**功能说明**：*查询订单收款账户总数*（自动推断）

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

**功能说明**：*新增团购*（自动推断）

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
| remark | `String` | 否 | 客户订单备注 |
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

**功能说明**：*查询团购活动分类*（自动推断）

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

**功能说明**：*查询会员团购活动列表*（自动推断）

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
| isClose | `Byte` | 否 | 是否禁用,0上线1下线 |
| startTime | `Integer` | 否 | 开团时间 |
| endTime | `Integer` | 否 | 结束时间 |
| isCheck | `Byte` | 否 | 平台审核 |
| checkRemark | `String` | 否 | 审核备注 |
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

**功能说明**：*查询团购活动详情*（自动推断）

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
| num | `Integer` | 否 | 订单数量（实际支付订单数 + 虚拟订单数） |
| num2 | `Integer` | 否 | 虚拟数量 |
| isClose | `Byte` | 否 | 是否关闭 |


#### 4. POST `/order/group/groupActivity/view`

**功能说明**：*查看（团购活动）*（自动推断）

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

**功能说明**：*查询团购活动店铺*（自动推断）

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

**功能说明**：*查询团购活动日志*（自动推断）

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

**功能说明**：*查询团购活动日志*（自动推断）

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

**功能说明**：*查询团购订单列表*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `MemberOrderListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**MemberOrderListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| goodsName | `String` | 否 | 商品名称 |
| status | `Integer` | 否 | 订单状态[不传 全部 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后(5 售后同时返回4 已退款的订单) 6 已取消] |
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

**功能说明**：*查询团购订单退款申请列表*（自动推断）

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

**功能说明**：*查询团购订单总数*（自动推断）

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

**功能说明**：*查询团购订单详情*（自动推断）

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

**功能说明**：*生成二维码（团购订单）*（自动推断）

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

**功能说明**：用户扫码核销-整单核销（核销订单下全部剩余可核销商品, 不含已退待收货部分）

核销成功后订单状态处理：
- 订单已有售后状态（存在待审核售后商品行）：核销完成后保持售后状态(5)不变
- 无售后：订单置已收货(3)，并记录收货时间(receiptTime)、核销时间(verifyTime)、实际领取自提点

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

**功能说明**：用户扫码核销-部分核销（仅核销所选商品行, 由商品行"剩余可核销数"约束, 可多次核销）

核销成功后订单状态处理：
- 订单已有售后状态（存在待审核售后商品行）：核销完成后保持售后状态(5)不变
- 无售后 且 完全核销（所有商品行 购买数 ≤ 已核销数+已退待收货数）：订单置已收货(3)，并记录收货时间(receiptTime)
- 无售后 且 未完全核销：订单置部分收货(2)
- 均记录核销时间(verifyTime)、实际领取自提点，并同步商品行已核销数量

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

**功能说明**：*退款（团购订单）*（自动推断）

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

**功能说明**：*查询团购订单退款原因列表*（自动推断）

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

**功能说明**：*查询团购订单退款记录*（自动推断）

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

**功能说明**：用户扫码核销-查询还有商品未全部收货（待核销）的订单列表

查询口径：
- 按用户id+团长id查询（入参 shopId 仅用于解析团长id并校验店铺，不参与订单查询条件）
- 已支付（pay_time>0）且订单状态为 1待收货/2部分收货/5售后
- 订单下存在未核销商品行：购买数(goods_num) > 已核销数(receipt_num)
- 状态为 5售后 时，商品行还须满足 购买数 > 已核销数 + 退款数(refund_num)，即确有未核销且未被退款占用的数量
- 返回订单列表并回填商品信息（goods 字段，含购买数/已核销数/退款数/售后状态等）

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

**功能说明**：*确认发货（团购订单）*（自动推断）

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

**功能说明**：*查询团购订单退款申请订单详情*（自动推断）

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

**功能说明**：*查询团购微信订单*（自动推断）

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

**功能说明**：*查询团长订单列表*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderOrderListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderOrderListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | `String` | 否 | 商品名称 或是手机号 |
| status | `Integer` | 否 | 订单状态[不传 全部 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后(5 售后同时返回4 已退款的订单) 6 已取消] |
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

**功能说明**：*查询团长退款列表*（自动推断）

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

**功能说明**：*查询团长订单总数*（自动推断）

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

**功能说明**：*查询团长订单状态*（自动推断）

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

**功能说明**：*扫码核销（团长订单）*（自动推断）

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

**功能说明**：*查询团长订单*（自动推断）

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

**功能说明**：*核销（团长订单）*（自动推断）

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

**功能说明**：*部分核销（团长订单）*（自动推断）

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

**功能说明**：*发送（团长订单）*（自动推断）

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

**功能说明**：*查询团长首页展示订单*（自动推断）

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

**功能说明**：*查询团长首页订单商品*（自动推断）

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

**功能说明**：*提交（团购活动汇总订单）*（自动推断）

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

> 接口数量：3

#### 1. GET `/order/leader/refund/count`

**功能说明**：*查询团长退款总数*（自动推断）

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

**功能说明**：团长端-批量查询用户退款申请（待审核）列表

查询口径（总数与列表完全一致，SQL 内先筛选再分页）：
- 订单状态为 5 售后，且订单下存在待审核售后商品行（apply_refund=1）
- keyword 同时匹配会员手机号与商品名称（模糊），为空则不过滤
- 分页取订单号后再批量回填订单与该笔申请的待审核商品行（不受 keyword 影响商品行，保证团长审核不遗漏商品）
- 同一订单同一时间只会有一笔待审核申请（用户端存在待审核商品行时不允许再次申请）
- 商品行的申请数量口径以"申请退中数量"(gb_order_goods_info.apply_refund_num)为准，不再使用申请累计值（累计值含历史已同意/被拒部分，会导致数量与金额虚高）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| request | `LeaderRefundApplyListRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**LeaderRefundApplyListRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | `String` | 否 | 关键字：手机号或商品名称（模糊匹配） |
| page | `Integer` | 否 | 页码，默认 1 |
| pageSize | `Integer` | 否 | 每页条数，默认 10，最大 20 |

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
| total | `Long` | 否 | 待审核申请订单总数(订单状态5售后 + 存在待审核(apply_refund=1)商品行, 不受分页影响, 与 list 同一口径) |
| page | `Integer` | 否 | 当前页码 |
| pageSize | `Integer` | 否 | 每页数量 |
| list | `List<LeaderRefundApplyResponse>` | 否 | 待审核申请订单列表(订单维度, 每单带该笔申请的待审核商品行) |


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
| goods | `List<LeaderRefundApplyGoodsResponse>` | 否 | ---------- 申请商品行(该笔申请的整笔待审核行 apply_refund=1, 与审核 refundGoodsMap 对齐) ---------- |


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
| applyRefundNum | `Integer` | 否 | 申请退中数量(gb_order_goods_info.apply_refund_num): 本次申请的待审核数量, 用户申请时写入, 审核处理完成(同意/拒绝)后置0 |
| refundNum | `Integer` | 否 | 本次申请退款数量(退待收货部分): refundFlag=1 时 = applyRefundNum, 否则 0(与审核入参 refundNum 对齐; 历史数据无申请退中数量或退款类型未知时按申请累计口径兜底) |
| refundGoodsNum | `Integer` | 否 | 本次申请退货退款数量(退已收货部分): refundFlag=2 时 = applyRefundNum, 否则 0(与审核入参 refundNum 对齐; 历史数据无申请退中数量或退款类型未知时按申请累计口径兜底) |
| refundAmount | `Double` | 否 | 行退款金额(单位:元) = 本次申请数量 x 商品单价 (商品维度退款金额不落库, 与审核端口径一致) |


#### 3. POST `/order/leader/refund/approve`

**功能说明**：*审核（团长退款）*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| approveRequest | `OrderApproveRequest` | Body | 是 | 请求体对象，字段说明见下方表格 |


**OrderApproveRequest 字段**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| refundOrderGoodsMap | `Map<String, OrderRefundInfoRequest>` | 否 | key String 是订单号 |
| status | `Integer` | 是 | 审核结果 1 同意 2 拒绝 |
| reason | `String` | 否 | — |


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

**功能说明**：*回调（团长退款）*（自动推断）

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）


### OrderPaymentController

> 类路径：`cn.com.shopgroup.order.controller.payment.OrderPaymentController`

> 接口数量：2

#### 1. GET `/order/payment/order/pay`

**功能说明**：*支付（支付订单）*（自动推断）

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

**功能说明**：*回调（支付订单）*（自动推断）

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）


## 3. gb-group-goods（商品/团购）


### GroupGoodsController

> 类路径：`cn.com.shopgroup.goods.controller.GroupGoodsController`

> 接口数量：2

#### 1. GET `/goods/group/goods/list`

**功能说明**：*查询团购商品列表*（自动推断）

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

**功能说明**：*查询团购商品库存*（自动推断）

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

> 接口数量：10

#### 1. GET `/goods/get/goods/cat`

**功能说明**：*查询商品分类*（自动推断）

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

**功能说明**：*查询团长商品上架*（自动推断）

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

**功能说明**：*查询团长商品列表*（自动推断）

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

**功能说明**：*查询团长商品总数*（自动推断）

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

**功能说明**：*查询团长商品详情*（自动推断）

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

**功能说明**：*新增商品团长商品*（自动推断）

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

**功能说明**：*修改团长商品*（自动推断）

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

**功能说明**：*启用/关闭团长商品*（自动推断）

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

**功能说明**：*查询团长商品SKU规格*（自动推断）

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

**功能说明**：*保存团长商品SKU*（自动推断）

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


### LeaderGroupManageController

> 类路径：`cn.com.shopgroup.goods.controller.LeaderGroupManageController`

> 接口数量：10

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


#### 2. GET `/goods/Leader/get/groupActivity/count`

**功能说明**：*查询团长团购活动总数*（自动推断）

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

**功能说明**：*查询团长团购活动标签列表*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据（类型见下） |

data 类型：`Object`（未能静态推断，源码返回表达式：`tagService.getEnabledTagList()`，以接口实际返回为准）

#### 4. POST `/goods/Leader/groupActivity/add`

**功能说明**：*新增团长团购活动*（自动推断）

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

**功能说明**：查询团长团购活动详情（含商品列表、跟团统计、跟团记录）

跟团记录 followRecords 为真实订单数据（已支付、未取消的有效订单），按购买时间倒序取最新50条。

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
| genTuanResponse | `LeaderGroupGenTuanResponse` | 否 | 跟团统计(团员人数/跟团人次/订单总金额) |
| followRecords | `List<LeaderGroupFollowRecordResponse>` | 否 | 跟团记录(真实订单数据: 手机号/姓名/头像/购买时间/购买商品/数量, 按购买时间倒序取最新50条) |


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
| buyTime | `String` | 否 | 购买时间(支付时间, 兜底下单时间, 格式化 yyyy-MM-dd HH:mm:ss) |
| goodsDesc | `String` | 否 | 购买商品描述(多件商品按 商品名/规格 拼接, 逗号分隔) |
| buyNum | `Integer` | 否 | 购买数量(订单商品数量合计) |


#### 6. POST `/goods/Leader/groupActivity/edit`

**功能说明**：*修改团长团购活动*（自动推断）

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

**功能说明**：*启用/关闭团长团购活动*（自动推断）

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

**功能说明**：*查询团长团购活动分类*（自动推断）

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

**功能说明**：*提交（团长分享团购活动海报）*（自动推断）

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

**功能说明**：*提交（团长分享团购活动海报）*（自动推断）

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


## 4. gb-group-admin（后台管理）


### AdminBusinessController

> 类路径：`cn.com.shopgroup.controller.AdminBusinessController`

> 接口数量：4

#### 1. GET `/admin/business/list`

**功能说明**：*查询收款账户列表*（自动推断）

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

**功能说明**：*查询收款账户总数*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/business/info`

**功能说明**：*查询收款账户详情*（自动推断）

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

**功能说明**：*新增收款账户*（自动推断）

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

**功能说明**：*查询商品列表*（自动推断）

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

**功能说明**：*查询商品总数*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/goods/info`

**功能说明**：*查询商品详情*（自动推断）

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

**功能说明**：*查询商品图片*（自动推断）

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

**功能说明**：*查询团购列表*（自动推断）

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


#### 2. GET `/admin/group/count`

**功能说明**：*查询团购总数*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. GET `/admin/group/info`

**功能说明**：*查询团购详情*（自动推断）

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



### AdminGroupTagController

> 类路径：`cn.com.shopgroup.controller.AdminGroupTagController`

> 接口数量：6

#### 1. GET `/admin/tag/list`

**功能说明**：*查询标签列表*（自动推断）

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

**功能说明**：*查询标签总数*（自动推断）

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

**功能说明**：*查询标签详情*（自动推断）

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

**功能说明**：*新增标签*（自动推断）

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

**功能说明**：*修改标签*（自动推断）

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

**功能说明**：*删除标签*（自动推断）

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

**功能说明**：*查询团长列表*（自动推断）

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

**功能说明**：*查询团长总数*（自动推断）

**入参**：无

**出参（JsonResult 统一返回体）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |
| msg | `String` | 提示信息 |
| data | `Object` | 返回数据，类型：`long`（具体字段见下方表格） |

data 类型：`long`（基本类型，无子字段）

#### 3. POST `/admin/leader/add`

**功能说明**：*新增团长*（自动推断）

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

**功能说明**：*查询团长*（自动推断）

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

**功能说明**：*查询验证码*（自动推断）

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

**功能说明**：*提交*（自动推断）

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

**功能说明**：*查询会员列表*（自动推断）

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

**功能说明**：*查询会员总数*（自动推断）

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

**功能说明**：*查询订单收款账户列表*（自动推断）

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

**功能说明**：*查询订单收款账户总数*（自动推断）

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

**功能说明**：*查询订单列表*（自动推断）

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

**功能说明**：*查询订单总数*（自动推断）

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

**功能说明**：*发送（订单）*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 2. GET `/task/order/divide`

**功能说明**：*分账（订单）*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 3. GET `/task/order/query`

**功能说明**：*查询订单*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 4. GET `/task/order/refund`

**功能说明**：*退款（订单）*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| orderNo | `String` | Query 参数 | 是 | 订单编号 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 5. GET `/task/order/cash`

**功能说明**：*查询订单提现*（自动推断）

**入参**

| 参数 | 类型 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| id | `int` | Query 参数 | 是 | 业务主键 ID |
| val | `int` | Query 参数 | 是 | 数值 |

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 6. GET `/task/order/verify`

**功能说明**：*核销（订单）*（自动推断）

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 7. GET `/task/order/backstock`

**功能说明**：*回库（订单）*（自动推断）

**入参**：无

**出参**：`String`（非统一返回体，直接返回该类型数据）

#### 8. GET `/task/test/user`

**功能说明**：*查询测试用户*（自动推断）

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

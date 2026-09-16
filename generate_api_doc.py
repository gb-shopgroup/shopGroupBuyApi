#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
扫描 ShopGroupBuyApi 各模块 Controller, 生成最新接口文档 API接口文档.md
- 解析每个接口的入参（query/header + Body 请求对象字段）
- 解析统一返回结构 JsonResult 及 data 的类型与字段
用法: python3 generate_api_doc.py
"""
import os
import re
import glob
import subprocess
from datetime import datetime

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "API接口文档.md")

MODULES = [
    ("gb-group-user", "用户/团长/员工"),
    ("gb-group-order", "订单/退款/分账"),
    ("gb-group-goods", "商品/团购"),
    ("gb-group-admin", "后台管理"),
    ("gb-group-task", "定时任务"),
]

HTTP_METHODS = {"Get": "GET", "Post": "POST", "Put": "PUT", "Delete": "DELETE", "Patch": "PATCH"}

# 忽略的非 DTO 类后缀
IGNORE_CLASS_PARTS = ("Controller", "Mapper", "Service", "ServiceImpl", "Config", "Application",
                      "Interceptor", "Handler", "Aspect", "Utils", "Helper", "Task", "Scheduler",
                      "Job", "Filter", "Listener", "Exception", "Advice", "Feign", "Client")

BASIC_TYPES = {
    "String", "Integer", "Long", "Double", "Float", "Short", "Byte", "Boolean", "Character",
    "BigDecimal", "Date", "LocalDate", "LocalDateTime", "LocalTime", "Timestamp",
    "int", "long", "double", "float", "short", "byte", "boolean", "char", "Object", "Void",
}

# ============================================================
# 参数含义词典：Query / Header 参数名 -> 中文含义
# ============================================================
PARAM_GLOSSARY = {
    # 分页
    "page": "页码（从 1 开始）",
    "pageSize": "每页条数（默认 10）",
    "pageNum": "页码（从 1 开始）",
    "limit": "每页条数",
    "offset": "偏移量",
    # 身份 / 上下文
    "id": "业务主键 ID",
    "ids": "主键 ID 列表",
    "token": "登录令牌（用户身份凭证）",
    "openid": "微信 openid（用户唯一标识）",
    "openId": "微信 openid（用户唯一标识）",
    "code": "微信授权 code（临时凭证，用于换取手机号 / openid）",
    "mobile": "手机号",
    "phone": "手机号",
    "leaderId": "团长 ID（<=0 或 null 时表示不过滤）",
    "lid": "团长 ID（<=0 或 null 时表示不过滤）",
    "staffId": "员工 ID",
    "sid": "员工 ID",
    "memberId": "会员（用户）ID",
    "userId": "用户 ID",
    "shopId": "店铺 ID",
    "shopCode": "商户编号",
    "goodsId": "商品 ID",
    "skuId": "SKU ID",
    "orderId": "订单 ID",
    "orderNo": "订单编号",
    "groupId": "团购活动 ID（<=0 或 null 时表示不过滤）",
    "gid": "团购活动 ID（<=0 或 null 时表示不过滤）",
    "catId": "团购分类 ID",
    "cat": "团购分类 ID",
    "categoryId": "分类 ID",
    "pid": "提货点（自提点）ID（<=0 或 null 时表示不过滤）",
    "point": "提货点（自提点）ID（<=0 或 null 时表示不过滤）",
    "pointId": "提货点（自提点）ID（<=0 或 null 时表示不过滤）",
    "addressId": "收货地址 ID",
    "articleId": "文章 ID",
    "msgId": "消息 ID",
    "msgType": "消息类型",
    "blackId": "黑名单记录 ID",
    # 时间
    "start": "开始时间（如 yyyy-MM-dd）",
    "end": "结束时间（如 yyyy-MM-dd）",
    "startTime": "开始时间",
    "endTime": "结束时间",
    "date": "日期",
    # 其他
    "type": "类型（含义见各接口说明）",
    "status": "状态（含义见各接口说明）",
    "name": "名称",
    "key": "键",
    "val": "数值",
    "aId": "用户 ID",
    "file": "上传文件",
    "sort": "排序值",
    "keyword": "搜索关键字",
    "remark": "备注",
    "avatar": "头像地址",
    "title": "标题",
    "content": "内容",
    "list": "列表数据",
    "param": "请求参数对象",
    "request": "请求参数对象",
    "requestList": "请求参数对象列表",
}

SPLIT_HINT = {
    "leader": "团长",
    "member": "会员",
    "staff": "员工",
    "shop": "店铺",
    "goods": "商品",
    "order": "订单",
    "group": "团购",
    "cat": "分类",
    "point": "提货点",
    "address": "地址",
    "message": "消息",
    "article": "文章",
    "black": "黑名单",
    "mobile": "手机号",
    "code": "编码",
    "time": "时间",
    "date": "日期",
    "count": "数量",
    "type": "类型",
    "status": "状态",
    "price": "价格",
    "stock": "库存",
    "id": "ID",
    "no": "编号",
}


# ============================================================
# 功能说明自动推断：源码无方法注释时, 依据路径段 + HTTP 方法 + 出入参推断中文说明
# （文档中该说明以斜体呈现, 与源码注释区分）
# ============================================================
PATH_HINTS = {
    "group": "团购", "groupactivity": "团购活动", "activity": "活动", "goods": "商品",
    "product": "商品", "order": "订单", "leader": "团长", "member": "会员",
    "mymember": "我的团员", "staff": "员工", "shop": "店铺", "store": "店铺",
    "cat": "分类", "category": "分类", "point": "自提点", "address": "地址",
    "message": "消息", "msg": "消息", "article": "文章", "black": "黑名单",
    "bill": "对账单", "business": "收款账户", "account": "账户", "refund": "退款",
    "reason": "原因", "receipt": "提货", "verify": "核销", "wx": "微信", "wechat": "微信",
    "image": "图片", "file": "文件", "avatar": "头像", "banner": "轮播图", "tag": "标签",
    "report": "报表", "focus": "关注", "phone": "手机号", "openid": "openid",
    "isleader": "是否为团长", "records": "记录", "recodes": "记录", "logs": "日志",
    "home": "首页", "show": "展示", "config": "配置", "setting": "设置",
    "errcode": "二维码", "ercode": "二维码", "qrcode": "二维码", "access": "资源",
    "shipping": "发货", "notallreceipt": "未全部提货", "applyrefund": "退款申请",
    "status": "状态", "price": "价格", "stock": "库存", "count": "数量",
    "total": "汇总", "test": "测试", "user": "用户", "payment": "支付", "pay": "支付",
    "balance": "余额", "cash": "提现", "commission": "佣金", "divide": "分账",
    "settle": "结算", "statistics": "统计", "report": "报表",
    "mobile": "手机号", "unread": "未读", "orderbusiness": "订单收款账户",
    "orders": "订单", "online": "上架", "img": "图片", "skuspec": "SKU规格",
    "sku": "SKU", "spec": "规格", "kaptcha": "验证码", "poster": "海报",
    "share": "分享", "tagname": "标签名", "nickname": "昵称", "trueName": "收货人",
}

ACTION_VERB = {
    "apply": "申请", "confirm": "确认", "cancel": "取消", "make": "生成", "send": "发送",
    "view": "查看", "get": "查询", "add": "新增", "save": "保存", "edit": "修改",
    "update": "修改", "remove": "删除", "delete": "删除", "close": "启用/关闭",
    "open": "开启", "read": "标记已读", "upload": "上传", "login": "登录",
    "logout": "退出登录", "reg": "注册", "bind": "绑定", "unbind": "解绑",
    "reset": "重置", "set": "设置", "copy": "复制", "sync": "同步", "import": "导入",
    "export": "导出", "audit": "审核", "check": "校验", "notify": "回调",
    "callback": "回调", "verify": "核销", "receipt": "提货", "refund": "退款",
    "pay": "支付", "prepay": "预支付", "settle": "结算", "divide": "分账",
    "withdraw": "提现", "freeze": "冻结", "unfreeze": "解冻", "summary": "汇总",
    "top": "置顶", "batch": "批量处理", "generate": "生成", "create": "创建",
    "modify": "修改", "clear": "清空", "show": "展示", "payment": "支付",
    "scan": "扫码核销", "writeoff": "核销", "partwriteoff": "部分核销",
    "write": "核销", "part": "部分核销",
    "approve": "审核", "submit": "提交", "backstock": "回库",
    "query": "查询", "select": "查询", "search": "搜索", "stat": "统计",
}

# 客体型动作: 直接跟宾语主体（新增/修改/删除...）
OBJ_VERBS = {"新增", "保存", "修改", "删除", "创建", "启用/关闭", "导入", "导出",
             "批量处理", "设置", "绑定", "解绑"}

# 查询型动作模板: 路径末段 -> (动词, 结果后缀)
ACTION_TPL = {
    "list": ("查询", "列表"), "count": ("查询", "总数"),
    "info": ("查询", "详情"), "detail": ("查询", "详情"),
    "logs": ("查询", "日志"), "logs2": ("查询", "日志"),
    "records": ("查询", "记录"), "recodes": ("查询", "记录"),
    "all": ("查询", "全部"), "page": ("查询", "分页列表"),
}


def _camel_words(s):
    """驼峰串 -> 词列表（如 applyRefund -> [apply, Refund]）"""
    return re.findall(r"[A-Z]?[a-z0-9]+|[A-Z]+(?![a-z])", s) or [s]


def _cn_segment(seg, skip_verbs=False):
    """路径段 -> 中文（整段优先命中词典, 否则按驼峰拆分逐词映射）"""
    if not seg:
        return ""
    if seg.lower() in PATH_HINTS:
        return PATH_HINTS[seg.lower()]
    out = []
    for w in _camel_words(seg):
        lw = w.lower()
        if lw in PATH_HINTS:
            out.append(PATH_HINTS[lw])
        elif skip_verbs and lw in ACTION_VERB:
            continue  # 主体名中不保留动词段
        else:
            out.append(w)
    return "".join(out)


def subject_cn(segs):
    """路径段列表 -> 主体中文名"""
    return _dedup_head("".join(_cn_segment(s, skip_verbs=True) for s in segs))


def _dedup_head(txt):
    """去除结果开头的相邻重复片段（如 支付支付订单 -> 支付订单）"""
    for _ in range(2):
        m = re.match(r"^(..+?)\1", txt)
        if not m:
            break
        txt = m.group(1) + txt[2 * len(m.group(1)):]
    return txt


def infer_desc(method, path, params, data_types):
    """依据路径与出入参推断接口功能说明（源码无注释时使用）
    例: POST /order/leader/myMember/list -> 查询团长我的团员列表"""
    segs = [s for s in path.strip("/").split("/") if s and not s.startswith("{")]
    if segs and segs[0].lower() in ("user", "order", "goods", "admin", "task"):
        segs = segs[1:]
    if not segs:
        return ""
    # 上传 / 静态资源访问
    if any(s.lower() == "upload" for s in segs):
        idx = [i for i, s in enumerate(segs) if s.lower() == "upload"][0]
        res = subject_cn(segs[idx + 1:]) or subject_cn(segs[:idx])
        return "上传%s文件" % (res or "")
    if segs[-1].lower() == "access":
        return "访问%s（静态资源）" % (subject_cn(segs[:-1]) or "文件")

    head, last = segs[:-1], segs[-1]
    verb, suffix = "", ""
    if last.lower() in ACTION_TPL:
        verb, suffix = ACTION_TPL[last.lower()]
    else:
        m = re.match(r"^(.*?)(List|Count|Info|Detail)$", last)
        if m and m.group(1):
            verb, suffix = "查询", {"List": "列表", "Count": "总数",
                                    "Info": "详情", "Detail": "详情"}[m.group(2)]
            head = head + [m.group(1)]
        else:
            words = _camel_words(last)
            if words and words[0].lower() in ACTION_VERB:
                verb = ACTION_VERB[words[0].lower()]
                rest = "".join(words[1:])
                if rest:
                    # 剩余词能汉化则作为动作后缀, 否则丢弃（避免说明中出现英文残留）
                    sfx = _cn_segment(rest)
                    suffix = sfx if (sfx and sfx != rest) else ""
            else:
                verb = "查询" if method == "GET" else "提交"
                head = head + [last]
    # 动作与后缀语义重复时丢弃后缀（如 扫码核销 + 二维码）
    if suffix == "二维码" and "扫码" in verb:
        suffix = ""

    subject = subject_cn(head)
    if verb == "查询":
        return _dedup_head("查询" + ((subject + suffix) if subject else (suffix or "数据")))
    act = verb + suffix
    if not subject:
        return _dedup_head(act)
    # 客体型动作(新增/修改/删除...)直接接主体; 操作型动作(退款/发货/核销...)主体放入括号
    if verb in OBJ_VERBS:
        return _dedup_head(act + subject)
    return _dedup_head("%s（%s）" % (act, subject))


def slugify(text):
    """Markdown 标题文本 -> 锚点（GitHub / IDE 预览通用规则）"""
    t = re.sub(r"[`*]", "", text.strip().lower())
    t = re.sub(r"[^\w\u4e00-\u9fff\s-]", "", t)
    return re.sub(r"\s+", "-", t.strip())


def camel_split(name):
    """驼峰/下划线参数名 -> 中文语义片段"""
    words = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", name)
    words = re.sub(r"[_-]+", " ", words).strip().lower()
    parts = [w for w in words.split(" ") if w]
    if not parts:
        return ""
    hints = []
    for p in parts:
        if p in ("id",):
            hints.append("ID")
        elif p in SPLIT_HINT:
            hints.append(SPLIT_HINT[p])
        else:
            hints.append(p)
    return "".join(hints)


def explain_param(req_name, var_name, desc=""):
    """解释 Query/Header 参数含义：
    1) 优先查词典（请求参数名、变量名）
    2) 结合方法功能说明推断（如 desc 含'消息'且参数为 type -> 消息类型）
    3) 兜底按驼峰拆词"""
    for key in (req_name, var_name):
        if key and key in PARAM_GLOSSARY:
            return PARAM_GLOSSARY[key]
    # 结合 desc 推断
    if desc:
        for kw, meaning in (("消息", "消息类型"), ("订单", "订单类型"), ("退款", "退款类型"),
                            ("商品", "商品类型"), ("团购", "团购类型"), ("核销", "核销方式"),
                            ("提货", "提货方式")):
            if kw in desc and var_name and ("type" in var_name.lower() or var_name == "type"):
                return meaning
        if ("订单" in desc or "消息" in desc or "团购" in desc) and var_name in ("status",):
            return "状态（含义见接口说明）"
    # 兜底拆词
    hint = camel_split(var_name or req_name or "")
    if hint:
        return "%s（含义以接口实际为准）" % hint
    return "—"

# ============================================================
# 类索引：简单类名 -> {"pkg": 包名, "fields": [(类型, 字段名, 注释, 必填)]}
# ============================================================
CLASS_INDEX = {}


def simplify_type(t):
    t = t.strip()
    t = re.sub(r"^java\.(util|lang)\.", "", t)
    t = re.sub(r"^java\.", "", t)
    t = re.sub(r"^cn\.com\.shopgroup\.([\w.]+\.)*", "", t)
    # 泛型内部
    t = re.sub(r"<([^<>]+)>", lambda m: "<" + simplify_type(m.group(1)) + ">", t)
    return t


def extract_field_comment(before):
    """提取字段前的注释：优先块注释, 其次收集字段上方紧邻的 // 行注释
    - 注解行跳过（字段注释可能位于注解上方）
    - 空行继续向前（容忍字段间空行）
    - 遇到第二个字段声明行 / 类声明行 / 其他代码行时终止"""
    bm = re.findall(r"/\*\*(.*?)\*/", before, re.S)
    if bm:
        lines = [l.strip().lstrip("*").strip() for l in bm[-1].splitlines() if l.strip()]
        txt = " ".join(x for x in lines if x)
        if txt:
            return txt
    lines = before.splitlines()
    collected = []
    for idx, line in enumerate(reversed(lines)):
        s = line.strip()
        if not s:
            break  # 空行: 终止回溯
        if s.startswith("//"):
            collected.append(s.lstrip("/").strip())
        elif s.startswith("public class") or s.startswith("class "):
            break  # 类声明之前的注释属于类, 不属于字段
        elif s.startswith("@"):
            continue  # 注解行跳过（字段注释可能位于注解上方）
        elif s.startswith("private") or s.startswith("public") or s.startswith("protected"):
            if idx == 0:
                continue  # 无注解时, 最后一行是当前字段自身的声明, 跳过
            break  # 遇到上一个字段/方法的声明, 终止
        else:
            break
    return " ".join(reversed(collected))


FIELD_RE = re.compile(
    r"(?s)((?:(?:@\w+(?:\([^)]*\))?)\s*)*?)"
    r"private\s+([\w<>\[\],\s.]+?)\s+(\w+)\s*(?:=|;)")


# 注解中的字段说明: @ApiModelProperty(value="...") / @ApiParam 等
ANNOT_DESC_RE = [
    re.compile(r"@ApiModelProperty\s*\(\s*value\s*=\s*\"([^\"]+)\""),
    re.compile(r"@ApiParam\s*\(\s*(?:value\s*=\s*)?\"([^\"]+)\""),
]


def parse_class_fields(src):
    """解析类的字段列表：[(类型, 字段名, 注释, 必填)]

    字段说明提取优先级：
      1) 字段上方的 // 行注释 或 /** Javadoc */（限定在本字段与上一个字段/类体之间, 避免串取类注释）
      2) 注解中的说明（@ApiModelProperty(value=...) / 校验注解 message）
      3) 字段声明同行的行尾 // 注释
    """
    fields = []
    class_brace = src.find("{")  # 类体起点, 排除类注释与类注解
    prev_start = class_brace if class_brace != -1 else 0
    for m in FIELD_RE.finditer(src):
        ann_part = m.group(1)
        ftype = simplify_type(m.group(2))
        fname = m.group(3)
        if ftype in ("static",) or fname.startswith("serialVersionUID"):
            continue
        # 注释提取区间: 上一个字段(或类体)之后 ~ 当前字段行首; 超出 400 字符时截取尾部(行对齐)
        line_start = src.rfind("\n", 0, m.start()) + 1
        before = src[prev_start:line_start]
        if len(before) > 400:
            cut = len(before) - 400
            nl = before.find("\n", cut)
            before = before[nl + 1:] if nl != -1 else before[cut:]
        comment = extract_field_comment(before)
        # 无独立注释时, 从字段注解中提取说明
        if not comment:
            for desc_re in ANNOT_DESC_RE:
                mm = desc_re.search(ann_part)
                if mm:
                    comment = mm.group(1).strip()
                    break
        if not comment:
            mm = re.search(r"@(?:NotNull|NotBlank|NotEmpty|Size|Min|Max|Email|Pattern|Valid)\([^)]*message\s*=\s*\"([^\"]+)\"", ann_part)
            if mm:
                comment = mm.group(1)
        # 仍无说明时, 提取字段声明同行的行尾 // 注释
        if not comment:
            eol = src.find("\n", m.end())
            if eol == -1:
                eol = len(src)
            tail = src[m.end():eol]
            tm = re.search(r"//\s*(.*?)\s*$", tail)
            if tm and tm.group(1).strip():
                comment = tm.group(1).strip()
        required = bool(re.search(r"@(NotNull|NotBlank|NotEmpty|Email|Pattern|Min|Max|Size)\b", ann_part))
        fields.append((ftype, fname, comment, required))
        prev_start = m.start()
    return fields


def build_class_index():
    """扫描所有模块 src/main/java 下的 DTO/实体类, 建立类索引"""
    CLASS_INDEX.clear()
    for mod, _ in MODULES:
        base = os.path.join(ROOT, mod, "src/main/java")
        if not os.path.isdir(base):
            continue
        for f in glob.glob(os.path.join(base, "**", "*.java"), recursive=True):
            with open(f, "r", encoding="utf-8", errors="ignore") as fh:
                src = fh.read()
            cm = re.search(r"public\s+class\s+(\w+)", src)
            if not cm:
                continue
            name = cm.group(1)
            if any(p in name for p in IGNORE_CLASS_PARTS):
                continue
            pkg_m = re.search(r"^package\s+([\w.]+);", src, re.M)
            pkg = pkg_m.group(1) if pkg_m else ""
            fields = parse_class_fields(src)
            CLASS_INDEX[name] = {"pkg": pkg, "fields": fields}


def is_basic_type(t):
    base = t.split("<")[0].strip()
    if base in BASIC_TYPES:
        return True
    if t.startswith(("List<", "Map<", "Set<", "Collection<")):
        return False
    if t in ("byte[]", "Byte[]"):
        return True
    return False


# ============================================================
# Controller 解析
# ============================================================
def split_args(sig):
    parts, depth, cur = [], 0, ""
    for ch in sig:
        if ch in "(<":
            depth += 1
        elif ch in ")>":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append(cur)
            cur = ""
        else:
            cur += ch
    if cur.strip():
        parts.append(cur)
    return parts


def parse_param(arg):
    arg = arg.strip()
    if not arg:
        return None
    req_name, body, header = None, False, False
    required = True  # Spring 默认必填
    for m in re.finditer(r"@(\w+)(?:\(([^)]*)\))?", arg):
        aname, aval = m.group(1), m.group(2) or ""
        if aname == "RequestParam":
            mm = re.search(r"value\s*=\s*\"([^\"]+)\"", aval) or re.search(r"^\s*\"([^\"]+)\"", aval)
            req_name = mm.group(1) if mm else None
            if re.search(r"required\s*=\s*false", aval):
                required = False
        elif aname == "RequestBody":
            body = True
        elif aname == "RequestHeader":
            header = True
            mm = re.search(r"value\s*=\s*\"([^\"]+)\"", aval) or re.search(r"^\s*\"([^\"]+)\"", aval)
            req_name = mm.group(1) if mm else None
            if re.search(r"required\s*=\s*false", aval):
                required = False
        elif aname in ("PathVariable", "RequestAttribute"):
            req_name = None
    stripped = re.sub(r"@\w+(\([^)]*\))?\s*", "", arg).strip()
    m = re.match(r"^(.*?)\s+(\w+)$", stripped, re.S)
    if not m:
        return None
    ptype, pname = simplify_type(m.group(1).strip()), m.group(2)
    if header:
        return {"label": "Header", "name": req_name or pname, "type": ptype,
                "var_name": pname, "required": required}
    if body:
        return {"label": "Body", "name": pname, "type": ptype,
                "var_name": pname, "required": True}
    return {"label": "query", "name": req_name or pname, "type": ptype,
            "var_name": pname, "required": required}


def extract_body(body, start_pos):
    """从 start_pos 开始平衡提取 { ... } 方法体, 返回 (body_text, end_pos)"""
    depth = 0
    i = body.find("{", start_pos)
    if i == -1:
        return "", -1
    j = i
    while j < len(body):
        if body[j] == "{":
            depth += 1
        elif body[j] == "}":
            depth -= 1
            if depth == 0:
                return body[i + 1:j], j
        j += 1
    return body[i + 1:], len(body)


def infer_return_data(method_body, param_types):
    """推断方法返回 data 的类型: 解析局部变量声明 + JsonResult.success(...) 参数
    返回 (data_type 列表, 未能推断时的源码表达式提示列表)"""
    # 局部变量声明: 类型 变量 = ...   (排除 for/if 等控制结构, 匹配赋值)
    local = {}
    for m in re.finditer(r"\b(?:final\s+)?([A-Z][\w<>\[\],\s.]*?|int|long|double|float|boolean|short|byte|char|String)\s+(\w+)\s*=", method_body):
        t, v = m.group(1).strip(), m.group(2)
        # 去掉注解残留
        t = re.sub(r"@\w+", "", t).strip()
        if t.startswith("(") or t in ("if", "for", "while", "switch", "new", "return"):
            continue
        local[v] = simplify_type(t)
    # 合并方法参数类型
    for pname, ptype in param_types:
        if pname not in local:
            local[pname] = ptype

    def resolve(expr):
        expr = expr.strip()
        if not expr:
            return None
        if expr.startswith("new "):
            mm = re.match(r"new\s+([\w.]+?)(?:<[^>]*>)?\s*\(", expr)
            if mm:
                return simplify_type(mm.group(1))
        if re.match(r'^"', expr):
            return "String"
        if re.match(r"^[-+]?\d+\.\d+", expr):
            return "Double"
        if re.match(r"^[-+]?\d+", expr):
            return "Integer"
        if expr in ("true", "false"):
            return "Boolean"
        # 含方法调用: 优先识别 ClassName.staticMethod(...) 形式的 DTO 构造
        # (如 OrderInfoResponse.getResponseList(...) / XxxResponse.build(...))
        if re.search(r"\w+\s*\(", expr):
            mm = re.match(r"^([A-Z]\w*)\s*\.\s*\w+\s*\(", expr)
            if mm and mm.group(1) in CLASS_INDEX:
                return mm.group(1)
            # 三元表达式(如 cond ? a : b): 尝试取两分支
            tm = re.match(r"^(.+?)\s*\?\s*(.+?)\s*:\s*(.+?)$", expr)
            if tm:
                r1, r2 = resolve(tm.group(2)), resolve(tm.group(3))
                return r1 or r2
            return None
        # 简单标识符 / 成员访问
        if re.match(r"^[\w.]+$", expr):
            parts = expr.split(".")
            if len(parts) == 1 and parts[0] in local:
                return local[parts[0]]
            if len(parts) == 1:
                return None
            for p in parts:
                if p in local:
                    return local[p]
            return None
        # 字符串拼接
        if "+" in expr and re.search(r'"[^"]*"', expr):
            return "String"
        # 复杂表达式: 从中找已知局部变量
        for v, t in local.items():
            if re.search(r"\b" + re.escape(v) + r"\b", expr):
                return t
        return None

    types, hints = [], []
    for m in re.finditer(r"(?:JsonResult\.)?success\s*\(", method_body):
        args, depth, i, start = [], 0, m.end(), m.end()
        while i < len(method_body):
            ch = method_body[i]
            if ch == "(":
                depth += 1
            elif ch == ")":
                if depth == 0:
                    break
                depth -= 1
            elif ch == "," and depth == 0:
                args.append(method_body[start:i].strip())
                start = i + 1
            i += 1
        if start < i:
            args.append(method_body[start:i].strip())
        # 最后一个参数为 data
        if args:
            t = resolve(args[-1])
            if t and t != "JsonResult":
                types.append(t)
            else:
                # 无法静态推断: 记录源码表达式, 供文档提示
                hint = re.sub(r"\s+", " ", args[-1]).replace("`", "").replace("|", "\\|").strip()
                if hint:
                    hints.append(hint[:90])
        else:
            types.append("<none>")  # success() 无参: data 为空
    # 去重
    seen, out = [], []
    for t in types:
        if t not in seen:
            seen.append(t)
            out.append(t)
    return out, hints


def parse_file(path):
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        src = f.read()
    # 先把非 Javadoc 的块注释(/* ... */)替换为空白(保留行数)，
    # 避免被注释掉的死代码接口(如 /* @GetMapping(...) */)被误扫描进文档；
    # /** ... */ Javadoc 保留，供下方功能说明提取使用
    src = re.sub(r"/\*(?!\*).*?\*/", lambda m: re.sub(r"[^\n]", " ", m.group(0)), src, flags=re.S)

    pkg = re.search(r"^package\s+([\w.]+);", src, re.M)
    pkg = pkg.group(1) if pkg else ""

    cls = re.search(r"public\s+class\s+(\w+)", src)
    cls = cls.group(1) if cls else os.path.basename(path)

    api = re.search(r"@Api\s*\(\s*value\s*=\s*\"([^\"]*)\"", src)
    api = api.group(1) if api else ""

    class_prefix = ""
    cm = re.search(r"@RequestMapping\s*(\([^)]*\))?", src)
    if cm:
        aval = cm.group(1) or ""
        mm = re.search(r"(?:value|path)\s*=\s*\"([^\"]+)\"", aval) or re.search(r"\"([^\"]+)\"", aval)
        if mm:
            class_prefix = mm.group(1)
            if class_prefix != "/" and not class_prefix.startswith("/"):
                class_prefix = "/" + class_prefix

    results = []
    for m in re.finditer(r"@(Get|Post|Put|Delete|Patch)Mapping\s*(\([^)]*\))?", src):
        # 跳过被注释掉(// 或块注释)的 Mapping
        pre = src[max(0, src.rfind("\n", 0, m.start())):m.start()].strip()
        if pre.startswith("//") or pre.startswith("*"):
            continue
        method = HTTP_METHODS[m.group(1)]
        aval = m.group(2) or ""
        p = re.search(r"(?:value|path)\s*=\s*\"([^\"]+)\"", aval) or re.search(r"\"([^\"]+)\"", aval)
        sub = p.group(1) if p else ""
        if sub and not sub.startswith("/"):
            sub = "/" + sub
        full = (class_prefix + sub).replace("//", "/")

        after = src[m.end():]
        sig = re.search(r"public\s+([\w<>\[\],\s.]+?)\s+(\w+)\s*\(", after, re.S)
        params = []
        ret_raw = "JsonResult"
        if sig:
            ret_raw = simplify_type(sig.group(1).strip())
            start_paren = sig.end() - 1
            depth = 0
            i = start_paren
            while i < len(after):
                if after[i] == "(":
                    depth += 1
                elif after[i] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                i += 1
            params_str = after[start_paren + 1:i]
            for a in split_args(params_str):
                pp = parse_param(a)
                if pp and pp["type"] not in ("HttpServletRequest", "HttpServletResponse", "MultipartFile"):
                    params.append(pp)
            # 方法体
            body, _ = extract_body(after, i)
        else:
            body = ""

        # 功能说明
        desc = ""
        head = src[max(0, m.start() - 500):m.start()]
        ao = re.findall(r"@ApiOperation\s*\(\s*\"([^\"]*)\"", head)
        if ao:
            desc = ao[-1]
        else:
            lines = head.splitlines()
            # 收集 Javadoc 中的非 @param/@return 说明行, 按出现顺序拼接
            # 规则: 从下往上扫描, 第一个非空/非 */ / 非 @param 的内容行作为起点,
            # 继续向上拼接所有非空说明行, 直到遇到 /** / 空 Javadoc 行 或 第一个非 * 行
            javadoc_lines = []
            in_javadoc = False
            for line in reversed(lines):
                s = line.strip()
                if s == "*/":
                    in_javadoc = True
                    continue
                if not in_javadoc:
                    # 尚未进入 Javadoc 段, 跳过
                    continue
                if s.startswith("//"):
                    # 单行注释优先级最高(覆盖 Javadoc)
                    desc = s.lstrip("/").strip()
                    javadoc_lines = []
                    break
                if not s.startswith("*"):
                    # 离开 Javadoc 段(遇到了普通代码或注解)
                    break
                # 在 Javadoc 内, s 以 "*" 开头
                content = s.lstrip("*").strip()
                if not content:
                    # Javadoc 内空行 -> 段落分隔符
                    if javadoc_lines and javadoc_lines[-1] != "<BR>":
                        javadoc_lines.append("<BR>")
                    continue
                if content.startswith("@"):
                    # @param/@return 等跳过, 不影响主说明
                    continue
                if content == "/**":
                    # 已是 /** 行, 停止
                    break
                # 清理 Javadoc 内嵌标签: <p> / </p> / <br> 等
                content = re.sub(r"</?p\s*/?>", "", content, flags=re.I)
                content = re.sub(r"<br\s*/?>", "", content, flags=re.I)
                content = re.sub(r"\{@code\s+([^}]+)\}", r"`\1`", content)
                # 收集说明行（拼接顺序: 由近及远 -> 反转后为自然顺序）
                javadoc_lines.append(content)
            if not desc and javadoc_lines:
                # 反转得到自然顺序, 多段以 <br> 在 markdown 中渲染为换行
                desc = " ".join(reversed(javadoc_lines)).replace(" <BR> ", "<br>").replace("<BR> ", "<br>")

        # 返回 data 类型推断（仅针对 JsonResult 统一返回体）
        is_json_result = ret_raw == "JsonResult" or ret_raw.startswith("JsonResult<")
        param_types = [(pp["name"], pp["type"]) for pp in params]
        data_types, data_hints = infer_return_data(body, param_types) if (body and is_json_result) else ([], [])

        # 源码无方法注释时, 依据路径 + 出入参自动推断功能说明（文档中以斜体标注, 与源码注释区分）
        desc_inferred = False
        if not desc:
            desc = infer_desc(method, full, params, data_types)
            desc_inferred = bool(desc)

        results.append({
            "method": method, "path": full, "desc": desc, "desc_inferred": desc_inferred,
            "params": params, "ret": ret_raw, "is_json_result": is_json_result,
            "data_types": data_types, "data_hints": data_hints,
        })
    return pkg, cls, api, results


# ============================================================
# 文档渲染
# ============================================================
def render_class_fields(type_name, level=0, visited=None, parent_label=None, max_depth=4):
    """递归渲染一个 DTO 类的字段表并展开嵌套对象字段
    - level: 嵌套层级（0 为顶层）
    - visited: 已渲染类型集合, 防循环引用
    - parent_label: 上级字段引用说明（如 "字段 xxx"）
    - max_depth: 最大展开层级
    返回 (markdown_str, visited)"""
    visited = visited or set()
    if type_name in visited or type_name not in CLASS_INDEX:
        return "", visited
    visited.add(type_name)
    info = CLASS_INDEX[type_name]
    lines = []
    arrow = "→" * level
    title = "**%s%s 字段**" % (arrow, type_name)
    if parent_label:
        title += "（%s）" % parent_label
    if not info["fields"]:
        return "\n**%s**：无字段\n" % title, visited
    lines.append("\n%s\n" % title)
    lines.append("| 字段 | 类型 | 必填 | 说明 |")
    lines.append("| --- | --- | --- | --- |")
    for ftype, fname, comment, required in info["fields"]:
        req = "是" if required else "否"
        cm = comment or "—"
        cm = cm.replace("|", "\\|")
        lines.append("| %s | `%s` | %s | %s |" % (fname, ftype, req, cm))
    lines.append("")
    if level >= max_depth:
        return "\n".join(lines), visited
    # 递归展开嵌套对象
    for ftype, fname, comment, required in info["fields"]:
        nested = find_nested_dto(ftype)
        if nested and nested not in visited:
            md, visited = render_class_fields(
                nested, level=level + 1, visited=visited,
                parent_label="字段 `%s`（%s）" % (fname, ftype), max_depth=max_depth)
            if md:
                lines.append(md)
    return "\n".join(lines), visited


def find_nested_dto(ftype):
    """提取字段类型中的自定义 DTO 类名（最内层泛型元素）"""
    if ftype in ("List", "Map", "Set"):
        return None
    # Map<String, X> 取 X; List<X> 取 X
    inner = ftype
    while "<" in inner:
        inner = inner[inner.index("<") + 1:inner.rindex(">")]
    if inner and inner not in BASIC_TYPES and inner in CLASS_INDEX:
        return inner
    # 兜底：在完整类型串里找第一个自定义类
    mm = re.findall(r"\b(\w+)\b", ftype)
    for w in mm:
        if w in CLASS_INDEX and not is_basic_type(w):
            return w
    return None


def render_body_dto(body_type, extra_visited=None):
    """渲染 Body 对象字段（递归展开全部嵌套层级）"""
    if body_type not in CLASS_INDEX:
        return ""
    visited = extra_visited or set()
    md, _ = render_class_fields(body_type, visited=visited)
    return md


def render_data_block(data_types, hints=None):
    """渲染出参 data 说明（递归展开字段）"""
    chunks = []
    if not data_types:
        if hints:
            chunks.append("data 类型：`Object`（未能静态推断，源码返回表达式：`%s`，以接口实际返回为准）" % hints[0])
        else:
            chunks.append("data 类型：`Object`（未能静态推断，以接口实际返回为准）")
        return "\n".join(chunks)
    # 同一方法既有空返回(JsonResult.success() 早退)又有真实数据分支时, 只保留真实类型说明, 避免"data 类型: 无"冗余
    real = [d for d in data_types if d != "<none>"]
    if real:
        data_types = real
    visited = set()
    for dt in data_types:
        if dt == "<none>":
            chunks.append("data 类型：无（接口仅返回操作结果，data 为 null）")
            continue
        is_list = bool(re.search(r"^(List|Set|Collection|ArrayList)<", dt))
        base = re.sub(r"^(?:List|Set|Collection|ArrayList)<|>$", "", dt) if "<" in dt else dt
        if is_list:
            elem = dt[dt.index("<") + 1:dt.rindex(">")].strip()
            if elem in CLASS_INDEX:
                chunks.append("data 类型：`%s`（数组，元素类型 `%s`，字段说明见下）" % (dt, elem))
            else:
                chunks.append("data 类型：`%s`（数组，元素为基本类型，无子字段）" % dt)
        else:
            if base in CLASS_INDEX:
                chunks.append("data 类型：`%s`（字段说明见下）" % dt)
            else:
                chunks.append("data 类型：`%s`（基本类型，无子字段）" % dt)
        if base in CLASS_INDEX:
            md, visited = render_class_fields(base, visited=visited)
            chunks.append(md)
    return "\n".join(chunks)


def render_interface(idx, itf):
    lines = []
    lines.append("#### %d. %s `%s`\n" % (idx, itf["method"], itf["path"]))
    desc_txt = itf["desc"] or "—"
    if itf.get("desc_inferred"):
        desc_txt = "*%s*（自动推断）" % desc_txt
    lines.append("**功能说明**：%s\n" % desc_txt)

    # 入参
    if itf["params"]:
        lines.append("**入参**\n")
        lines.append("| 参数 | 类型 | 位置 | 必填 | 说明 |")
        lines.append("| --- | --- | --- | --- | --- |")
        for pp in itf["params"]:
            loc = {"query": "Query 参数", "Body": "Body", "Header": "请求头"}[pp["label"]]
            req = "是" if pp.get("required", True) else "否"
            if pp["label"] == "Body":
                cm = "请求体对象，字段说明见下方表格"
                if pp["name"] != pp.get("var_name"):
                    cm += "（变量名 %s）" % pp["var_name"]
            else:
                cm = explain_param(pp["name"], pp.get("var_name"), itf["desc"])
                if pp["name"] != pp.get("var_name"):
                    cm += "（变量名 %s）" % pp["var_name"]
            lines.append("| %s | `%s` | %s | %s | %s |" % (pp["name"], pp["type"], loc, req, cm))
        lines.append("")
    else:
        lines.append("**入参**：无\n")

    # Body 对象字段
    for pp in itf["params"]:
        if pp["label"] == "Body" and pp["type"] in CLASS_INDEX:
            lines.append(render_body_dto(pp["type"]))
            break

    # 出参
    if itf["is_json_result"]:
        lines.append("**出参（JsonResult 统一返回体）**\n")
        lines.append("| 字段 | 类型 | 说明 |")
        lines.append("| --- | --- | --- |")
        lines.append("| code | `Integer` | 状态码：200=成功，300=失败，400=无权限，500=错误 |")
        lines.append("| msg | `String` | 提示信息 |")
        if itf["data_types"] and itf["data_types"] != ["<none>"]:
            dts = "、".join("`%s`" % d for d in itf["data_types"] if d != "<none>")
            lines.append("| data | `Object` | 返回数据，类型：%s（具体字段见下方表格） |" % (dts or "无"))
        else:
            lines.append("| data | `Object` | 返回数据（类型见下） |")
        lines.append("")
        lines.append(render_data_block(itf["data_types"], itf.get("data_hints")))
    else:
        lines.append("**出参**：`%s`（非统一返回体，直接返回该类型数据）" % itf["ret"])
    lines.append("")
    return "\n".join(lines)


# ============================================================
# 金额字段变更记录（破坏性变更提示, 供接入方同步；稳定后可清空）
# ============================================================
AMOUNT_MIGRATION = [
    ("2026-09-16", "/order/orderbusiness/list", "GET",
     "orderFee、receivedFee、busFee、serviceFee、otherFee", "分 → 元"),
    ("2026-09-16", "/admin/orderbusiness/list", "GET",
     "orderFee、receivedFee、busFee、serviceFee、otherFee", "分 → 元"),
    ("2026-09-16", "/admin/order/list", "GET", "payFee、refundFee", "分 → 元"),
    ("2026-09-16", "/order/group/order/refund/recodes", "GET", "refundFee",
     "原固定返回 `null`，现按「分 → 元」返回"),
    ("2026-09-16", "/order/leader/myMember/list", "POST", "consumeAmount",
     "`String` → `Double`（元）"),
    ("2026-09-16", "/order/leader/myMember/detail", "GET", "consumeAmount、refundAmount",
     "`String` → `Double`（元）"),
]


def render_amount_migration():
    """渲染金额字段变更记录（字段名不变、数值语义变化, 需接入方同步）"""
    if not AMOUNT_MIGRATION:
        return ""
    parts = ["## 金额字段变更记录\n"]
    parts.append("> 接口金额字段已统一为「元」（`Double`）。下表为历史调整项：字段名保持不变，"
                 "但数值语义或类型可能变化（如原「分」现值「元」，相差 100 倍），接入方需同步调整解析逻辑。\n")
    parts.append("| 日期 | 接口 | 方式 | 受影响字段 | 变更 |")
    parts.append("| --- | --- | --- | --- | --- |")
    for date, path, method, fields, note in AMOUNT_MIGRATION:
        parts.append("| %s | `%s` | %s | %s | %s |" % (date, path, method, fields, note))
    parts.append("")
    return "\n".join(parts)


# ============================================================
# README.md 接口清单同步
# ============================================================
def collect_recent_changes(limit=15, since_days=14):
    """从 git log 提取近期接口/字段变更, 用于文档头部「近期变更」章节
    - 关注 *Controller.java / http/request/ / http/response/ 路径下的提交
    - 合并同一天、同一 commit 的多文件改动, 按日期降序
    - 没有 git 或无匹配时返回空列表"""
    try:
        # 仅查 Controller 与 http/request|http/response 路径下的近期提交
        cmd = [
            "git", "-C", ROOT, "log",
            f"--since={since_days}.days",
            "--pretty=format:%h|%ad|%s",
            "--date=short",
            "--name-only",
            "--",
            "*/src/main/java/**/*Controller.java",
            "*/src/main/java/**/http/request/**",
            "*/src/main/java/**/http/response/**",
        ]
        out = subprocess.check_output(cmd, stderr=subprocess.DEVNULL).decode("utf-8", errors="ignore")
    except Exception:
        return []
    if not out.strip():
        return []
    commits = []
    current = None
    for line in out.splitlines():
        if "|" in line and len(line.split("|", 2)) == 3 and re.match(r"^[0-9a-f]{7,}\|\d{4}-\d{2}-\d{2}\|", line):
            if current:
                commits.append(current)
            h, d, s = line.split("|", 2)
            current = {"hash": h, "date": d, "subject": s.strip(), "files": []}
        elif line.strip() and current:
            # 仅保留 Controller/http 下的相关文件
            if ("/http/request/" in line or "/http/response/" in line or line.endswith("Controller.java")) \
                    and "src/main/java" in line:
                # 文件名转中文类型提示
                short = line.rsplit("/", 1)[-1]
                current["files"].append(short)
    if current:
        commits.append(current)
    # 去重 (按 hash), 限制条数
    seen, result = set(), []
    for c in commits:
        if c["hash"] in seen:
            continue
        seen.add(c["hash"])
        result.append(c)
        if len(result) >= limit:
            break
    return result


def render_recent_changes(changes):
    """渲染近期变更 Markdown 段落"""
    if not changes:
        return ""
    parts = ["## 近期变更\n"]
    parts.append("> 以下为最近 %d 条与接口定义相关的提交（来源 `git log`, 由 `generate_api_doc.py` 自动生成）；\n"
                 "> 完整变更请查阅 git 提交记录。\n" % len(changes))
    parts.append("| 日期 | 提交 | 摘要 | 涉及文件 |")
    parts.append("| --- | --- | --- | --- |")
    for c in changes:
        files = ", ".join(sorted(set(c["files"]))[:5])
        if len(set(c["files"])) > 5:
            files += " 等"
        subj = c["subject"].replace("|", "\\|")
        parts.append("| %s | `%s` | %s | %s |" % (c["date"], c["hash"], subj, files or "—"))
    parts.append("")
    return "\n".join(parts)
def render_readme_params(itf):
    """生成 README 表格中的参数列（紧凑格式）"""
    bits = []
    for pp in itf["params"]:
        if pp["label"] == "Body":
            bits.append("Body: **%s** (%s, JSON)" % (pp["name"], pp["type"]))
        elif pp["label"] == "Header":
            bits.append("Header: **%s** (%s)" % (pp["name"], pp["type"]))
        else:
            bits.append("**%s** (%s)" % (pp["name"], pp["type"]))
    return "; ".join(bits) if bits else "无"


def render_readme_api_section(module_datas):
    """生成 README 8.2 接口清单（按 Controller 分组）"""
    parts = ["### 8.2 接口清单\n"]
    parts.append("> 功能说明优先取源码方法注释；*斜体* 为脚本依据路径与出入参自动推断（仅供参考），"
                 "字段级说明见 [`API接口文档.md`](API接口文档.md)。\n")
    for n, (mod, label, total, ctrl_summary) in enumerate(module_datas, 1):
        parts.append("#### 8.2.%d %s（%s）— %d 个接口\n" % (n, mod, label, total))
        for cls, pkg, res in ctrl_summary:
            parts.append("**%s**（`%s`）\n" % (cls, pkg))
            parts.append("| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |")
            parts.append("| --- | --- | --- | --- | --- |")
            for i, itf in enumerate(res, 1):
                desc = (itf["desc"] or "—").strip().replace("|", "\\|") or "—"
                if itf.get("desc_inferred"):
                    desc = "*%s*" % desc
                parts.append("| %d | %s | `%s` | %s | %s |"
                              % (i, itf["method"], itf["path"], desc, render_readme_params(itf)))
            parts.append("")
    return "\n".join(parts)


def update_readme(module_datas):
    """同步 README.md：8.2 接口清单 + 8.1 接口数量统计 + 4.x 模块章节接口数"""
    readme = os.path.join(ROOT, "README.md")
    with open(readme, "r", encoding="utf-8") as f:
        content = f.read()
    # 1) 替换 8.2 接口清单整段（至「附：文件结构」前）
    new_section = render_readme_api_section(module_datas)
    pattern = re.compile(r"### 8\.2 接口清单.*?(?=\n## 附：文件结构)", re.S)
    if pattern.search(content):
        content = pattern.sub(new_section + "\n", content)
    # 2) 更新 8.1 接口数量统计行
    total_all = sum(d[2] for d in module_datas)
    counts = "、".join("%s %d 个" % (m.replace("gb-group-", ""), t) for m, _, t, _ in module_datas)
    content = re.sub(r"- \*\*接口数量统计\*\*：.*",
                     "- **接口数量统计**：%s，合计 **%d 个**。" % (counts, total_all), content)
    # 3) 更新 4.x 各模块章节中的接口数量
    for m, _, t, _ in module_datas:
        seg_m = re.search(r"###\s+\d+\.\d+\s+" + re.escape(m) + r"[\s\S]*?(?=\n###\s+\d+\.\d+|\Z)", content)
        if seg_m:
            seg = seg_m.group(0)
            content = content.replace(seg, re.sub(r"\*\*\d+ 个接口\*\*", "**%d 个接口**" % t, seg))
    with open(readme, "w", encoding="utf-8") as f:
        f.write(content)
    print("README.md 已同步: 接口总数 %d" % total_all)


def main():
    build_class_index()
    module_datas = []
    lines = []
    lines.append("# ShopGroupBuyApi 接口文档（详细版）\n")
    lines.append("> 自动生成时间：%s\n" % datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    lines.append("> 生成方式：扫描各模块 `*Controller.java` 源码（`python3 generate_api_doc.py` 可重新生成）\n")
    lines.append("> 请求头公共参数：`token`（用户登录令牌）、`lid`（团长id）、`sid`（员工id），实际以各接口校验为准\n")
    lines.append("> 参数约定：Query/Header 参数「必填」默认「是」（`@RequestParam` 默认必填，标注 `required=false` 则为「否」）；Body 请求对象各字段的「必填」取自字段校验注解（`@NotNull` 等），未注解时以实际逻辑为准\n")
    lines.append("> 分页约定：列表类接口一般通过 `page`（页码，从 1 开始）/ `pageSize`（每页条数）分页，配套 `count` 接口获取总数\n")
    lines.append("> 统一出参：所有接口返回 `JsonResult`（`code` 状态码 / `msg` 提示信息 / `data` 业务数据），`data` 字段说明见各接口；嵌套对象字段以 `→` 前缀递进展开\n")
    lines.append("> 金额约定：接口返回的金额字段统一为 `Double`，单位「元」（最多 2 位小数）；字段名含 `fee` / `amount` / `price` / `money` / `balance` 的均为金额。数据库实体（`model` 包）内部仍以「分」存储，由 Response 层经 `MoneyUtil.centToYuan` 转换后返回，接口不暴露「分」\n")
    lines.append("> 功能说明：优先取源码方法注释；源码无注释时由脚本依据路径、入参与出参自动推断（表格中显示为 *斜体*，仅供参考）；入参「位置」为 Query 参数 / Body（`@RequestBody` 对象）/ 请求头\n")

    # 金额字段变更记录（破坏性变更提示, 由 AMOUNT_MIGRATION 维护）
    lines.append(render_amount_migration())

    # 近期变更（从 git log 自动提取, 仅做接口变更追踪参考, 不替代人工 commit message）
    lines.append(render_recent_changes(collect_recent_changes()))

    order = 0
    idx_global = 0
    toc = []
    sections = []
    index_rows = []
    for mod, label in MODULES:
        ctrl_files = sorted(glob.glob(os.path.join(ROOT, mod, "src/main/java/**/*Controller.java"), recursive=True))
        blocks = []
        total = 0
        ctrl_summary = []
        for cf in ctrl_files:
            pkg, cls, api, res = parse_file(cf)
            if not res:
                continue
            total += len(res)
            ctrl_summary.append((cls, pkg, res))
            blocks.append("\n### %s\n" % cls)
            if api:
                blocks.append("> 模块说明：%s\n" % api)
            blocks.append("> 类路径：`%s.%s`\n" % (pkg, cls))
            blocks.append("> 接口数量：%d\n" % len(res))
            for i, itf in enumerate(res, 1):
                blocks.append(render_interface(i, itf))
                idx_global += 1
                desc = (itf["desc"] or "—").strip().replace("|", "\\|") or "—"
                if itf.get("desc_inferred"):
                    desc = "*%s*" % desc
                anchor = slugify("%d. %s %s" % (idx_global, itf["method"], itf["path"]))
                index_rows.append("| [%d](#%s) | %s | %s | `%s` | %s |"
                                  % (idx_global, anchor, mod.replace("gb-group-", ""),
                                     itf["method"], itf["path"], desc))
        if total:
            order += 1
            module_datas.append((mod, label, total, ctrl_summary))
            toc.append("%d. [**%s**（%s）](#%s) — %d 个接口"
                       % (order, mod, label, slugify("%d. %s（%s）" % (order, mod, label)), total))
            sections.append("\n## %d. %s（%s）\n" % (order, mod, label))
            sections.extend(blocks)

    lines.append("## 目录\n")
    lines.append("\n".join(toc))
    lines.append("\n---\n")

    # 接口总览索引表
    lines.append("## 接口总览索引\n")
    lines.append("> 共 %d 个接口，按下表序号定位到下方各模块接口详情；「功能说明」列为 `—` 表示源码无方法注释，可按入参 / 出参字段推断用途。\n" % idx_global)
    lines.append("| 序号 | 模块 | 方式 | 路径 | 功能说明 |")
    lines.append("| --- | --- | --- | --- | --- |")
    lines.extend(index_rows)
    lines.append("\n---\n")

    lines.extend(sections)

    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print("done ->", OUT, "| 类索引 DTO 数量:", len(CLASS_INDEX))
    update_readme(module_datas)


if __name__ == "__main__":
    main()

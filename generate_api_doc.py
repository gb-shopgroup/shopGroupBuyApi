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
    "leaderId": "团长 ID",
    "lid": "团长 ID",
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
    "groupId": "团购活动 ID",
    "gid": "团购活动 ID",
    "catId": "团购分类 ID",
    "cat": "团购分类 ID",
    "categoryId": "分类 ID",
    "pid": "提货点（自提点）ID",
    "point": "提货点（自提点）ID",
    "pointId": "提货点（自提点）ID",
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


def parse_class_fields(src):
    """解析类的字段列表：[(类型, 字段名, 注释, 必填)]"""
    fields = []
    for m in FIELD_RE.finditer(src):
        ann_part = m.group(1)
        ftype = simplify_type(m.group(2))
        fname = m.group(3)
        if ftype in ("static",) or fname.startswith("serialVersionUID"):
            continue
        # 从字段声明所在行的行首向前取 400 字符, 避免截断在行中间
        line_start = src.rfind("\n", 0, m.start()) + 1
        before = src[max(0, line_start - 400):line_start]
        comment = extract_field_comment(before)
        if not comment:
            mm = re.search(r"@(?:NotNull|NotBlank|NotEmpty|Size|Min|Max|Email|Pattern|Valid)\([^)]*message\s*=\s*\"([^\"]+)\"", ann_part)
            if mm:
                comment = mm.group(1)
        required = bool(re.search(r"@(NotNull|NotBlank|NotEmpty|Email|Pattern|Min|Max|Size)\b", ann_part))
        fields.append((ftype, fname, comment, required))
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
    返回 (data_type 或 None, 说明文本)"""
    # 局部变量声明: 类型 变量 = ...   (排除 for/if 等控制结构, 匹配赋值)
    local = {}
    for m in re.finditer(r"\b(?:final\s+)?([A-Z][\w<>\[\],\s.]+?)\s+(\w+)\s*=", method_body):
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
        # 含方法调用(如 service.getList(...)): 不做推断, 避免误判
        if re.search(r"\w+\s*\(", expr):
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

    types = []
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
            types.append("<none>")  # success() 无参: data 为空
    # 去重
    seen, out = [], []
    for t in types:
        if t not in seen:
            seen.append(t)
            out.append(t)
    return out


def parse_file(path):
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        src = f.read()

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
            for line in reversed(lines):
                s = line.strip()
                if s.startswith("//"):
                    desc = s.lstrip("/").strip()
                    break
                if s.startswith("*"):
                    # Javadoc 注释：跳过收尾行与 @param/@return 行，取最近一条说明行
                    if s == "*/":
                        continue
                    content = s.lstrip("*").strip()
                    if content and not content.startswith("@"):
                        desc = content
                        break
                    continue  # @param/@return 行或空行，继续向上查找
                if s and not s.startswith("@"):
                    break

        # 返回 data 类型推断（仅针对 JsonResult 统一返回体）
        is_json_result = ret_raw == "JsonResult" or ret_raw.startswith("JsonResult<")
        param_types = [(pp["name"], pp["type"]) for pp in params]
        data_types = infer_return_data(body, param_types) if (body and is_json_result) else []

        results.append({
            "method": method, "path": full, "desc": desc,
            "params": params, "ret": ret_raw, "is_json_result": is_json_result,
            "data_types": data_types,
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


def render_data_block(data_types):
    """渲染出参 data 说明（递归展开字段）"""
    chunks = []
    if not data_types:
        chunks.append("data 类型：`Object`（未能静态推断，以接口实际返回为准）")
        return "\n".join(chunks)
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
    lines.append("**功能说明**：%s\n" % (itf["desc"] or "—"))

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
        lines.append(render_data_block(itf["data_types"]))
    else:
        lines.append("**出参**：`%s`（非统一返回体，直接返回该类型数据）" % itf["ret"])
    lines.append("")
    return "\n".join(lines)


# ============================================================
# README.md 接口清单同步
# ============================================================
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
    for n, (mod, label, total, ctrl_summary) in enumerate(module_datas, 1):
        parts.append("#### 8.2.%d %s（%s）— %d 个接口\n" % (n, mod, label, total))
        for cls, pkg, res in ctrl_summary:
            parts.append("**%s**（`%s`）\n" % (cls, pkg))
            parts.append("| 序号 | 请求方式 | 路径 | 功能说明 | 参数 |")
            parts.append("| --- | --- | --- | --- | --- |")
            for i, itf in enumerate(res, 1):
                desc = (itf["desc"] or "—").strip().replace("|", "\\|") or "—"
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
                index_rows.append("| %d | %s | %s | `%s` | %s |"
                                  % (idx_global, mod.replace("gb-group-", ""), itf["method"], itf["path"], desc))
        if total:
            order += 1
            module_datas.append((mod, label, total, ctrl_summary))
            toc.append("%d. **%s**（%s）— %d 个接口" % (order, mod, label, total))
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

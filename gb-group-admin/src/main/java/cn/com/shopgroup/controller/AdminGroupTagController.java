package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.goods.model.GbGroupTag;
import cn.com.shopgroup.goods.service.GbGroupTagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AdminGroupTagController {

    @Resource
    private GbGroupTagService tagService;

    // 分页查询标签列表(含停用, 名称模糊)
    @GetMapping("/admin/tag/list")
    public JsonResult tagList(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam("page") int page,
                              @RequestParam("pageSize") int pageSize) {

        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        return JsonResult.success(tagService.getAdminTagList(keyword, page, pageSize));
    }

    // 标签总数(与列表筛选条件一致, 供分页)
    @GetMapping("/admin/tag/count")
    public JsonResult tagCount(@RequestParam(value = "keyword", required = false) String keyword) {

        long total = tagService.getAdminTagCount(keyword);
        return JsonResult.success(total);
    }

    // 标签详情
    @GetMapping("/admin/tag/info")
    public JsonResult tagInfo(@RequestParam("tagId") Long tagId) {

        GbGroupTag tag = tagService.getTagById(tagId);
        if (tag == null) {
            return JsonResult.fail("未查询到标签信息");
        }
        return JsonResult.success(tag);
    }

    // 新增标签
    @PostMapping("/admin/tag/add")
    public JsonResult addTag(@RequestBody GbGroupTag tag) {

        if (tag.getTagName() == null || tag.getTagName().trim().isEmpty()) {
            return JsonResult.fail("标签名称不能为空");
        }
        if (tagService.getTagByTagName(tag.getTagName().trim(), null) != null) {
            return JsonResult.fail("标签名称已存在");
        }
        if (tagService.addTag(tag)) {
            return JsonResult.success("添加成功");
        }
        return JsonResult.fail("添加失败");
    }

    // 编辑标签(可修改名称/颜色/排序/状态启停, 未传字段保留原值)
    @PostMapping("/admin/tag/edit")
    public JsonResult editTag(@RequestBody GbGroupTag tag) {

        if (tag.getTagId() == null) {
            return JsonResult.fail("标签id不能为空");
        }
        if (tag.getTagName() == null || tag.getTagName().trim().isEmpty()) {
            return JsonResult.fail("标签名称不能为空");
        }
        if (tagService.getTagByTagName(tag.getTagName().trim(), tag.getTagId()) != null) {
            return JsonResult.fail("标签名称已存在");
        }
        if (tagService.updateTag(tag)) {
            return JsonResult.success("修改成功");
        }
        return JsonResult.fail("修改失败");
    }

    // 删除标签(被团购活动使用中禁止删除, 可改为停用)
    @PostMapping("/admin/tag/delete")
    public JsonResult deleteTag(@RequestParam("tagId") Long tagId) {

        Long refCount = tagService.countGroupActivityByTagId(tagId);
        if (refCount != null && refCount > 0) {
            return JsonResult.fail("该标签已被团购活动使用, 不可删除, 可改为停用");
        }
        if (tagService.deleteTag(tagId)) {
            return JsonResult.success("删除成功");
        }
        return JsonResult.fail("删除失败");
    }
}

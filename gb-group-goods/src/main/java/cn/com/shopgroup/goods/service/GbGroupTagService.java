package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGroupTag;

import java.util.List;

public interface GbGroupTagService {

    // 查询启用的团购标签列表(按排序), 团长端/前端下拉使用
    List<GbGroupTag> getEnabledTagList();

    // 根据id查询标签
    GbGroupTag getTagById(Long tagId);

    // 按名称查询标签(重名校验用, excludeTagId 不为空时排除自身)
    GbGroupTag getTagByTagName(String tagName, Long excludeTagId);

    // 管理端-分页查询标签列表(含停用, 名称模糊)
    List<GbGroupTag> getAdminTagList(String keyword, int page, int pageSize);

    // 管理端-标签总数(名称模糊, 与列表筛选条件一致)
    Long getAdminTagCount(String keyword);

    // 统计引用该标签的团购活动数量(删除前检查)
    Long countGroupActivityByTagId(Long tagId);

    // 新增标签
    Boolean addTag(GbGroupTag tag);

    // 编辑标签(含启停: status 1启用 0停用)
    Boolean updateTag(GbGroupTag tag);

    // 删除标签(被团购活动引用时禁止删除)
    Boolean deleteTag(Long tagId);
}

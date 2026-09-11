package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGroupActivityInfoMapper;
import cn.com.shopgroup.goods.mapper.GbGroupTagMapper;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.model.GbGroupTag;
import cn.com.shopgroup.goods.service.GbGroupTagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbGroupTagServiceImpl implements GbGroupTagService {

    @Resource
    private GbGroupTagMapper tagMapper;

    @Resource
    private GbGroupActivityInfoMapper activityInfoMapper;

    @Override
    public List<GbGroupTag> getEnabledTagList() {
        LambdaQueryWrapper<GbGroupTag> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupTag::getStatus, (byte) 1);
        queryWrapper.orderByAsc(GbGroupTag::getSortOrder);
        List<GbGroupTag> result = tagMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public GbGroupTag getTagById(Long tagId) {
        if (tagId == null || tagId <= 0) {
            return null;
        }
        return tagMapper.selectById(tagId);
    }

    @Override
    public GbGroupTag getTagByTagName(String tagName, Long excludeTagId) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<GbGroupTag> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupTag::getTagName, tagName.trim());
        if (excludeTagId != null && excludeTagId > 0) {
            queryWrapper.ne(GbGroupTag::getTagId, excludeTagId);
        }
        return tagMapper.selectOne(queryWrapper);
    }

    @Override
    public List<GbGroupTag> getAdminTagList(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<GbGroupTag> queryWrapper = Wrappers.lambdaQuery();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(GbGroupTag::getTagName, keyword.trim());
        }
        queryWrapper.orderByAsc(GbGroupTag::getSortOrder);
        queryWrapper.orderByDesc(GbGroupTag::getAddTime);
        int curPage = page < 1 ? 1 : page;
        int curSize = pageSize < 1 ? 10 : (pageSize > 20 ? 20 : pageSize);
        queryWrapper.last("limit " + (curPage - 1) * curSize + "," + curSize);
        List<GbGroupTag> result = tagMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public Long getAdminTagCount(String keyword) {
        LambdaQueryWrapper<GbGroupTag> queryWrapper = Wrappers.lambdaQuery();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(GbGroupTag::getTagName, keyword.trim());
        }
        return tagMapper.selectCount(queryWrapper);
    }

    @Override
    public Long countGroupActivityByTagId(Long tagId) {
        if (tagId == null || tagId <= 0) {
            return 0L;
        }
        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupActivityInfo::getTagId, tagId);
        return activityInfoMapper.selectCount(queryWrapper);
    }

    @Override
    public Boolean addTag(GbGroupTag tag) {
        if (tag == null) {
            return false;
        }
        String tagName = tag.getTagName() == null ? "" : tag.getTagName().trim();
        if (tagName.isEmpty()) {
            return false;
        }
        // 重名校验
        if (getTagByTagName(tagName, null) != null) {
            return false;
        }
        tag.setTagId(null);
        tag.setTagName(tagName);
        tag.setTagColor(tag.getTagColor() == null ? "" : tag.getTagColor());
        tag.setSortOrder(tag.getSortOrder() == null ? 0 : tag.getSortOrder());
        tag.setStatus(tag.getStatus() == null ? (byte) 1 : tag.getStatus());
        tag.setAddTime(TimeUtils.getTimeStamp());
        return tagMapper.insert(tag) > 0;
    }

    @Override
    public Boolean updateTag(GbGroupTag tag) {
        if (tag == null || tag.getTagId() == null || tag.getTagId() <= 0) {
            return false;
        }
        GbGroupTag exist = tagMapper.selectById(tag.getTagId());
        if (exist == null) {
            return false;
        }
        String tagName = tag.getTagName() == null ? "" : tag.getTagName().trim();
        if (tagName.isEmpty()) {
            return false;
        }
        // 重名校验(排除自身)
        if (getTagByTagName(tagName, tag.getTagId()) != null) {
            return false;
        }
        tag.setTagName(tagName);
        // 未传字段保留原值
        tag.setTagColor(tag.getTagColor() == null ? exist.getTagColor() : tag.getTagColor());
        tag.setSortOrder(tag.getSortOrder() == null ? exist.getSortOrder() : tag.getSortOrder());
        tag.setStatus(tag.getStatus() == null ? exist.getStatus() : tag.getStatus());
        return tagMapper.updateById(tag) > 0;
    }

    @Override
    public Boolean deleteTag(Long tagId) {
        if (tagId == null || tagId <= 0) {
            return false;
        }
        if (tagMapper.selectById(tagId) == null) {
            return false;
        }
        // 被团购活动引用时禁止物理删除, 防止 tag_id 悬挂
        // (活动表已冗余 tag_name, 如需保留展示可用停用代替删除)
        if (countGroupActivityByTagId(tagId) > 0) {
            return false;
        }
        return tagMapper.deleteById(tagId) > 0;
    }
}

package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbMemberAdviseFeedbackMapper;
import cn.com.shopgroup.user.model.GbMemberAdviseFeedback;
import cn.com.shopgroup.user.service.GbMemberAdviseFeedbackService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GbMemberAdviseFeedbackServiceImpl implements GbMemberAdviseFeedbackService {

    @Resource
    private GbMemberAdviseFeedbackMapper mapper;

    // 新增建议反馈: 状态默认 0-待处理, 时间由应用层写入(表默认值仅作兜底)
    @Override
    public Long addAdviseFeedback(GbMemberAdviseFeedback info) {

        GbMemberAdviseFeedback data = new GbMemberAdviseFeedback();
        data.setTitle(info.getTitle() == null ? "" : info.getTitle());
        data.setContent(info.getContent());
        data.setMemberId(info.getMemberId());
        data.setStatus(info.getStatus() == null ? 0 : info.getStatus());
        data.setReply(info.getReply());
        Date now = new Date();
        data.setAddTime(now);
        data.setUpdateTime(now);
        mapper.insert(data);
        return data.getId();
    }

    // 修改建议反馈: 仅本人记录可改(status 过滤在 controller 层做业务判断, 此处按 member_id + id 定位)
    @Override
    public Boolean editAdviseFeedback(Long memberId, GbMemberAdviseFeedback info) {

        LambdaUpdateWrapper<GbMemberAdviseFeedback> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbMemberAdviseFeedback::getTitle, info.getTitle());
        updateWrapper.set(GbMemberAdviseFeedback::getContent, info.getContent());
        updateWrapper.set(GbMemberAdviseFeedback::getUpdateTime, new Date());
        updateWrapper.eq(GbMemberAdviseFeedback::getId, info.getId());
        updateWrapper.eq(GbMemberAdviseFeedback::getMemberId, memberId);
        // 仅待处理(0)的记录允许修改, 已受理的记录不允许再改
        updateWrapper.eq(GbMemberAdviseFeedback::getStatus, 0);
        int flag = mapper.update(updateWrapper);
        return flag > 0;
    }

    // 删除建议反馈: 仅本人记录可删
    @Override
    public Boolean deleteAdviseFeedback(Long memberId, Long id) {

        LambdaQueryWrapper<GbMemberAdviseFeedback> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberAdviseFeedback::getId, id);
        queryWrapper.eq(GbMemberAdviseFeedback::getMemberId, memberId);
        int flag = mapper.delete(queryWrapper);
        return flag > 0;
    }

    // 按记录id查询详情(调用方负责校验归属)
    @Override
    public GbMemberAdviseFeedback getAdviseFeedbackById(Long id) {

        if (id == null || id <= 0) {
            return null;
        }
        return mapper.selectById(id);
    }

    // 我的建议反馈列表: 按用户id倒序(最新在前), status 非空时按状态过滤
    @Override
    public List<GbMemberAdviseFeedback> getAdviseFeedbackList(Long memberId, Integer status, int page, int pageSize) {

        LambdaQueryWrapper<GbMemberAdviseFeedback> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberAdviseFeedback::getMemberId, memberId);
        if (status != null && status >= 0) {
            queryWrapper.eq(GbMemberAdviseFeedback::getStatus, status);
        }
        queryWrapper.orderByDesc(GbMemberAdviseFeedback::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbMemberAdviseFeedback> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 我的建议反馈总数(过滤条件与列表保持一致)
    @Override
    public Long getAdviseFeedbackCount(Long memberId, Integer status) {

        LambdaQueryWrapper<GbMemberAdviseFeedback> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberAdviseFeedback::getMemberId, memberId);
        if (status != null && status >= 0) {
            queryWrapper.eq(GbMemberAdviseFeedback::getStatus, status);
        }
        return mapper.selectCount(queryWrapper);
    }

    // 记录是否属于当前用户(避免越权查看/修改他人反馈)
    @Override
    public boolean isOwner(GbMemberAdviseFeedback info, Long memberId) {

        if (ObjectUtils.isEmpty(info) || memberId == null || memberId <= 0) {
            return false;
        }
        return info.getMemberId() != null && info.getMemberId().equals(memberId);
    }

    // 后台-官方回复: 写入回复内容, status 非空时同步流转处理状态(为空则保持原状态)
    @Override
    public Boolean replyAdviseFeedback(Long id, String reply, Integer status) {

        if (id == null || id <= 0) {
            return false;
        }
        LambdaUpdateWrapper<GbMemberAdviseFeedback> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbMemberAdviseFeedback::getReply, reply);
        if (status != null && status >= 0) {
            updateWrapper.set(GbMemberAdviseFeedback::getStatus, status);
        }
        updateWrapper.set(GbMemberAdviseFeedback::getUpdateTime, new Date());
        updateWrapper.eq(GbMemberAdviseFeedback::getId, id);
        int flag = mapper.update(updateWrapper);
        return flag > 0;
    }

    // 后台-建议反馈列表: 待处理优先(status升序), 同状态最新在前(id倒序); keyword 非空时按标题/内容模糊搜索
    @Override
    public List<GbMemberAdviseFeedback> getAdminAdviseFeedbackList(Integer status, String keyword, int page, int pageSize) {

        LambdaQueryWrapper<GbMemberAdviseFeedback> queryWrapper = Wrappers.lambdaQuery();
        applyAdminFilter(queryWrapper, status, keyword);
        queryWrapper.orderByAsc(GbMemberAdviseFeedback::getStatus);
        queryWrapper.orderByDesc(GbMemberAdviseFeedback::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbMemberAdviseFeedback> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 后台-建议反馈总数(过滤条件与列表保持一致)
    @Override
    public Long getAdminAdviseFeedbackCount(Integer status, String keyword) {

        LambdaQueryWrapper<GbMemberAdviseFeedback> queryWrapper = Wrappers.lambdaQuery();
        applyAdminFilter(queryWrapper, status, keyword);
        return mapper.selectCount(queryWrapper);
    }

    // 后台列表筛选条件(状态精确过滤 + 标题/内容模糊搜索), 列表与总数共用, 保证分页总页数一致
    private void applyAdminFilter(LambdaQueryWrapper<GbMemberAdviseFeedback> queryWrapper, Integer status, String keyword) {

        if (status != null && status >= 0) {
            queryWrapper.eq(GbMemberAdviseFeedback::getStatus, status);
        }
        if (keyword != null && keyword.trim().length() > 0) {
            String kw = keyword.trim();
            queryWrapper.nested(w -> w.like(GbMemberAdviseFeedback::getTitle, kw)
                    .or()
                    .like(GbMemberAdviseFeedback::getContent, kw));
        }
    }

}

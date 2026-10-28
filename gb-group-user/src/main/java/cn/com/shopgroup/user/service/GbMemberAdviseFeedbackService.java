package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbMemberAdviseFeedback;

import java.util.List;

public interface GbMemberAdviseFeedbackService {

    // 新增建议反馈(返回记录id)
    Long addAdviseFeedback(GbMemberAdviseFeedback info);

    // 修改建议反馈(仅本人且待处理状态可改)
    Boolean editAdviseFeedback(Long memberId, GbMemberAdviseFeedback info);

    // 删除建议反馈(仅本人可删)
    Boolean deleteAdviseFeedback(Long memberId, Long id);

    // 查询建议反馈详情(按记录id)
    GbMemberAdviseFeedback getAdviseFeedbackById(Long id);

    // 查询建议反馈列表(按用户id, status为空时不过滤状态)
    List<GbMemberAdviseFeedback> getAdviseFeedbackList(Long memberId, Integer status, int page, int pageSize);

    // 查询建议反馈总数(与 getAdviseFeedbackList 过滤条件保持一致, 保证分页总页数正确)
    Long getAdviseFeedbackCount(Long memberId, Integer status);

    // 记录是否属于当前用户(避免越权查看/修改他人反馈)
    boolean isOwner(GbMemberAdviseFeedback info, Long memberId);

    // 后台-官方回复(可同时流转处理状态, status 为空时保持原状态)
    Boolean replyAdviseFeedback(Long id, String reply, Integer status);

    // 后台-建议反馈列表(status为空时不过滤状态, keyword非空时按标题/内容模糊搜索)
    List<GbMemberAdviseFeedback> getAdminAdviseFeedbackList(Integer status, String keyword, int page, int pageSize);

    // 后台-建议反馈总数(过滤条件与 getAdminAdviseFeedbackList 保持一致)
    Long getAdminAdviseFeedbackCount(Integer status, String keyword);

}

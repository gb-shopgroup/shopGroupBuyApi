package cn.com.shopgroup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 管理后台数据查询 Mapper。
 * 分库后用户域数据在 group_buy_user 中，此处通过跨库 SQL 读取（需同 MySQL 实例）。
 */
@Mapper
@Repository
public interface AdminLoginMapper {

    // 后台用户信息（group_buy_user）
    @Select("SELECT `user_id` AS userId, `user_name` AS userName, `pass_word` AS passWord, `true_name` AS trueName, `avatar`, `is_close` AS isClose FROM `group_buy_user`.`gb_sys_user_info` WHERE `user_name` = #{userName}")
    Map<String, Object> selectSysUserByUserName(String userName);

    // 会员分页列表（group_buy_user）
    @Select("SELECT `member_id` AS memberId, `mobile`, `nickname`, `avatar`, `openid`, `leader_id` AS leaderId, `is_close` AS isClose, `add_time` AS addTime FROM `group_buy_user`.`gb_member_info` ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectMemberPage(int offset, int pageSize);

    // 会员总数（group_buy_user）
    @Select("SELECT COUNT(*) FROM `group_buy_user`.`gb_member_info`")
    long selectMemberCount();

}

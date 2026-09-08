package cn.com.shopgroup.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 管理后台业务数据查询 Mapper（跨库）。
 * 拆分后：团长/用户数据在 group_buy_user，商品数据在 group_buy_goods，订单数据在 group_buy_order。
 * 此处通过同 MySQL 实例跨库 SQL 读取。
 */
@Mapper
@Repository
public interface AdminQueryMapper {

    // ========== 团长 ==========

    // 团长分页列表（group_buy_user）
    @Select("SELECT `leader_id` AS leaderId, `leader_name` AS leaderName, `mobile`, `nickname`, `commission`, `is_close` AS isClose, `add_time` AS addTime FROM `group_buy_user`.`gb_org_leader_info` WHERE (#{mobile} IS NULL OR `mobile` = #{mobile}) ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectLeaderPage(String mobile, int offset, int pageSize);

    // 团长总数（group_buy_user）
    @Select("SELECT COUNT(*) FROM `group_buy_user`.`gb_org_leader_info` WHERE (#{mobile} IS NULL OR `mobile` = #{mobile})")
    long selectLeaderCount(String mobile);

    // 团长下拉选项（启用中）
    @Select("SELECT `leader_id` AS value, `leader_name` AS label FROM `group_buy_user`.`gb_org_leader_info` WHERE `is_close` = 0 ORDER BY `add_time` DESC")
    List<Map<String, Object>> selectLeaderOptions();

    // 新增团长（group_buy_user，自增主键回填 leaderId）
    @Insert("INSERT INTO `group_buy_user`.`gb_org_leader_info` (`leader_name`, `mobile`, `commission`, `is_close`, `add_time`) VALUES (#{leaderName}, #{mobile}, #{commission}, 0, UNIX_TIMESTAMP())")
    @Options(useGeneratedKeys = true, keyProperty = "leaderId")
    int insertLeader(Map<String, Object> leader);

    // 新增团长店铺（group_buy_user）
    @Insert("INSERT INTO `group_buy_user`.`gb_org_shop_info` (`leader_id`, `shop_name`) VALUES (#{leaderId}, #{shopName})")
    int insertShop(Map<String, Object> shop);

    // 新增团长收款账户（group_buy_user）
    @Insert("INSERT INTO `group_buy_user`.`gb_org_business_info` (`leader_id`, `bus_type`, `bus_name`, `check_cust_id`, `is_close`, `is_check`, `add_time`) VALUES (#{leaderId}, 1, #{shopName}, #{shopCode}, 0, 0, UNIX_TIMESTAMP())")
    int insertBusiness(Map<String, Object> business);

    // ========== 团长收款账户 ==========

    // 收款账户分页列表（group_buy_user）
    @Select("SELECT `bus_id` AS busId, `leader_id` AS leaderId, `bus_type` AS busType, `bus_name` AS busName, `legal_name` AS legalName, `bus_balance` AS busBalance, `tax_limit` AS taxLimit, `is_close` AS isClose, `is_check` AS isCheck, `check_cust_id` AS checkCustId, `add_time` AS addTime FROM `group_buy_user`.`gb_org_business_info` WHERE (#{leaderId} IS NULL OR `leader_id` = #{leaderId}) ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectBusinessPage(Integer leaderId, int offset, int pageSize);

    // 收款账户总数（group_buy_user）
    @Select("SELECT COUNT(*) FROM `group_buy_user`.`gb_org_business_info` WHERE (#{leaderId} IS NULL OR `leader_id` = #{leaderId})")
    long selectBusinessCount(Integer leaderId);

    // ========== 商品 ==========

    // 商品分页列表（group_buy_goods）
    @Select("SELECT `goods_id` AS goodsId, `leader_id` AS leaderId, `goods_type` AS goodsType, `goods_name` AS goodsName, `goods_img` AS goodsImg, `sales_price` AS salesPrice, `market_price` AS marketPrice, `is_stock` AS isStock, `goods_num` AS goodsNum, `goods_unit` AS goodsUnit, `is_close` AS isClose, `is_check` AS isCheck, `add_time` AS addTime FROM `group_buy_goods`.`gb_goods_info` ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectGoodsPage(int offset, int pageSize);

    // 商品总数（group_buy_goods）
    @Select("SELECT COUNT(*) FROM `group_buy_goods`.`gb_goods_info`")
    long selectGoodsCount();

    // ========== 团购活动 ==========

    // 团购活动分页列表（group_buy_goods）
    @Select("SELECT `group_id` AS groupId, `cat_id` AS catId, `leader_id` AS leaderId, `group_name` AS groupName, `group_img` AS groupImg, `group_img2` AS groupImg2, `group_img3` AS groupImg3, `order_total` AS orderTotal, `is_close` AS isClose, `is_check` AS isCheck, `add_time` AS addTime FROM `group_buy_goods`.`gb_group_activity_info` ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectGroupPage(int offset, int pageSize);

    // 团购活动总数（group_buy_goods）
    @Select("SELECT COUNT(*) FROM `group_buy_goods`.`gb_group_activity_info`")
    long selectGroupCount();

    // ========== 订单 ==========

    // 订单分页列表（group_buy_order）
    @Select("SELECT `order_id` AS orderId, `member_id` AS memberId, `group_id` AS groupId, `leader_id` AS leaderId, `shop_name` AS shopName, `mobile`, `nickname`, `avatar`, `group_name` AS groupName, `order_price` AS orderPrice, `is_pay` AS isPay, `pay_fee` AS payFee, `pay_time` AS payTime, `pay_no` AS payNo, `is_refund` AS isRefund, `refund_fee` AS refundFee, `refund_time` AS refundTime, `refund_no` AS refundNo, `is_receipt` AS isReceipt, `receipt_time` AS receiptTime, `receipt_code` AS receiptCode, `add_time` AS addTime FROM `group_buy_order`.`gb_order_info` ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectOrderPage(int offset, int pageSize);

    // 订单总数（group_buy_order）
    @Select("SELECT COUNT(*) FROM `group_buy_order`.`gb_order_info`")
    long selectOrderCount();

    // ========== 团长订单 ==========

    // 团长订单分页列表（group_buy_order）
    @Select("SELECT `order_id` AS orderId, `bus_id` AS busId, `merchant_no` AS merchantNo, `order_sn` AS orderSn, `is_send` AS isSend, `send_time` AS sendTime, `is_divide` AS isDivide, `divide_status` AS divideStatus, `divide_time` AS divideTime, `divide_no` AS divideNo, `order_fee` AS orderFee, `received_fee` AS receivedFee, `bus_fee` AS busFee, `service_fee` AS serviceFee, `other_fee` AS otherFee, `comm_status` AS commStatus, `add_time` AS addTime FROM `group_buy_order`.`gb_order_business_info` ORDER BY `add_time` DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectOrderBusinessPage(int offset, int pageSize);

    // 团长订单总数（group_buy_order）
    @Select("SELECT COUNT(*) FROM `group_buy_order`.`gb_order_business_info`")
    long selectOrderBusinessCount();

}

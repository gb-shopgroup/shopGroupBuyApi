-- =====================================================
-- 团购查看记录表（团长端"我的团员"中 查看次数/查看动态 数据来源）
-- 说明：用户在 C 端打开团购详情时记录一次（防抖去重后落库）
-- =====================================================

CREATE TABLE IF NOT EXISTS `gb_group_view_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '记录id,主键自增',
  `member_id` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览用户id,外键',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键,冗余(便于团长端聚合)',
  `group_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团购id,外键',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购名称,冗余',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码,冗余',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信昵称,冗余',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信头像,冗余',
  `view_time` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览时间,秒级时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_leader_member_time` (`leader_id`,`member_id`,`view_time`),
  KEY `idx_member_time` (`member_id`,`view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团购查看记录表';

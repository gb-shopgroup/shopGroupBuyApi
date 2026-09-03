-- =====================================================
-- 团长信息表(gb_org_leader_info) 新增字段：结算到账方式
-- 已有环境需执行本脚本；新环境由 group_purchase.sql 全量建表（已含该列）
-- 说明：0=支付时延迟到账型(默认), 1=核销时延迟到账型
-- =====================================================

ALTER TABLE `gb_org_leader_info`
  ADD COLUMN `cash_type` tinyint unsigned NOT NULL DEFAULT '0'
  COMMENT '结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型'
  AFTER `commission`;

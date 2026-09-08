-- group_purchase.gb_article_info definition

CREATE TABLE `gb_article_info` (
  `article_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '文章id,主键自增',
  `article_cat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '文章分类',
  `article_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '文章类型',
  `article_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文章标题',
  `article_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文章配图',
  `article_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '文章内容',
  `sort_order` tinyint unsigned NOT NULL DEFAULT '255' COMMENT '排列顺序',
  `is_home` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否首显',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章信息表';

-- group_purchase.gb_focus_info definition

CREATE TABLE `gb_focus_info` (
  `focus_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '轮播id,主键自增',
  `focus_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '轮播标题',
  `focus_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '轮播图片',
  `focus_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '链接地址,小程序页面地址',
  `sort_order` tinyint unsigned NOT NULL DEFAULT '255' COMMENT '排列顺序',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`focus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='轮播图信息表';

-- group_purchase.gb_goods_category_info definition

CREATE TABLE `gb_goods_category_info` (
  `cat_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '分类id,主键自增',
  `cat_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分类名称',
  `sort_order` tinyint unsigned NOT NULL DEFAULT '255' COMMENT '排列顺序',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`cat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类信息表';

-- group_purchase.gb_goods_image_info definition

CREATE TABLE `gb_goods_image_info` (
  `img_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '图片id,主键自增',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品id,外键',
  `goods_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '图片类型,1主图, 2 banner图',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '图片地址',
  PRIMARY KEY (`img_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品图片表';

-- group_purchase.gb_goods_info definition

CREATE TABLE `gb_goods_info` (
  `goods_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '商品id,主键自增',
  `cat_id` bigint NOT NULL DEFAULT '0' COMMENT '分类id,外键',
  `leader_id` bigint NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `goods_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '商品类型,1普通商品2称重商品',
  `goods_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品主图',
  `cost_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '进货价格',
  `sales_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '销售价格',
  `market_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '市场价格',
  `is_stock` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '启用库存,1=启用',
  `goods_num` int unsigned NOT NULL DEFAULT '0' COMMENT '商品库存,总库存',
  `is_limit` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '启用限购,1=启用',
  `limit_num` int unsigned NOT NULL DEFAULT '0' COMMENT '限购数量',
  `goods_unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品单位',
  `goods_info` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '商品介绍',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `is_check` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '平台审核',
  `check_remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核备注',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品信息表';

-- group_purchase.gb_goods_package_info definition

CREATE TABLE `gb_goods_package_info` (
  `pack_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '包装id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品id,外键',
  `pack_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '包装名称',
  `sales_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '销售价格',
  `market_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '市场价格',
  `pack_num` int unsigned NOT NULL DEFAULT '0' COMMENT '包装数量',
  `goods_unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品单位',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`pack_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品包装信息表';

-- group_purchase.gb_goods_restock_info definition

CREATE TABLE `gb_goods_restock_info` (
  `restock_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '补货id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品id,外键',
  `sku_id` int unsigned NOT NULL DEFAULT '0' COMMENT 'skuid,外键',
  `restock_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '补货类型,1=减少2=增加',
  `goods_num` int unsigned NOT NULL DEFAULT '0' COMMENT '商品数量,补货数量',
  `goods_unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品单位',
  `restock_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '单据状态,1申请2审批3作废',
  `staff_id` int unsigned NOT NULL DEFAULT '0' COMMENT '添加人员id',
  `staff_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '添加人员姓名',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`restock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品库存补货表';

-- group_purchase.gb_goods_sku_info definition

CREATE TABLE `gb_goods_sku_info` (
  `sku_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'skuid,主键自增',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品id,外键',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `sku_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'skuids,规格1+规格2+......',
  `sku_names` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'sku名称,规格1+规格2+......',
  `cost_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '进货价格',
  `sales_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '销售价格',
  `market_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '市场价格',
  `goods_num` int unsigned NOT NULL DEFAULT '0' COMMENT '商品库存',
  `pack_num` int unsigned NOT NULL DEFAULT '0' COMMENT '商品包装',
  `goods_unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品单位',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品图片',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品sku信息表';

-- group_purchase.gb_goods_spec_info definition

CREATE TABLE `gb_goods_spec_info` (
  `spec_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '规格id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `goods_id` bigint NOT NULL DEFAULT '0' COMMENT '商品id',
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规格名称',
  `is_price` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否设置价格',
  `is_stock` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否设置库存',
  `sort_order` tinyint unsigned NOT NULL DEFAULT '255' COMMENT '排列顺序',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品规格信息表';

-- group_purchase.gb_goods_spec_value definition

CREATE TABLE `gb_goods_spec_value` (
  `val_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '规格值id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `goods_id` bigint NOT NULL DEFAULT '0' COMMENT '商品id',
  `spec_id` int unsigned NOT NULL DEFAULT '0' COMMENT '规格id,外键',
  `spec_val` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规格值',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`val_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品规格值表';

-- group_purchase.gb_group_activity_goods definition

CREATE TABLE `gb_group_activity_goods` (
  `group_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团购id,外键',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品id,外键',
  `goods_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品名称,冗余',
  `goods_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '商品类型,冗余',
  `group_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品主图,冗余',
  `group_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '团购价格',
  `market_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '市场价格',
  PRIMARY KEY (`group_id`,`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团购商品信息表';

-- group_purchase.gb_group_activity_info definition

CREATE TABLE `gb_group_activity_info` (
  `group_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '团购id,主键自增',
  `cat_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团购分类id,外键',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `isolation_id` int unsigned NOT NULL DEFAULT '0' COMMENT '数据隔离id',
  `pickup_style` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '商品提货方式,1自提2邮递',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购名称',
  `group_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购主图',
  `group_img2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购主图2',
  `group_img3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购主图3',
  `group_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '团购价格/最小价格',
  `group_price2` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '团购价格/最大价格',
  `market_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '市场价格/划线价格',
  `start_time` int unsigned NOT NULL DEFAULT '0' COMMENT '开团时间',
  `end_time` int unsigned NOT NULL DEFAULT '0' COMMENT '结束时间',
  `group_info` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '团购介绍',
  `order_total` int unsigned NOT NULL DEFAULT '0' COMMENT '实际订单数量',
  `virtual_order` int unsigned NOT NULL DEFAULT '0' COMMENT '虚拟订单数量',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用,0上线1下线',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排列顺序,平台算法',
  `staff_id` int unsigned NOT NULL DEFAULT '0' COMMENT '添加人员id',
  `staff_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '添加人员姓名',
  `is_check` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '平台审核',
  `check_remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核备注',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  `point_id` int unsigned NOT NULL DEFAULT '0' COMMENT '自提点id,0未选择',
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团购活动信息表';

-- group_purchase.gb_group_category_info definition

CREATE TABLE `gb_group_category_info` (
  `cat_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '分类id,主键自增',
  `cat_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分类名称',
  `sort_order` tinyint unsigned NOT NULL DEFAULT '255' COMMENT '排列顺序',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`cat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团购分类信息表';

-- group_purchase.gb_image_library_info definition

CREATE TABLE `gb_image_library_info` (
  `img_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '图片id',
  `img_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '图片类型',
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '图片地址',
  `is_private` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否私密',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`img_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='图片库信息表';

-- group_purchase.gb_member_address_info definition

CREATE TABLE `gb_member_address_info` (
  `address_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '地址id,主键自增',
  `member_id` int unsigned NOT NULL DEFAULT '0' COMMENT '用户id,外键',
  `true_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '收货姓名',
  `telephone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '收货电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '所属省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '所属城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '所属区县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '详细地址',
  `is_default` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否默认',
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户/会员地址表';

-- group_purchase.gb_member_blacklist definition

CREATE TABLE `gb_member_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id,主键自增',
  `leader_id` int unsigned NOT NULL COMMENT '团长id',
  `member_id` int unsigned NOT NULL COMMENT '用户id',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信头像',
  `expire_time` int unsigned NOT NULL DEFAULT '0' COMMENT '有效时间',
  `staff_id` int unsigned NOT NULL DEFAULT '0' COMMENT '添加人id',
  `staff_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '添加人姓名',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`),
  KEY `idx_leader_id_ member_id` (`leader_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='黑名单信息表';

-- group_purchase.gb_member_info definition

CREATE TABLE `gb_member_info` (
  `member_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id,主键自增',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信头像',
  `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'openid,唯一索引（自动登录）',
  `map_id` int unsigned NOT NULL DEFAULT '0' COMMENT '地图定位id,外键',
  `map_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '地图定位名称',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键/来源',
  `isolation_id` int unsigned NOT NULL DEFAULT '0' COMMENT '数据隔离id',
  `ercode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '小程序二维码',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户/会员信息表';

-- group_purchase.gb_member_message_info definition

CREATE TABLE `gb_member_message_info` (
  `msg_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '消息id,主键自增',
  `member_id` int unsigned NOT NULL DEFAULT '0' COMMENT '用户id,外键',
  `msg_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '消息类型',
  `msg_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '消息内容',
  `is_read` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否阅读',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户/会员消息表';

-- group_purchase.gb_order_business_info definition

CREATE TABLE `gb_order_business_info` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'id,主键自增',
  `order_no` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `bus_id` int unsigned NOT NULL COMMENT '账户id,外键',
  `merchant_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '易宝商户编号,冗余',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购名称,冗余',
  `order_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订单编号,冗余',
  `is_send` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否发货,微信发货',
  `transaction_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信单号,微信发货',
  `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'openid,微信发货',
  `send_time` int unsigned NOT NULL DEFAULT '0' COMMENT '发货时间,微信发货',
  `is_unfreeze` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否解冻,微信冻结',
  `unfreeze_time` int unsigned NOT NULL DEFAULT '0' COMMENT '解冻时间,微信冻结',
  `is_divide` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否分账',
  `divide_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分账状态',
  `divide_time` int unsigned NOT NULL DEFAULT '0' COMMENT '分账时间',
  `divide_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分账流水号',
  `order_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '订单金额',
  `received_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '实到金额',
  `bus_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '分账金额',
  `service_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '平台服务费',
  `other_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '其他佣金',
  `comm_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '数据状态',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单收款账户信息表';

-- group_purchase.gb_order_commission_info definition

CREATE TABLE `gb_order_commission_info` (
  `comm_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '明细id,主键自增',
  `order_no` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `comm_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '分账方类型,1=商户货款2=平台抽成3=分账佣金4=帮卖佣金',
  `comm_user` int unsigned NOT NULL DEFAULT '0' COMMENT '分账方id',
  `comm_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分账方姓名,冗余',
  `order_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '订单金额,单位:分',
  `comm_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '分账金额,单位:分',
  `comm_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '分账状态,0未分账,1已分账',
  `comm_time` int unsigned NOT NULL DEFAULT '0' COMMENT '分账时间,请求接口时间',
  `comm_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分账流水号,来自分账接口',
  `comm_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分账备注,系统自动备注',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`comm_id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单分账/佣金信息表';

-- group_purchase.gb_order_goods_info definition

CREATE TABLE `gb_order_goods_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id,主键自增',
  `order_no` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品id,外键',
  `goods_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品名称',
  `goods_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '商品价格',
  `goods_num` int unsigned NOT NULL DEFAULT '0' COMMENT '商品数量',
  `receipt_num` int unsigned NOT NULL DEFAULT '0' COMMENT '收货数量',
  `apply_refund` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '售后（退款）状态 0 无 1 待审核 2 同意 3 不同意',
  `refund_goods_num` int unsigned NOT NULL DEFAULT '0' COMMENT '退货退款数量(退已收货部分, 申请累计, 含待审核/已同意/不同意)',
  `refund_num` int unsigned NOT NULL DEFAULT '0' COMMENT '退款数量(退待收货部分, 申请累计, 含待审核/已同意/不同意)',
  `goods_unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品单位',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品图片',
  `goods_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '商品类型',
  `pack_id` int unsigned NOT NULL DEFAULT '0' COMMENT '包装id,外键',
  `pack_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '包装名称',
  `pack_num` int unsigned NOT NULL DEFAULT '0' COMMENT '包装数量',
  `sku_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'skuids',
  `sku_names` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'sku名称',
  `sku_id` int unsigned NOT NULL DEFAULT '0' COMMENT 'skuid,外键',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '生成时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品信息表';

-- group_purchase.gb_order_goods_refund_record definition

CREATE TABLE `gb_order_goods_refund_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `order_no` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `refund_goods_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '退款商品描述',
  `refund_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '退款类型:1=退款(退待收货部分) 2=退货退款(退已收货部分)',
  `refund_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '本次申请退款金额(单位:分)',
  `operate_id` int unsigned NOT NULL DEFAULT '0' COMMENT '操作人id',
  `operate_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '操作人姓名',
  `is_agree` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态 0 待审核 1 同意 2 不同意',
  `action_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '申请原因',
  `extra_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '补充原因',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单退款记录信息表';

-- group_purchase.gb_refund_reason definition

CREATE TABLE `gb_refund_reason` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '原因id,主键自增',
  `reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '退款原因文本',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序,数值越小越靠前',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态,0=停用,1=启用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间,秒级时间戳',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间,秒级时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款原因配置表';

-- group_purchase.gb_order_info definition

CREATE TABLE `gb_order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单id,主键自增',
  `order_no` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号,18位唯一',
  `member_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id,外键',
  `group_id` bigint NOT NULL DEFAULT '0' COMMENT '团购id,外键',
  `leader_id` bigint NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `isolation_id` int unsigned NOT NULL DEFAULT '0' COMMENT '数据隔离id',
  `shop_id` bigint NOT NULL DEFAULT '0' COMMENT '店铺id',
  `shop_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '店铺名称,冗余',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码,冗余',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信昵称,冗余',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信头像,冗余',
  `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'openid,冗余',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购名称,冗余',
  `group_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '团购价格,冗余',
  `order_price` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '订单价格,冗余（帮卖价格）',
  `bus_id` bigint NOT NULL DEFAULT '0' COMMENT '收款账户id,外键',
  `merchant_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '易宝商户编号,冗余',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消',
  `pay_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '支付金额,单位：分',
  `pay_time` int unsigned NOT NULL DEFAULT '0' COMMENT '支付时间',
  `pay_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '支付流水号,来自支付接口',
  `refund_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '退款金额,单位：分',
  `refund_time` int unsigned NOT NULL DEFAULT '0' COMMENT '退款时间',
  `refund_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '退款流水号,来自退款接口',
  `refund_staff` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '退款人员',
  `refund_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '拒退理由',
  `receipt_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '收货方式,1=自提2=邮寄',
  `true_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '收货姓名,自提和邮寄都需要',
  `telephone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '收货电话,自提和邮寄都需要',
  `receipt_time` int unsigned NOT NULL DEFAULT '0' COMMENT '收货时间',
  `verify_time` int unsigned NOT NULL DEFAULT '0' COMMENT '核销时间',
  `point_id` int unsigned NOT NULL DEFAULT '0' COMMENT '自提点id,自提/外键',
  `point_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '自提点名称,自提',
  `point_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '详细地址,自提',
  `receipt_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '核销码,自提',
  `staff_id` int unsigned NOT NULL DEFAULT '0' COMMENT '核销人员id',
  `staff_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '核销人员姓名',
  `point_id2` int unsigned NOT NULL DEFAULT '0' COMMENT '自提点id,实际领取自提点',
  `point_name2` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '自提点名称,实际领取自提点',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订单备注,c端客户使用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '下单时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更改时间',
  `wx_shipment` int NOT NULL DEFAULT '0' COMMENT '微信发货是否已调用,0=未调用,1=已调用',
  `click_confirm_flag` int NOT NULL DEFAULT '0' COMMENT '0 未操作,1 已操作',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单信息表';

-- group_purchase.gb_org_business_info definition

CREATE TABLE `gb_org_business_info` (
  `bus_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '账户id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `bus_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '账户类型,1=一般企业2=小微企业3=个体户4=个人',
  `bus_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '账户名称',
  `legal_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '法人姓名',
  `cid_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '身份证号',
  `cid_front` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '身份证正面',
  `cid_back` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '身份证反面',
  `license_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '营业执照号',
  `license_front` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '营业执照正面',
  `license_back` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '营业执照反面',
  `bus_balance` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `limit_amount` int unsigned NOT NULL DEFAULT '10' COMMENT '限制最高收款金额(万)',
  `tax_limit` int unsigned NOT NULL DEFAULT '0' COMMENT '纳税额度,单位：万（收款提醒）',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `is_check` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否审核',
  `check_remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核备注',
  `check_cust_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商户支付ID',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`bus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团长分账户信息表';

-- group_purchase.gb_org_cash_bank_info definition

CREATE TABLE `gb_org_cash_bank_info` (
  `bank_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '银行id',
  `band_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '银行名称',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`bank_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='银行信息表';

-- group_purchase.gb_org_cash_bank_no definition

CREATE TABLE `gb_org_cash_bank_no` (
  `bankno_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '银行卡id',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id',
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '银行',
  `bank_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '银行卡',
  `true_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '姓名',
  PRIMARY KEY (`bankno_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='银行卡信息表';

-- group_purchase.gb_org_cash_info definition

CREATE TABLE `gb_org_cash_info` (
  `cash_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '提现id',
  `bus_id` int unsigned NOT NULL DEFAULT '0' COMMENT '账户id,外键',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `cash_fee` int unsigned NOT NULL DEFAULT '0' COMMENT '提现金额',
  `cash_bank` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '提现银行',
  `cash_bank_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '提现银行卡',
  `true_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '提现人姓名',
  `cash_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '提现状态',
  `cash_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '提现流水号',
  `cash_time` int unsigned NOT NULL DEFAULT '0' COMMENT '到账时间',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '申请时间',
  PRIMARY KEY (`cash_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团长提现信息表';

-- group_purchase.gb_org_leader_info definition

CREATE TABLE `gb_org_leader_info` (
  `leader_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '团长id,主键自增',
  `region_id` int unsigned NOT NULL DEFAULT '0' COMMENT '区域id,外键',
  `region_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '区域名称,冗余',
  `leader_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '团长类型,1=平台团长2=独立团长',
  `isolation_id` int unsigned NOT NULL DEFAULT '0' COMMENT '数据隔离id,平台团长固定值,独立团长分配唯一值',
  `leader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团长姓名',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信头像',
  `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'openid,唯一（自动登录）',
  `start_time` int unsigned NOT NULL DEFAULT '0' COMMENT '开始时间,使用有效期',
  `end_time` int unsigned NOT NULL DEFAULT '0' COMMENT '结束时间,使用有效期',
  `leader_balance` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `commission` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '平台抽成,千分率',
  `cash_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型',
  `num_limit` int unsigned NOT NULL DEFAULT '0' COMMENT '发团数量限制（状态：在线）',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '系统备注',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`leader_id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团长信息表';

-- group_purchase.gb_org_message_info definition

CREATE TABLE `gb_org_message_info` (
  `msg_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '消息id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `staff_id` int unsigned NOT NULL DEFAULT '0' COMMENT '员工id,外键',
  `msg_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '消息类型,1=系统消息2=内部消息3=业务消息',
  `msg_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '消息内容',
  `is_read` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否阅读',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团长消息表';

-- group_purchase.gb_org_point_info definition

CREATE TABLE `gb_org_point_info` (
  `point_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自提点id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `point_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '自提点名称',
  `point_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '详细地址',
  `point_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '门头照片',
  `longitude` decimal(7,4) NOT NULL DEFAULT '0.0000' COMMENT '经度,精度为10米级',
  `latitude` decimal(7,4) NOT NULL DEFAULT '0.0000' COMMENT '纬度,精度为10米级',
  `point_scope` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '自提范围,单位：公里',
  `point_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '自提说明,给c端用户看的',
  `point_ercode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '小程序二维码',
  `person` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '联系人',
  `phone` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '联系人电话',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`point_id`),
  KEY `idx_leader_id` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='自提点信息表';

-- group_purchase.gb_org_shop_info definition

CREATE TABLE `gb_org_shop_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id,主键自增',
  `leader_id` bigint NOT NULL DEFAULT '0' COMMENT '团长id',
  `shop_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '店铺名称',
  `shop_short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '店铺简称',
  `shop_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '店铺logo',
  `shop_banner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '店铺banner',
  `shop_mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '联系电话',
  `shop_code_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '店铺二维码图,用户扫码查看待核销订单使用',
  `shop_info` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '店铺介绍',
  PRIMARY KEY (`id`),
  KEY `idx_leader_id` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团长店铺信息表';

-- group_purchase.gb_org_staff_info definition

CREATE TABLE `gb_org_staff_info` (
  `staff_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '员工id,主键自增',
  `leader_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团长id,外键',
  `staff_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '员工姓名',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信头像',
  `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'openid,唯一（自动登录）',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '员工备注',
  `auth_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '权限设置,json格式',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_leader_id` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工信息表';

-- group_purchase.gb_sys_config_info definition

CREATE TABLE `gb_sys_config_info` (
  `config_id` tinyint unsigned NOT NULL AUTO_INCREMENT COMMENT '配置id,主键自增',
  `config_key` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置项目,英文',
  `config_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置名称,中文',
  `config_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置数值,json格式',
  `is_edit` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否编辑,1可以编辑',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置信息表';

-- group_purchase.gb_sys_user_info definition

CREATE TABLE `gb_sys_user_info` (
  `user_id` tinyint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id,主键自增',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '登录账号,唯一约束',
  `pass_word` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '登录密码',
  `true_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户头像',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户备注',
  `is_close` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否禁用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户信息表';

-- group_purchase.gb_group_tag definition
-- 团购标签配置表(添加/编辑团购活动时选择的标签)

CREATE TABLE `gb_group_tag` (
  `tag_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '标签id,主键自增',
  `tag_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '标签名称',
  `tag_color` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '标签颜色,前端展示用',
  `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序,数值越小越靠前',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态,0=停用,1=启用',
  `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间,秒级时间戳',
  PRIMARY KEY (`tag_id`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团购标签配置表';

-- 预设团购标签数据
INSERT INTO `gb_group_tag` (`tag_name`,`tag_color`,`sort_order`,`status`,`add_time`) VALUES
('超快回复', '#FF6B35', 1, 1, UNIX_TIMESTAMP()),
('超多回头客', '#2E9BE6', 2, 1, UNIX_TIMESTAMP()),
('热门团购', '#FF3B30', 3, 1, UNIX_TIMESTAMP());

-- 团购活动表新增标签字段(添加/编辑团购活动时选择的标签)
ALTER TABLE `gb_group_activity_info`
  ADD COLUMN `tag_id` int unsigned NOT NULL DEFAULT '0' COMMENT '团购标签id,0=未选择',
  ADD COLUMN `tag_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '团购标签名称,冗余展示';


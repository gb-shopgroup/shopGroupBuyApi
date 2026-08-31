package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购合集活动信息表
@Data
@TableName("gb_group_collection_activity")
public class GbGroupCollectionActivity {

    // 合集id,主键/外键
    @TableField("coll_id")
    private Long collId;
    // 团购id,主键/外键
    @TableField("group_id")
    private Long groupId;

}

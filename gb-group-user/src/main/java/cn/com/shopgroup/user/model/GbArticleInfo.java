package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 文章信息表
@Data
@TableName("gb_article_info")
public class GbArticleInfo {

    // 文章id,主键自增
    @TableId(type = IdType.AUTO)
    private Long articleId;
    // 文章分类
    @TableField("article_cat")
    private Byte articleCat;
    // 文章类型
    @TableField("article_type")
    private Byte articleType;
    // 文章标题
    @TableField("article_title")
    private String articleTitle;
    // 文章配图
    @TableField("article_img")
    private String articleImg;
    // 文章内容
    @TableField("article_content")
    private String articleContent;
    // 排列顺序
    @TableField("sort_order")
    private Byte sortOrder;
    // 是否首显
    @TableField("is_home")
    private Byte isHome;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}

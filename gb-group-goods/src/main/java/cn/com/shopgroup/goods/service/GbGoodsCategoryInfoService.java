package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsCategoryInfo;

import java.util.List;
import java.util.Map;

public interface GbGoodsCategoryInfoService {

    List<GbGoodsCategoryInfo> getGoodsCategoryList();

    /**
     * 查询全部分类名称映射, key=分类id, value=分类名称
     * 用于商品列表/详情返回分类名称, 避免 N+1 查询
     */
    Map<Long, String> getGoodsCategoryNameMap();
}

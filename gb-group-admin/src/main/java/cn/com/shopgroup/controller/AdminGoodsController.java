package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.goods.http.response.leader.LeaderSpecResponse;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSpecInfoService;
import cn.com.shopgroup.http.response.AdminGoodsResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AdminGoodsController {

    @Resource
    private GbGoodsInfoService service;

    @Resource
    private GbGoodsSpecInfoService specService;

    // 分页查询商品列表(含规格)
    @GetMapping("/admin/goods/list")
    public JsonResult goodsList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 20) pageSize = 20;
        List<GbGoodsInfo> result = service.getAdminGoodsList(page, pageSize);
        List<AdminGoodsResponse> data = AdminGoodsResponse.getAdminGoodsResponseList(result);
        // 一次批量查询所有商品的规格(含规格值), 避免 N+1
        fillGoodsSpecList(data);
        return JsonResult.success(data);
    }

    // 查询商品总数
    @GetMapping("/admin/goods/count")
    public JsonResult goodsCount() {

        long total = service.getAdminGoodsCount();
        return JsonResult.success(total);
    }

    // 查询商品详情(含规格)
    @GetMapping("/admin/goods/info")
    public JsonResult goodsInfo(@RequestParam("id") Long id) {

        GbGoodsInfo info = service.getGoodsInfo(id);
        AdminGoodsResponse response = new AdminGoodsResponse(info);
        if (response.getGoodsId() != null) {
            // 单商品直接按 id 查询规格
            List<GbGoodsSpecInfo> specs = specService.getGoodsSpecListByGoodsId(response.getGoodsId());
            response.setSpecList(LeaderSpecResponse.getSpecResponseList(specs));
        }
        return JsonResult.success(response);
    }

    // 查询商品缩略图列表(最多3张)
    @GetMapping("/admin/goods/img")
    public JsonResult goodsImg(@RequestParam("id") Long id) {

        List<String> img = service.getMiniGoodsImgList(id, 3);
        return JsonResult.success(img);
    }

    /**
     * 为商品列表批量填充规格(含规格值), 一次查询避免 N+1
     */
    private void fillGoodsSpecList(List<AdminGoodsResponse> data) {

        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        Set<Long> goodsIdSet = new LinkedHashSet<>();
        for (AdminGoodsResponse item : data) {
            if (item.getGoodsId() != null) {
                goodsIdSet.add(item.getGoodsId());
            }
        }
        if (goodsIdSet.isEmpty()) {
            return;
        }
        Map<Long, List<GbGoodsSpecInfo>> specMap =
                specService.getGoodsSpecListByGoodsIds(new ArrayList<>(goodsIdSet));
        for (AdminGoodsResponse item : data) {
            List<GbGoodsSpecInfo> specs = specMap.get(item.getGoodsId());
            if (specs != null) {
                item.setSpecList(LeaderSpecResponse.getSpecResponseList(specs));
            }
        }
    }


}
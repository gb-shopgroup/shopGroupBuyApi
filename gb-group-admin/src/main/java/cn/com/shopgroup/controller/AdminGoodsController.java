package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class AdminGoodsController {

    @Resource
    private GbGoodsInfoService service;

    // 分页查询商品列表
    @GetMapping("/admin/goods/list")
    public JsonResult goodsList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 20) pageSize = 20;
        List<GbGoodsInfo> result = service.getAdminGoodsList(page, pageSize);
        return JsonResult.success(result);
    }

    // 查询商品总数
    @GetMapping("/admin/goods/count")
    public JsonResult goodsCount() {

        long total = service.getAdminGoodsCount();
        return JsonResult.success(total);
    }

    // 查询商品详情
    @GetMapping("/admin/goods/info")
    public JsonResult goodsInfo(@RequestParam("id") Long id) {

        GbGoodsInfo info = service.getGoodsInfo(id);
        return JsonResult.success(info);
    }

    // 查询商品缩略图列表(最多3张)
    @GetMapping("/admin/goods/img")
    public JsonResult goodsImg(@RequestParam("id") Long id) {

        List<String> img = service.getMiniGoodsImgList(id, 3);
        return JsonResult.success(img);
    }


}

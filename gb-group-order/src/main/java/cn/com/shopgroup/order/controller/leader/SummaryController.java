package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.order.http.response.SummaryOrderGoodsPointSkuResponse;
import cn.com.shopgroup.order.http.response.SummaryOrderGoodsResponse;
import cn.com.shopgroup.order.http.response.SummaryOrderGoodsSkuResponse;
import cn.com.shopgroup.order.http.response.SummaryOrderResponse;
import cn.com.shopgroup.order.http.response.SummaryPointOrderGoodsResponse;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class SummaryController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbOrgPointInfoService pointService;

    // (团长)汇总订单数量, 已支付, 未退款, 区分已核销/未核销的数量
    @GetMapping("/leader/summary/order")
    public JsonResult summaryOrder(@RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 汇总订单数量, 已支付, 未退款, 区分已核销/未核销的数量
        List<Map<String, Object>> results = orderInfoService.getSummaryOrderList(leaderId, times[0], times[1]);

        // 要返回的结果
        SummaryOrderResponse response = new SummaryOrderResponse();
        response.setNum1(0L);
        response.setNum2(0L);
        for (Map<String, Object> item : results) {

            int tempIsReceipt = ((Number) item.get("is_receipt")).intValue();
            long tempOrderNum = ((Number) item.get("num_total")).longValue();
            if (tempIsReceipt == 0) {
                // 未核销订单数量
                response.setNum2(response.getNum2() + tempOrderNum);
            } else {
                // 已核销订单数量
                response.setNum1(response.getNum1() + tempOrderNum);
            }
        }
        // 总数量
        response.setTotal(response.getNum1() + response.getNum2());
        // 返回结果
        return JsonResult.success(response);
    }

    // (团长)汇总订单商品数量, 已支付, 未退款, 区分已核销/未核销的数量
    @GetMapping("/leader/summary/goods")
    public JsonResult summaryGoods(@RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 汇总订单商品数量, 已支付, 未退款, 区分已核销/未核销的数量
        List<Map<String, Object>> results = orderInfoService.getSummaryOrderGoodsList(leaderId, times[0], times[1]);
        if (results == null || results.isEmpty()) {
            return JsonResult.success(new ArrayList<>());
        }

        // 构建kv数据结构
        Map<Long, SummaryOrderGoodsResponse> dataMap = new HashMap<>();
        // 商品信息缓存, 避免循环内重复查询
        Map<Long, GbGoodsInfo> goodsInfoCache = new HashMap<>();
        for (Map<String, Object> item : results) {

            // 商品和数量汇总, 还有是否已经核销
            long tempGoodsId = ((Number) item.get("goods_id")).longValue();
            int tempIsReceipt = ((Number) item.get("is_receipt")).intValue();
            long tempGoodsNum = ((BigDecimal) item.get("num_total")).longValue();

            // 添加商品信息
            SummaryOrderGoodsResponse goodsResponse = dataMap.get(tempGoodsId);
            if (goodsResponse == null) {

                goodsResponse = this.transSummaryOrderGoodsResponse(item, goodsInfoCache);
                if (goodsResponse == null) continue;
                dataMap.put(tempGoodsId, goodsResponse);

            } else {

                if (tempIsReceipt == 0) {
                    // 未核销数量
                    goodsResponse.setNum2(goodsResponse.getNum2() + tempGoodsNum);
                } else {
                    // 已核销数量
                    goodsResponse.setNum1(goodsResponse.getNum1() + tempGoodsNum);
                }
                // 总数量
                goodsResponse.setTotal(goodsResponse.getNum1() + goodsResponse.getNum2());
            }
        }

        // 待返回的数据
        return JsonResult.success(new ArrayList<>(dataMap.values()));
    }

    // (团长)汇总订单商品数量, 已支付, 未退款, 区分已核销/未核销的数量, 增加提货点分组
    @GetMapping("/leader/summary/point")
    public JsonResult summaryGoodsByPoint(@RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 汇总订单商品数量, 已支付, 未退款, 区分已核销/未核销的数量, 增加提货点分组
        List<Map<String, Object>> results = orderInfoService.getSummaryOrderGoodsListByPoint(leaderId, times[0], times[1]);
        if (results == null || results.isEmpty()) {
            return JsonResult.success(new ArrayList<>());
        }

        // 提货点id -> 提货点汇总, 提货点id -> (商品id -> 商品汇总)
        Map<Long, SummaryPointOrderGoodsResponse> pointDataMap = new HashMap<>();
        Map<Long, Map<Long, SummaryOrderGoodsResponse>> pointGoodsMap = new HashMap<>();
        // 商品/提货点信息缓存, 避免循环内重复查询
        Map<Long, GbGoodsInfo> goodsInfoCache = new HashMap<>();
        Map<Long, GbOrgPointInfo> pointInfoCache = new HashMap<>();
        for (Map<String, Object> item : results) {

            // 提货点，商品和数量汇总
            long tempPointId = ((Number) item.get("point_id")).longValue();
            long tempGoodsId = ((Number) item.get("goods_id")).longValue();
            int tempIsReceipt = ((Number) item.get("is_receipt")).intValue();
            long tempGoodsNum = ((BigDecimal) item.get("num_total")).longValue();

            // 添加提货点
            SummaryPointOrderGoodsResponse pointResponse = pointDataMap.get(tempPointId);
            if (pointResponse == null) {

                GbOrgPointInfo pointInfo = getPointInfoCached(tempPointId, pointInfoCache);
                if (pointInfo == null) continue;
                pointResponse = new SummaryPointOrderGoodsResponse(tempPointId, pointInfo.getPointName());
                pointResponse.setLists(new ArrayList<>());
                pointDataMap.put(tempPointId, pointResponse);
                pointGoodsMap.put(tempPointId, new HashMap<>());
            }

            // 查看商品是否已经添加
            Map<Long, SummaryOrderGoodsResponse> goodsMap = pointGoodsMap.get(tempPointId);
            SummaryOrderGoodsResponse goodsResponse = goodsMap.get(tempGoodsId);
            // 添加商品
            if (goodsResponse == null) {

                goodsResponse = this.transSummaryOrderGoodsResponse(item, goodsInfoCache);
                if (goodsResponse == null) continue;
                goodsMap.put(tempGoodsId, goodsResponse);
                pointResponse.getLists().add(goodsResponse);

            } else {

                // 补充已存在商品数量
                if (tempIsReceipt == 0) {
                    // 未核销数量
                    goodsResponse.setNum2(goodsResponse.getNum2() + tempGoodsNum);
                } else {
                    // 已核销数量
                    goodsResponse.setNum1(goodsResponse.getNum1() + tempGoodsNum);
                }
                // 总数量
                goodsResponse.setTotal(goodsResponse.getNum1() + goodsResponse.getNum2());
            }
        }

        // 待返回的数据
        return JsonResult.success(new ArrayList<>(pointDataMap.values()));
    }

    // (团长)汇总订单商品"sku"/"包装"数量, 已支付, 未退款, 不区分是否核销
    @GetMapping("/leader/summary/sku")
    public JsonResult summaryGoodsSkuPack(@RequestParam("gid") Long goodsId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 查询商品信息
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        if (goodsInfo == null) return JsonResult.fail("商品不存在");

        // 直接按照规格分组汇总商品数量
        List<SummaryOrderGoodsSkuResponse> data = new ArrayList<>();
        if (goodsInfo.getGoodsType() == 1) {
            // 1普通商品：根据商品id汇总商品规格数量, 已支付, 未退款, 不区分是否核销
            List<Map<String, Object>> results = orderInfoService.getSummaryOrderGoodsSkuList(leaderId, goodsId, times[0], times[1]);
            // 循环解析数据
            for (Map<String, Object> item : results) {

                String skuIds = (String) item.get("sku_ids");
                String skuNames = (String) item.get("sku_names");
                long tempNum = ((BigDecimal) item.get("num_total")).longValue();
                data.add(new SummaryOrderGoodsSkuResponse(skuIds, skuNames, tempNum));
            }
        } else {
            // 2称重商品：根据商品id汇总商品包装数量, 已支付, 未退款, 不区分是否核销
            List<Map<String, Object>> results = orderInfoService.getSummaryOrderGoodsPackList(leaderId, goodsId, times[0], times[1]);
            // 循环解析数据
            for (Map<String, Object> item : results) {

                String packId = String.valueOf(((Number) item.get("pack_id")).longValue());
                String packName = (String) item.get("pack_name");
                long tempNum = ((BigDecimal) item.get("num_total")).longValue();
                data.add(new SummaryOrderGoodsSkuResponse(packId, packName, tempNum));
            }
        }

        // 返回数据
        return JsonResult.success(data);
    }

    // (团长)汇总订单商品"sku"/"包装"数量, 已支付, 未退款, 不区分是否核销, 增加提货点分组
    @GetMapping("/leader/summary/pointsku")
    public JsonResult summaryGoodsSkuPackByGroup(@RequestParam("gid") Long goodsId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 查询商品信息
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        if (goodsInfo == null) return JsonResult.fail("商品不存在");

        // 根据商品id进行汇总, 先按照提货点分组, 再按照规格分组
        Map<Long, SummaryOrderGoodsPointSkuResponse> dataMap = new HashMap<>();
        // 提货点信息缓存, 避免循环内重复查询
        Map<Long, GbOrgPointInfo> pointInfoCache = new HashMap<>();
        if (goodsInfo.getGoodsType() == 1) {
            // 1普通商品：根据商品id汇总商品规格数量, 已支付, 未退款, 不区分是否核销, 增加提货点分组
            List<Map<String, Object>> results = orderInfoService.getSummaryOrderGoodsSkuListByPoint(leaderId, goodsId, times[0], times[1]);
            // 循环解析数据
            for (Map<String, Object> item : results) {

                long pointId = ((Number) item.get("point_id")).longValue();
                String skuIds = (String) item.get("sku_ids");
                String skuNames = (String) item.get("sku_names");
                long tempNum = ((BigDecimal) item.get("num_total")).longValue();
                this.transSummaryOrderGoodsPointSkuResponse(dataMap, pointInfoCache, pointId, skuIds, skuNames, tempNum);
            }
        } else {
            // 2称重商品：根据商品id汇总商品包装数量, 已支付, 未退款, 不区分是否核销, 增加提货点分组
            List<Map<String, Object>> results = orderInfoService.getSummaryOrderGoodsPackListByPoint(leaderId, goodsId, times[0], times[1]);
            // 循环解析数据
            for (Map<String, Object> item : results) {

                long pointId = ((Number) item.get("point_id")).longValue();
                String packId = String.valueOf(((Number) item.get("pack_id")).longValue());
                String packName = (String) item.get("pack_name");
                long tempNum = ((BigDecimal) item.get("num_total")).longValue();
                this.transSummaryOrderGoodsPointSkuResponse(dataMap, pointInfoCache, pointId, packId, packName, tempNum);
            }
        }

        // 返回数据
        return JsonResult.success(new ArrayList<>(dataMap.values()));
    }

    // (店员)指定 提货点id 汇总订单商品数量, 已支付, 未退款, 区分已核销/未核销的数量
    @GetMapping("/leader/summary/pointgoods")
    public JsonResult summaryPointGoods(@RequestParam("pid") Long pointId, @RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 指定 提货点id 汇总订单商品数量, 已支付, 未退款, 区分已核销/未核销的数量
        List<Map<String, Object>> results = orderInfoService.getSummaryPointOrderGoodsList(leaderId, pointId, times[0], times[1]);
        if (results == null || results.isEmpty()) {
            return JsonResult.success(new ArrayList<>());
        }

        // 构建kv数据结构
        Map<Long, SummaryOrderGoodsResponse> dataMap = new HashMap<>();
        // 商品信息缓存, 避免循环内重复查询
        Map<Long, GbGoodsInfo> goodsInfoCache = new HashMap<>();
        for (Map<String, Object> item : results) {

            // 商品和数量汇总, 还有是否已经核销
            long tempGoodsId = ((Number) item.get("goods_id")).longValue();
            int tempIsReceipt = ((Number) item.get("is_receipt")).intValue();
            long tempGoodsNum = ((BigDecimal) item.get("num_total")).longValue();

            // 添加商品信息
            SummaryOrderGoodsResponse goodsResponse = dataMap.get(tempGoodsId);
            if (goodsResponse == null) {

                goodsResponse = this.transSummaryOrderGoodsResponse(item, goodsInfoCache);
                if (goodsResponse == null) continue;
                dataMap.put(tempGoodsId, goodsResponse);

            } else {

                if (tempIsReceipt == 0) {
                    // 未核销数量
                    goodsResponse.setNum2(goodsResponse.getNum2() + tempGoodsNum);
                } else {
                    // 已核销数量
                    goodsResponse.setNum1(goodsResponse.getNum1() + tempGoodsNum);
                }
                // 总数量
                goodsResponse.setTotal(goodsResponse.getNum1() + goodsResponse.getNum2());
            }
        }

        // 待返回的数据
        return JsonResult.success(new ArrayList<>(dataMap.values()));
    }

    // (店员)指定提货点, 进行汇总订单商品"sku"/"包装"数量, 已支付, 未退款, 不区分是否核销
    @GetMapping("/leader/summary/pointgoodssku")
    public JsonResult summaryPointGoodsSkuPack(@RequestParam("pid") Long pointId, @RequestParam("gid") Long goodsId,
                                               @RequestParam("start") String startDate, @RequestParam("end") String endDate) {

        // 从请求头中获取团长id, 并校验
        Long leaderId = getRequestLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 解析时间范围, 为空则使用默认范围
        int[] times = parseTimeRange(startDate, endDate);

        // 查询商品信息
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        if (goodsInfo == null) return JsonResult.fail("商品不存在");

        // 根据商品id进行汇总, 先按照提货点分组, 再按照规格分组
        List<SummaryOrderGoodsSkuResponse> data = new ArrayList<>();
        if (goodsInfo.getGoodsType() == 1) {
            // 1普通商品：指定 提货点id 和 商品id 进行汇总订单商品"sku"数量, 已支付, 未退款, 不区分是否核销
            List<Map<String, Object>> results = orderInfoService.getSummaryPointOrderGoodsSkuList(leaderId, pointId, goodsId, times[0], times[1]);
            // 循环解析数据
            for (Map<String, Object> item : results) {

                String skuIds = (String) item.get("sku_ids");
                String skuNames = (String) item.get("sku_names");
                long tempNum = ((BigDecimal) item.get("num_total")).longValue();
                data.add(new SummaryOrderGoodsSkuResponse(skuIds, skuNames, tempNum));
            }
        } else {
            // 2称重商品：指定 提货点id 和 商品id 汇总订单商品包装数量, 已支付, 未退款, 不区分是否核销
            List<Map<String, Object>> results = orderInfoService.getSummaryPointOrderGoodsPackList(leaderId, pointId, goodsId, times[0], times[1]);
            // 循环解析数据
            for (Map<String, Object> item : results) {

                String packId = String.valueOf(((Number) item.get("pack_id")).longValue());
                String packName = (String) item.get("pack_name");
                long tempNum = ((BigDecimal) item.get("num_total")).longValue();
                data.add(new SummaryOrderGoodsSkuResponse(packId, packName, tempNum));
            }
        }

        // 返回数据
        return JsonResult.success(data);
    }


    // 从请求头中获取团长id
    private Long getRequestLeaderId() {
        return RequestParamsUtils.getRequestHeaderLeaderId();
    }

    // 解析时间范围, 未传的边界使用默认值, 返回 {startTime, endTime}
    private int[] parseTimeRange(String startDate, String endDate) {

        int startTime;
        int endTime;
        if (startDate != null && startDate.length() > 0) {
            startTime = TimeUtils.toFormatTimeStamp(startDate + " 00:00:00");
        } else {
            startTime = 1;
        }
        if (endDate != null && endDate.length() > 0) {
            endTime = TimeUtils.toFormatTimeStamp(endDate + " 23:59:59");
        } else {
            endTime = TimeUtils.getTimeStamp();
        }
        return new int[]{startTime, endTime};
    }

    // 查询商品信息, 按商品id缓存, 避免循环内重复查询
    private GbGoodsInfo getGoodsInfoCached(Long goodsId, Map<Long, GbGoodsInfo> goodsInfoCache) {

        GbGoodsInfo goodsInfo = goodsInfoCache.get(goodsId);
        if (goodsInfo == null) {
            goodsInfo = goodsService.getGoodsInfo(goodsId);
            if (goodsInfo != null) {
                goodsInfoCache.put(goodsId, goodsInfo);
            }
        }
        return goodsInfo;
    }

    // 查询提货点信息, 按提货点id缓存, 避免循环内重复查询
    private GbOrgPointInfo getPointInfoCached(Long pointId, Map<Long, GbOrgPointInfo> pointInfoCache) {

        GbOrgPointInfo pointInfo = pointInfoCache.get(pointId);
        if (pointInfo == null) {
            pointInfo = pointService.getPointInfo(pointId);
            if (pointInfo != null) {
                pointInfoCache.put(pointId, pointInfo);
            }
        }
        return pointInfo;
    }

    // 将数据表记录对象转化为 SummaryOrderGoodsResponse 对象
    private SummaryOrderGoodsResponse transSummaryOrderGoodsResponse(Map<String, Object> item, Map<Long, GbGoodsInfo> goodsInfoCache) {

        // 商品和数量汇总, 还有是否已经核销
        long tempGoodsId = ((Number) item.get("goods_id")).longValue();
        int tempIsReceipt = ((Number) item.get("is_receipt")).intValue();
        long tempGoodsNum = ((BigDecimal) item.get("num_total")).longValue();

        // 查询商品名称和单位
        GbGoodsInfo goodsInfo = getGoodsInfoCached(tempGoodsId, goodsInfoCache);
        if (ObjectUtils.isEmpty(goodsInfo)) {
            return null;
        }

        // 要返回的结果
        SummaryOrderGoodsResponse goodsResponse = new SummaryOrderGoodsResponse();
        goodsResponse.setNum1(0L);
        goodsResponse.setNum2(0L);

        // 商品id和名称
        goodsResponse.setId(goodsInfo.getGoodsId());
        goodsResponse.setName(goodsInfo.getGoodsName());

        // 区分数量
        if (tempIsReceipt == 0) {
            // 未核销数量
            goodsResponse.setNum2(tempGoodsNum);
        } else {
            // 已核销数量
            goodsResponse.setNum1(tempGoodsNum);
        }

        // 单位
        goodsResponse.setUnit(goodsInfo.getGoodsUnit());

        // 总数量
        goodsResponse.setTotal(goodsResponse.getNum1() + goodsResponse.getNum2());

        // 返回结果
        return goodsResponse;
    }

    // 将数据表记录对象转为为 SummaryOrderGoodsPointSkuResponse 对象
    private void transSummaryOrderGoodsPointSkuResponse(Map<Long, SummaryOrderGoodsPointSkuResponse> dataMap,
                                                        Map<Long, GbOrgPointInfo> pointInfoCache,
                                                        long pointId, String skuIds, String skuNames, long tempNum) {

        // 先判断提货点是否存在
        SummaryOrderGoodsPointSkuResponse response = dataMap.get(pointId);
        if (response == null) {

            // 查询提货点信息
            GbOrgPointInfo pointInfo = getPointInfoCached(pointId, pointInfoCache);
            if (pointInfo == null) return;
            response = new SummaryOrderGoodsPointSkuResponse(pointId, pointInfo.getPointName());
            response.setLists(new ArrayList<>());
            dataMap.put(pointId, response);
        }

        // 添加指定规格的数量汇总
        response.getLists().add(new SummaryOrderGoodsSkuResponse(skuIds, skuNames, tempNum));
    }

    //根据团购活动id统计订单数（实时，团长段=端有需求时使用）
    @PostMapping("/get/groupActivity/totalOrder")
    public JsonResult getSumOfGroupActivityOrder(@RequestParam("groupId") Long groupId) {
        Integer total = 0;
        if (groupId == null || groupId.intValue() == 0) {
            return JsonResult.success(total);
        }
        total = orderInfoService.getSumOfGroupActivityOrder(groupId);
        return JsonResult.success(total);
    }


}

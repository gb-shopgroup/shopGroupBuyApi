package cn.com.shopgroup.goods.utils;

import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkuGenerateUtils {

    /**
     * 核心方法：根据规格列表生成所有SKU组合
     * @param specList 规格列表（入参）
     * @return SKU列表（出参）
     */
    public static List<GbGoodsSkuInfo> generateSkuList(List<GbGoodsSpecInfo> specList) {

        // 待返回的sku列表
        List<GbGoodsSkuInfo> resultList = new ArrayList<>();

        // 空值判断
        if (specList == null || specList.isEmpty()) {
            return resultList;
        }

        // 递归生成组合
        generateCombination(specList, 0, new ArrayList<>(), new ArrayList<>(), resultList);
        return resultList;
    }

    /**
     * 递归生成笛卡尔积组合
     * @param specList 所有规格
     * @param index 当前处理的规格索引
     * @param tempValueIds 临时存储的规格值ID
     * @param tempValueNames 临时存储的规格值名称
     * @param resultList 最终结果
     */
    private static void generateCombination(List<GbGoodsSpecInfo> specList, int index, List<Long> tempValueIds, List<String> tempValueNames, List<GbGoodsSkuInfo> resultList) {

        // 递归终止条件：已经遍历完所有规格
        if (index == specList.size()) {
            GbGoodsSkuInfo skuDTO = new GbGoodsSkuInfo();
            // 拼接逗号分隔字符串
            skuDTO.setSkuIds(tempValueIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            skuDTO.setSkuNames(tempValueNames.stream().collect(Collectors.joining(",")));
            resultList.add(skuDTO);
            return;
        }

        // 获取当前规格的所有值
        GbGoodsSpecInfo currentSpec = specList.get(index);
        List<GbGoodsSpecValue> specValueList = currentSpec.getSpecValueList();

        // 遍历当前规格的所有值，递归下一个规格
        for (GbGoodsSpecValue value : specValueList) {

            tempValueIds.add(value.getValId());
            tempValueNames.add(value.getSpecVal());

            // 递归处理下一个规格
            generateCombination(specList, index + 1, tempValueIds, tempValueNames, resultList);

            // 回溯：移除最后一个元素，处理下一个值
            tempValueIds.remove(tempValueIds.size() - 1);
            tempValueNames.remove(tempValueNames.size() - 1);
        }
    }
}

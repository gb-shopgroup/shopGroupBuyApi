package cn.com.shopgroup.user.controller.group;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.http.response.ArticleResponse;
import cn.com.shopgroup.user.http.response.FocusResponse;
import cn.com.shopgroup.user.model.GbArticleInfo;
import cn.com.shopgroup.user.model.GbFocusInfo;
import cn.com.shopgroup.user.service.GbArticleInfoService;
import cn.com.shopgroup.user.service.GbFocusInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class ShowsPageController {

    @Resource
    private GbArticleInfoService articleInfoService;

    @Resource
    private GbFocusInfoService focusInfoService;

    // 文章详情
    @GetMapping("/article/info")
    public JsonResult articleInfo(@RequestParam("id") Long articleId) {
        log.info("[get /group/article/info param id:{}", articleId);
        GbArticleInfo result = articleInfoService.getMiniArticleInfo(articleId);
        ArticleResponse data = new ArticleResponse();
        if (!ObjectUtils.isEmpty(result)) {
            data = new ArticleResponse(result);
        }

        return JsonResult.success(data);
    }

    // 轮播图列表
    @GetMapping("/focus")
    public JsonResult focus() {
        List<GbFocusInfo> lists = focusInfoService.getMiniFocusList();
        if (CollectionUtils.isEmpty(lists)) {
            return JsonResult.success("请求返回暂无数据");
        }
        List<FocusResponse> data = FocusResponse.getFocusResponseList(lists);
        return JsonResult.success(data);
    }

}

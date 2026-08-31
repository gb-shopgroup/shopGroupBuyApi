package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbArticleInfoMapper;
import cn.com.shopgroup.user.model.GbArticleInfo;
import cn.com.shopgroup.user.service.GbArticleInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbArticleInfoServiceImpl implements GbArticleInfoService {


    @Resource
    private GbArticleInfoMapper mapper;


    public GbArticleInfo getMiniArticleInfo(Long articleId) {

        LambdaQueryWrapper<GbArticleInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbArticleInfo::getArticleId,
                GbArticleInfo::getArticleTitle,
                GbArticleInfo::getArticleContent);
        queryWrapper.eq(GbArticleInfo::getIsClose, 0);
        queryWrapper.eq(GbArticleInfo::getArticleId, articleId);
        return mapper.selectOne(queryWrapper);
    }


}

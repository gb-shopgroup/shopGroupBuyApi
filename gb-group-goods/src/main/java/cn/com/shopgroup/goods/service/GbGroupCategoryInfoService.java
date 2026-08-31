package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.mapper.GbGroupCategoryInfoMapper;
import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public interface GbGroupCategoryInfoService {

    List<GbGroupCategoryInfo> getMiniGroupCategoryList();

}

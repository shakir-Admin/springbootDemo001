package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.ShopType;
import com.example.demo.Mapper.ShopTypeMapper;
import com.example.demo.service.ShopTypeService;
import com.example.demo.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.demo.utils.RedisConstatns.SHOP_TYPE_KEY;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

    @Autowired
    RedisCache redisCache;

    @Override
    public Result queryShopType(String isValid) {
        List<ShopType> shopTypeList = new ArrayList<>();
        //1.先查redis
        shopTypeList = redisCache.getCacheList(SHOP_TYPE_KEY);

        if(shopTypeList.size()>0){
            //2.如果存在，直接返回
            //return Result.ok(JSONUtil.toJsonStr(shopTypeList));
            return Result.ok(shopTypeList);
        }

        //3.如果不存在，查数据库
        shopTypeList = lambdaQuery().eq(ShopType::getIsValid, isValid).list();

        if(shopTypeList.isEmpty()){
            //4.数据库不存在，直接返回
            return Result.fail("商户类型配置为空！");
        }

        //5.数据库存在，写进redis
        redisCache.setCacheList(SHOP_TYPE_KEY, shopTypeList);
        //6.返回
        return Result.ok(shopTypeList);
    }
}

package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.Shop;

public interface ShopService extends IService<Shop> {

    Result insertShop(Shop shop);

    Result queryShopById(String shopId);

    Result updateByShopId(Shop shop);

    Shop selectByShopId(String shopId);}


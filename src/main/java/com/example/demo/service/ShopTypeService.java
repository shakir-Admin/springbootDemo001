package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.ShopType;

public interface ShopTypeService extends IService<ShopType> {
    Result queryShopType(String isValid);
}

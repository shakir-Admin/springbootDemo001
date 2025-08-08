package com.example.demo.controller;

import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.Shop;
import com.example.demo.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    final ShopService shopService;

    @PostMapping("addShop")
    public Result insetShop(@RequestBody Shop shop){
        return shopService.insertShop(shop);
    }

    @PostMapping("queryShopById")
    public Result queryShopById(@RequestParam(name="shopId") String shopId) {
        return shopService.queryShopById(shopId);
    }

    @PostMapping("updateByShopId")
    public Result updateByShopId(@RequestBody Shop shop){
        return shopService.updateByShopId(shop);
    }
}

package com.example.demo.controller;

import com.example.demo.DTO.OV.Result;
import com.example.demo.service.ShopTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-type")
public class ShopTypeController {

    final ShopTypeService shopTypeService;

    @PostMapping("queryShopType")
    public Result queryShopType(@RequestParam(name="isValid") String isValid){
        return shopTypeService.queryShopType(isValid);

    }
}

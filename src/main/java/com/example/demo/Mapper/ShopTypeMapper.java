package com.example.demo.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.DTO.entity.ShopType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShopTypeMapper extends BaseMapper<ShopType> {

}

package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品和口味
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品和口味信息和菜品图片
    DishDto getByIdWithFlavor(Long id);

    //修改菜品，同时修改菜品和口味
    boolean updateDish(DishDto dishDto);

    //修改状态
    boolean updateStatusById(Integer status,String[] ids);

    boolean deleteDishAndDishFlavor(String[] ids);

}

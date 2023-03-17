package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐和套餐下的菜品
    void saveWithDish(SetmealDto setmealDto);

    //根据套餐id回显数据
    SetmealDto getByIdWithSetmeal(Long id);

    //根据SetmealId修改Setmeal
    void updateSetmeal(SetmealDto setmealDto);

    //根据setmealId修改动态状态
    void updateStatus(Integer status,String[] ids);

    //根据Id删除Setmeal
    void deleteSetmeal(String[] ids);
}

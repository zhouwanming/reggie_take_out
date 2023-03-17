package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 实现新增套餐和套餐菜品
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        //先保存套餐
        this.save(setmealDto);

        //根据categoryId查询出CategoryName,并写入SetmealDto
        Long categoryId = setmealDto.getCategoryId();
        setmealDto.setCategoryName(categoryService.getById(categoryId).getName());

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item)->{
           item.setSetmealId(setmealDto.getId());
           return item;
        }).collect(Collectors.toList());

        //保存套餐菜品关系
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 根据套餐id查询数据进行回显
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithSetmeal(Long id) {

        //先根据setmealId查询Setmeal表
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();

        //对象拷贝
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询SetmealDish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //根据SetmealId查询SetmealDish关联的菜品
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        //根据SetmealId查询出关联的套餐菜品
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        //将查询出的存储到要返回的数据中
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 修改Setmeal和SetmealDish
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateSetmeal(SetmealDto setmealDto) {

        //根据SetmealId修改
        this.updateById(setmealDto);

        //条件构造器
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmealDto.getId() != null,SetmealDish::getSetmealId,setmealDto.getId());

        //先删除相关菜品
        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 根据SetmealId修改状态
     * @param ids
     */
    @Override
    public void updateStatus(Integer status,String[] ids) {

       if (ids.length != 0 && status != null){
           LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
           updateWrapper.in(Setmeal::getId, Arrays.asList(ids)).set(Setmeal::getStatus,status);
           this.update(updateWrapper);
       }


    }

    /**
     * 根据id删除Setmeal
     * @param ids
     */
    @Override
    public void deleteSetmeal(String[] ids) {

        //条件构造器
        LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        LambdaUpdateWrapper<SetmealDish> setmealDishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();

        setmealLambdaUpdateWrapper.in(Setmeal::getId,Arrays.asList(ids));
        setmealDishLambdaUpdateWrapper.in(SetmealDish::getSetmealId,Arrays.asList(ids));

        this.remove(setmealLambdaUpdateWrapper);
        setmealDishService.remove(setmealDishLambdaUpdateWrapper);


    }


}

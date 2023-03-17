package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品和同时保存口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) ->{
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList());


        //保存菜品口味
        dishFlavorService.saveBatch(flavors);
    }


    //根据菜品id查询菜品和口味信息和菜品图片
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        //根据菜品id查询相关的菜品信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //对象拷贝
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品的相关口味 （从dish_Flavor表）
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

        dishDto.setFlavors(flavors);

        return dishDto;
    }

    //修改菜品和口味
    @Override
    @Transactional
    public boolean updateDish(DishDto dishDto) {
        //修改菜品信息，DishDto继承Dish的，所以可以直接使用
        boolean update = this.updateById(dishDto);
        //判断菜品是否修改成功，后再更新菜品口味
        if (update){
            //先根据菜品Id删除DishFlavor相关的口味
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            //判断是否删除相关菜品的口味
            if (dishFlavorService.remove(dishFlavorLambdaQueryWrapper)) {
                List<DishFlavor> flavors = dishDto.getFlavors();
                flavors = flavors.stream().map((item) ->{
                    item.setDishId(dishDto.getId());
                    return item;
                }).collect(Collectors.toList());
                //保存菜品新口味
                dishFlavorService.saveBatch(flavors);
                return true;
            }
        }
        return false;

    }

    /**
     * 根据菜品Id修改菜品状态
     * @param status
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public boolean updateStatusById(Integer status, String[] ids) {
        //判断参数是否合法
        if (ids.length != 0 && ids != null && status != null && (status ==0 || status ==1)) {
            LambdaUpdateWrapper<Dish> dishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            dishLambdaUpdateWrapper.in(Dish::getId,Arrays.asList(ids)).set(Dish::getStatus,status);
            this.update(dishLambdaUpdateWrapper);
            return true;
        }
        return false;
    }

    /**
     * 批量逻辑删除根据id
     * @param ids
     * @return
     */
    @Override
    public boolean deleteDishAndDishFlavor(String[] ids) {

        //遍历要逻辑删除的id
//        for(String id : ids) {
//            //条件构造器
//            LambdaUpdateWrapper<Dish> dishWrapper = new LambdaUpdateWrapper<>();
//            LambdaUpdateWrapper<DishFlavor> dishFlavorWrapper = new LambdaUpdateWrapper<>();
//            //设置逻辑删除的参数值
//            dishWrapper.set(Dish::getIsDeleted,1).eq(Dish::getId,id);
//            dishFlavorWrapper.set(DishFlavor::getIsDeleted,1).eq(DishFlavor::getId,id);
//
//            this.update(dishWrapper);
//            dishFlavorService.update(dishFlavorWrapper);
//        }

        LambdaUpdateWrapper<Dish> dishWrapper = new LambdaUpdateWrapper<>();
        LambdaUpdateWrapper<DishFlavor> dishFlavorWrapper = new LambdaUpdateWrapper<>();

        dishWrapper.in(Dish::getId,Arrays.asList(ids)).set(Dish::getIsDeleted,1);
        dishFlavorWrapper.in(DishFlavor::getDishId,Arrays.asList(ids)).set(DishFlavor::getIsDeleted,1);

        this.update(dishWrapper);
        dishFlavorService.update(dishFlavorWrapper);

        return true;
    }

}

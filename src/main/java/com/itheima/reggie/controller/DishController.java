package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    private Long id;

    /**
     * 根据菜品id查询菜品和口味信息和菜品图片
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {

        //分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        //查询条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //姓名不能为空
        dishLambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //判断是否逻辑删除
        dishLambdaQueryWrapper.eq(Dish::getIsDeleted,0);
        //根据修改时间排序
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage, dishLambdaQueryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage);
        List<Dish> records = dishPage.getRecords();
        List<DishDto> dishDtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据菜品分类Id查询对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品和同时保存口味
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        try {
            dishService.saveWithFlavor(dishDto);
        } catch (Exception e) {
            R.error("添加失败");
        }

        return R.success("添加成功");
    }

    /**
     * 根据菜品id回显菜品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id) {

        this.id = id;

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        if (dishDto == null) {
            return R.error("查询失败");
        }
        return R.success(dishDto);
    }


    /**
     * 修改菜品和口味
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        dishDto.setId(id);
        boolean update = dishService.updateDish(dishDto);

        if (update) {
            return R.success("修改成功");
        }
        return R.error("修改失败");

    }

    /**
     * 根据菜品Id修改菜品状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatusById(@PathVariable Integer status, String[] ids) {

        log.info(status + " " + ids);

        if (dishService.updateStatusById(status, ids)) {
            return R.success("状态更改成功");
        }

        return R.error("更改状态失败");
    }

    /**
     * 通过菜品Id实现逻辑删除菜品和口味
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteDishById(String[] ids) {


        if (ids == null || ids.length == 0) {
           return R.error("删除失败");
        }

        dishService.deleteDishAndDishFlavor(ids);

        return R.success("删除成功");
    }


    /**
     * 根据菜品分类CategoryId查询Dish菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDishByCategoryId(Dish dish){

        List<DishDto> collect = null;

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        dishLambdaQueryWrapper.eq(dish.getStatus() != null,Dish::getStatus,dish.getStatus());
        dishLambdaQueryWrapper.like(dish.getName() != null,Dish::getName,dish.getName());
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort);
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);

        collect = dishList.stream().map((item)->{

            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long dishId = item.getId();

            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();

            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);

            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);;

            return dishDto;
        }).collect(Collectors.toList());


        return R.success(collect);
    }
}

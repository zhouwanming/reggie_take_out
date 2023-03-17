package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 显示套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize,String name){

        //分页构造器
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        //姓名不能为空
        setmealWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        //根据更新时间降序排序
        setmealWrapper.orderByDesc(Setmeal::getUpdateTime);
        //判断是否逻辑删除
        setmealWrapper.eq(Setmeal::getIsDeleted,0);
        setmealService.page(setmealPage,setmealWrapper);
        //对象拷贝
        BeanUtils.copyProperties(setmealPage,setmealDtoPage);
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //分类Id
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return R.success(setmealDtoPage);
    }

    /**
     * 添加套餐信息和套餐的菜品
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){

        try {
            setmealService.saveWithDish(setmealDto);
        } catch (Exception e) {
            R.error("新增失败");
        }

        return R.success("新增成功");
    }

    /**
     * 给修改页面回显数据
     * @param setmealId
     * @return
     */
    @GetMapping("/{setmealId}")
    public R<SetmealDto> getSetmealDtoBySetmealId(@PathVariable Long setmealId){

        if (setmealId != null){
            SetmealDto setmealDto = setmealService.getByIdWithSetmeal(setmealId);
            return R.success(setmealDto);
        }

        return R.error("Setmeal数据回显失败");
    }


    /**
     * 修改Setmeal和SetmealDish
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){

        setmealService.updateSetmeal(setmealDto);

        return R.success("成功修改");
    }


    /**
     *  根据setmealId动态修改status状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status,String[] ids){

        //判断是否为空
        if(status == null || ids.length == 0){
            return R.error("修改状态失败");
        }

        setmealService.updateStatus(status,ids);

        return R.success("修改状态成功");
    }


    /**
     * 根据id删除Setmeal
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteSetmealById(String[] ids){

        if (ids.length == 0){
            return R.error("删除失败");
        }

        setmealService.deleteSetmeal(ids);

        return R.success("删除成功");
    }
}

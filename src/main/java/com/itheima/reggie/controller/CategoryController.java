package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/category")
@RestController
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){

        categoryService.save(category);

        return R.success("1");
    }

    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize){

        //分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //排序条件
        queryWrapper.orderByAsc(Category::getSort);

//        执行查询
        categoryService.page(pageInfo,queryWrapper);


        return R.success(pageInfo);
    }


    /**
     * 根据Id修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){

        if (categoryService.updateById(category)) {
            return R.success("成功");
        }
        return R.error("分类修改失败");
    }

    /**
     * 根据分类id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){

      categoryService.remove(ids);

      return R.success("分类信息删除成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){

        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件 根据type判断查询菜品分类还是套餐分类
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> categoryList = categoryService.list(queryWrapper);

        return R.success(categoryList);
    }
}

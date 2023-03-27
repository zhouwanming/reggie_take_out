package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {


    @Override
    public ShoppingCart addShoppingCart(ShoppingCart shoppingCart, Long userId) {

        Long setmealId = shoppingCart.getSetmealId();
        String cartName = shoppingCart.getName();
        String image = shoppingCart.getImage();

        //判断参数是否有为null
        if (setmealId  != null && cartName != null && image != null) {

            //查询该菜品或套餐是否已有加入购物车
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId).eq(ShoppingCart::getSetmealId, setmealId);
            ShoppingCart shoppingCartOne = this.getOne(queryWrapper);

            if (shoppingCartOne != null) {
                Integer number = shoppingCartOne.getNumber();
                shoppingCartOne.setNumber(number+1);
                this.updateById(shoppingCartOne);
                shoppingCartOne = shoppingCart;
                return shoppingCartOne;
            }

            //第一次添加该菜品或套餐
            shoppingCart.setUserId(userId);
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
            shoppingCartOne = shoppingCart;
            return shoppingCartOne;
        }

        return shoppingCart;
    }

    @Override
    public void subShoppingCart(ShoppingCart shoppingCart) {

        //菜品或套餐id
        Long setmealId = (Long) shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);

        ShoppingCart shoppingCart1 = this.getOne(queryWrapper);

        //获取当前菜品或套餐的数量
        Integer number = (Integer) shoppingCart1.getNumber();

        if (number > 1){
            shoppingCart1.setNumber(number-1);
            this.updateById(shoppingCart1);
        }else {
            this.remove(queryWrapper);
        }

    }
}

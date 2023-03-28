package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {

        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
            return R.success(shoppingCartList);
        }

        return R.error("no");
    }


    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session) {

        Long userId = (Long) session.getAttribute("user");

        if (userId != null) {

            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            shoppingCartService.remove(queryWrapper);
            return R.success("清空购物车成功");
        }

        return R.error("清空失败");
    }

    /**
     * 新增购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {

        Long userId = (Long) session.getAttribute("user");

        ShoppingCart shoppingCartOne = shoppingCartService.addShoppingCart(shoppingCart, userId);

        return R.success(shoppingCartOne);
    }


    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {

        if (shoppingCart.getSetmealId() != null || shoppingCart.getDishId() != null) {
            shoppingCartService.subShoppingCart(shoppingCart);
            return R.success("sub成功");
        }
        return R.error("sub失败");
    }
}

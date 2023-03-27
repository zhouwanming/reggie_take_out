package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {

    ShoppingCart addShoppingCart(ShoppingCart shoppingCart,Long userId);

    void subShoppingCart(ShoppingCart shoppingCart);
}

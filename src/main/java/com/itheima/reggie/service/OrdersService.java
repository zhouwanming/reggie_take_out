package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    //根据订单id修改订单状态
    void updateOrderStatusById(Orders orders);

    void submit(Orders orders);
}

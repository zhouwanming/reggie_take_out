package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.OrdersService;
import org.springframework.stereotype.Service;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    //根据订单id修改订单状态
    @Override
    public void updateOrderStatusById(Orders orders) {

        //条件构造器
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        //验证订单id是否合法
        updateWrapper.eq(orders.getId() != null,Orders::getId,orders.getId());
        //修改订单状态
        updateWrapper.set(orders.getStatus() != null, Orders::getStatus, orders.getStatus());

        this.update(updateWrapper);
    }
}

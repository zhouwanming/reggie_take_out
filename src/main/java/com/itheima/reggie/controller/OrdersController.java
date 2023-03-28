package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 显示订单分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String number, String beginTime, String endTime) {

        //分页构造器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        log.info(beginTime + endTime);

        //条件构造器
        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        //查询订单号
        ordersQueryWrapper.like(StringUtils.isNotEmpty(number), Orders::getNumber, number);
        //订单时间
        ordersQueryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);

        ordersService.page(ordersPage, ordersQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage, ordersDtoPage);

        List<Orders> records = ordersPage.getRecords();

        List<OrdersDto> ordersDtoList = records.stream().map((item) -> {

            OrdersDto ordersDto = new OrdersDto();
            //对象拷贝
            BeanUtils.copyProperties(item, ordersDto);
            //用户Id
            Long userId = item.getUserId();
            //根据用户id查询用户名
            User user = userService.getById(userId);
            if (user != null) {
                ordersDto.setUserName(user.getName());
            }

            //获取地址id
            Long addressBookId = item.getAddressBookId();
            //根据地址id获取地址信息
            AddressBook addressBook = addressBookService.getById(addressBookId);
            if (addressBook != null) {
                ordersDto.setAddress(addressBook.getDetail());
                ordersDto.setConsignee(addressBook.getConsignee());
                ordersDto.setPhone(addressBook.getPhone());
            }

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

    /**
     * 修改订单状态: 取消，派送，完成
     *
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> updateOrderStatus(@RequestBody Orders orders) {

        try {
            ordersService.updateOrderStatusById(orders);
        } catch (Exception e) {
            return R.error("修改失败");
        }

        return R.success("修改成功");
    }

    /**
     * 展示个人用户的最新订单
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(Integer page, Integer pageSize, HttpSession session) {

        //获取当前用户的id
        Long userid = (Long) session.getAttribute("user");

        //分页构造器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userid != null, Orders::getUserId, userid);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(ordersPage, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage, ordersDtoPage);

        List<Orders> records = ordersPage.getRecords();

        List<OrdersDto> ordersDtoList = records.stream().map((item) -> {

            OrdersDto ordersDto = new OrdersDto();

            //对象拷贝
            BeanUtils.copyProperties(item, ordersDto);

            //查询用户名
            if (userid != null) {
                User user = userService.getById(userid);
                ordersDto.setUserName(user.getName());
            }

            Long itemId = item.getId();

            Page<OrderDetail> orderDetailPage = new Page<>();
            if (itemId != null) {
                LambdaQueryWrapper<OrderDetail> orderDetailQueryWrapper = new LambdaQueryWrapper<>();
                orderDetailQueryWrapper.eq(itemId != null, OrderDetail::getOrderId, itemId);
                orderDetailService.page(orderDetailPage, orderDetailQueryWrapper);
            }

            ordersDto.setOrderDetails(orderDetailPage.getRecords());

            return ordersDto;

        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }


    /**
     * 提交订单
     *
     * @return
     */
    @PostMapping("/submit")
    public R<String> submitOrder(@RequestBody Orders orders) {

        log.info("订单数据 : {}" + orders);

        ordersService.submit(orders);

        return R.success("下单成功");
    }
}

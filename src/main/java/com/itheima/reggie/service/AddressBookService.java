package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.mapper.AddressBookMapper;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {

    List<AddressBook> list(Long userid);

    void updateDefaultAddress(Long userId,AddressBook addressBook);
}

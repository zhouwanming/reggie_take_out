package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.mapper.AddressBookMapper;
import com.itheima.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
    @Override
    public List<AddressBook> list(Long userid) {

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(userid != null,AddressBook::getUserId,userid);

        List<AddressBook> addressBookList = this.list(queryWrapper);

        return addressBookList;
    }

    @Override
    public void updateDefaultAddress(Long userId,AddressBook addressBook) {

        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(userId != null,AddressBook::getUserId,userId);
        updateWrapper.set(AddressBook::getIsDefault,0);
        this.update(updateWrapper);

        addressBook.setIsDefault(1);
        this.updateById(addressBook);

    }
}

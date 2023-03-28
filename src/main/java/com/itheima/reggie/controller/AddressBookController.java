package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RequestMapping("/addressBook")
@RestController
public class AddressBookController {

    @Resource
    private AddressBookService addressBookService;


    /**
     * 根据用户id查询地址管理
     * @param session
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(HttpSession session){

        //获取当前移动用户的id
        Long userid = (Long) session.getAttribute("user");

        if (userid != null) {
            List<AddressBook> addressBookList = addressBookService.list(userid);
            return R.success(addressBookList);
        }

        return R.error("查询失败");

    }


    /**
     * 修改默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> defaultAddress(@RequestBody AddressBook addressBook,HttpSession session){

        Long userId = (Long) session.getAttribute("user");

        if (userId != null && addressBook.getId() != null){
            addressBookService.updateDefaultAddress(userId,addressBook);
            return R.success("修改默认地址成功");
        }

        return R.error("修改默认地址失败");
    }


    @GetMapping("/{id}")
    public R<AddressBook> getAddressById(@PathVariable Long id){

        if (id != null) {
            AddressBook addressBook = addressBookService.getById(id);
            return R.success(addressBook);
        }
        return R.error("查询失败");
    }

    /**
     * 添加收货地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> saveAddressBook(@RequestBody AddressBook addressBook,HttpSession session){

        addressBook.setUserId((Long) session.getAttribute("user"));

        addressBookService.save(addressBook);

        return R.success("添加成功");
    }


    @GetMapping("/default")
    public R<AddressBook> getDefaultAddressBook(HttpSession session){

        //获取用户Id
        Long userId = (Long) session.getAttribute("user");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(userId != null,AddressBook::getUserId,userId);
        queryWrapper.eq(AddressBook::getIsDefault,1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);


        return R.success(addressBook);
    }
}

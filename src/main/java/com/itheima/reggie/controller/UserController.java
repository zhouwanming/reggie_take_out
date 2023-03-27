package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    Boolean flog = true;

    /**
     * 发送验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

//        System.out.println(user);

        if (flog != true){
            return R.error("请稍后，再试！");
        }

        //获取手机号
        String phone = user.getPhone();

        //判断手机号是否为空
        if (StringUtils.isNotEmpty(phone)){

            //生成随机4位数的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("验证码" + code);
            //调用阿里云短信服务
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //将生成的验证码保存到session
            session.setAttribute("code:" + phone,code);
            flog = false;
            //验证码时间保存在session里
            long timestamp = System.currentTimeMillis();
            session.setAttribute("timestamp:"+phone,String.valueOf(timestamp));

            return  R.success("发送验证码成功");
        }

        return R.error("手机号不能为空");
    }

    /**
     * 手机登录，从未登陆过就进行保存到User表
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody Map map,HttpSession session){

        flog = true;

        //获取验证码和时间戳

        //获取手机号
        String phone =(String) map.get("phone");
        //获取验证码
        String code = (String) map.get("code");
        //获取时间戳
        String timestamp = (String) session.getAttribute("timestamp:"+phone);
        //过期时间
        Long timeout = 300000l;

        //验证码是否不正确
        if (!session.getAttribute("code:" + phone).equals(code)){
            return R.error("验证码错误");
        }

        //判断验证码是否已过期
        long currentTime = System.currentTimeMillis();// 获取当前时间戳
        //当前时间戳减去验证码时间戳是否大于过期时间
        if (currentTime - Long.parseLong(timestamp) > timeout ){
            return R.error("验证码已过期");
        }

        //判断一下当前手机号是否没注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,phone);

        //查询用户是否已注册
        User user = userService.getOne(queryWrapper);

        if (null == user ){
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setStatus(1);
            if (userService.save(newUser)) {
                //将userId放进session中
                session.setAttribute("user",newUser.getId());
                return R.success("验证成功");
            }
            return R.error("注册失败");
        }
        session.setAttribute("user",user.getId());

        return R.success("验证成功");
    }

    /**
     * 移动用户退出登陆功能
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session){

        //删除session中的userId
        session.removeAttribute("user");

        return R.success("退出成功");
    }
}

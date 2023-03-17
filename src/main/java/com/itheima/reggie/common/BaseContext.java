package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类,用户保存和获取当前线程登录的用户Id
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置Id
     * @param id
     */
    public static void setThreadLocal(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取Id
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}

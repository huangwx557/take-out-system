package org.project.reggie.common;

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    //用户登录后回调用来设置id,就是logincheckfiler
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }
    public  static Long getCurrentId(){
        return  threadLocal.get();
    }
}

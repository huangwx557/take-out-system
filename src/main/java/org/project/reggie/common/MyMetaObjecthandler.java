package org.project.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

//自定义元数据对象处理器
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("自动填充[insert]...");
        try {
            // 检查字段是否存在
            if (metaObject.hasSetter("createTime")) {
                // 判断字段类型并设置对应类型的值
                Class<?> fieldType = metaObject.getSetterType("createTime");
                if (fieldType == LocalDateTime.class) {
                    metaObject.setValue("createTime", LocalDateTime.now());
                } else if (fieldType == Date.class) {
                    metaObject.setValue("createTime", new Date());
                }
                log.info("自动填充createTime成功");
            }
            
            if (metaObject.hasSetter("updateTime")) {
                // 判断字段类型并设置对应类型的值
                Class<?> fieldType = metaObject.getSetterType("updateTime");
                if (fieldType == LocalDateTime.class) {
                    metaObject.setValue("updateTime", LocalDateTime.now());
                } else if (fieldType == Date.class) {
                    metaObject.setValue("updateTime", new Date());
                }
                log.info("自动填充updateTime成功");
            }
            
            // 获取当前登录用户ID
            Long userId = BaseContext.getCurrentId();
            log.info("当前线程用户ID: {}", userId);
            
            // 如果用户ID为空，使用默认值
            if (userId == null) {
                userId = 1L;
                log.info("用户ID为空，使用默认值: {}", userId);
            }
            
            if (metaObject.hasSetter("createUser")) {
                metaObject.setValue("createUser", userId);
                log.info("自动填充createUser成功: {}", userId);
            }
            
            if (metaObject.hasSetter("updateUser")) {
                metaObject.setValue("updateUser", userId);
                log.info("自动填充updateUser成功: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("自动填充异常: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("自动填充[update]...");
        try {
            if (metaObject.hasSetter("updateTime")) {
                // 判断字段类型并设置对应类型的值
                Class<?> fieldType = metaObject.getSetterType("updateTime");
                if (fieldType == LocalDateTime.class) {
                    metaObject.setValue("updateTime", LocalDateTime.now());
                } else if (fieldType == Date.class) {
                    metaObject.setValue("updateTime", new Date());
                }
                log.info("自动填充updateTime成功");
            }
            
            // 获取当前登录用户ID
            Long userId = BaseContext.getCurrentId();
            log.info("当前线程用户ID: {}", userId);
            
            // 如果用户ID为空，使用默认值
            if (userId == null) {
                userId = 1L;
                log.info("用户ID为空，使用默认值: {}", userId);
            }
            
            if (metaObject.hasSetter("updateUser")) {
                metaObject.setValue("updateUser", userId);
                log.info("自动填充updateUser成功: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("自动填充异常: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}

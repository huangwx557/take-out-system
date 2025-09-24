package org.project.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.project.reggie.common.R;
import org.project.reggie.entity.User;
import org.project.reggie.service.UserService;
import org.project.reggie.utils.SMSUtils;
import org.project.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 获取验证码（前端调用的接口）
     * @param phone
     * @param session
     * @return
     */
    @GetMapping("/code")
    public R<String> getCode(String phone, HttpSession session) {
        log.info("获取验证码，手机号: {}", phone);
        if (!StringUtils.isEmpty(phone)) {
            //生成随机6位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code={}", code);
            //调用阿里云提供的短信服务api完成发送短信
            //SMSUtils.sendMessage("短信签名","模板code",phone,code);
            log.info("短信发送成功，验证码：{}", code);
//            //将生成的验证码保存到Session
//            session.setAttribute(phone, code);
            //将生成的验证码缓存到Redis中，设置有效期为五分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机号为空，短信发送失败");
    }
    
    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session){
        log.info("用户登录：{}", map);
        //获取手机号
        String phone = map.get("phone");
        //获取验证码
        String code = map.get("code");
//        //从Session中获取保存的验证码
//        Object sessionCode = session.getAttribute(phone);

        //从Redis获取缓存对象
        String sessionCode = redisTemplate.opsForValue().get(phone);

        //进行验证码比对（页面提交的验证码和Redis中保存的验证码比对）
        if(sessionCode != null && sessionCode.equals(code)){
            //如果比对成功，说明登录成功
            
            //判断当前手机号是否为新用户，如果是新用户则自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //新用户自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1); //默认正常状态
                userService.save(user);
            }
            
            //登录成功，将用户id存入Session
            session.setAttribute("user", user.getId());

            //登录成功将验证码在Redis中删除
            redisTemplate.delete(phone);

            return R.success(user);
        }
        
        return R.error("验证码错误，登录失败");
    }
}

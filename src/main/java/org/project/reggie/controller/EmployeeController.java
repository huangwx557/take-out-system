package org.project.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.project.reggie.common.R;
import org.project.reggie.entity.Employee;
import org.project.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;
    //登录
    @PostMapping("login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //获取密码md5加密
        String password = employee.getPassword();
        String pwd = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据页面提交用户username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername,employee.getUsername());
        //获取查询信息
        Employee emp = employeeService.getOne(lambdaQueryWrapper);

        if(emp == null){
            return  R.error("登录失败");
        }
        //比对查询的密码与数据库加密密码
        if(!emp.getPassword().equals(pwd)){
            return  R.error("登录失败");
        }
        //查看员工状态
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //登录成功,将用户id放入Session,返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    //退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清除Session中保存的当前员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //新增员工
    @PostMapping
    @CacheEvict(value = "employeeCache", allEntries = true)
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工",employee.toString());
        employee.setPassword( DigestUtils.md5DigestAsHex("123456".getBytes()));
        
        // 手动设置创建时间和更新时间
        employee.setCreateTime(new Date());
        employee.setUpdateTime(new Date());
        
        // 获取当前登录用户ID
        Long empId = (Long)request.getSession().getAttribute("employee");
        if (empId == null) {
            empId = 1L; // 默认用户ID
        }
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }
    @GetMapping("/page")
    @Cacheable(value = "employeeCache", key = "#page + '_' + #pageSize + '_' + (#name != null ? #name : '')")
    public R<Page> page(int page, int pageSize, String name){
            log.info("page = {}, pageSize = {}, name = {}",page,pageSize,name);
            System.out.println("从数据库查询员工分页数据");
            
            //构造分页构造器
            Page pageInfo = new Page(page, pageSize);
            //条件构造器
            LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //添加过滤条件
            lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
            //排序条件
            lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
            //执行查询
            employeeService.page(pageInfo,lambdaQueryWrapper);
            
            return R.success(pageInfo);
    }

    @PutMapping
    @CacheEvict(value = "employeeCache", allEntries = true)
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为{}",id);
        
        // 手动设置更新时间
        employee.setUpdateTime(new Date());
        
        // 获取当前登录用户ID
        Long empId = (Long)request.getSession().getAttribute("employee");
        if (empId == null) {
            empId = 1L; // 默认用户ID
        }
        employee.setUpdateUser(empId);
        
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    /*

   根据id查询员工信息
     */
    @GetMapping("/{id}")
    @Cacheable(value = "employeeCache", key = "'emp_' + #id")
    public R<Employee> get(@PathVariable("id") Long id){
        log.info("查询员工信息");
        System.out.println("从数据库查询员工信息，ID: " + id);
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return  R.success(employee);
        }
       return  R.error("没有查询到员工信息");
    }
}

package org.project.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.R;
import org.project.reggie.entity.Category;
import org.project.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    //新增分类
    @PostMapping
    @CacheEvict(value = "categoryCache", allEntries = true)
    public R<String> save(@RequestBody Category category) {
        log.info("新增分类", category);
        
        // 手动设置创建时间和更新时间
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        
        // 获取当前登录用户ID
        Long userId = (Long) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                .getAttribute("employee", org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        if (userId == null) {
            userId = 1L; // 默认用户ID
        }
        category.setCreateUser(userId);
        category.setUpdateUser(userId);
        
        categoryService.save(category);
        return  R.success("新增分类成功");
    }
    //分页查询
   @GetMapping("/page")
    @Cacheable(value = "categoryCache", key = "'page_' + #page + '_' + #pageSize")
    public R<Page> page(int page ,int pageSize){
        System.out.println("从数据库查询分类分页数据");
        
        //分页构造器
       Page<Category> pageInfo = new Page<>(page, pageSize);
       //条件构造器
       LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
       lambdaQueryWrapper.orderByAsc(Category::getSort);
       categoryService.page(pageInfo,lambdaQueryWrapper);
       
       System.out.println("查询到分类分页数据，总记录数: " + pageInfo.getTotal());
       return  R.success(pageInfo);
   }
   //根据id来删除分类
   @DeleteMapping
   @CacheEvict(value = "categoryCache", allEntries = true)
    public  R<String> delete(Long ids){
        log.info("id为",ids);
//        categoryService.removeById(ids);
       categoryService.remove(ids);
        return  R.success("删除成功");
   }
   //修改分类
    @PutMapping
    @CacheEvict(value = "categoryCache", allEntries = true)
    public   R<String> update(@RequestBody Category category){
        log.info("修改分类信息",category);
        
        // 手动设置更新时间
        category.setUpdateTime(new Date());
        
        // 获取当前登录用户ID
        Long userId = (Long) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                .getAttribute("employee", org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        if (userId == null) {
            userId = 1L; // 默认用户ID
        }
        category.setUpdateUser(userId);
        
        categoryService.updateById(category);
        return   R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "categoryCache", key = "#category.type != null ? #category.type : 'all'")
    public R<List<Category>> list(Category category){
        System.out.println("从数据库查询分类数据，type: " + (category.getType() != null ? category.getType() : "all"));
        
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lambdaQueryWrapper);
        
        System.out.println("查询到" + list.size() + "条分类数据");
        return   R.success(list);
    }
}

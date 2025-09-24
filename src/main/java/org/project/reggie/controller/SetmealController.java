package org.project.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.R;
import org.project.reggie.dto.SetmealDto;
import org.project.reggie.entity.Category;
import org.project.reggie.entity.Setmeal;
import org.project.reggie.service.CategoryService;
import org.project.reggie.service.SetmealDishService;
import org.project.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import javax.management.DescriptorKey;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        System.out.println("新增套餐并清除所有套餐缓存");
        
        // 手动设置创建时间和更新时间
        setmealDto.setCreateTime(new java.util.Date());
        setmealDto.setUpdateTime(new java.util.Date());
        
        // 手动设置创建用户和更新用户
        Long userId = org.project.reggie.common.BaseContext.getCurrentId();
        if (userId == null) {
            userId = 1L; // 默认用户ID
        }
        setmealDto.setCreateUser(userId);
        setmealDto.setUpdateUser(userId);
        
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
  @GetMapping("/page")
    @Cacheable(value = "setmealCache", key = "'page_' + #page + '_' + #pageSize + '_' + (#name != null ? #name : '')")
    public R<Page> list(int page,int pageSize,String name){
      log.info("分页查询套餐，page = {}, pageSize = {}, name = {}", page, pageSize, name);
      System.out.println("从数据库查询套餐分页数据");
      
      Page<Setmeal> pageInfo = new Page<>(page, pageSize);
      Page<SetmealDto> dtoPage = new Page<>();

      LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.like(name != null, Setmeal::getName, name);
      queryWrapper.orderByDesc(Setmeal::getUpdateTime);
      setmealService.page(pageInfo, queryWrapper);

      //拷贝
      BeanUtils.copyProperties(pageInfo, dtoPage, "records");
      List<Setmeal> records = pageInfo.getRecords();

      List<SetmealDto> list = records.stream().map((item) -> {
          SetmealDto setmealDto = new SetmealDto();
          BeanUtils.copyProperties(item, setmealDto);
          //分类id
          Long categoryId = item.getCategoryId();
          //根据分类id查询分类对象
          Category category = categoryService.getById(categoryId);
          if(category != null){
              //分类名称
              String categoryName = category.getName();
              setmealDto.setCategoryName(categoryName);
          }
          return setmealDto;
      }).collect(Collectors.toList());
      dtoPage.setRecords(list);
      
      log.info("查询到套餐分页数据，总记录数: {}", pageInfo.getTotal());
      return R.success(dtoPage);
  }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
       System.out.println("删除套餐并清除所有套餐缓存");
       setmealService.removeWithDish(ids);
       return R.success("套餐删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId != null ? #setmeal.categoryId : 'all'")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("查询套餐数据，categoryId: {}", setmeal.getCategoryId());
        System.out.println("从数据库查询套餐数据");
        
        // 确保status有默认值
        if(setmeal.getStatus() == null) {
            setmeal.setStatus(1); // 设置默认值为1
        }
        
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        
        log.info("查询到{}条套餐数据", list.size());

        return R.success(list);
    }
}

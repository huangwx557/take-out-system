package org.project.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.R;
import org.project.reggie.dto.DishDto;
import org.project.reggie.entity.Category;
import org.project.reggie.entity.Dish;
import org.project.reggie.entity.DishFlavor;
import org.project.reggie.service.CategoryService;
import org.project.reggie.service.DishFlavorService;
import org.project.reggie.service.DishService;
import org.project.reggie.service.impl.DishServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishServiceImpl dishServiceImpl;

    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        
        // 手动设置创建时间和更新时间
        dishDto.setCreateTime(new java.util.Date());
        dishDto.setUpdateTime(new java.util.Date());
        
        // 手动设置创建用户和更新用户
        Long userId = org.project.reggie.common.BaseContext.getCurrentId();
        if (userId == null) {
            userId = 1L; // 默认用户ID
        }
        dishDto.setCreateUser(userId);
        dishDto.setUpdateUser(userId);
        
        dishService.saveWithFlavor(dishDto);

        //     //清理所有菜品
//        Set<String> keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        //清理部分缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";//状态都是显示的
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }
    
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝1.被拷2.拷入.3.不拷
        BeanUtils.copyProperties(pageInfo, dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            if (categoryId != null) {
                Category category = categoryService.getById(categoryId);
                if (category != null) {
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
                }
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public  R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return  R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
     dishService.updateWithFlavor(dishDto);

//     //清理所有菜品
//        Set<String> keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        //清理部分缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";//状态都是显示的
        redisTemplate.delete(key);
        return  R.success("修改菜品成功");
    }

//    /**
//     * 根据条件查询相应菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);
//        //排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//        return  R.success(list);
//    }

    /**
     * 根据条件查询相应菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish  dish) {
        // 确保status有默认值
        if(dish.getStatus() == null) {
            dish.setStatus(1); // 设置默认值为1
            System.out.println("菜品status为null，设置默认值为1");
        }
        
        List<DishDto> dishDtoList = null;
        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397844391040167938_1
        System.out.println("查询菜品数据，使用缓存key: " + key);
        //从redis缓存获取数据

        // 从Redis获取数据，并进行类型转换
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            dishDtoList = (List<DishDto>) value;
            System.out.println("从缓存获取到菜品数据");
        }

        if(dishDtoList != null){
            //在缓存中存在，无需查询数据库，直接返回
            return  R.success(dishDtoList);
        }
        //如果不存在，查询数据库，将数据添加到缓存中

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        //排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            if (categoryId != null) {
                Category category = categoryService.getById(categoryId);
                if (category != null) {
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
                }
            }
            //查询口味数据
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper2 = new LambdaQueryWrapper<>();
            List<DishFlavor> list1 = dishFlavorService.list(queryWrapper2.eq(DishFlavor::getDishId, dishId));
            dishDto.setFlavors(list1);
            return dishDto;
        }).collect(Collectors.toList());

      //将菜品将数据添加到缓存中
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}

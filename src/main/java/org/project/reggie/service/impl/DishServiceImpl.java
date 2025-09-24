package org.project.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.BaseContext;
import org.project.reggie.dto.DishDto;
import org.project.reggie.entity.Dish;
import org.project.reggie.entity.DishFlavor;
import org.project.reggie.mapper.DishMapper;
import org.project.reggie.service.DishFlavorService;
import org.project.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    
    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品，同时插入菜品对应口味数据
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到菜品表dish
        this.save(dishDto);
        Long dishDtoId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors = flavors.stream().map((item) -> {
                item.setDishId(dishDtoId);
                // 手动设置创建和更新时间
                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());
                
                // 获取当前登录用户ID
                Long userId = BaseContext.getCurrentId();
                if (userId == null) {
                    userId = 1L; // 默认用户ID
                }
                
                // 手动设置创建和更新用户
                item.setCreateUser(userId);
                item.setUpdateUser(userId);
                
                return item;
            }).collect(Collectors.toList());
            //保存菜品口味数据到菜品口味表dishFlavor
            dishFlavorService.saveBatch(flavors);
        }
    }
    //根据id来查询菜品及口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        //查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> LambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(LambdaQueryWrapper);
        dishDto.setFlavors(flavors);

        return  dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息
        this.updateById(dishDto);
        //更新dishFlavor表信息（先清空信息，再添加信息）
        //清理
        LambdaQueryWrapper<DishFlavor> LambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(LambdaQueryWrapper);
        //更新
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }
}

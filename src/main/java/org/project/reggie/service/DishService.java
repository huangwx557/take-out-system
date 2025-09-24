package org.project.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.reggie.dto.DishDto;
import org.project.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应口味数据
    public void saveWithFlavor (DishDto dishDto);

    //更新菜品信息及口味信息
     public void updateWithFlavor (DishDto dishDto);
    //根据id来查询菜品及口味信息
    public DishDto getByIdWithFlavor(Long id);
}

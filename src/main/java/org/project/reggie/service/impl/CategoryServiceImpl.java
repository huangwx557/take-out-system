package org.project.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.reggie.common.CustomException;
import org.project.reggie.entity.Category;
import org.project.reggie.entity.Dish;
import org.project.reggie.entity.Setmeal;
import org.project.reggie.mapper.CategoryMapper;
import org.project.reggie.service.CategoryService;
import org.project.reggie.service.DishService;
import org.project.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    
    @Autowired
    private SetmealService setmealService;

    //根据id删除分类
    @Override
    public void remove(Long id) {
        //查询是否关联菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        long count1 = dishService.count(dishLambdaQueryWrapper);
        //是否关联菜品
        if (count1 > 0) {
            //抛出业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //查询是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        long count2 = setmealService.count(setmealLambdaQueryWrapper);
        //是否关联套餐
        if(count2 > 0){
            //抛出业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //正常删除
        super.removeById(id);
    }
}

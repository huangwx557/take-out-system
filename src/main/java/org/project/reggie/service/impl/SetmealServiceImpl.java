package org.project.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.BaseContext;
import org.project.reggie.common.CustomException;
import org.project.reggie.dto.SetmealDto;
import org.project.reggie.entity.Setmeal;
import org.project.reggie.entity.SetmealDish;
import org.project.reggie.mapper.SetmealMapper;
import org.project.reggie.service.SetmealDishService;
import org.project.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时保存套餐与菜品之间的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes = setmealDishes.stream().map((item) -> {
                item.setSetmealId(setmealDto.getId().toString()); // 转换为String类型
                
                // 手动设置创建和更新时间
                Date now = new Date();
                item.setCreateTime(now);
                item.setUpdateTime(now);
                
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
            //保存套餐与菜品的关联关系,操作setmealDish
            setmealDishService.saveBatch(setmealDishes);
        }
    }
    /**
     * 删除套餐同时删除套餐和菜品的关联关系
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确认是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        long count = this.count(queryWrapper);

        //不能删除，抛出业务异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //可以删除先删除套餐中的数据--setmeal
        this.removeByIds(ids);
        //删除关系表的数据--setmeal_dish
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        // 将Long类型的id转换为String类型，因为SetmealDish中的setmealId是String类型
        List<String> setmealIds = ids.stream().map(Object::toString).collect(Collectors.toList());
        queryWrapper1.in(SetmealDish::getSetmealId, setmealIds);
        setmealDishService.remove(queryWrapper1);
    }
}

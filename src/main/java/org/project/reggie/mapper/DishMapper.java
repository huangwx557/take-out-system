package org.project.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.project.reggie.entity.Dish;
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}

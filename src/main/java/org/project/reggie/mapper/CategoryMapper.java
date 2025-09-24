package org.project.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.project.reggie.entity.Category;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}

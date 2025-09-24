package org.project.reggie.dto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.reggie.entity.Dish;
import org.project.reggie.entity.DishFlavor;

import java.util.ArrayList;
import java.util.List;

/**
 * 原本的dish不够用了，对dish类进行扩展
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}

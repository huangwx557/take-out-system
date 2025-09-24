package org.project.reggie.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.reggie.entity.Setmeal;
import org.project.reggie.entity.SetmealDish;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

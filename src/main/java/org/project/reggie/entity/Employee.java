package org.project.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工信息
 * </p>
 *
 * @author anyi
 * @since 2022-05-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "员工信息")
public class Employee implements Serializable {

    private static final long serialVersionUID=1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "状态 0:禁用，1:正常")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    //自动填充策略
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "修改人")
    private Long updateUser;
}

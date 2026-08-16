package com.ordering.modules.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工：绑定角色（role_id），status 1在职 0停用。
 */
@Data
@TableName("employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String name;
    private String phone;
    private String account;
    private String password;
    private Long roleId;
    private Integer status;
    private LocalDateTime createTime;
}

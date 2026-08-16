package com.ordering.modules.employee.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工视图对象：附带角色名（roleName）。
 */
@Data
public class EmployeeVO {
    private Long id;
    private String name;
    private String phone;
    private String account;
    private Long roleId;
    private String roleName;
    private Integer status;
    private LocalDateTime createTime;
}

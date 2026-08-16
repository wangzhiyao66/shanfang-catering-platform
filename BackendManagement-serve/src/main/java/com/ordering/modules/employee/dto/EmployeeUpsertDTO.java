package com.ordering.modules.employee.dto;

import lombok.Data;

/**
 * 员工新增/编辑入参。
 */
@Data
public class EmployeeUpsertDTO {
    private String name;
    private String phone;
    private String account;
    private String password;   // 可选，缺省置为 123456
    private Long roleId;
    private Integer status;     // 1在职 0停用
}

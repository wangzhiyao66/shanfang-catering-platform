package com.ordering.modules.employee.dto;

import lombok.Data;

/**
 * 角色新增/编辑入参。
 */
@Data
public class RoleUpsertDTO {
    private String name;
    private String permissions; // 逗号分隔权限码；"*" 表示全部
    private Integer status;
}

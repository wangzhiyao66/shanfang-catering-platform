package com.ordering.modules.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色：permissions 为逗号分隔的权限码（如 dish:manage,order:manage）；"*" 表示全部。
 */
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String name;
    private String permissions;
    private Integer status;
    private LocalDateTime createTime;
}

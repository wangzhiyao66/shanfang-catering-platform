package com.ordering.modules.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long categoryId;
    private String name;
    private Integer price;          // 单位：分（前端 ÷100 展示）
    private String description;
    private String image;
    private Integer status;         // 1 上架 0 下架
    private Integer isSoldOut;      // 1 售罄 0 否
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;        // 逻辑删除

    @Version
    private Integer version;        // 乐观锁（接单/改状态防并发）

    /** 规格选项（来自 dish_spec，非 DB 列；仅详情接口回填） */
    @TableField(exist = false)
    private List<DishSpec> specs;
}

package com.ordering.modules.employee.service;

import com.ordering.modules.employee.dto.RoleUpsertDTO;
import com.ordering.modules.employee.entity.Role;

import java.util.List;

public interface RoleService {

    /** 角色列表 */
    List<Role> listRoles(Long shopId);

    /** 新增角色，返回主键 */
    Long createRole(Long shopId, RoleUpsertDTO dto);

    /** 编辑角色 */
    void updateRole(Long shopId, Long id, RoleUpsertDTO dto);

    /** 删除角色 */
    void deleteRole(Long shopId, Long id);
}

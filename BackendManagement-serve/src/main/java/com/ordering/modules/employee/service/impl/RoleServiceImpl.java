package com.ordering.modules.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.employee.dto.RoleUpsertDTO;
import com.ordering.modules.employee.entity.Role;
import com.ordering.modules.employee.mapper.RoleMapper;
import com.ordering.modules.employee.service.RoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Role> listRoles(Long shopId) {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getShopId, shopId).orderByDesc(Role::getCreateTime));
    }

    @Override
    public Long createRole(Long shopId, RoleUpsertDTO dto) {
        Role r = new Role();
        r.setShopId(shopId);
        r.setName(dto.getName());
        r.setPermissions(dto.getPermissions());
        r.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        r.setCreateTime(LocalDateTime.now());
        roleMapper.insert(r);
        return r.getId();
    }

    @Override
    public void updateRole(Long shopId, Long id, RoleUpsertDTO dto) {
        Role r = require(shopId, id);
        if (dto.getName() != null) r.setName(dto.getName());
        if (dto.getPermissions() != null) r.setPermissions(dto.getPermissions());
        if (dto.getStatus() != null) r.setStatus(dto.getStatus());
        roleMapper.updateById(r);
    }

    @Override
    public void deleteRole(Long shopId, Long id) {
        require(shopId, id);
        roleMapper.deleteById(id);
    }

    private Role require(Long shopId, Long id) {
        Role r = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getId, id).eq(Role::getShopId, shopId));
        if (r == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "角色不存在");
        }
        return r;
    }
}

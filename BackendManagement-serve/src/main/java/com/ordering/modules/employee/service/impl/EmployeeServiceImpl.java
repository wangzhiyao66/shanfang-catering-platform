package com.ordering.modules.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.employee.dto.EmployeeUpsertDTO;
import com.ordering.modules.employee.entity.Employee;
import com.ordering.modules.employee.entity.Role;
import com.ordering.modules.employee.mapper.EmployeeMapper;
import com.ordering.modules.employee.mapper.RoleMapper;
import com.ordering.modules.employee.service.EmployeeService;
import com.ordering.modules.employee.vo.EmployeeVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final RoleMapper roleMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper, RoleMapper roleMapper) {
        this.employeeMapper = employeeMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<EmployeeVO> listEmployees(Long shopId) {
        List<Employee> emps = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>().eq(Employee::getShopId, shopId)
                        .orderByDesc(Employee::getCreateTime));
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().eq(Role::getShopId, shopId));
        Map<Long, String> roleName = roles.stream()
                .collect(Collectors.toMap(Role::getId, Role::getName, (a, b) -> a));
        return emps.stream().map(e -> {
            EmployeeVO v = new EmployeeVO();
            v.setId(e.getId());
            v.setName(e.getName());
            v.setPhone(e.getPhone());
            v.setAccount(e.getAccount());
            v.setRoleId(e.getRoleId());
            v.setRoleName(e.getRoleId() == null ? null : roleName.get(e.getRoleId()));
            v.setStatus(e.getStatus());
            v.setCreateTime(e.getCreateTime());
            return v;
        }).collect(Collectors.toList());
    }

    @Override
    public Long createEmployee(Long shopId, EmployeeUpsertDTO dto) {
        Employee e = new Employee();
        e.setShopId(shopId);
        e.setName(dto.getName());
        e.setPhone(dto.getPhone());
        e.setAccount(dto.getAccount());
        e.setPassword(dto.getPassword() == null ? "123456" : dto.getPassword());
        e.setRoleId(dto.getRoleId());
        e.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        e.setCreateTime(LocalDateTime.now());
        employeeMapper.insert(e);
        return e.getId();
    }

    @Override
    public void updateEmployee(Long shopId, Long id, EmployeeUpsertDTO dto) {
        Employee e = require(shopId, id);
        if (dto.getName() != null) e.setName(dto.getName());
        if (dto.getPhone() != null) e.setPhone(dto.getPhone());
        if (dto.getAccount() != null) e.setAccount(dto.getAccount());
        if (dto.getPassword() != null) e.setPassword(dto.getPassword());
        if (dto.getRoleId() != null) e.setRoleId(dto.getRoleId());
        if (dto.getStatus() != null) e.setStatus(dto.getStatus());
        employeeMapper.updateById(e);
    }

    @Override
    public void deleteEmployee(Long shopId, Long id) {
        require(shopId, id);
        employeeMapper.deleteById(id);
    }

    private Employee require(Long shopId, Long id) {
        Employee e = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getId, id).eq(Employee::getShopId, shopId));
        if (e == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "员工不存在");
        }
        return e;
    }
}

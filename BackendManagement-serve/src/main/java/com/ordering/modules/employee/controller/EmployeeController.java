package com.ordering.modules.employee.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.employee.dto.EmployeeUpsertDTO;
import com.ordering.modules.employee.dto.RoleUpsertDTO;
import com.ordering.modules.employee.entity.Role;
import com.ordering.modules.employee.service.EmployeeService;
import com.ordering.modules.employee.service.RoleService;
import com.ordering.modules.employee.vo.EmployeeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RoleService roleService;

    public EmployeeController(EmployeeService employeeService, RoleService roleService) {
        this.employeeService = employeeService;
        this.roleService = roleService;
    }

    // ===== 员工 =====
    @GetMapping("/admin/employees")
    public R<List<EmployeeVO>> listEmployees() {
        return R.ok(employeeService.listEmployees(RequestContext.getShopId()));
    }

    @PostMapping("/admin/employee")
    public R<Long> createEmployee(@RequestBody EmployeeUpsertDTO dto) {
        return R.ok(employeeService.createEmployee(RequestContext.getShopId(), dto));
    }

    @PutMapping("/admin/employee/{id}")
    public R<Void> updateEmployee(@PathVariable Long id, @RequestBody EmployeeUpsertDTO dto) {
        employeeService.updateEmployee(RequestContext.getShopId(), id, dto);
        return R.ok();
    }

    @DeleteMapping("/admin/employee/{id}")
    public R<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(RequestContext.getShopId(), id);
        return R.ok();
    }

    // ===== 角色 =====
    @GetMapping("/admin/roles")
    public R<List<Role>> listRoles() {
        return R.ok(roleService.listRoles(RequestContext.getShopId()));
    }

    @PostMapping("/admin/role")
    public R<Long> createRole(@RequestBody RoleUpsertDTO dto) {
        return R.ok(roleService.createRole(RequestContext.getShopId(), dto));
    }

    @PutMapping("/admin/role/{id}")
    public R<Void> updateRole(@PathVariable Long id, @RequestBody RoleUpsertDTO dto) {
        roleService.updateRole(RequestContext.getShopId(), id, dto);
        return R.ok();
    }

    @DeleteMapping("/admin/role/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(RequestContext.getShopId(), id);
        return R.ok();
    }
}

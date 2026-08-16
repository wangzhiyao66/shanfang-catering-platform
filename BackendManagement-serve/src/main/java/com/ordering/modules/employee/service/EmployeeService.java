package com.ordering.modules.employee.service;

import com.ordering.modules.employee.dto.EmployeeUpsertDTO;
import com.ordering.modules.employee.vo.EmployeeVO;

import java.util.List;

public interface EmployeeService {

    /** 员工列表（附带角色名） */
    List<EmployeeVO> listEmployees(Long shopId);

    /** 新增员工，返回主键 */
    Long createEmployee(Long shopId, EmployeeUpsertDTO dto);

    /** 编辑员工（仅更新非空字段） */
    void updateEmployee(Long shopId, Long id, EmployeeUpsertDTO dto);

    /** 删除员工 */
    void deleteEmployee(Long shopId, Long id);
}

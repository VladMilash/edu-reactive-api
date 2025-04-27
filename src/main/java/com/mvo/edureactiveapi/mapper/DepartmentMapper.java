package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.DepartmentDTO;
import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.entity.Department;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    default ResponseDepartmentDTO toResponseDepartmentDTO(Department department) {
        return new ResponseDepartmentDTO(
            department.getId(),
            department.getName(),
            null
        );
    }

    default Department fromDepartmentTransientDTO(DepartmentTransientDTO departmentTransientDTO) {
        return new Department(
            null,
            departmentTransientDTO.name(),
            null
        );
    }
}

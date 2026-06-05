package org.example.user.dto;

import lombok.Data;

/**
 * 用户分页查询 DTO
 */
@Data
public class SysUserQueryDTO {

    private Integer page = 1;
    private Integer size = 10;
    private Integer roleCode;
    private String keyword;
}

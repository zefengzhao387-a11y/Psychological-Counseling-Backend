package org.example.user.dto;

import lombok.Data;

/**
 * 学生自助注册
 */
@Data
public class RegisterDTO {

    private String userNo;
    private String username;
    private String password;
    private String phone;
    private String gender;
    private String department;
}

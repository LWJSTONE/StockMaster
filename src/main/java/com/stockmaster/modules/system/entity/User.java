package com.stockmaster.modules.system.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String realName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "text")
    private String avatar;

    @Column(name = "gender", columnDefinition = "tinyint default 0")
    private Integer gender = 0;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @Column(name = "admin", columnDefinition = "tinyint(1) default 0")
    private Boolean admin = false;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;
}

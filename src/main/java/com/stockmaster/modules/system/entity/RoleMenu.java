package com.stockmaster.modules.system.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_role_menu")
@IdClass(RoleMenuId.class)
public class RoleMenu {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;
}

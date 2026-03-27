package com.stockmaster.modules.system.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class RoleMenuId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roleId;
    private Long menuId;

    public RoleMenuId() {
    }

    public RoleMenuId(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleMenuId that = (RoleMenuId) o;

        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null) return false;
        return menuId != null ? menuId.equals(that.menuId) : that.menuId == null;
    }

    @Override
    public int hashCode() {
        int result = roleId != null ? roleId.hashCode() : 0;
        result = 31 * result + (menuId != null ? menuId.hashCode() : 0);
        return result;
    }
}

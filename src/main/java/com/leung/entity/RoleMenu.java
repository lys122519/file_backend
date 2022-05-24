package com.leung.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @Description:
 * @author: leung
 * @date: 2022-04-08 15:25
 */
@TableName("sys_role_menu")
public class RoleMenu {

    private Integer roleId;
    private Integer menuId;


    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }
}

package com.example.grademanage.Entity;

import com.example.grademanage.Util.TableId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Role {
    @TableId(isAutoIncrement = true)
    private Integer roleId; // 角色ID（对应role_id）
    private String roleName; // 角色名称
    private String roleDesc; // 角色描述

    public Role(Integer roleId, String roleName, String roleDesc) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleDesc = roleDesc;
    }
}

package com.example.grademanage.Entity;

import com.example.grademanage.Util.TableId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermission {
    @TableId(isAutoIncrement = true)
    private Integer rpId; // 关联ID
    private Integer roleId; // 角色ID
    private Integer permissionId; // 权限ID

    public RolePermission(Integer rpId, Integer roleId, Integer permissionId) {
        this.rpId = rpId;
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
}

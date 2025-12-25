package com.example.grademanage.Entity;

import com.example.grademanage.Util.TableId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission {
    @TableId(isAutoIncrement = true)
    private Integer permissionId; // 权限ID
    private String permissionName; // 权限名称
    private String permissionCode; // 权限编码
    private String permissionDesc; // 权限描述

    public Permission(Integer permissionId, String permissionName,
                      String permissionCode, String permissionDesc) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.permissionDesc = permissionDesc;
    }
}

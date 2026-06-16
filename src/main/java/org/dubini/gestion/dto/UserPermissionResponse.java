package org.dubini.gestion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionResponse {
    private String email;
    private boolean canManagePermissions;
    private boolean canManageOrganization;
    private boolean canManageWeb;
    private boolean canManageFinances;
}

package org.dubini.gestion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {
    @Id
    private Long id;
    private String email;
    private boolean canManagePermissions;
    private boolean canManageOrganization;
    private boolean canManageWeb;
    private boolean canManageFinances;
}

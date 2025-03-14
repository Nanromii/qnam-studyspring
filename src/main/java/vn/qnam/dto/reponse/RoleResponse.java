package vn.qnam.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class RoleResponse {
    private String name;
    private String description;
    private Set<PermissionResponse> permissions;
}

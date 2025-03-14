package vn.qnam.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionResponse {
    private String name;
    private String description;
}

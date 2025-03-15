package vn.qnam.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private String name;
    private String description;
}

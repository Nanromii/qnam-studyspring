package vn.qnam.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class RoleDTO implements Serializable {
    private String name;
    private String description;
    private Set<String> permissions;
}

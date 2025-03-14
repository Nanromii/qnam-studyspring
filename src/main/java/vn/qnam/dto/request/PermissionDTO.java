package vn.qnam.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PermissionDTO implements Serializable {
    private String name;
    private String description;
}

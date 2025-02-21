package vn.qnam.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Type {
    @JsonProperty("owner")
    OWNER,
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("user")
    USER;
}

package vn.qnam.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Scope {
    @JsonProperty("owner")
    OWNER,
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("user")
    USER;
}

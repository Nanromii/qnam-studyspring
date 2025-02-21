package vn.qnam.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("male"),
    FEMALE("female"),
    OTHER("other");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromString(String key) {
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(key) || gender.name().equalsIgnoreCase(key)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + key);
    }
}

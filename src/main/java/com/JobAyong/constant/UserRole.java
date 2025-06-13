package com.JobAyong.constant;

public enum UserRole {
    USER("user"),
    COMPANY("company"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant com.JobAyong.constant.UserRole." + text);
    }
} 
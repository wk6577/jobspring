package com.JobAyong.constant;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    DELETE_WAITING("DELETE_WAITING");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserStatus fromString(String value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return ACTIVE; // 기본값
    }
} 
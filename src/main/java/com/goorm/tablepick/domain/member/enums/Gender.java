package com.goorm.tablepick.domain.member.enums;

public enum Gender {
    MALE, FEMALE, OTHER;

    public static Gender from(String value) {
        if (value == null) {
            return OTHER;
        }
        try {
            return Gender.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}

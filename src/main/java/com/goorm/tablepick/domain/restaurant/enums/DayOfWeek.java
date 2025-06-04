package com.goorm.tablepick.domain.restaurant.enums;

public enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public java.time.DayOfWeek toJavaDayOfWeek() {
        return java.time.DayOfWeek.valueOf(this.name());
    }

    public static DayOfWeek fromJavaDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        return valueOf(javaDayOfWeek.name());
    }
}

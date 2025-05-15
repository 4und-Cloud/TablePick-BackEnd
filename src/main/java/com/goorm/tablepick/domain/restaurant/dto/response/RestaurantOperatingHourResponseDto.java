package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RestaurantOperatingHourResponseDto {
    @Schema(description = "요일", example = "MONDAY")
    private final String dayOfWeek;

    @Schema(description = "영업 시작 시간", example = "12:00")
    private final LocalTime openTime;

    @Schema(description = "영업 종료 시간", example = "24:00")
    private final LocalTime closeTime;

    @Schema(description = "휴일 여부", example = "false")
    private final boolean isHoliday;

    public static RestaurantOperatingHourResponseDto from(RestaurantOperatingHour entity) {
        return RestaurantOperatingHourResponseDto.builder()
                .dayOfWeek(entity.getDayOfWeek().name())
                .openTime(entity.getOpenTime())
                .closeTime(entity.getCloseTime())
                .isHoliday(entity.isHoliday())
                .build();
    }
}
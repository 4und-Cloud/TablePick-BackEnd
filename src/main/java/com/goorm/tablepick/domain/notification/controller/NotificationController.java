package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.notification.dto.request.FCMTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "알림 예약, 조회 및 FCM 토큰 관리를 위한 API")
public class NotificationController {
    private final NotificationService notificationService;
    private final FCMTokenService fcmTokenService;

    @Operation(
            summary = "알림 예약",
            description = "새로운 알림을 예약합니다. 회원ID, 알림타입ID, 예약ID, 예약시간을 포함해야 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 예약 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/schedule")
    public ResponseEntity<NotificationResponse> scheduleNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "알림 예약 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationRequest.class))
            )
            @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.scheduleNotification(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "알림 상태 조회",
            description = "특정 알림의 상태를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationStatus(
            @Parameter(
                    name = "id",
                    description = "알림 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationStatus(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원 알림 목록 조회",
            description = "특정 회원의 알림 목록을 조회합니다. 선택적으로 상태 필터링이 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 알림 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<NotificationResponse>> getMemberNotifications(
            @Parameter(
                    name = "memberId",
                    description = "회원 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long memberId,
            @Parameter(
                    name = "status",
                    description = "알림 상태 필터 (예: PENDING, SENT, FAILED)",
                    required = false,
                    example = "SENT",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) String status) {
        List<NotificationResponse> notifications = notificationService.getMemberNotifications(memberId, status);
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "FCM 토큰 업데이트",
            description = "회원의 FCM 토큰을 부분 업데이트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @Parameter(
                    name = "memberId",
                    description = "회원 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.QUERY
            )
            @RequestParam Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "FCM 토큰 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FCMTokenRequest.class))
            )
            @RequestBody FCMTokenRequest request) {
        fcmTokenService.updateFcmToken(memberId, request.getToken());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "FCM 토큰 삭제",
            description = "회원의 FCM 토큰을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/fcm-token")
    public ResponseEntity<Void> deleteFcmToken(
            @Parameter(
                    name = "memberId",
                    description = "회원 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.QUERY
            )
            @RequestParam Long memberId) {
        fcmTokenService.deleteFcmToken(memberId);
        return ResponseEntity.ok().build();
    }

    // Swagger에서 사용할 오류 응답 스키마 정의
    @Schema(name = "ErrorResponse", description = "오류 응답")
    private static class ErrorResponse {
        @Schema(description = "오류 메시지", example = "Member not found")
        private String message;

        @Schema(description = "오류 코드", example = "MEMBER_NOT_FOUND")
        private String code;
    }
}

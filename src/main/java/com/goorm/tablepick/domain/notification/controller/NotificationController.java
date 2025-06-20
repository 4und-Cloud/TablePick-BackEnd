package com.goorm.tablepick.domain.notification.controller;

import com.google.api.core.ApiFuture;
import com.goorm.tablepick.domain.notification.dto.request.FCMTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.FCMNotificationResponse;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.notification.service.FCMService;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import com.goorm.tablepick.global.exception.NotificationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;
    private final FCMService fcmService;
    private final FCMTokenService fcmTokenService;
    private final NotificationTypesRepository notificationTypesRepository;
    
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
        try {
            NotificationResponse response = notificationService.getNotificationStatus(id);
            return ResponseEntity.ok(response);
        } catch (NotificationException e) {
            // 알림이 없을 때 예외가 발생하면 404 반환
            return ResponseEntity.notFound().build();
        }
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
            summary = "FCM 토큰 NULL로 변경",
            description = "회원의 FCM 토큰을 NULL로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 NULL로 변경 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/fcm-token/remove")
    public ResponseEntity<Void> removeFcmToken(@RequestParam Long memberId) {
        fcmTokenService.updateFcmTokenToNull(memberId);
        return ResponseEntity.ok().build();
    }
    
    @Operation(
            summary = "FCM 토큰을 직접 넣어서 알림 전송",
            description = "지정된 FCM 토큰으로 푸시 알림을 전송합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 전송 성공",
                    content = @Content(schema = @Schema(implementation = FCMNotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (토큰이 null이거나 공백인 경우)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "FCM 전송 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/send-with-fcmToken")
    public ResponseEntity<FCMNotificationResponse> sendNotification(
            @Parameter(
                    name = "token",
                    description = "FCM 토큰",
                    required = true,
                    example = "dGhpcyBpcyBhIGZha2UgdG9rZW4",
                    in = ParameterIn.QUERY
            )
            @RequestParam String token,
            @Parameter(
                    name = "title",
                    description = "알림 제목",
                    required = true,
                    example = "테이블픽 알림",
                    in = ParameterIn.QUERY
            )
            @RequestParam String title,
            @Parameter(
                    name = "body",
                    description = "알림 내용",
                    required = true,
                    example = "새로운 예약이 있습니다.",
                    in = ParameterIn.QUERY
            )
            @RequestParam String body,
            @Parameter(
                    name = "data",
                    description = "추가 데이터 (JSON 형식, 선택사항)",
                    required = false,
                    example = "{\"reservationId\":\"123\",\"restaurantName\":\"맛있는 식당\"}",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) String data) {
        
        // 전송 시작 시간 기록
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();
        
        log.info("로고 알림 전송 요청 시작 - 시간: {}, 토큰: {}, 제목: {}, 내용: {}",
                startTime, token, title, body);
        
        try {
            // data 파라미터를 Map으로 변환
            java.util.Map<String, String> dataMap = new java.util.HashMap<>();
            if (data != null && !data.trim().isEmpty()) {
                try {
                    // 간단한 JSON 파싱
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    dataMap = objectMapper.readValue(data, java.util.Map.class);
                } catch (Exception e) {
                    log.warn("데이터 파라미터 파싱 실패, 빈 맵 사용: {}", e.getMessage());
                }
            }
            
            // 순수 FCM 전송 시간 측정 시작
            long fcmStartMillis = System.currentTimeMillis();
            
            // FCM 알림 전송
            String response = fcmService.sendMessageWithLogo(token, title, body, dataMap);
            long fcmDurationMillis = System.currentTimeMillis() - fcmStartMillis;
            log.info("순수 FCM 전송 소요시간: {}ms", fcmDurationMillis);
            
            long endMillis = System.currentTimeMillis();
            long durationMs = endMillis - startMillis;
            
            if (response != null) {
                log.info("로고 알림 전송 성공 - 응답: {}, 소요시간: {}ms", response, durationMs);
                return ResponseEntity.ok(FCMNotificationResponse.success(response, startTime, durationMs));
            } else {
                log.warn("로고 알림 전송 실패 - 토큰이 유효하지 않음, 소요시간: {}ms", durationMs);
                return ResponseEntity.badRequest()
                        .body(FCMNotificationResponse.failure("FCM 토큰이 유효하지 않습니다.", "INVALID_TOKEN", startTime,
                                durationMs));
                
            }
        } catch (NotificationException e) {
            long endMillis = System.currentTimeMillis();
            long durationMs = endMillis - startMillis;
            log.error("로고 알림 전송 중 오류 발생: {}, 소요시간: {}ms", e.getMessage(), durationMs);
            return ResponseEntity.internalServerError()
                    .body(FCMNotificationResponse.failure(e.getMessage(), e.getErrorCode(), startTime, durationMs));
        } catch (Exception e) {
            long endMillis = System.currentTimeMillis();
            long durationMs = endMillis - startMillis;
            log.error("로고 알림 전송 중 예상치 못한 오류 발생: {}, 소요시간: {}ms", e.getMessage(), durationMs);
            return ResponseEntity.internalServerError()
                    .body(FCMNotificationResponse.failure("알림 전송 중 오류가 발생했습니다.", "UNEXPECTED_ERROR", startTime,
                            durationMs));
        }
    }
    
    @Operation(
            summary = "비동기식 FCM 알림 전송",
            description = "지정된 FCM 토큰으로 비동기식으로 푸시 알림을 전송합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 전송 성공",
                    content = @Content(schema = @Schema(implementation = FCMNotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (토큰이 null이거나 공백인 경우)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "FCM 전송 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/send-async-with-fcmToken")
    public ResponseEntity<FCMNotificationResponse> sendAsyncNotification(
            @Parameter(
                    name = "token",
                    description = "FCM 토큰",
                    required = true,
                    example = "dGhpcyBpcyBhIGZha2UgdG9rZW4",
                    in = ParameterIn.QUERY
            )
            @RequestParam String token,
            @Parameter(
                    name = "title",
                    description = "알림 제목",
                    required = true,
                    example = "테이블픽 알림",
                    in = ParameterIn.QUERY
            )
            @RequestParam String title,
            @Parameter(
                    name = "body",
                    description = "알림 내용",
                    required = true,
                    example = "새로운 예약이 있습니다.",
                    in = ParameterIn.QUERY
            )
            @RequestParam String body,
            @Parameter(
                    name = "data",
                    description = "추가 데이터 (JSON 형식, 선택사항)",
                    required = false,
                    example = "{\"reservationId\":\"123\",\"restaurantName\":\"맛있는 식당\"}",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) String data) {
        
        // 전송 시작 시간 기록
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();
        
        log.info("로고 알림 전송 요청 시작 - 시간: {}, 토큰: {}, 제목: {}, 내용: {}",
                startTime, token, title, body);
        
        try {
            // data 파라미터를 Map으로 변환
            java.util.Map<String, String> dataMap = new java.util.HashMap<>();
            if (data != null && !data.trim().isEmpty()) {
                try {
                    // 간단한 JSON 파싱
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    dataMap = objectMapper.readValue(data, java.util.Map.class);
                } catch (Exception e) {
                    log.warn("데이터 파라미터 파싱 실패, 빈 맵 사용: {}", e.getMessage());
                }
            }
            
            // 순수 FCM 전송 시간 측정 시작
            long fcmStartMillis = System.currentTimeMillis();
            
            // FCM 알림 비동기 전송
            ApiFuture<String> responseFuture = fcmService.sendMessageAsync(token, title, body,
                    dataMap, false);
            String response;
            try {
                response = responseFuture.get(); // 비동기 결과 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                throw new NotificationException("FCM 비동기 전송 중 인터럽트 발생: " + e.getMessage(), "FCM_INTERRUPTED");
            } catch (ExecutionException e) {
                throw new NotificationException("FCM 비동기 전송 실패: " + e.getCause().getMessage(), "FCM_ASYNC_FAILED");
            }
            
            long fcmDurationMillis = System.currentTimeMillis() - fcmStartMillis;
            log.info("순수 FCM 전송 소요시간: {}ms", fcmDurationMillis);
            
            long endMillis = System.currentTimeMillis();
            long durationMs = endMillis - startMillis;
            
            if (response != null) {
                log.info("로고 알림 전송 성공 - 응답: {}, 소요시간: {}ms", response, durationMs);
                return ResponseEntity.ok(FCMNotificationResponse.success(response, startTime, durationMs));
            } else {
                log.warn("로고 알림 전송 실패 - 토큰이 유효하지 않음, 소요시간: {}ms", durationMs);
                return ResponseEntity.badRequest()
                        .body(FCMNotificationResponse.failure("FCM 토큰이 유효하지 않습니다.", "INVALID_TOKEN", startTime,
                                durationMs));
                
            }
        } catch (NotificationException e) {
            long endMillis = System.currentTimeMillis();
            long durationMs = endMillis - startMillis;
            log.error("로고 알림 전송 중 오류 발생: {}, 소요시간: {}ms", e.getMessage(), durationMs);
            return ResponseEntity.internalServerError()
                    .body(FCMNotificationResponse.failure(e.getMessage(), e.getErrorCode(), startTime, durationMs));
        } catch (Exception e) {
            long endMillis = System.currentTimeMillis();
            long durationMs = endMillis - startMillis;
            log.error("로고 알림 전송 중 예상치 못한 오류 발생: {}, 소요시간: {}ms", e.getMessage(), durationMs);
            return ResponseEntity.internalServerError()
                    .body(FCMNotificationResponse.failure("알림 전송 중 오류가 발생했습니다.", "UNEXPECTED_ERROR", startTime,
                            durationMs));
        }
    }
    
    
    @Operation(
            summary = "알림 타입 목록 조회",
            description = "모든 알림 타입 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 타입 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationTypes.class)))
            )
    })
    @GetMapping("/notification-types")
    public ResponseEntity<List<NotificationTypes>> getNotificationTypes() {
        log.info("알림 타입 목록 조회 요청");
        List<NotificationTypes> notificationTypes = notificationTypesRepository.findAll();
        log.info("알림 타입 목록 조회 완료 - 총 {} 개", notificationTypes.size());
        return ResponseEntity.ok(notificationTypes);
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

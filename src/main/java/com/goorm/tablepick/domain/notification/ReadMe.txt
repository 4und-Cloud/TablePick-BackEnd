1. FCM 관련
- FCMConfig.java (FCM 초기 설정)
- FCMService.java (FCM 알림 전송 로직)
- FCMToken.java (FCM 토큰 엔티티)

2. 알림 관련
- NotificationService.java (알림 비즈니스 로직)
- NotificationRepository.java (알림 데이터 저장/조회)
- Notification.java (알림 엔티티)

3. 스케줄러 관련
- NotificationScheduler.java (예약시간 기반 알림 스케줄러)
- SchedulerConfig.java (스케줄러 설정)

4. DTO
- NotificationRequestDTO.java (알림 요청 데이터)
- NotificationResponseDTO.java (알림 응답 데이터)

5. Controller
- NotificationController.java (알림 관련 API 엔드포인트)

6. Exception
- FCMException.java (FCM 관련 예외처리)

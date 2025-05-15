package com.goorm.tablepick.domain.global.config;

import com.goorm.tablepick.domain.notification.constant.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.global.config.NotificationTypesInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTypesInitializerTest {

    @Mock
    private NotificationTypesRepository notificationTypesRepository;

    @InjectMocks
    private NotificationTypesInitializer notificationTypesInitializer;

    @Test
    void run_ShouldInitializeNotificationTypes_WhenRepositoryIsEmpty() {
        // Given
        when(notificationTypesRepository.count()).thenReturn(0L);

        // When
        notificationTypesInitializer.run(null);

        // Then
        verify(notificationTypesRepository, times(1)).count();
        verify(notificationTypesRepository, times(NotificationTypes.values().length)).save(any());
    }

    @Test
    void run_ShouldNotInitializeNotificationTypes_WhenRepositoryIsNotEmpty() {
        // Given
        when(notificationTypesRepository.count()).thenReturn(3L);

        // When
        notificationTypesInitializer.run(null);

        // Then
        verify(notificationTypesRepository, times(1)).count();
        verify(notificationTypesRepository, never()).save(any());
    }
}

package com.clubloyalty.server.service.notification;

import com.clubloyalty.server.dto.NotificationDtos.NotificationDto;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> fetchForMember(String username, boolean markRead);

    void createForMember(Long memberId, String title, String message, String type, boolean auto);

    void createForAll(String title, String message, String type, boolean auto);
}

package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.NotificationDtos.NotificationDto;
import com.clubloyalty.server.service.notification.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<NotificationDto> list(Principal principal) {
        return notifications.fetchForMember(principal.getName(), true);
    }
}

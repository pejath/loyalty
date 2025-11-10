package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.Member;
import com.clubloyalty.server.domain.Notification;
import com.clubloyalty.server.dto.NotificationDtos.NotificationDto;
import com.clubloyalty.server.repo.MemberRepository;
import com.clubloyalty.server.repo.NotificationRepository;
import com.clubloyalty.server.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notifications;
    private final MemberRepository members;

    public NotificationServiceImpl(NotificationRepository notifications, MemberRepository members) {
        this.notifications = notifications;
        this.members = members;
    }

    public List<NotificationDto> fetchForMember(String username, boolean markRead) {
        Member member = members.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        List<Notification> items = notifications.findByMemberIdOrderByCreatedAtDesc(member.getId());
        if (markRead) {
            var unread = items.stream().filter(n -> n.getReadAt() == null).collect(Collectors.toList());
            unread.forEach(Notification::markRead);
            notifications.saveAll(unread);
        }
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    public void createForMember(Long memberId, String title, String message, String type, boolean auto) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        notifications.save(new Notification(member, title, message, type, auto));
    }

    public void createForAll(String title, String message, String type, boolean auto) {
        List<Member> allMembers = members.findAll();
        for (Member member : allMembers) {
            notifications.save(new Notification(member, title, message, type, auto));
        }
    }

    private NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.id = notification.getId();
        dto.title = notification.getTitle();
        dto.message = notification.getMessage();
        dto.type = notification.getType();
        dto.createdAt = notification.getCreatedAt();
        dto.read = notification.getReadAt() != null;
        return dto;
    }
}

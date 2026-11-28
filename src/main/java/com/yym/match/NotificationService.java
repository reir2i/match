package com.yym.match;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;
    private final MailService mailService;

    public NotificationService(NotificationRepository notificationRepository,
                               ProfileRepository profileRepository,
                               MailService mailService) {
        this.notificationRepository = notificationRepository;
        this.profileRepository = profileRepository;
        this.mailService = mailService;
    }

    @Transactional
    public void push(Member receiver, Notification.Type type, String message, String link) {
        notificationRepository.save(new Notification(receiver, type, message, link));

        profileRepository.findByMemberId(receiver.getId())
                .map(Profile::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .ifPresent(email -> mailService.sendNotice(email, type));
    }
}
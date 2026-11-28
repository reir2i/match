package com.yym.match;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAdvice {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    public GlobalModelAdvice(MemberRepository memberRepository,
                             NotificationRepository notificationRepository) {
        this.memberRepository = memberRepository;
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        if (principal == null) {
            return 0L;
        }
        return memberRepository.findByUsername(principal.getName())
                .map(m -> notificationRepository.countByReceiverIdAndCheckedFalse(m.getId()))
                .orElse(0L);
    }
}
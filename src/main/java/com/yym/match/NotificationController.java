package com.yym.match;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class NotificationController {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    public NotificationController(MemberRepository memberRepository,
                                  NotificationRepository notificationRepository) {
        this.memberRepository = memberRepository;
        this.notificationRepository = notificationRepository;
    }

    private Member currentMember(Principal principal) {
        return memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("/notifications")
    public String list(Principal principal, Model model) {
        Member me = currentMember(principal);
        model.addAttribute("notifications",
                notificationRepository.findTop50ByReceiverIdOrderByCreatedAtDesc(me.getId()));
        return "notifications";
    }

    @GetMapping("/notifications/{id}/go")
    public String go(@PathVariable("id") Long id, Principal principal) {
        Member me = currentMember(principal);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!notification.getReceiver().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!notification.isChecked()) {
            notification.markChecked();
            notificationRepository.save(notification);
        }

        String link = notification.getLink();
        return "redirect:" + ((link != null && link.startsWith("/")) ? link : "/notifications");
    }

    @PostMapping("/notifications/read-all")
    public String readAll(Principal principal) {
        Member me = currentMember(principal);
        notificationRepository.markAllChecked(me.getId());
        return "redirect:/notifications";
    }
}
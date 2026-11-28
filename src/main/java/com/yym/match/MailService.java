package com.yym.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> senderProvider;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${app.base-url}")
    private String baseUrl;

    public MailService(ObjectProvider<JavaMailSender> senderProvider) {
        this.senderProvider = senderProvider;
    }

    @Async
    public void sendNotice(String to, Notification.Type type) {
        JavaMailSender sender = senderProvider.getIfAvailable();
        if (sender == null || username == null || username.isBlank()) {
            log.info("메일 설정이 없어 발송을 건너뜁니다.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username);
            message.setTo(to);
            message.setSubject("[팀 프로젝트 매칭] 새 알림이 있습니다");
            message.setText(type.getMailLine() + "\n"
                    + "자세한 내용은 사이트의 알림칸에서 확인하세요.\n"
                    + baseUrl + "/notifications");
            sender.send(message);
        } catch (Exception e) {
            log.warn("메일 발송 실패: {}", e.getMessage());
        }
    }
}
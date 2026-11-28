package com.yym.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum Type {
        APPLY("지원", "새 지원이 들어왔습니다."),
        ACCEPT("수락", "지원이 수락되었습니다."),
        REJECT("거절", "지원이 거절되었습니다."),
        MESSAGE("쪽지", "새 쪽지가 도착했습니다.");

        private final String label;
        private final String mailLine;

        Type(String label, String mailLine) {
            this.label = label;
            this.mailLine = mailLine;
        }

        public String getLabel() { return label; }
        public String getMailLine() { return mailLine; }
    }

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(length = 200)
    private String link;

    @Column(nullable = false)
    private boolean checked = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Notification() {
    }

    public Notification(Member receiver, Type type, String message, String link) {
        this.receiver = receiver;
        this.type = type;
        this.message = message;
        this.link = link;
    }

    public void markChecked() {
        this.checked = true;
    }

    public String getTypeLabel() {
        return type.getLabel();
    }

    public String getCreatedText() {
        return createdAt.format(FORMAT);
    }

    public Long getId() { return id; }
    public Member getReceiver() { return receiver; }
    public Type getType() { return type; }
    public String getMessage() { return message; }
    public String getLink() { return link; }
    public boolean isChecked() { return checked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
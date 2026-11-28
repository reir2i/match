package com.yym.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "direct_messages")
public class DirectMessage {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean checked = false;

    @Column(nullable = false)
    private boolean senderDeleted = false;

    @Column(nullable = false)
    private boolean receiverDeleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected DirectMessage() {
    }

    public DirectMessage(Member sender, Member receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public void markChecked() {
        this.checked = true;
    }

    public void deleteBySender() {
        this.senderDeleted = true;
    }

    public void deleteByReceiver() {
        this.receiverDeleted = true;
    }

    public String getPreview() {
        String oneLine = content.replace("\r", " ").replace("\n", " ");
        return oneLine.length() > 40 ? oneLine.substring(0, 40) + "…" : oneLine;
    }

    public String getCreatedText() {
        return createdAt.format(FORMAT);
    }

    public Long getId() { return id; }
    public Member getSender() { return sender; }
    public Member getReceiver() { return receiver; }
    public String getContent() { return content; }
    public boolean isChecked() { return checked; }
    public boolean isSenderDeleted() { return senderDeleted; }
    public boolean isReceiverDeleted() { return receiverDeleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
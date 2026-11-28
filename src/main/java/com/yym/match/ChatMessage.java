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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ChatMessage() {
    }

    public ChatMessage(Post post, Member sender, String content) {
        this.post = post;
        this.sender = sender;
        this.content = content;
    }

    public static ChatMessage system(Post post, String content) {
        return new ChatMessage(post, null, content);
    }

    public boolean isSystem() {
        return sender == null;
    }

    public String getTimeText() {
        return createdAt.format(FORMAT);
    }

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public Member getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
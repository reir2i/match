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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "applicant_id"}))
public class Application {

    public enum Status { PENDING, ACCEPTED, REJECTED }

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Member applicant;

    @Column(length = 300)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Application() {
    }

    public Application(Post post, Member applicant, String message) {
        this.post = post;
        this.applicant = applicant;
        this.message = message;
    }

    public void accept() {
        this.status = Status.ACCEPTED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public String getStatusLabel() {
        switch (status) {
            case ACCEPTED: return "수락";
            case REJECTED: return "거절";
            default: return "대기중";
        }
    }

    public String getStatusClass() {
        return "status-" + status.name().toLowerCase();
    }

    public String getCreatedText() {
        return createdAt.format(FORMAT);
    }

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public Member getApplicant() { return applicant; }
    public String getMessage() { return message; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
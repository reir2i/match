package com.yym.match;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    public enum Status { OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.OPEN;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Post() {
    }

    public Post(Member author, String title, String content, int capacity,
                LocalDate deadline, Set<Tag> tags) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.capacity = capacity;
        this.deadline = deadline;
        this.tags.addAll(tags);
    }

    public void update(String title, String content, int capacity,
                       LocalDate deadline, Set<Tag> tags) {
        this.title = title;
        this.content = content;
        this.capacity = capacity;
        this.deadline = deadline;
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public void close() {
        this.status = Status.CLOSED;
    }

    public boolean isRecruiting() {
        return status == Status.OPEN && !deadline.isBefore(LocalDate.now());
    }

    public String getDdayLabel() {
        if (!isRecruiting()) {
            return "마감";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        return days == 0 ? "D-Day" : "D-" + days;
    }

    public String getCreatedDate() {
        return createdAt.toLocalDate().toString();
    }

    public Long getId() { return id; }
    public Member getAuthor() { return author; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getCapacity() { return capacity; }
    public LocalDate getDeadline() { return deadline; }
    public Status getStatus() { return status; }
    public Set<Tag> getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
package com.yym.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(length = 50)
    private String department;

    private Integer grade;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(length = 200)
    private String githubUrl;

    @Column(length = 100)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "profile_tags",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    protected Profile() {
    }

    public Profile(Member member) {
        this.member = member;
    }

    public void update(String department, Integer grade, String intro,
                       String githubUrl, String email, Set<Tag> tags) {
        this.department = department;
        this.grade = grade;
        this.intro = intro;
        this.githubUrl = githubUrl;
        this.email = email;
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public String getDepartment() { return department; }
    public Integer getGrade() { return grade; }
    public String getIntro() { return intro; }
    public String getGithubUrl() { return githubUrl; }
    public String getEmail() { return email; }
    public Set<Tag> getTags() { return tags; }
}
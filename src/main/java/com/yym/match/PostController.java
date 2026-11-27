package com.yym.match;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PostController {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final ApplicationRepository applicationRepository;

    public PostController(MemberRepository memberRepository,
                          PostRepository postRepository,
                          CommentRepository commentRepository,
                          TagRepository tagRepository,
                          ApplicationRepository applicationRepository) {
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
        this.applicationRepository = applicationRepository;
    }

    private Member currentMember(Principal principal) {
        return memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void checkAuthor(Post post, Member me) {
        if (!post.getAuthor().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Set<Tag> loadTags(List<Long> tagIds) {
        return tagIds == null ? new HashSet<>() : new HashSet<>(tagRepository.findAllById(tagIds));
    }

    private String validate(String title, String content, int capacity, LocalDate deadline) {
        if (title.isEmpty() || title.length() > 100) {
            return "제목은 1~100자로 입력하세요.";
        }
        if (content.isEmpty() || content.length() > 2000) {
            return "내용은 1~2000자로 입력하세요.";
        }
        if (capacity < 1 || capacity > 20) {
            return "모집 인원은 1~20명으로 입력하세요.";
        }
        if (deadline.isBefore(LocalDate.now())) {
            return "마감일은 오늘 이후로 선택하세요.";
        }
        return null;
    }

    private void fillForm(Model model, Post post, Set<Long> selectedTagIds,
                          String formAction, String heading, String error) {
        model.addAttribute("post", post);
        model.addAttribute("allTags", tagRepository.findAll(Sort.by("id")));
        model.addAttribute("selectedTagIds", selectedTagIds);
        model.addAttribute("formAction", formAction);
        model.addAttribute("heading", heading);
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("error", error);
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        fillForm(model, null, new HashSet<>(), "/posts", "모집글 작성", null);
        return "post-form";
    }

    @PostMapping("/posts")
    public String create(Principal principal,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content,
                         @RequestParam("capacity") int capacity,
                         @RequestParam("deadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
                         @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                         Model model) {
        Member me = currentMember(principal);
        title = title.trim();
        content = content.trim();
        Set<Tag> tags = loadTags(tagIds);

        String error = validate(title, content, capacity, deadline);
        if (error != null) {
            Post temp = new Post(me, title, content, capacity, deadline, tags);
            fillForm(model, temp, tags.stream().map(Tag::getId).collect(Collectors.toSet()),
                    "/posts", "모집글 작성", error);
            return "post-form";
        }

        Post saved = postRepository.save(new Post(me, title, content, capacity, deadline, tags));
        return "redirect:/posts/" + saved.getId();
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable("id") Long id, Principal principal, Model model) {
        Member me = currentMember(principal);
        Post post = findPost(id);
        boolean isAuthor = post.getAuthor().getId().equals(me.getId());

        model.addAttribute("post", post);
        model.addAttribute("comments", commentRepository.findByPostIdOrderByCreatedAtAsc(id));
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("meId", me.getId());

        model.addAttribute("applications",
                isAuthor ? applicationRepository.findByPostIdOrderByCreatedAtAsc(id) : List.of());
        model.addAttribute("myApplication",
                applicationRepository.findByPostIdAndApplicantId(id, me.getId()).orElse(null));
        model.addAttribute("acceptedCount",
                applicationRepository.countByPostIdAndStatus(id, Application.Status.ACCEPTED));
        return "post-detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Principal principal, Model model) {
        Member me = currentMember(principal);
        Post post = findPost(id);
        checkAuthor(post, me);

        fillForm(model, post, post.getTags().stream().map(Tag::getId).collect(Collectors.toSet()),
                "/posts/" + id + "/edit", "모집글 수정", null);
        return "post-form";
    }

    @PostMapping("/posts/{id}/edit")
    public String edit(@PathVariable("id") Long id, Principal principal,
                       @RequestParam("title") String title,
                       @RequestParam("content") String content,
                       @RequestParam("capacity") int capacity,
                       @RequestParam("deadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
                       @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                       Model model) {
        Member me = currentMember(principal);
        Post post = findPost(id);
        checkAuthor(post, me);

        title = title.trim();
        content = content.trim();
        Set<Tag> tags = loadTags(tagIds);

        String error = validate(title, content, capacity, deadline);
        if (error != null) {
            Post temp = new Post(me, title, content, capacity, deadline, tags);
            fillForm(model, temp, tags.stream().map(Tag::getId).collect(Collectors.toSet()),
                    "/posts/" + id + "/edit", "모집글 수정", error);
            return "post-form";
        }

        post.update(title, content, capacity, deadline, tags);
        postRepository.save(post);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable("id") Long id, Principal principal) {
        Member me = currentMember(principal);
        Post post = findPost(id);
        checkAuthor(post, me);

        applicationRepository.deleteByPostId(id);
        postRepository.delete(post);
        return "redirect:/";
    }

    @PostMapping("/posts/{id}/comments")
    public String addComment(@PathVariable("id") Long id, Principal principal,
                             @RequestParam("content") String content) {
        Member me = currentMember(principal);
        Post post = findPost(id);

        content = content.trim();
        if (!content.isEmpty() && content.length() <= 500) {
            commentRepository.save(new Comment(post, me, content));
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") Long id,
                                @PathVariable("commentId") Long commentId,
                                Principal principal) {
        Member me = currentMember(principal);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!comment.getAuthor().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        commentRepository.delete(comment);
        return "redirect:/posts/" + id;
    }
}
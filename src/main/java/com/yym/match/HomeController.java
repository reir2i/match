package com.yym.match;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;

@Controller
public class HomeController {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public HomeController(MemberRepository memberRepository,
                          ProfileRepository profileRepository,
                          PostRepository postRepository,
                          TagRepository tagRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", defaultValue = "") String keyword,
                       @RequestParam(value = "tag", defaultValue = "0") long tagId,
                       @RequestParam(value = "open", defaultValue = "false") boolean onlyOpen,
                       Principal principal, Model model) {

        memberRepository.findByUsername(principal.getName()).ifPresent(m -> {
            model.addAttribute("nickname", m.getNickname());
            model.addAttribute("hasProfile", profileRepository.findByMemberId(m.getId()).isPresent());
        });

        keyword = keyword.trim();
        if (keyword.length() > 50) {
            keyword = keyword.substring(0, 50);
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), 10);
        Page<Post> posts = postRepository.search(
                keyword, Math.max(tagId, 0L), onlyOpen, Post.Status.OPEN, LocalDate.now(), pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("tagId", Math.max(tagId, 0L));
        model.addAttribute("onlyOpen", onlyOpen);
        model.addAttribute("allTags", tagRepository.findAll(Sort.by("id")));
        model.addAttribute("searching", !keyword.isEmpty() || tagId > 0 || onlyOpen);
        return "home";
    }
}
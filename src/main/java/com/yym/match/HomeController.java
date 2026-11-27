package com.yym.match;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public HomeController(MemberRepository memberRepository,
                          ProfileRepository profileRepository,
                          PostRepository postRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "open", defaultValue = "false") boolean onlyOpen,
                       Principal principal, Model model) {

        memberRepository.findByUsername(principal.getName()).ifPresent(m -> {
            model.addAttribute("nickname", m.getNickname());
            model.addAttribute("hasProfile", profileRepository.findByMemberId(m.getId()).isPresent());
        });

        Pageable pageable = PageRequest.of(Math.max(page, 0), 10);
        LocalDate today = LocalDate.now();

        Page<Post> posts = onlyOpen
                ? postRepository.findRecruiting(Post.Status.OPEN, today, pageable)
                : postRepository.findAllRecruitingFirst(Post.Status.OPEN, today, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("onlyOpen", onlyOpen);
        return "home";
    }
}
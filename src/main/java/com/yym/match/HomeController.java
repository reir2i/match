package com.yym.match;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

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
                       Principal principal, Model model) {

        memberRepository.findByUsername(principal.getName()).ifPresent(m -> {
            model.addAttribute("nickname", m.getNickname());
            model.addAttribute("hasProfile", profileRepository.findByMemberId(m.getId()).isPresent());
        });

        Page<Post> posts = postRepository.findAll(
                PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("posts", posts);
        return "home";
    }
}
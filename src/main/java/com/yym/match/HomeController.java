package com.yym.match;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

    public HomeController(MemberRepository memberRepository, ProfileRepository profileRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        memberRepository.findByUsername(principal.getName()).ifPresent(m -> {
            model.addAttribute("nickname", m.getNickname());
            model.addAttribute("hasProfile", profileRepository.findByMemberId(m.getId()).isPresent());
        });
        return "home";
    }
}
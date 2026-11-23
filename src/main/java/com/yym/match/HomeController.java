package com.yym.match;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final MemberRepository memberRepository;

    public HomeController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        if (principal != null) {
            memberRepository.findByUsername(principal.getName())
                    .ifPresent(m -> model.addAttribute("nickname", m.getNickname()));
        }
        return "home";
    }
}
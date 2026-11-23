package com.yym.match;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("nickname") String nickname,
                         Model model) {

        if (username.isBlank() || nickname.isBlank() || password.length() < 4) {
            model.addAttribute("error", "학번과 이름을 입력하고, 비밀번호는 4자 이상으로 입력하세요.");
            return "signup";
        }
        if (memberRepository.existsByUsername(username.trim())) {
            model.addAttribute("error", "이미 가입된 학번입니다.");
            return "signup";
        }

        memberRepository.save(new Member(username.trim(), passwordEncoder.encode(password), nickname.trim()));
        return "redirect:/login?signup";
    }
}
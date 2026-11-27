package com.yym.match;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ApplicationController {

    private final MemberRepository memberRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;

    public ApplicationController(MemberRepository memberRepository,
                                 ApplicationRepository applicationRepository,
                                 ApplicationService applicationService) {
        this.memberRepository = memberRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
    }

    private Member currentMember(Principal principal) {
        return memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/posts/{postId}/apply")
    public String apply(@PathVariable("postId") Long postId,
                        @RequestParam(value = "message", defaultValue = "") String message,
                        Principal principal, RedirectAttributes redirect) {
        Member me = currentMember(principal);
        try {
            applicationService.apply(postId, me, message);
            redirect.addFlashAttribute("msg", "지원이 완료되었습니다.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/applications/{id}/accept")
    public String accept(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirect) {
        Member me = currentMember(principal);
        Long postId = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getPost().getId();
        try {
            applicationService.accept(id, me);
            redirect.addFlashAttribute("msg", "지원을 수락했습니다.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/applications/{id}/reject")
    public String reject(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirect) {
        Member me = currentMember(principal);
        Long postId = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getPost().getId();
        try {
            applicationService.reject(id, me);
            redirect.addFlashAttribute("msg", "지원을 거절했습니다.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/applications/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirect) {
        Member me = currentMember(principal);
        try {
            applicationService.cancel(id, me);
            redirect.addFlashAttribute("msg", "지원을 취소했습니다.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/applications/mine";
    }

    @GetMapping("/applications/mine")
    public String mine(Principal principal, Model model) {
        Member me = currentMember(principal);
        model.addAttribute("applications",
                applicationRepository.findByApplicantIdOrderByCreatedAtDesc(me.getId()));
        return "my-applications";
    }
}
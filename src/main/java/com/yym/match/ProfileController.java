package com.yym.match;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final TagRepository tagRepository;

    public ProfileController(MemberRepository memberRepository,
                             ProfileRepository profileRepository,
                             TagRepository tagRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
        this.tagRepository = tagRepository;
    }

    private Member currentMember(Principal principal) {
        return memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("/profile")
    public String myProfile(Principal principal, Model model) {
        Member me = currentMember(principal);
        Optional<Profile> profile = profileRepository.findByMemberId(me.getId());
        if (profile.isEmpty()) {
            return "redirect:/profile/edit";
        }
        model.addAttribute("member", me);
        model.addAttribute("profile", profile.get());
        model.addAttribute("mine", true);
        return "profile";
    }

    @GetMapping("/profile/{memberId}")
    public String viewProfile(@PathVariable("memberId") Long memberId,
                              Principal principal, Model model) {
        Member me = currentMember(principal);
        Member target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("member", target);
        model.addAttribute("profile", profileRepository.findByMemberId(memberId).orElse(null));
        model.addAttribute("mine", me.getId().equals(target.getId()));
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editForm(Principal principal, Model model) {
        Member me = currentMember(principal);
        Profile profile = profileRepository.findByMemberId(me.getId()).orElse(null);

        Set<Long> selected = profile == null
                ? new HashSet<>()
                : profile.getTags().stream().map(Tag::getId).collect(Collectors.toSet());

        model.addAttribute("profile", profile);
        model.addAttribute("allTags", tagRepository.findAll(Sort.by("id")));
        model.addAttribute("selectedTagIds", selected);
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String save(Principal principal,
                       @RequestParam("department") String department,
                       @RequestParam("grade") Integer grade,
                       @RequestParam("intro") String intro,
                       @RequestParam("githubUrl") String githubUrl,
                       @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                       Model model) {

        Member me = currentMember(principal);

        department = department.trim();
        intro = intro.trim();
        githubUrl = githubUrl.trim();

        Set<Tag> tags = tagIds == null
                ? new HashSet<>()
                : new HashSet<>(tagRepository.findAllById(tagIds));

        String error = null;
        if (department.isEmpty()) {
            error = "학과를 입력하세요.";
        } else if (grade < 1 || grade > 4) {
            error = "학년을 선택하세요.";
        } else if (intro.length() > 500) {
            error = "자기소개는 500자 이하로 입력하세요.";
        } else if (!githubUrl.isEmpty()
                && !(githubUrl.startsWith("https://") || githubUrl.startsWith("http://"))) {
            error = "GitHub 링크는 https:// 로 시작해야 합니다.";
        }

        if (error != null) {
            Profile temp = new Profile(me);
            temp.update(department, grade, intro, githubUrl, "", tags);
            model.addAttribute("error", error);
            model.addAttribute("profile", temp);
            model.addAttribute("allTags", tagRepository.findAll(Sort.by("id")));
            model.addAttribute("selectedTagIds",
                    tags.stream().map(Tag::getId).collect(Collectors.toSet()));
            return "profile-edit";
        }

        Profile profile = profileRepository.findByMemberId(me.getId())
                .orElseGet(() -> new Profile(me));
        profile.update(department, grade, intro, githubUrl, "", tags);
        profileRepository.save(profile);

        return "redirect:/profile";
    }
}
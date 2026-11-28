package com.yym.match;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    public record ChatLine(Long id, String sender, boolean mine, boolean system,
                           String content, String time) {
    }

    private static final String WELCOME =
            "모집이 마감되어 채팅방이 열렸습니다. 카카오톡 오픈채팅 링크나 연락처를 공유해 보세요.";

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final ApplicationRepository applicationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatController(MemberRepository memberRepository,
                          PostRepository postRepository,
                          ApplicationRepository applicationRepository,
                          ChatMessageRepository chatMessageRepository) {
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.applicationRepository = applicationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    private Member currentMember(Principal principal) {
        return memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Post requireRoom(Long postId, Member me) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean closed = post.getStatus() == Post.Status.CLOSED;
        boolean participant = post.getAuthor().getId().equals(me.getId())
                || applicationRepository.findByPostIdAndApplicantId(postId, me.getId())
                .map(a -> a.getStatus() == Application.Status.ACCEPTED)
                .orElse(false);

        if (!closed || !participant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return post;
    }

    private ChatLine toLine(ChatMessage m, Member me) {
        boolean system = m.isSystem();
        return new ChatLine(
                m.getId(),
                system ? "안내" : m.getSender().getNickname(),
                !system && m.getSender().getId().equals(me.getId()),
                system,
                m.getContent(),
                m.getTimeText());
    }

    @GetMapping("/posts/{id}/chat")
    public String room(@PathVariable("id") Long id, Principal principal, Model model) {
        Member me = currentMember(principal);
        Post post = requireRoom(id, me);

        if (chatMessageRepository.countByPostId(id) == 0) {
            chatMessageRepository.save(ChatMessage.system(post, WELCOME));
        }

        List<ChatMessage> messages = chatMessageRepository.findByPostIdOrderByIdAsc(id);
        long lastId = messages.isEmpty() ? 0L : messages.get(messages.size() - 1).getId();

        List<Member> members = new ArrayList<>();
        members.add(post.getAuthor());
        applicationRepository.findByPostIdOrderByCreatedAtAsc(id).stream()
                .filter(a -> a.getStatus() == Application.Status.ACCEPTED)
                .forEach(a -> members.add(a.getApplicant()));

        model.addAttribute("post", post);
        model.addAttribute("messages", messages);
        model.addAttribute("lastId", lastId);
        model.addAttribute("meId", me.getId());
        model.addAttribute("members", members);
        return "chat";
    }

    @GetMapping("/posts/{id}/chat/messages")
    @ResponseBody
    public List<ChatLine> poll(@PathVariable("id") Long id,
                               @RequestParam(value = "after", defaultValue = "0") long after,
                               Principal principal) {
        Member me = currentMember(principal);
        requireRoom(id, me);

        return chatMessageRepository.findByPostIdAndIdGreaterThanOrderByIdAsc(id, after).stream()
                .map(m -> toLine(m, me))
                .collect(Collectors.toList());
    }

    @PostMapping("/posts/{id}/chat")
    public String send(@PathVariable("id") Long id,
                       @RequestParam("content") String content,
                       Principal principal) {
        Member me = currentMember(principal);
        Post post = requireRoom(id, me);

        content = content.trim();
        if (!content.isEmpty() && content.length() <= 500) {
            chatMessageRepository.save(new ChatMessage(post, me, content));
        }
        return "redirect:/posts/" + id + "/chat";
    }
}
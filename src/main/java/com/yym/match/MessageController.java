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
public class MessageController {

    private final MemberRepository memberRepository;
    private final DirectMessageRepository messageRepository;
    private final NotificationService notificationService;

    public MessageController(MemberRepository memberRepository,
                             DirectMessageRepository messageRepository,
                             NotificationService notificationService) {
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
    }

    private Member currentMember(Principal principal) {
        return memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private DirectMessage findMessage(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/messages")
    public String inbox(Principal principal, Model model) {
        Member me = currentMember(principal);
        model.addAttribute("box", "inbox");
        model.addAttribute("messages",
                messageRepository.findTop100ByReceiverIdAndReceiverDeletedFalseOrderByCreatedAtDesc(me.getId()));
        return "messages";
    }

    @GetMapping("/messages/sent")
    public String sent(Principal principal, Model model) {
        Member me = currentMember(principal);
        model.addAttribute("box", "sent");
        model.addAttribute("messages",
                messageRepository.findTop100BySenderIdAndSenderDeletedFalseOrderByCreatedAtDesc(me.getId()));
        return "messages";
    }

    @GetMapping("/messages/new")
    public String form(@RequestParam(value = "to", required = false) Long to, Model model) {
        Member target = to == null ? null : memberRepository.findById(to).orElse(null);
        model.addAttribute("receiverUsername", target == null ? "" : target.getUsername());
        model.addAttribute("receiverName", target == null ? null : target.getNickname());
        model.addAttribute("content", "");
        return "message-form";
    }

    @PostMapping("/messages")
    public String send(Principal principal,
                       @RequestParam("receiverUsername") String receiverUsername,
                       @RequestParam("content") String content,
                       Model model, RedirectAttributes redirect) {
        Member me = currentMember(principal);
        receiverUsername = receiverUsername.trim();
        content = content.trim();

        Member receiver = receiverUsername.isEmpty()
                ? null : memberRepository.findByUsername(receiverUsername).orElse(null);

        String error = null;
        if (receiverUsername.isEmpty()) {
            error = "받는 사람 학번을 입력하세요.";
        } else if (receiver == null) {
            error = "해당 학번의 사용자를 찾을 수 없습니다.";
        } else if (receiver.getId().equals(me.getId())) {
            error = "본인에게는 쪽지를 보낼 수 없습니다.";
        } else if (content.isEmpty() || content.length() > 1000) {
            error = "내용은 1~1000자로 입력하세요.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("receiverUsername", receiverUsername);
            model.addAttribute("receiverName", receiver == null ? null : receiver.getNickname());
            model.addAttribute("content", content);
            return "message-form";
        }

        DirectMessage saved = messageRepository.save(new DirectMessage(me, receiver, content));

        notificationService.push(receiver, Notification.Type.MESSAGE,
                me.getNickname() + "님이 쪽지를 보냈습니다.",
                "/messages/" + saved.getId());

        redirect.addFlashAttribute("msg", "쪽지를 보냈습니다.");
        return "redirect:/messages/sent";
    }

    @GetMapping("/messages/{id}")
    public String detail(@PathVariable("id") Long id, Principal principal, Model model) {
        Member me = currentMember(principal);
        DirectMessage dm = findMessage(id);

        boolean isReceiver = dm.getReceiver().getId().equals(me.getId());
        boolean isSender = dm.getSender().getId().equals(me.getId());

        if (isReceiver) {
            if (dm.isReceiverDeleted()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            if (!dm.isChecked()) {
                dm.markChecked();
                messageRepository.save(dm);
            }
        } else if (isSender) {
            if (dm.isSenderDeleted()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("dm", dm);
        model.addAttribute("isReceiver", isReceiver);
        return "message-detail";
    }

    @PostMapping("/messages/{id}/delete")
    public String delete(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirect) {
        Member me = currentMember(principal);
        DirectMessage dm = findMessage(id);

        boolean isReceiver = dm.getReceiver().getId().equals(me.getId());
        boolean isSender = dm.getSender().getId().equals(me.getId());

        if (isReceiver) {
            dm.deleteByReceiver();
        } else if (isSender) {
            dm.deleteBySender();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        messageRepository.save(dm);

        redirect.addFlashAttribute("msg", "쪽지를 삭제했습니다.");
        return isReceiver ? "redirect:/messages" : "redirect:/messages/sent";
    }
}
package com.yym.match;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              PostRepository postRepository,
                              ProfileRepository profileRepository) {
        this.applicationRepository = applicationRepository;
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
    }

    private Application findApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void checkPostAuthor(Application application, Member me) {
        if (!application.getPost().getAuthor().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @Transactional
    public void apply(Long postId, Member me, String message) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (post.getAuthor().getId().equals(me.getId())) {
            throw new IllegalStateException("본인 모집글에는 지원할 수 없습니다.");
        }
        if (!post.isRecruiting()) {
            throw new IllegalStateException("마감된 모집글입니다.");
        }
        if (applicationRepository.findByPostIdAndApplicantId(postId, me.getId()).isPresent()) {
            throw new IllegalStateException("이미 지원한 모집글입니다.");
        }
        if (profileRepository.findByMemberId(me.getId()).isEmpty()) {
            throw new IllegalStateException("지원하려면 먼저 프로필을 작성하세요.");
        }

        message = message == null ? "" : message.trim();
        if (message.length() > 300) {
            throw new IllegalStateException("지원 메시지는 300자 이하로 입력하세요.");
        }

        applicationRepository.save(new Application(post, me, message));
    }

    @Transactional
    public Long accept(Long applicationId, Member me) {
        Application application = findApplication(applicationId);
        checkPostAuthor(application, me);
        Post post = application.getPost();

        if (!application.isPending()) {
            throw new IllegalStateException("이미 처리된 지원입니다.");
        }

        long accepted = applicationRepository.countByPostIdAndStatus(post.getId(), Application.Status.ACCEPTED);
        if (accepted >= post.getCapacity()) {
            throw new IllegalStateException("모집 인원이 이미 찼습니다.");
        }

        application.accept();
        if (accepted + 1 >= post.getCapacity()) {
            post.close();
        }
        return post.getId();
    }

    @Transactional
    public Long reject(Long applicationId, Member me) {
        Application application = findApplication(applicationId);
        checkPostAuthor(application, me);

        if (!application.isPending()) {
            throw new IllegalStateException("이미 처리된 지원입니다.");
        }

        application.reject();
        return application.getPost().getId();
    }

    @Transactional
    public void cancel(Long applicationId, Member me) {
        Application application = findApplication(applicationId);

        if (!application.getApplicant().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!application.isPending()) {
            throw new IllegalStateException("대기 중인 지원만 취소할 수 있습니다.");
        }

        applicationRepository.delete(application);
    }
}
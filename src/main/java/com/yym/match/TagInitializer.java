package com.yym.match;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagInitializer implements CommandLineRunner {

    private static final List<String> DEFAULT_TAGS = List.of(
            "Java", "Spring", "Python", "JavaScript", "TypeScript",
            "React", "Vue", "Node.js", "C/C++", "Kotlin",
            "Swift", "Flutter", "MySQL", "AWS", "Docker",
            "Git", "Figma", "기획", "디자인", "발표"
    );

    private final TagRepository tagRepository;

    public TagInitializer(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(String... args) {
        for (String name : DEFAULT_TAGS) {
            if (!tagRepository.existsByName(name)) {
                tagRepository.save(new Tag(name));
            }
        }
    }
}
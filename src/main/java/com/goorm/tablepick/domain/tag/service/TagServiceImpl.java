package com.goorm.tablepick.domain.tag.service;

import com.goorm.tablepick.domain.tag.dto.TagResponseDto;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    @Override
    public List<TagResponseDto> getTagList() {
        List<Tag> tagList = tagRepository.findAll();

        return tagList.stream()
                .map(TagResponseDto::toDto)
                .collect(Collectors.toList());
    }
}

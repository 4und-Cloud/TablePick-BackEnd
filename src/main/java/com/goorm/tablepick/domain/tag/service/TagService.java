package com.goorm.tablepick.domain.tag.service;

import com.goorm.tablepick.domain.tag.dto.TagResponseDto;
import java.util.List;

public interface TagService {
    List<TagResponseDto> getTagList();
}

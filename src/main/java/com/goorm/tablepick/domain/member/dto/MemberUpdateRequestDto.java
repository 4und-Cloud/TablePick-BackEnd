package com.goorm.tablepick.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goorm.tablepick.domain.member.entity.MemberTag;
import com.goorm.tablepick.domain.member.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 정보 수정시 정보")
public class MemberUpdateRequestDto {
    @Schema(description = "닉네임", example = "gildong1234")
    @NotBlank(message = "닉네임은 필수 항목입니다.")
    private String nickname;

    @Schema(description = "성별", example = "male")
    private Gender gender;

    @Schema(description = "생년월일", example = "2002-01-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    @Schema(description = "전화번호", example = "01098765432")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    private String phoneNumber;

    @Schema(description = "프로필이미지", example = "http://img1.kakaocdn.net/thumb/R640x640.q7")
    private String profileImage;

    @Schema(description = "사용자 선호 태그")
    private List<MemberTag> memberTags = new ArrayList<>();

}

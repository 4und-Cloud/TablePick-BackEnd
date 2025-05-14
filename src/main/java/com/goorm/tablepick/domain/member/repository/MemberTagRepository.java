package com.goorm.tablepick.domain.member.repository;

import com.goorm.tablepick.domain.member.entity.MemberTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTagRepository extends JpaRepository<MemberTag, Long> {
}

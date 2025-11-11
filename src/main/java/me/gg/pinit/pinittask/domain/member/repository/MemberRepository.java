package me.gg.pinit.pinittask.domain.member.repository;

import me.gg.pinit.pinittask.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

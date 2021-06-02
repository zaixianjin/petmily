package com.example.petmily.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petmily.entity.Member;

@Repository
public interface MemberRepository  extends JpaRepository <Member, Long>{
	List<Member> findAllMemberByTeamId(Long id);
	
}

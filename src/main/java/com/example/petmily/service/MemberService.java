package com.example.petmily.service;

import java.util.List;

import com.example.petmily.entity.Member;

public interface MemberService {
	List<Member> findAllMemberByTeamId(Long id);
}

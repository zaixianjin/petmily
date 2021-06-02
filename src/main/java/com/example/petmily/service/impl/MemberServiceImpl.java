package com.example.petmily.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.petmily.entity.Member;
import com.example.petmily.repository.MemberRepository;
import com.example.petmily.service.MemberService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;
	
	@Override
	public List<Member> findAllMemberByTeamId(Long id) {
		// TODO Auto-generated method stub
		return memberRepository.findAllMemberByTeamId(id);
	}

}

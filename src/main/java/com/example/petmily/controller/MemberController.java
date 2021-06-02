package com.example.petmily.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petmily.entity.Member;
import com.example.petmily.service.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberController {
	private final MemberService memberService = null;
	
	@GetMapping("/memberhello")
	public String getMemberHello() {
		return "Member Hello";
	}

	@GetMapping("/member/teams/{teamId}")
	public ResponseEntity<List<Member>> findAllMemberByTeamId(@PathVariable Long teamId){
		return ResponseEntity.ok().body(memberService.findAllMemberByTeamId(teamId));
	}
}

package com.example.petmily.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petmily.entity.Member;
import com.example.petmily.service.MemberService;
import com.example.petmily.service.PetService;

@RestController
@RequestMapping("/api/pet")
public class PetController {
	private final PetService petService = null;
	
	@GetMapping("/pethello")
	public String getPetHello() {
		return "Member Hello";
	}

	@GetMapping("/member/pets/{memberId}")
	public ResponseEntity<List<Member>> findAllMemberByTeamId(@PathVariable Long memberId){
		return ResponseEntity.ok().body(petService.findAllPetByMemberId(memberId));
	}
}

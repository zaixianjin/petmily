package com.example.petmily.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.petmily.entity.Member;
import com.example.petmily.repository.MemberRepository;
import com.example.petmily.repository.PetRepository;
import com.example.petmily.service.MemberService;
import com.example.petmily.service.PetService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PetServiceImpl implements PetService {
	private final PetRepository petRepository;
	
	@Override
	public List<Member> findAllPetByMemberId(Long id) {
		// TODO Auto-generated method stub
		return petRepository.findAllPetByMemberId(id);
	}

}

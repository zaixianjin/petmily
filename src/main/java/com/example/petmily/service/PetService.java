package com.example.petmily.service;

import java.util.List;

import com.example.petmily.entity.Member;

public interface PetService {
	List<Member> findAllPetByMemberId(Long id);
}

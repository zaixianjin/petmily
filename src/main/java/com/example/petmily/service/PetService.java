package com.example.petmily.service;

import java.util.List;
import java.util.UUID;

import com.example.petmily.entity.Member;
import com.example.petmily.entity.Pet;

public interface PetService {
	List<Member> findAllPetByMemberId(Long id);

	Iterable<Pet> findAll();

	Pet save(Pet pet);

	void deleteById(Long id);
}

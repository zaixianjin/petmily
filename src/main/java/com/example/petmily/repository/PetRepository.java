package com.example.petmily.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petmily.entity.Member;
import com.example.petmily.entity.Pet;

@Repository
public interface PetRepository  extends JpaRepository <Pet, Long>{
	List<Member> findAllPetByMemberId(Long id);
	
}

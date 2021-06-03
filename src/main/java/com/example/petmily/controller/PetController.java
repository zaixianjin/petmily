package com.example.petmily.controller;

import java.util.ArrayList;
import java.util.List; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petmily.entity.Member;
import com.example.petmily.entity.Pet; 
import com.example.petmily.service.PetService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/pet")
public class PetController {
	private final PetService petService = null;

	@GetMapping("/pethello")
	public String getPetHello() {
		return "Member Hello";
	}

	@GetMapping("/member/pets/{memberId}")
	public ResponseEntity<List<Member>> findAllMemberByTeamId(@PathVariable Long memberId) {
		return ResponseEntity.ok().body(petService.findAllPetByMemberId(memberId));
	}

	@PostMapping("/pet")
	public Pet createBook(@RequestBody Pet Pet) {
		Pet created = petService.save(Pet);
		return created;
	}

	@GetMapping("/pet")
	public List<Pet> listAllBooks() {
		List<Pet> list = new ArrayList<>();
		Iterable<Pet> iterable = petService.findAll();
		for (Pet Pet : iterable) {
			list.add(Pet);
		}
		return list;
	}

	@PutMapping("/pet/{id}")
	public Pet updateBook(@PathVariable Long id, @RequestBody Pet Pet) {
		Pet.setId(id);
		Pet updated = petService.save(Pet);
		return updated;
	}

	@DeleteMapping("/pet/{id}")
	public void deleteBook(@PathVariable Long id) {
		petService.deleteById(id);
	}

}

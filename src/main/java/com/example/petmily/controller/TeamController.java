package com.example.petmily.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/team/v1")
public class TeamController {
	@GetMapping("/team/hello")
	public String getTeamHello() {
		return "Hello";
	}

}

package com.example.petmily.vo;

import javax.persistence.Column;

public class Contact {
	@Column(name = "email")
	private String email;
	@Column(name = "hp")
	private String hp;
	
}

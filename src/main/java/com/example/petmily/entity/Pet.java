package com.example.petmily.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pet")
public class Pet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "petType")
	private String petType;

	@Column(name = "name")
	private String name;

	@Column(name = "age")
	private String age;

	@Column(name = "weight")
	private String weight;

	@Column(name = "photo")
	private String photo;

	// 식별번호
	@Column(name = "idnum")
	private String idnum;

	// 소개글
	@Column(name = "introduce")
	private String introduce;

	// 중성화여부
	@Column(name = "neutralization")
	private String neutralization;

	// memberId
	@Column(name = "memberId")
	private String memberId;

}

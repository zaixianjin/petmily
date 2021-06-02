package com.example.petmily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petmily.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository <Team, Long>{

}

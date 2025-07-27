package com.example.crud.example;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.crud.example.entity.Product;

public interface TestH2Repository extends JpaRepository<Product,Integer> {
}

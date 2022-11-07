package com.example.demo.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.demo.domain.File;

public interface FileRepository  extends PagingAndSortingRepository<File, Long> {

}

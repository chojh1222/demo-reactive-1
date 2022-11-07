package com.example.demo.service;

import org.springframework.core.io.buffer.DataBuffer;

import com.example.demo.domain.File;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileStorage {
	Mono<File> createFile(final File file);
    Mono<Integer> putObject(final Long id, final Flux<DataBuffer> parts);
    Mono<Integer> writeChunk(final Long id, final Flux<DataBuffer> parts, final long offset);
}

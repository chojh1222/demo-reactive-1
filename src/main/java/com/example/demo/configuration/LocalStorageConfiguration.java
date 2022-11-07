package com.example.demo.configuration;

import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class LocalStorageConfiguration {
	@Bean
    Function<Path, Mono<AsynchronousFileChannel>> channelFunction() {
        return path -> Mono.fromSupplier(() -> {
            try {
                return AsynchronousFileChannel.open(path, WRITE);
            } catch (IOException e) {
                throw new RuntimeException("File open operation fault", e);
            }
        });
    }
}

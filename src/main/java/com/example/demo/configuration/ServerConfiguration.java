package com.example.demo.configuration;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private String fileDirectory = "upload-files/";
	
	@Bean
    public Path fileDirectory() throws IOException {
        final Path writeDirectoryPath = Paths.get(fileDirectory);

        log.debug("Files path: {}, created: {}", writeDirectoryPath, Files.exists(writeDirectoryPath));
        if (!Files.exists(writeDirectoryPath)) {
            Files.createDirectories(writeDirectoryPath);
        }

        if (!Files.isWritable(writeDirectoryPath)) {
            throw new AccessDeniedException(fileDirectory);
        }
        return writeDirectoryPath;
    }
}

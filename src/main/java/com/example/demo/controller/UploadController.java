package com.example.demo.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.domain.File;
import com.example.demo.repository.FileRepository;
import com.example.demo.service.UploadService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/upload")
public class UploadController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final UploadService uploadService;
    private final FileRepository filesRepository;


    @Autowired
    public UploadController(
        final UploadService uploadService,
        final FileRepository filesRepository
    ) {
        this.uploadService = uploadService;
        this.filesRepository = filesRepository;
    }

	@PostMapping
	public Mono<ResponseEntity<Object>> uploadStart(
			@RequestHeader(name = "Upload-Length") final Long fileSize,
	        @RequestHeader(name = "Upload-Metadata") final String metadata,
	        @RequestHeader(name = "Mime-Type", defaultValue = "") final String mimeType,
	        final UriComponentsBuilder uriComponentsBuilder,
	        final ServerHttpRequest request
			) {
		request.getHeaders().forEach((k, v) -> log.debug("headers: {} {}", k, v));
		
		final Map<String, String> parsedMetadata = uploadService.parseMetadata(metadata);

        final File file = new File();
        file.setMimeType(mimeType);
        file.setContentLength(fileSize);
        file.setOriginalName(parsedMetadata.getOrDefault("filename", "FILE NAME NOT EXISTS"));
        file.setContentOffset(0L);
        file.setLastUploadedChunkNumber(0L);
        file.setFingerprint(parsedMetadata.getOrDefault("fingerprint", "FINGERPRINT NAME NOT EXISTS"));

        return uploadService
            .createUpload(file)
            .map(f -> Stream.concat(
                request.getPath().elements().stream().map(PathContainer.Element::value),
                Stream.of(f.getId().toString())
            ))
            .map(stringStream -> stringStream.filter(s -> !"/".equals(s)).toArray(String[]::new))
            .map(strings -> uriComponentsBuilder.pathSegment(strings).build().toUri())
            .map(s -> ResponseEntity
                .created(s)
                .header("Access-Control-Expose-Headers", "Location, Tus-Resumable")
                .header("Tus-Resumable", "1.0.0")
                .build()
            )
            .doOnError(throwable -> log.error("Error on file create", throwable))
            .onErrorReturn(ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .build()
            );
	}
	
	
	@RequestMapping(
	        method = {RequestMethod.POST, RequestMethod.PATCH,},
	        value = {"/{id}"},
	        consumes = {"application/offset+octet-stream"}
	    )
	    public Mono<ResponseEntity<Object>> uploadProcess(
	        @PathVariable("id") final Long id,
	        final ServerHttpRequest request,
	        @RequestHeader(name = "Upload-Offset") final long offset,
	        @RequestHeader(name = "Content-Length", defaultValue = "0") final long length
	    ) {
	        request.getHeaders().forEach((k, v) -> log.debug("headers: {} {}", k, v));

	        return
	            uploadService
	                .uploadChunkAndGetUpdatedOffset(
	                    id,
	                    request.getBody(),
	                    offset,
	                    length
	                )
//	                .log()
	                .map(e -> ResponseEntity
	                    .status(NO_CONTENT)
	                    .header("Access-Control-Expose-Headers", "Location, Tus-Resumable")
	                    .header("Upload-Offset", Long.toString(e.getContentOffset()))
	                    .header("Tus-Resumable", "1.0.0")
	                    .build()
	                )
	                .doOnNext(r -> log.info("{}", r.getHeaders()))
	            ;
	    }


	    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
	    public Mono<ResponseEntity<?>> header(@PathVariable("id") final Long id) {
	        return Mono.just(filesRepository.findById(id).map(e ->
	            ResponseEntity
	                .status(NO_CONTENT)
	                .headers(new HttpHeaders())
	                .header("Location", e.getId().toString())
	                .header("Cache-Control", "no-store")
	                .header("Upload-Length", e.getContentLength().toString())
	                .header("Upload-Offset", e.getContentOffset().toString())
	                .build())
	            .orElseGet(() -> ResponseEntity.notFound().build()));
	    }


	    @RequestMapping(method = RequestMethod.OPTIONS)
	    public Mono<ResponseEntity> processOptions() {
	        return Mono.just(ResponseEntity
	            .status(NO_CONTENT)
	            .header("Access-Control-Expose-Headers", "Tus-Resumable, Tus-Version, Tus-Max-Size, Tus-Extension")
	            .header("Tus-Resumable", "1.0.0")
	            .header("Tus-Version", "1.0.0,0.2.2,0.2.1")
	            .header("Tus-Extension", "creation,expiration")
	            .header("Access-Control-Allow-Methods", "GET,PUT,PATCH,POST,DELETE")
	            .build());
	    }
			
}

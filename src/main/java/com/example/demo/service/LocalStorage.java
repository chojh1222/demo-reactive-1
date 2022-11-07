package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;

import com.example.demo.domain.File;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LocalStorage implements FileStorage {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private Path fileDirectory;
    private Function<Path, Mono<AsynchronousFileChannel>> channelFunction;

    @Autowired
    public void setFileDirectory(Path fileDirectory) {
        this.fileDirectory = fileDirectory;
    }

    @Autowired
    public void setChannelFunction(Function<Path, Mono<AsynchronousFileChannel>> channelFunction) {
        this.channelFunction = channelFunction;
    }

    @Override
    public Mono<Integer> putObject(
        final Long id,
        final Flux<DataBuffer> parts
    ) {
        return parts
            .map(v -> flushBufferToFile(v, id))
            .reduce(0, Integer::sum);
    }

    @Override
    public Mono<File> createFile(final File file) {
        return Mono.fromSupplier(() -> {
            try {
            	
                Files.createFile(Paths.get(fileDirectory.toString(), file.getId().toString()));
                return file;
            } catch (IOException e) {
                throw new RuntimeException("File creation failed: " + file, e);
            }
        });
    }

    @Override
    public Mono<Integer> writeChunk(
        final Long id,
        final Flux<DataBuffer> parts,
        final long offset
    ) {

        final Path file = Paths.get(fileDirectory.toString(), id.toString());
        final Mono<AsynchronousFileChannel> channel = channelFunction.apply(file);

        return channel
            .flatMapMany(asynchronousFileChannel -> DataBufferUtils.write(parts, asynchronousFileChannel, offset))
            .map(dataBuffer -> {
                final int capacity = dataBuffer.capacity();
                DataBufferUtils.release(dataBuffer);
                return capacity;
            })
            .reduce(0, Integer::sum)
            .doOnSuccessOrError((integer, throwable) -> channel.subscribe(this::closeChannel));
    }

    void closeChannel(
        final AsynchronousFileChannel asynchronousFileChannel
    ){
        try {
            asynchronousFileChannel.close();
        } catch (IOException e) {
            throw new RuntimeException("Channel close error");
        }
    }

    int flushBufferToFile(
        final DataBuffer dataBuffer,
        final Long id
    ) {
        try {
            final InputStream inputStream = dataBuffer.asInputStream(true);
            int bytesLength = inputStream.available();
            final Path file = Paths.get(fileDirectory.toString(), id.toString());

            if (!Files.exists(file)) {
                Files.createFile(file);
            }

            Files.write(
                file,
                IOUtils.toByteArray(inputStream),
                StandardOpenOption.APPEND
            );

            inputStream.close();
            return bytesLength;
        } catch (IOException e) {
            log.error("file write error");
            throw new RuntimeException("file write error", e);
        }
    }
}

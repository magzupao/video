package com.video.app.service;

import com.video.app.service.dto.*;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PythonVideoService {

    private static final Logger LOG = LoggerFactory.getLogger(PythonVideoService.class);
    private static final String PYTHON_API_URL = "http://video-python:9094/generate_video/";
    private static final String PYTHON_API_WITHOUT_URL = "http://video-python:9094/generate_video_whitout/";

    private final WebClient webClient;

    public PythonVideoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<PythonVideoResponse> generateVideoWithAudio(String imagesPath, String audioPath, String videoOutputPath, String format) {
        LOG.info("Llamando a Python para generar video CON audio");
        LOG.info("Images: {}, Audio: {}, Output: {}, Format: {}", imagesPath, audioPath, videoOutputPath, format);

        VideoGenerationRequest request = new VideoGenerationRequest();
        request.setImages_path(imagesPath);
        request.setAudio_path(audioPath);
        request.setVideo_path(videoOutputPath);
        request.setFormat(format != null ? format : "mp4");

        return webClient
            .post()
            .uri(PYTHON_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(body -> {
                        LOG.error("Error 4xx en Python: {}", body);
                        return Mono.error(new RuntimeException("Error 4xx en Python: " + body));
                    })
            )
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(body -> {
                        LOG.error("Error 5xx en Python: {}", body);
                        return Mono.error(new RuntimeException("Error 5xx en Python: " + body));
                    })
            )
            .bodyToMono(PythonVideoResponse.class)
            .timeout(Duration.ofMinutes(10))
            .doOnSuccess(response -> LOG.info("Video generado exitosamente: {}", response.getMetadata().getFull_path()))
            .doOnError(error -> LOG.error("Error generando video en Python", error));
    }

    public Mono<PythonVideoResponse> generateVideoWithoutAudio(
        String imagesPath,
        String videoOutputPath,
        String format,
        Integer transicionSegundos
    ) {
        LOG.info("Llamando a Python para generar video SIN audio");
        LOG.info("Images: {}, Output: {}, Format: {}, Transición: {}s", imagesPath, videoOutputPath, format, transicionSegundos);

        VideoWhitoutAudioGenerationRequest request = new VideoWhitoutAudioGenerationRequest();
        request.setImages_path(imagesPath);
        request.setVideo_path(videoOutputPath);
        request.setFormat(format != null ? format : "mp4");
        request.setTransicion_segundos(transicionSegundos != null ? transicionSegundos : 3);

        return webClient
            .post()
            .uri(PYTHON_API_WITHOUT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(body -> {
                        LOG.error("Error 4xx en Python: {}", body);
                        return Mono.error(new RuntimeException("Error 4xx en Python: " + body));
                    })
            )
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(body -> {
                        LOG.error("Error 5xx en Python: {}", body);
                        return Mono.error(new RuntimeException("Error 5xx en Python: " + body));
                    })
            )
            .bodyToMono(PythonVideoResponse.class)
            .timeout(Duration.ofMinutes(10))
            .doOnSuccess(response -> LOG.info("Video generado exitosamente: {}", response.getMetadata().getFull_path()))
            .doOnError(error -> LOG.error("Error generando video en Python", error));
    }
}

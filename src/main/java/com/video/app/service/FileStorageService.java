package com.video.app.service;

import com.video.app.service.dto.FileSystemPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);
    private static final String BASE_STORAGE_PATH = "/app/shared-data/videos";

    /**
     * Guarda archivos en disco usando el ID del video como identificador único.
     * Estructura: /app/shared-data/videos/{videoId}/images/
     *            /app/shared-data/videos/{videoId}/audio/
     *            /app/shared-data/videos/{videoId}/output/
     */
    public Mono<FileSystemPaths> saveFilesToDisk(Long videoId, List<FilePart> images, FilePart audio) {
        LOG.info("=== Guardando archivos para video ID: {} ===", videoId);
        LOG.info("Número de imágenes: {}", images != null ? images.size() : 0);
        LOG.info("Audio: {}", audio != null ? audio.filename() : "sin audio");

        if (images == null || images.isEmpty()) {
            LOG.error("❌ No se recibieron imágenes para el video: {}", videoId);
            return Mono.error(new RuntimeException("No se recibieron imágenes"));
        }

        String baseDir = BASE_STORAGE_PATH + "/" + videoId;
        String imagesDir = baseDir + "/images";
        String audioDir = baseDir + "/audio";
        String outputDir = baseDir + "/output";

        Mono<Void> createDirs = Mono.fromCallable(() -> {
            Files.createDirectories(Path.of(imagesDir));
            Files.createDirectories(Path.of(audioDir));
            Files.createDirectories(Path.of(outputDir));
            return true;
        })
            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
            .then()
            .doOnSuccess(v -> LOG.info("✅ Directorios creados: {}", baseDir));

        Flux<Void> saveImages = Flux.fromIterable(images)
            .index()
            .flatMap(tuple -> {
                long i = tuple.getT1();
                FilePart image = tuple.getT2();
                String filename = String.format("image_%03d.jpg", i);
                Path imagePath = Path.of(imagesDir, filename);
                LOG.debug("Guardando imagen {}/{}: {}", i + 1, images.size(), filename);
                return image.transferTo(imagePath);
            });

        Mono<Void> audioSave = Mono.empty();
        String finalAudioPath = null;

        if (audio != null) {
            String audioFilename = audio.filename();
            Path audioPath = Path.of(audioDir, audioFilename);
            LOG.info("Guardando audio: {}", audioFilename);
            audioSave = audio.transferTo(audioPath);
            finalAudioPath = audioPath.toString();
        }

        String audioPathFinal = finalAudioPath;

        return createDirs
            .thenMany(saveImages)
            .then(audioSave)
            .thenReturn(new FileSystemPaths(imagesDir, audioPathFinal, outputDir))
            .doOnSuccess(paths -> {
                LOG.info("✅ Archivos guardados exitosamente para video: {}", videoId);
                LOG.info("   - Imágenes: {}", imagesDir);
                LOG.info("   - Audio: {}", audioPathFinal != null ? audioPathFinal : "sin audio");
                LOG.info("   - Output: {}", outputDir);
            })
            .doOnError(error -> LOG.error("❌ Error guardando archivos para video: {}", videoId, error));
    }

    /**
     * Limpia los archivos temporales de un video (útil en caso de error)
     */
    public Mono<Void> cleanupFiles(String videoId) {
        return Mono.fromRunnable(() -> {
            String baseDir = BASE_STORAGE_PATH + "/" + videoId;
            Path basePath = Path.of(baseDir);

            try {
                if (Files.exists(basePath)) {
                    LOG.info("Limpiando archivos del video: {}", videoId);

                    // Eliminar recursivamente todos los archivos y directorios
                    try (Stream<Path> walk = Files.walk(basePath)) {
                        walk
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    LOG.warn("No se pudo eliminar: {}", path, e);
                                }
                            });
                    }

                    LOG.info("✅ Archivos limpiados para video: {}", videoId);
                } else {
                    LOG.warn("No existe directorio para limpiar: {}", baseDir);
                }
            } catch (IOException e) {
                LOG.error("Error limpiando archivos del video: {}", videoId, e);
                throw new RuntimeException("Error limpiando archivos", e);
            }
        });
    }

    /**
     * Verifica si los directorios de un video existen
     */
    public boolean videoDirectoryExists(String videoId) {
        String baseDir = BASE_STORAGE_PATH + "/" + videoId;
        return Files.exists(Path.of(baseDir));
    }
}

package com.video.app.service;

import com.video.app.domain.enumeration.EstadoVideo;
import com.video.app.service.dto.FileSystemPaths;
import com.video.app.service.dto.PythonVideoResponse;
import com.video.app.service.dto.VideoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class VideoProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(VideoProcessingService.class);

    private final FileStorageService fileStorageService;
    private final PythonVideoService pythonVideoService;
    private final VideoService videoService;

    public VideoProcessingService(FileStorageService fileStorageService, PythonVideoService pythonVideoService, VideoService videoService) {
        this.fileStorageService = fileStorageService;
        this.pythonVideoService = pythonVideoService;
        this.videoService = videoService;
    }

    /**
     * Procesa el video de forma asíncrona en un hilo separado.
     * Este método NO bloquea la respuesta HTTP al cliente.
     *
     * @param videoDTO el video a procesar
     * @param images lista de imágenes
     * @param audio archivo de audio opcional
     */
    @Async("videoTaskExecutor")
    public void processVideoAsync(Long videoId) {
        String threadName = Thread.currentThread().getName();

        LOG.info("=== INICIO PROCESAMIENTO ASÍNCRONO ===");
        LOG.info("Video ID: {}", videoId);
        LOG.info("Thread: {}", threadName);

        VideoDTO videoDTO = null;

        try {
            // 1) Cargar video desde BD
            videoDTO = videoService
                .findOne(videoId)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Video no encontrado: " + videoId));

            LOG.info("✅ [{}] Video cargado desde BD. Tiene audio: {}", videoId, videoDTO.getTieneAudio());

            // 2) Construir paths (asumimos que archivos YA están persistidos)
            String baseDir = "/app/shared-data/videos/" + videoId;
            String imagesDir = baseDir + "/images";
            String outputDir = baseDir + "/output";

            String audioPath = null;
            if (Boolean.TRUE.equals(videoDTO.getTieneAudio()) && videoDTO.getAudioFilename() != null) {
                audioPath = baseDir + "/audio/" + videoDTO.getAudioFilename();
            }

            FileSystemPaths paths = new FileSystemPaths(imagesDir, audioPath, outputDir);

            LOG.info("📁 [{}] Usando paths persistidos:", videoId);
            LOG.info("   - Images: {}", paths.getImagesPath());
            LOG.info("   - Audio: {}", paths.getAudioPath() != null ? paths.getAudioPath() : "sin audio");
            LOG.info("   - Output: {}", paths.getVideoOutputPath());

            // 3) Llamar a Python
            LOG.info("🐍 [{}] Llamando a Python para generar video...", videoId);

            Mono<PythonVideoResponse> pythonCall;

            if (paths.getAudioPath() != null) {
                LOG.info("🎵 [{}] Generando video CON audio", videoId);
                pythonCall = pythonVideoService.generateVideoWithAudio(
                    paths.getImagesPath(),
                    paths.getAudioPath(),
                    paths.getVideoOutputPath(),
                    videoDTO.getFormato()
                );
            } else {
                LOG.info("🔇 [{}] Generando video SIN audio", videoId);
                pythonCall = pythonVideoService.generateVideoWithoutAudio(
                    paths.getImagesPath(),
                    paths.getVideoOutputPath(),
                    videoDTO.getFormato(),
                    videoDTO.getDuracionTransicion()
                );
            }

            PythonVideoResponse pythonResponse = pythonCall.block();

            if (pythonResponse == null || pythonResponse.getMetadata() == null) {
                throw new RuntimeException("Python no retornó metadata válida");
            }

            LOG.info("✅ [{}] Python generó el video exitosamente", videoId);
            LOG.info("   - Video path: {}", pythonResponse.getMetadata().getFull_path());
            LOG.info("   - Duration: {}", pythonResponse.getMetadata().getDuration());

            // 4) Actualizar estado en BD
            LOG.info("💾 [{}] Actualizando video en BD con estado COMPLETADO...", videoId);

            String fullPath = pythonResponse.getMetadata().getFull_path();

            videoDTO.setVideoPath(fullPath);
            videoDTO.setOutputFilename(java.nio.file.Path.of(fullPath).getFileName().toString());
            videoDTO.setEstado(EstadoVideo.COMPLETADO);
            videoDTO.setDuracionTransicion(pythonResponse.getMetadata().getDuration().intValue());

            VideoDTO updatedVideo = videoService.update(videoDTO).block();

            LOG.info("✅ [{}] Video actualizado exitosamente. Estado={}", videoId, updatedVideo.getEstado());
            LOG.info("=== FIN PROCESAMIENTO ASÍNCRONO EXITOSO ===");
        } catch (Exception error) {
            LOG.error("❌❌❌ ERROR en procesamiento asíncrono del video {} ❌❌❌", videoId, error);

            try {
                LOG.info("🔄 [{}] Marcando video como ERROR en BD...", videoId);

                if (videoDTO == null) {
                    videoDTO = videoService.findOne(videoId).blockOptional().orElse(null);
                }
                if (videoDTO != null) {
                    videoDTO.setEstado(EstadoVideo.ERROR);
                    videoService.update(videoDTO).block();
                }

                LOG.info("✅ [{}] Video marcado como ERROR", videoId);

                LOG.info("🗑️ [{}] Limpiando archivos...", videoId);
                fileStorageService
                    .cleanupFiles(videoId.toString())
                    .subscribe(
                        success -> LOG.info("✅ Archivos limpiados para video: {}", videoId),
                        err -> LOG.error("❌ Error limpiando archivos", err)
                    );
            } catch (Exception updateError) {
                LOG.error("❌ Error crítico: no se pudo marcar el video {} como ERROR", videoId, updateError);
            }

            LOG.error("=== FIN PROCESAMIENTO ASÍNCRONO CON ERROR ===");
        }
    }
}

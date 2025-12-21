package com.video.app.web.rest;

import com.video.app.domain.User;
import com.video.app.domain.Video;
import com.video.app.domain.enumeration.EstadoVideo;
import com.video.app.repository.UserRepository;
import com.video.app.repository.VideoRepository;
import com.video.app.service.FileStorageService;
import com.video.app.service.PythonVideoService;
import com.video.app.service.VideoService;
import com.video.app.service.dto.PythonVideoResponse;
import com.video.app.service.dto.UserDTO;
import com.video.app.service.dto.VideoDTO;
import com.video.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.video.app.domain.Video}.
 */
@RestController
@RequestMapping("/api/videos")
public class VideoResource {

    private static final Logger LOG = LoggerFactory.getLogger(VideoResource.class);

    private static final String ENTITY_NAME = "video";

    @Value("${jhipster.clientApp.name:video}")
    private String applicationName;

    private final VideoService videoService;

    private final VideoRepository videoRepository;

    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;

    private final PythonVideoService pythonVideoService;

    public VideoResource(
        VideoService videoService,
        VideoRepository videoRepository,
        UserRepository userRepository,
        FileStorageService fileStorageService,
        PythonVideoService pythonVideoService
    ) {
        this.videoService = videoService;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.pythonVideoService = pythonVideoService;
    }

    /**
     * {@code POST  /videos} : Create a new video.
     *
     * @param videoDTO the videoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new videoDTO, or with status {@code 400 (Bad Request)} if the video has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<VideoDTO>> createVideo(@Valid @RequestBody VideoDTO videoDTO) throws URISyntaxException {
        LOG.debug("REST request to save Video : {}", videoDTO);
        if (videoDTO.getId() != null) {
            throw new BadRequestAlertException("A new video cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return videoService
            .save(videoDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/videos/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code POST  /videos} : Create a new video with images and optional audio.
     *
     * @param videoDTO the videoDTO to create.
     * @param images the list of image files.
     * @param audio the optional audio file.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new videoDTO.
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<VideoDTO>> createVideoMultipart(
        @Valid @RequestPart("video") VideoDTO videoDTO,
        @RequestPart("images") List<FilePart> images,
        @RequestPart(value = "audio", required = false) FilePart audio,
        @AuthenticationPrincipal Jwt jwt
    ) {
        LOG.info("=== INICIO createVideoMultipart ===");
        LOG.info("REST request to save Video (multipart) : {}", videoDTO);
        LOG.info("Number of images received: {}", images != null ? images.size() : 0);
        LOG.info("Audio file received: {}", audio != null ? audio.filename() : "none");

        if (videoDTO.getId() != null) {
            LOG.error("❌ Video ya tiene ID: {}", videoDTO.getId());
            throw new BadRequestAlertException("A new video cannot already have an ID", ENTITY_NAME, "idexists");
        }

        // Configurar datos iniciales del video
        String videoTitle = "video-" + UUID.randomUUID().toString().substring(0, 8);
        videoDTO.setTitulo(videoTitle);
        videoDTO.setAudioFilename(audio != null ? audio.filename() : null);
        videoDTO.setTieneAudio(audio != null);
        videoDTO.setEstado(EstadoVideo.EN_PROCESO);
        videoDTO.setFechaCreacion(Instant.now());

        LOG.info("✅ Video configurado:");
        LOG.info("   - Titulo: {}", videoDTO.getTitulo());
        LOG.info("   - Audio filename: {}", videoDTO.getAudioFilename());
        LOG.info("   - Tiene audio: {}", videoDTO.getTieneAudio());
        LOG.info("   - Estado: {}", videoDTO.getEstado());
        LOG.info("   - Formato: {}", videoDTO.getFormato());

        // Obtener usuario del JWT
        String currentUserLogin = jwt.getClaimAsString("sub");
        LOG.info("🔐 Usuario del JWT: {}", currentUserLogin);

        if (currentUserLogin == null || currentUserLogin.isEmpty()) {
            LOG.error("❌ No hay usuario autenticado en JWT");
            return Mono.error(new RuntimeException("No hay usuario autenticado"));
        }

        LOG.info("🔍 Buscando usuario en BD: {}", currentUserLogin);

        return userRepository
            .findOneByLogin(currentUserLogin)
            .doOnNext(user -> LOG.info("✅ Usuario encontrado - ID: {}, Login: {}", user.getId(), user.getLogin()))
            .switchIfEmpty(
                Mono.defer(() -> {
                    LOG.error("❌ Usuario NO encontrado en BD: {}", currentUserLogin);
                    return Mono.error(new RuntimeException("Usuario no encontrado: " + currentUserLogin));
                })
            )
            .flatMap(user -> {
                LOG.info("📝 Asignando usuario al VideoDTO...");
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setLogin(user.getLogin());
                videoDTO.setUser(userDTO);

                LOG.info("✅ Usuario asignado - User ID: {}, Login: {}", videoDTO.getUser().getId(), videoDTO.getUser().getLogin());

                // ✅ PRIMERO: Guardar en BD para obtener ID único
                LOG.info("💾 Intentando guardar video en BD...");
                LOG.info("   VideoDTO antes de save: {}", videoDTO);

                return videoService
                    .save(videoDTO)
                    .doOnSuccess(saved -> {
                        LOG.info("✅ Video guardado EXITOSAMENTE en BD");
                        LOG.info("   - ID generado: {}", saved.getId());
                        LOG.info("   - Titulo: {}", saved.getTitulo());
                        LOG.info("   - Estado: {}", saved.getEstado());
                        LOG.info("   - User ID: {}", saved.getUser() != null ? saved.getUser().getId() : "NULL");
                    })
                    .doOnError(error -> {
                        LOG.error("❌ ERROR guardando video en BD", error);
                        LOG.error("   Tipo de error: {}", error.getClass().getName());
                        LOG.error("   Mensaje: {}", error.getMessage());
                    });
            })
            .flatMap(savedVideo -> {
                // ✅ SEGUNDO: Usar el ID de BD como identificador único
                String uniqueVideoId = savedVideo.getId().toString();
                LOG.info("📁 Video creado con ID: {}", uniqueVideoId);
                LOG.info("🗂️ Iniciando guardado de archivos en disco...");

                // ✅ TERCERO: Guardar archivos usando el ID de BD
                return fileStorageService
                    .saveFilesToDisk(uniqueVideoId, images, audio)
                    .doOnSuccess(paths -> {
                        LOG.info("✅ Archivos guardados en disco");
                        LOG.info("   - Images path: {}", paths.getImagesPath());
                        LOG.info("   - Audio path: {}", paths.getAudioPath());
                        LOG.info("   - Output path: {}", paths.getVideoOutputPath());
                    })
                    .doOnError(error -> {
                        LOG.error("❌ ERROR guardando archivos en disco", error);
                    })
                    .flatMap(paths -> {
                        // ✅ CUARTO: Llamar a Python para generar el video
                        LOG.info("🐍 Preparando llamada a Python...");

                        Mono<PythonVideoResponse> pythonCall;

                        if (savedVideo.getTieneAudio() != null && savedVideo.getTieneAudio()) {
                            LOG.info("🎵 Generando video CON audio");
                            LOG.info("   - Images: {}", paths.getImagesPath());
                            LOG.info("   - Audio: {}", paths.getAudioPath());
                            LOG.info("   - Output: {}", paths.getVideoOutputPath());
                            LOG.info("   - Format: {}", savedVideo.getFormato());

                            pythonCall = pythonVideoService.generateVideoWithAudio(
                                paths.getImagesPath(),
                                paths.getAudioPath(),
                                paths.getVideoOutputPath(),
                                savedVideo.getFormato()
                            );
                        } else {
                            LOG.info("🔇 Generando video SIN audio");
                            LOG.info("   - Images: {}", paths.getImagesPath());
                            LOG.info("   - Output: {}", paths.getVideoOutputPath());
                            LOG.info("   - Format: {}", savedVideo.getFormato());
                            LOG.info("   - Transición: {} segundos", savedVideo.getDuracionTransicion());

                            pythonCall = pythonVideoService.generateVideoWithoutAudio(
                                paths.getImagesPath(),
                                paths.getVideoOutputPath(),
                                savedVideo.getFormato(),
                                savedVideo.getDuracionTransicion()
                            );
                        }

                        return pythonCall
                            .doOnSuccess(response -> {
                                LOG.info("✅ Python respondió EXITOSAMENTE");
                                LOG.info("   - Status: {}", response.getStatus());
                                LOG.info("   - Video path: {}", response.getVideo_path());
                                if (response.getMetadata() != null) {
                                    LOG.info("   - Full path: {}", response.getMetadata().getFull_path());
                                    LOG.info("   - Duration: {}", response.getMetadata().getDuration());
                                    LOG.info("   - Images used: {}", response.getMetadata().getImages_used());
                                }
                            })
                            .doOnError(error -> {
                                LOG.error("❌ ERROR en llamada a Python", error);
                                LOG.error("   Tipo: {}", error.getClass().getName());
                                LOG.error("   Mensaje: {}", error.getMessage());
                            })
                            .flatMap(pythonResponse -> {
                                // ✅ QUINTO: Actualizar video con metadata
                                LOG.info("🔄 Actualizando video con metadata de Python...");

                                savedVideo.setVideoPath(pythonResponse.getMetadata().getFull_path());
                                savedVideo.setEstado(EstadoVideo.COMPLETADO);
                                savedVideo.setDuracionTransicion(pythonResponse.getMetadata().getDuration().intValue());

                                LOG.info("   - Nuevo estado: {}", savedVideo.getEstado());
                                LOG.info("   - Video path: {}", savedVideo.getVideoPath());
                                LOG.info("   - Duración: {}", savedVideo.getDuracionTransicion());

                                return videoService
                                    .update(savedVideo)
                                    .doOnSuccess(updated -> {
                                        LOG.info("✅ Video actualizado EXITOSAMENTE");
                                        LOG.info("   - ID: {}", updated.getId());
                                        LOG.info("   - Estado final: {}", updated.getEstado());
                                    })
                                    .doOnError(error -> {
                                        LOG.error("❌ ERROR actualizando video en BD", error);
                                    });
                            })
                            .onErrorResume(error -> {
                                // Si falla, marcar como ERROR y limpiar archivos
                                LOG.error("❌ ERROR en proceso de generación de video para ID: {}", uniqueVideoId, error);
                                LOG.error("   Marcando video como ERROR...");

                                savedVideo.setEstado(EstadoVideo.ERROR);

                                return videoService
                                    .update(savedVideo)
                                    .doOnSuccess(v -> {
                                        LOG.info("✅ Video marcado como ERROR en BD");
                                        LOG.info("🗑️ Iniciando limpieza de archivos...");

                                        // Limpiar archivos en caso de error (opcional)
                                        fileStorageService
                                            .cleanupFiles(uniqueVideoId)
                                            .subscribe(
                                                success -> LOG.info("✅ Archivos limpiados para video: {}", uniqueVideoId),
                                                err -> LOG.error("❌ Error limpiando archivos", err)
                                            );
                                    })
                                    .doOnError(updateError -> {
                                        LOG.error("❌ ERROR marcando video como ERROR", updateError);
                                    });
                            });
                    })
                    .map(result -> {
                        try {
                            LOG.info("✅ Generando respuesta final");
                            LOG.info("   - Video ID: {}", result.getId());
                            LOG.info("   - Estado: {}", result.getEstado());
                            LOG.info("   - Video path: {}", result.getVideoPath());
                            LOG.info("=== FIN createVideoMultipart EXITOSO ===");

                            return ResponseEntity.created(new URI("/api/videos/" + result.getId()))
                                .headers(
                                    HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())
                                )
                                .body(result);
                        } catch (URISyntaxException e) {
                            LOG.error("❌ Error creando URI", e);
                            throw new RuntimeException(e);
                        }
                    });
            })
            .doOnError(error -> {
                LOG.error("❌❌❌ ERROR FINAL en createVideoMultipart ❌❌❌", error);
                LOG.error("   Tipo: {}", error.getClass().getName());
                LOG.error("   Mensaje: {}", error.getMessage());
                if (error.getCause() != null) {
                    LOG.error("   Causa: {}", error.getCause().getMessage());
                }
                LOG.error("=== FIN createVideoMultipart CON ERROR ===");
            });
    }

    /**
     * Saves images and optional audio to disk.
     *
     * @param images list of image files
     * @param audio optional audio file
     * @return Mono<Void> indicating completion
     */
    private Mono<Void> saveFilesToDisk(List<FilePart> images, FilePart audio) {
        Path imagesPath = Path.of(System.getProperty("user.dir"), "uploads", "images");
        Path audioPath = Path.of(System.getProperty("user.dir"), "uploads", "audio");

        try {
            Files.createDirectories(imagesPath);
            Files.createDirectories(audioPath);
        } catch (IOException e) {
            return Mono.error(e);
        }

        // Guardar imágenes
        Mono<Void> imagesMono = Flux.fromIterable(images)
            .flatMap(file -> {
                Path destination = imagesPath.resolve(UUID.randomUUID() + "-" + file.filename());
                LOG.debug("Saving image to: {}", destination);
                return file.transferTo(destination);
            })
            .then();

        // Guardar audio si existe
        Mono<Void> audioMono = audio != null
            ? Mono.defer(() -> {
                  Path destination = audioPath.resolve(UUID.randomUUID() + "-" + audio.filename());
                  LOG.debug("Saving audio to: {}", destination);
                  return audio.transferTo(destination);
              })
            : Mono.empty();

        return Mono.when(imagesMono, audioMono);
    }

    /**
     * {@code PUT  /videos/:id} : Updates an existing video.
     *
     * @param id the id of the videoDTO to save.
     * @param videoDTO the videoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoDTO,
     * or with status {@code 400 (Bad Request)} if the videoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the videoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<VideoDTO>> updateVideo(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VideoDTO videoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Video : {}, {}", id, videoDTO);
        if (videoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return videoService
                    .update(videoDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PUT  /videos/:id} : Updates an existing video with images and optional audio.
     *
     * @param id the id of the videoDTO to save.
     * @param videoDTO the videoDTO to update.
     * @param images the list of image files.
     * @param audio the optional audio file.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoDTO.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<VideoDTO>> updateVideoMultipart(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestPart("video") VideoDTO videoDTO,
        @RequestPart(value = "images", required = false) List<FilePart> images,
        @RequestPart(value = "audio", required = false) FilePart audio
    ) {
        LOG.debug("REST request to update Video (multipart) : {}, {}", id, videoDTO);
        LOG.debug("Number of images received: {}", images != null ? images.size() : 0);
        LOG.debug("Audio file received: {}", audio != null ? audio.filename() : "none");

        if (videoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Void> filesMono = Mono.empty();
                if ((images != null && !images.isEmpty()) || audio != null) {
                    filesMono = saveFilesToDisk(images != null ? images : List.of(), audio);
                }

                return filesMono
                    .then(videoService.update(videoDTO))
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /videos/:id} : Partial updates given fields of an existing video, field will ignore if it is null
     *
     * @param id the id of the videoDTO to save.
     * @param videoDTO the videoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoDTO,
     * or with status {@code 400 (Bad Request)} if the videoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the videoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the videoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<VideoDTO>> partialUpdateVideo(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VideoDTO videoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Video partially : {}, {}", id, videoDTO);
        if (videoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<VideoDTO> result = videoService.partialUpdate(videoDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /videos} : get all the videos.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of videos in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<VideoDTO>>> getAllVideos(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Videos");
        return videoService
            .countAll()
            .zipWith(videoService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /videos/:id} : get the "id" video.
     *
     * @param id the id of the videoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the videoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<VideoDTO>> getVideo(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Video : {}", id);
        Mono<VideoDTO> videoDTO = videoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(videoDTO);
    }

    /**
     * {@code DELETE  /videos/:id} : delete the "id" video.
     *
     * @param id the id of the videoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteVideo(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Video : {}", id);
        return videoService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}

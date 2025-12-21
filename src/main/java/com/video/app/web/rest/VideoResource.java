package com.video.app.web.rest;

import com.video.app.domain.enumeration.EstadoVideo;
import com.video.app.repository.UserRepository;
import com.video.app.repository.VideoRepository;
import com.video.app.service.FileStorageService;
import com.video.app.service.VideoProcessingService;
import com.video.app.service.VideoService;
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

    private final VideoProcessingService videoProcessingService;

    private final FileStorageService fileStorageService;

    public VideoResource(
        VideoService videoService,
        VideoRepository videoRepository,
        UserRepository userRepository,
        VideoProcessingService videoProcessingService,
        FileStorageService fileStorageService
    ) {
        this.videoService = videoService;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.videoProcessingService = videoProcessingService;
        this.fileStorageService = fileStorageService;
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
     * Retorna inmediatamente 202 Accepted y procesa el video de forma asíncrona.
     *
     * @param videoDTO the videoDTO to create.
     * @param images the list of image files.
     * @param audio the optional audio file.
     * @return the {@link ResponseEntity} with status {@code 202 (Accepted)} and with body the new videoDTO.
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<VideoDTO>> createVideoMultipart(
        @Valid @RequestPart("video") VideoDTO videoDTO,
        @RequestPart("images") List<FilePart> images,
        @RequestPart(value = "audio", required = false) FilePart audio,
        @AuthenticationPrincipal Jwt jwt
    ) {
        LOG.info("=== INICIO createVideoMultipart (ASÍNCRONO) ===");
        LOG.info("REST request to save Video (multipart) : {}", videoDTO);
        LOG.info("Number of images received: {}", images != null ? images.size() : 0);
        LOG.info("Audio file received: {}", audio != null ? audio.filename() : "none");

        if (videoDTO.getId() != null) {
            throw new BadRequestAlertException("A new video cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (images == null || images.isEmpty()) {
            throw new BadRequestAlertException("At least one image is required", ENTITY_NAME, "noimages");
        }

        String videoTitle = "video-" + UUID.randomUUID().toString().substring(0, 8);
        videoDTO.setTitulo(videoTitle);
        videoDTO.setAudioFilename(audio != null ? audio.filename() : null);
        videoDTO.setTieneAudio(audio != null);
        videoDTO.setEstado(EstadoVideo.EN_PROCESO);
        videoDTO.setFechaCreacion(Instant.now());

        String currentUserLogin = jwt.getClaimAsString("sub");
        if (currentUserLogin == null || currentUserLogin.isEmpty()) {
            return Mono.error(new RuntimeException("No hay usuario autenticado"));
        }

        return userRepository
            .findOneByLogin(currentUserLogin)
            .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado: " + currentUserLogin)))
            .flatMap(user -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setLogin(user.getLogin());
                videoDTO.setUser(userDTO);

                return videoService.save(videoDTO);
            })
            .flatMap(savedVideo -> {
                final Long videoId = savedVideo.getId();

                Mono<VideoDTO> persistFilesThenReturnVideo = fileStorageService
                    .saveFilesToDisk(videoId, images, audio)
                    .thenReturn(savedVideo)
                    .onErrorResume(err -> {
                        // marcar ERROR si falla el guardado de archivos
                        savedVideo.setEstado(EstadoVideo.ERROR);
                        return videoService.update(savedVideo).then(Mono.error(err));
                    });

                return persistFilesThenReturnVideo;
            })
            .map(savedVideo -> {
                final Long videoId = savedVideo.getId();

                // async SOLO con id
                videoProcessingService.processVideoAsync(videoId);

                return ResponseEntity.accepted()
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, videoId.toString()))
                    .body(savedVideo);
            })
            .doOnError(err -> LOG.error("❌ ERROR FINAL en createVideoMultipart", err));
    }

    /**
     * {@code GET  /videos/:id/status} : get the status of the "id" video.
     * Endpoint optimizado para polling que retorna solo la información de estado.
     *
     * @param id the id of the video to get status.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the video status.
     */
    @GetMapping("/{id}/status")
    public Mono<ResponseEntity<VideoDTO>> getVideoStatus(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Video status : {}", id);

        return videoService
            .findOne(id)
            .map(videoDTO -> {
                LOG.debug("Video {} status: {}", id, videoDTO.getEstado());
                return ResponseEntity.ok().body(videoDTO);
            })
            .defaultIfEmpty(ResponseEntity.notFound().build());
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

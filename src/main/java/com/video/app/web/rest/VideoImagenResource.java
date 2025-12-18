package com.video.app.web.rest;

import com.video.app.repository.VideoImagenRepository;
import com.video.app.service.VideoImagenService;
import com.video.app.service.dto.VideoImagenDTO;
import com.video.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.video.app.domain.VideoImagen}.
 */
@RestController
@RequestMapping("/api/video-imagens")
public class VideoImagenResource {

    private static final Logger LOG = LoggerFactory.getLogger(VideoImagenResource.class);

    private static final String ENTITY_NAME = "videoImagen";

    @Value("${jhipster.clientApp.name:video}")
    private String applicationName;

    private final VideoImagenService videoImagenService;

    private final VideoImagenRepository videoImagenRepository;

    public VideoImagenResource(VideoImagenService videoImagenService, VideoImagenRepository videoImagenRepository) {
        this.videoImagenService = videoImagenService;
        this.videoImagenRepository = videoImagenRepository;
    }

    /**
     * {@code POST  /video-imagens} : Create a new videoImagen.
     *
     * @param videoImagenDTO the videoImagenDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new videoImagenDTO, or with status {@code 400 (Bad Request)} if the videoImagen has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<VideoImagenDTO>> createVideoImagen(@Valid @RequestBody VideoImagenDTO videoImagenDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save VideoImagen : {}", videoImagenDTO);
        if (videoImagenDTO.getId() != null) {
            throw new BadRequestAlertException("A new videoImagen cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return videoImagenService
            .save(videoImagenDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/video-imagens/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /video-imagens/:id} : Updates an existing videoImagen.
     *
     * @param id the id of the videoImagenDTO to save.
     * @param videoImagenDTO the videoImagenDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoImagenDTO,
     * or with status {@code 400 (Bad Request)} if the videoImagenDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the videoImagenDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<VideoImagenDTO>> updateVideoImagen(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VideoImagenDTO videoImagenDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update VideoImagen : {}, {}", id, videoImagenDTO);
        if (videoImagenDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoImagenDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoImagenRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return videoImagenService
                    .update(videoImagenDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /video-imagens/:id} : Partial updates given fields of an existing videoImagen, field will ignore if it is null
     *
     * @param id the id of the videoImagenDTO to save.
     * @param videoImagenDTO the videoImagenDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoImagenDTO,
     * or with status {@code 400 (Bad Request)} if the videoImagenDTO is not valid,
     * or with status {@code 404 (Not Found)} if the videoImagenDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the videoImagenDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<VideoImagenDTO>> partialUpdateVideoImagen(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VideoImagenDTO videoImagenDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VideoImagen partially : {}, {}", id, videoImagenDTO);
        if (videoImagenDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoImagenDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoImagenRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<VideoImagenDTO> result = videoImagenService.partialUpdate(videoImagenDTO);

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
     * {@code GET  /video-imagens} : get all the videoImagens.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of videoImagens in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<VideoImagenDTO>>> getAllVideoImagens(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get a page of VideoImagens");
        return videoImagenService
            .countAll()
            .zipWith(videoImagenService.findAll(pageable).collectList())
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
     * {@code GET  /video-imagens/:id} : get the "id" videoImagen.
     *
     * @param id the id of the videoImagenDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the videoImagenDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<VideoImagenDTO>> getVideoImagen(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VideoImagen : {}", id);
        Mono<VideoImagenDTO> videoImagenDTO = videoImagenService.findOne(id);
        return ResponseUtil.wrapOrNotFound(videoImagenDTO);
    }

    /**
     * {@code DELETE  /video-imagens/:id} : delete the "id" videoImagen.
     *
     * @param id the id of the videoImagenDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteVideoImagen(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VideoImagen : {}", id);
        return videoImagenService
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

package com.video.app.web.rest;

import com.video.app.repository.VideoCreditoRepository;
import com.video.app.service.VideoCreditoService;
import com.video.app.service.dto.VideoCreditoDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.video.app.domain.VideoCredito}.
 */
@RestController
@RequestMapping("/api/video-creditos")
public class VideoCreditoResource {

    private static final Logger LOG = LoggerFactory.getLogger(VideoCreditoResource.class);

    private static final String ENTITY_NAME = "videoCredito";

    @Value("${jhipster.clientApp.name:video}")
    private String applicationName;

    private final VideoCreditoService videoCreditoService;

    private final VideoCreditoRepository videoCreditoRepository;

    public VideoCreditoResource(VideoCreditoService videoCreditoService, VideoCreditoRepository videoCreditoRepository) {
        this.videoCreditoService = videoCreditoService;
        this.videoCreditoRepository = videoCreditoRepository;
    }

    /**
     * {@code POST  /video-creditos} : Create a new videoCredito.
     *
     * @param videoCreditoDTO the videoCreditoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new videoCreditoDTO, or with status {@code 400 (Bad Request)} if the videoCredito has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<VideoCreditoDTO>> createVideoCredito(@Valid @RequestBody VideoCreditoDTO videoCreditoDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save VideoCredito : {}", videoCreditoDTO);
        if (videoCreditoDTO.getId() != null) {
            throw new BadRequestAlertException("A new videoCredito cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return videoCreditoService
            .save(videoCreditoDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/video-creditos/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /video-creditos/:id} : Updates an existing videoCredito.
     *
     * @param id the id of the videoCreditoDTO to save.
     * @param videoCreditoDTO the videoCreditoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoCreditoDTO,
     * or with status {@code 400 (Bad Request)} if the videoCreditoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the videoCreditoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<VideoCreditoDTO>> updateVideoCredito(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VideoCreditoDTO videoCreditoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update VideoCredito : {}, {}", id, videoCreditoDTO);
        if (videoCreditoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoCreditoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoCreditoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return videoCreditoService
                    .update(videoCreditoDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /video-creditos/:id} : Partial updates given fields of an existing videoCredito, field will ignore if it is null
     *
     * @param id the id of the videoCreditoDTO to save.
     * @param videoCreditoDTO the videoCreditoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated videoCreditoDTO,
     * or with status {@code 400 (Bad Request)} if the videoCreditoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the videoCreditoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the videoCreditoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<VideoCreditoDTO>> partialUpdateVideoCredito(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VideoCreditoDTO videoCreditoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VideoCredito partially : {}, {}", id, videoCreditoDTO);
        if (videoCreditoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, videoCreditoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return videoCreditoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<VideoCreditoDTO> result = videoCreditoService.partialUpdate(videoCreditoDTO);

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
     * {@code GET  /video-creditos} : get all the videoCreditos.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of videoCreditos in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<VideoCreditoDTO>>> getAllVideoCreditos(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of VideoCreditos");
        return videoCreditoService
            .countAll()
            .zipWith(videoCreditoService.findAll(pageable).collectList())
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
     * {@code GET  /video-creditos/:id} : get the "id" videoCredito.
     *
     * @param id the id of the videoCreditoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the videoCreditoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<VideoCreditoDTO>> getVideoCredito(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VideoCredito : {}", id);
        Mono<VideoCreditoDTO> videoCreditoDTO = videoCreditoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(videoCreditoDTO);
    }

    /**
     * {@code DELETE  /video-creditos/:id} : delete the "id" videoCredito.
     *
     * @param id the id of the videoCreditoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteVideoCredito(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VideoCredito : {}", id);
        return videoCreditoService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }

    /**
     * {@code GET  /video-creditos/current-user} : get the videoCredito of the current authenticated user.
     *
     * @param jwt the JWT authentication principal containing user information
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the videoCreditoDTO,
     *         or with status {@code 404 (Not Found)} if videoCredito not found.
     */
    @GetMapping("/current-user")
    public Mono<ResponseEntity<VideoCreditoDTO>> getCurrentUserVideoCredito(@AuthenticationPrincipal Jwt jwt) {
        LOG.debug("REST request to get VideoCredito for current user");

        String currentUserLogin = jwt.getClaimAsString("sub");
        if (currentUserLogin == null || currentUserLogin.isEmpty()) {
            return Mono.error(new RuntimeException("No hay usuario autenticado"));
        }

        LOG.debug("Looking for VideoCredito for user: {}", currentUserLogin);

        return videoCreditoService
            .findByUserLogin(currentUserLogin)
            .map(videoCredito -> {
                LOG.debug("Found VideoCredito for user {}: {}", currentUserLogin, videoCredito);
                return ResponseEntity.ok(videoCredito);
            })
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

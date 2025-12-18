package com.video.app.service;

import com.video.app.service.dto.VideoImagenDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.video.app.domain.VideoImagen}.
 */
public interface VideoImagenService {
    /**
     * Save a videoImagen.
     *
     * @param videoImagenDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<VideoImagenDTO> save(VideoImagenDTO videoImagenDTO);

    /**
     * Updates a videoImagen.
     *
     * @param videoImagenDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<VideoImagenDTO> update(VideoImagenDTO videoImagenDTO);

    /**
     * Partially updates a videoImagen.
     *
     * @param videoImagenDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<VideoImagenDTO> partialUpdate(VideoImagenDTO videoImagenDTO);

    /**
     * Get all the videoImagens.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<VideoImagenDTO> findAll(Pageable pageable);

    /**
     * Returns the number of videoImagens available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" videoImagen.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<VideoImagenDTO> findOne(Long id);

    /**
     * Delete the "id" videoImagen.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}

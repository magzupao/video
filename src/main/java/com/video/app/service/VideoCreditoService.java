package com.video.app.service;

import com.video.app.service.dto.VideoCreditoDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.video.app.domain.VideoCredito}.
 */
public interface VideoCreditoService {
    /**
     * Save a videoCredito.
     *
     * @param videoCreditoDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<VideoCreditoDTO> save(VideoCreditoDTO videoCreditoDTO);

    /**
     * Updates a videoCredito.
     *
     * @param videoCreditoDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<VideoCreditoDTO> update(VideoCreditoDTO videoCreditoDTO);

    /**
     * Partially updates a videoCredito.
     *
     * @param videoCreditoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<VideoCreditoDTO> partialUpdate(VideoCreditoDTO videoCreditoDTO);

    /**
     * Get all the videoCreditos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<VideoCreditoDTO> findAll(Pageable pageable);

    /**
     * Get all the videoCreditos with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<VideoCreditoDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of videoCreditos available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" videoCredito.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<VideoCreditoDTO> findOne(Long id);

    /**
     * Delete the "id" videoCredito.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}

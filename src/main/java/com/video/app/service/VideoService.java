package com.video.app.service;

import com.video.app.service.dto.VideoDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.video.app.domain.Video}.
 */
public interface VideoService {
    /**
     * Save a video.
     *
     * @param videoDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<VideoDTO> save(VideoDTO videoDTO);

    /**
     * Updates a video.
     *
     * @param videoDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<VideoDTO> update(VideoDTO videoDTO);

    /**
     * Partially updates a video.
     *
     * @param videoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<VideoDTO> partialUpdate(VideoDTO videoDTO);

    /**
     * Get all the videos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<VideoDTO> findAll(Pageable pageable);

    /**
     * Get all the videos with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<VideoDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of videos available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" video.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<VideoDTO> findOne(Long id);

    /**
     * Delete the "id" video.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}

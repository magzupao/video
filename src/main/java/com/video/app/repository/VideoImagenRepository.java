package com.video.app.repository;

import com.video.app.domain.VideoImagen;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the VideoImagen entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VideoImagenRepository extends ReactiveCrudRepository<VideoImagen, Long>, VideoImagenRepositoryInternal {
    Flux<VideoImagen> findAllBy(Pageable pageable);

    @Query("SELECT * FROM video_imagen entity WHERE entity.video_id = :id")
    Flux<VideoImagen> findByVideo(Long id);

    @Query("SELECT * FROM video_imagen entity WHERE entity.video_id IS NULL")
    Flux<VideoImagen> findAllWhereVideoIsNull();

    @Override
    <S extends VideoImagen> Mono<S> save(S entity);

    @Override
    Flux<VideoImagen> findAll();

    @Override
    Mono<VideoImagen> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface VideoImagenRepositoryInternal {
    <S extends VideoImagen> Mono<S> save(S entity);

    Flux<VideoImagen> findAllBy(Pageable pageable);

    Flux<VideoImagen> findAll();

    Mono<VideoImagen> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<VideoImagen> findAllBy(Pageable pageable, Criteria criteria);
}

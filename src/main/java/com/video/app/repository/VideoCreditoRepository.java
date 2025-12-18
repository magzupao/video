package com.video.app.repository;

import com.video.app.domain.VideoCredito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the VideoCredito entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VideoCreditoRepository extends ReactiveCrudRepository<VideoCredito, Long>, VideoCreditoRepositoryInternal {
    Flux<VideoCredito> findAllBy(Pageable pageable);

    @Override
    Mono<VideoCredito> findOneWithEagerRelationships(Long id);

    @Override
    Flux<VideoCredito> findAllWithEagerRelationships();

    @Override
    Flux<VideoCredito> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM video_credito entity WHERE entity.user_id = :id")
    Flux<VideoCredito> findByUser(Long id);

    @Query("SELECT * FROM video_credito entity WHERE entity.user_id IS NULL")
    Flux<VideoCredito> findAllWhereUserIsNull();

    @Override
    <S extends VideoCredito> Mono<S> save(S entity);

    @Override
    Flux<VideoCredito> findAll();

    @Override
    Mono<VideoCredito> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface VideoCreditoRepositoryInternal {
    <S extends VideoCredito> Mono<S> save(S entity);

    Flux<VideoCredito> findAllBy(Pageable pageable);

    Flux<VideoCredito> findAll();

    Mono<VideoCredito> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<VideoCredito> findAllBy(Pageable pageable, Criteria criteria);

    Mono<VideoCredito> findOneWithEagerRelationships(Long id);

    Flux<VideoCredito> findAllWithEagerRelationships();

    Flux<VideoCredito> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}

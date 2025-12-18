package com.video.app.repository;

import com.video.app.domain.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Video entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VideoRepository extends ReactiveCrudRepository<Video, Long>, VideoRepositoryInternal {
    Flux<Video> findAllBy(Pageable pageable);

    @Override
    Mono<Video> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Video> findAllWithEagerRelationships();

    @Override
    Flux<Video> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM video entity WHERE entity.user_id = :id")
    Flux<Video> findByUser(Long id);

    @Query("SELECT * FROM video entity WHERE entity.user_id IS NULL")
    Flux<Video> findAllWhereUserIsNull();

    @Override
    <S extends Video> Mono<S> save(S entity);

    @Override
    Flux<Video> findAll();

    @Override
    Mono<Video> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface VideoRepositoryInternal {
    <S extends Video> Mono<S> save(S entity);

    Flux<Video> findAllBy(Pageable pageable);

    Flux<Video> findAll();

    Mono<Video> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Video> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Video> findOneWithEagerRelationships(Long id);

    Flux<Video> findAllWithEagerRelationships();

    Flux<Video> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}

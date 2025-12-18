package com.video.app.service.impl;

import com.video.app.repository.VideoRepository;
import com.video.app.service.VideoService;
import com.video.app.service.dto.VideoDTO;
import com.video.app.service.mapper.VideoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.video.app.domain.Video}.
 */
@Service
@Transactional
public class VideoServiceImpl implements VideoService {

    private static final Logger LOG = LoggerFactory.getLogger(VideoServiceImpl.class);

    private final VideoRepository videoRepository;

    private final VideoMapper videoMapper;

    public VideoServiceImpl(VideoRepository videoRepository, VideoMapper videoMapper) {
        this.videoRepository = videoRepository;
        this.videoMapper = videoMapper;
    }

    @Override
    public Mono<VideoDTO> save(VideoDTO videoDTO) {
        LOG.debug("Request to save Video : {}", videoDTO);
        return videoRepository.save(videoMapper.toEntity(videoDTO)).map(videoMapper::toDto);
    }

    @Override
    public Mono<VideoDTO> update(VideoDTO videoDTO) {
        LOG.debug("Request to update Video : {}", videoDTO);
        return videoRepository.save(videoMapper.toEntity(videoDTO)).map(videoMapper::toDto);
    }

    @Override
    public Mono<VideoDTO> partialUpdate(VideoDTO videoDTO) {
        LOG.debug("Request to partially update Video : {}", videoDTO);

        return videoRepository
            .findById(videoDTO.getId())
            .map(existingVideo -> {
                videoMapper.partialUpdate(existingVideo, videoDTO);

                return existingVideo;
            })
            .flatMap(videoRepository::save)
            .map(videoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<VideoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Videos");
        return videoRepository.findAllBy(pageable).map(videoMapper::toDto);
    }

    public Flux<VideoDTO> findAllWithEagerRelationships(Pageable pageable) {
        return videoRepository.findAllWithEagerRelationships(pageable).map(videoMapper::toDto);
    }

    public Mono<Long> countAll() {
        return videoRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<VideoDTO> findOne(Long id) {
        LOG.debug("Request to get Video : {}", id);
        return videoRepository.findOneWithEagerRelationships(id).map(videoMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Video : {}", id);
        return videoRepository.deleteById(id);
    }
}

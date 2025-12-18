package com.video.app.service.impl;

import com.video.app.repository.VideoImagenRepository;
import com.video.app.service.VideoImagenService;
import com.video.app.service.dto.VideoImagenDTO;
import com.video.app.service.mapper.VideoImagenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.video.app.domain.VideoImagen}.
 */
@Service
@Transactional
public class VideoImagenServiceImpl implements VideoImagenService {

    private static final Logger LOG = LoggerFactory.getLogger(VideoImagenServiceImpl.class);

    private final VideoImagenRepository videoImagenRepository;

    private final VideoImagenMapper videoImagenMapper;

    public VideoImagenServiceImpl(VideoImagenRepository videoImagenRepository, VideoImagenMapper videoImagenMapper) {
        this.videoImagenRepository = videoImagenRepository;
        this.videoImagenMapper = videoImagenMapper;
    }

    @Override
    public Mono<VideoImagenDTO> save(VideoImagenDTO videoImagenDTO) {
        LOG.debug("Request to save VideoImagen : {}", videoImagenDTO);
        return videoImagenRepository.save(videoImagenMapper.toEntity(videoImagenDTO)).map(videoImagenMapper::toDto);
    }

    @Override
    public Mono<VideoImagenDTO> update(VideoImagenDTO videoImagenDTO) {
        LOG.debug("Request to update VideoImagen : {}", videoImagenDTO);
        return videoImagenRepository.save(videoImagenMapper.toEntity(videoImagenDTO)).map(videoImagenMapper::toDto);
    }

    @Override
    public Mono<VideoImagenDTO> partialUpdate(VideoImagenDTO videoImagenDTO) {
        LOG.debug("Request to partially update VideoImagen : {}", videoImagenDTO);

        return videoImagenRepository
            .findById(videoImagenDTO.getId())
            .map(existingVideoImagen -> {
                videoImagenMapper.partialUpdate(existingVideoImagen, videoImagenDTO);

                return existingVideoImagen;
            })
            .flatMap(videoImagenRepository::save)
            .map(videoImagenMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<VideoImagenDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all VideoImagens");
        return videoImagenRepository.findAllBy(pageable).map(videoImagenMapper::toDto);
    }

    public Mono<Long> countAll() {
        return videoImagenRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<VideoImagenDTO> findOne(Long id) {
        LOG.debug("Request to get VideoImagen : {}", id);
        return videoImagenRepository.findById(id).map(videoImagenMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete VideoImagen : {}", id);
        return videoImagenRepository.deleteById(id);
    }
}

package com.video.app.service.impl;

import com.video.app.repository.VideoCreditoRepository;
import com.video.app.service.VideoCreditoService;
import com.video.app.service.dto.VideoCreditoDTO;
import com.video.app.service.mapper.VideoCreditoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.video.app.domain.VideoCredito}.
 */
@Service
@Transactional
public class VideoCreditoServiceImpl implements VideoCreditoService {

    private static final Logger LOG = LoggerFactory.getLogger(VideoCreditoServiceImpl.class);

    private final VideoCreditoRepository videoCreditoRepository;

    private final VideoCreditoMapper videoCreditoMapper;

    public VideoCreditoServiceImpl(VideoCreditoRepository videoCreditoRepository, VideoCreditoMapper videoCreditoMapper) {
        this.videoCreditoRepository = videoCreditoRepository;
        this.videoCreditoMapper = videoCreditoMapper;
    }

    @Override
    public Mono<VideoCreditoDTO> save(VideoCreditoDTO videoCreditoDTO) {
        LOG.debug("Request to save VideoCredito : {}", videoCreditoDTO);
        return videoCreditoRepository.save(videoCreditoMapper.toEntity(videoCreditoDTO)).map(videoCreditoMapper::toDto);
    }

    @Override
    public Mono<VideoCreditoDTO> update(VideoCreditoDTO videoCreditoDTO) {
        LOG.debug("Request to update VideoCredito : {}", videoCreditoDTO);
        return videoCreditoRepository.save(videoCreditoMapper.toEntity(videoCreditoDTO)).map(videoCreditoMapper::toDto);
    }

    @Override
    public Mono<VideoCreditoDTO> partialUpdate(VideoCreditoDTO videoCreditoDTO) {
        LOG.debug("Request to partially update VideoCredito : {}", videoCreditoDTO);

        return videoCreditoRepository
            .findById(videoCreditoDTO.getId())
            .map(existingVideoCredito -> {
                videoCreditoMapper.partialUpdate(existingVideoCredito, videoCreditoDTO);

                return existingVideoCredito;
            })
            .flatMap(videoCreditoRepository::save)
            .map(videoCreditoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<VideoCreditoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all VideoCreditos");
        return videoCreditoRepository.findAllBy(pageable).map(videoCreditoMapper::toDto);
    }

    public Flux<VideoCreditoDTO> findAllWithEagerRelationships(Pageable pageable) {
        return videoCreditoRepository.findAllWithEagerRelationships(pageable).map(videoCreditoMapper::toDto);
    }

    public Mono<Long> countAll() {
        return videoCreditoRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<VideoCreditoDTO> findOne(Long id) {
        LOG.debug("Request to get VideoCredito : {}", id);
        return videoCreditoRepository.findOneWithEagerRelationships(id).map(videoCreditoMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete VideoCredito : {}", id);
        return videoCreditoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<VideoCreditoDTO> findByUserLogin(String login) {
        LOG.debug("Request to get VideoCredito by user login : {}", login);
        return videoCreditoRepository.findByUserLogin(login).map(videoCreditoMapper::toDto);
    }

    @Override
    public Mono<VideoCreditoDTO> incrementarVideosConsumidos(Long userId) {
        LOG.debug("Request to increment videos consumidos for user : {}", userId);

        return videoCreditoRepository
            .incrementarVideosConsumidosByUserId(userId)
            .map(videoCreditoMapper::toDto)
            .doOnSuccess(dto ->
                LOG.info(
                    "✅ Créditos incrementados para user {}: consumidos={}, disponibles={}",
                    userId,
                    dto.getVideosConsumidos(),
                    dto.getVideosDisponibles()
                )
            )
            .doOnError(error -> LOG.error("❌ Error incrementando créditos para user {}: {}", userId, error.getMessage()));
    }
}

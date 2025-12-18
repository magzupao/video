package com.video.app.service.mapper;

import static com.video.app.domain.VideoCreditoAsserts.*;
import static com.video.app.domain.VideoCreditoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VideoCreditoMapperTest {

    private VideoCreditoMapper videoCreditoMapper;

    @BeforeEach
    void setUp() {
        videoCreditoMapper = new VideoCreditoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVideoCreditoSample1();
        var actual = videoCreditoMapper.toEntity(videoCreditoMapper.toDto(expected));
        assertVideoCreditoAllPropertiesEquals(expected, actual);
    }
}

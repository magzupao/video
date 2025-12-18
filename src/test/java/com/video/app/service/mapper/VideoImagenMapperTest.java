package com.video.app.service.mapper;

import static com.video.app.domain.VideoImagenAsserts.*;
import static com.video.app.domain.VideoImagenTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VideoImagenMapperTest {

    private VideoImagenMapper videoImagenMapper;

    @BeforeEach
    void setUp() {
        videoImagenMapper = new VideoImagenMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVideoImagenSample1();
        var actual = videoImagenMapper.toEntity(videoImagenMapper.toDto(expected));
        assertVideoImagenAllPropertiesEquals(expected, actual);
    }
}

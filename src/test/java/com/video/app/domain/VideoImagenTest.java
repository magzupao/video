package com.video.app.domain;

import static com.video.app.domain.VideoImagenTestSamples.*;
import static com.video.app.domain.VideoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.video.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VideoImagenTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VideoImagen.class);
        VideoImagen videoImagen1 = getVideoImagenSample1();
        VideoImagen videoImagen2 = new VideoImagen();
        assertThat(videoImagen1).isNotEqualTo(videoImagen2);

        videoImagen2.setId(videoImagen1.getId());
        assertThat(videoImagen1).isEqualTo(videoImagen2);

        videoImagen2 = getVideoImagenSample2();
        assertThat(videoImagen1).isNotEqualTo(videoImagen2);
    }

    @Test
    void videoTest() {
        VideoImagen videoImagen = getVideoImagenRandomSampleGenerator();
        Video videoBack = getVideoRandomSampleGenerator();

        videoImagen.setVideo(videoBack);
        assertThat(videoImagen.getVideo()).isEqualTo(videoBack);

        videoImagen.video(null);
        assertThat(videoImagen.getVideo()).isNull();
    }
}

package com.video.app.domain;

import static com.video.app.domain.VideoImagenTestSamples.*;
import static com.video.app.domain.VideoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.video.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VideoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Video.class);
        Video video1 = getVideoSample1();
        Video video2 = new Video();
        assertThat(video1).isNotEqualTo(video2);

        video2.setId(video1.getId());
        assertThat(video1).isEqualTo(video2);

        video2 = getVideoSample2();
        assertThat(video1).isNotEqualTo(video2);
    }

    @Test
    void imagenesTest() {
        Video video = getVideoRandomSampleGenerator();
        VideoImagen videoImagenBack = getVideoImagenRandomSampleGenerator();

        video.addImagenes(videoImagenBack);
        assertThat(video.getImagenes()).containsOnly(videoImagenBack);
        assertThat(videoImagenBack.getVideo()).isEqualTo(video);

        video.removeImagenes(videoImagenBack);
        assertThat(video.getImagenes()).doesNotContain(videoImagenBack);
        assertThat(videoImagenBack.getVideo()).isNull();

        video.imagenes(new HashSet<>(Set.of(videoImagenBack)));
        assertThat(video.getImagenes()).containsOnly(videoImagenBack);
        assertThat(videoImagenBack.getVideo()).isEqualTo(video);

        video.setImagenes(new HashSet<>());
        assertThat(video.getImagenes()).doesNotContain(videoImagenBack);
        assertThat(videoImagenBack.getVideo()).isNull();
    }
}

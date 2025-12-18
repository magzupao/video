package com.video.app.domain;

import static com.video.app.domain.VideoCreditoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.video.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VideoCreditoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VideoCredito.class);
        VideoCredito videoCredito1 = getVideoCreditoSample1();
        VideoCredito videoCredito2 = new VideoCredito();
        assertThat(videoCredito1).isNotEqualTo(videoCredito2);

        videoCredito2.setId(videoCredito1.getId());
        assertThat(videoCredito1).isEqualTo(videoCredito2);

        videoCredito2 = getVideoCreditoSample2();
        assertThat(videoCredito1).isNotEqualTo(videoCredito2);
    }
}

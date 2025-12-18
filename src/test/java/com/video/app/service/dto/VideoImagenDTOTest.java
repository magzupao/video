package com.video.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.video.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VideoImagenDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VideoImagenDTO.class);
        VideoImagenDTO videoImagenDTO1 = new VideoImagenDTO();
        videoImagenDTO1.setId(1L);
        VideoImagenDTO videoImagenDTO2 = new VideoImagenDTO();
        assertThat(videoImagenDTO1).isNotEqualTo(videoImagenDTO2);
        videoImagenDTO2.setId(videoImagenDTO1.getId());
        assertThat(videoImagenDTO1).isEqualTo(videoImagenDTO2);
        videoImagenDTO2.setId(2L);
        assertThat(videoImagenDTO1).isNotEqualTo(videoImagenDTO2);
        videoImagenDTO1.setId(null);
        assertThat(videoImagenDTO1).isNotEqualTo(videoImagenDTO2);
    }
}

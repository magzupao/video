package com.video.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.video.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VideoCreditoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VideoCreditoDTO.class);
        VideoCreditoDTO videoCreditoDTO1 = new VideoCreditoDTO();
        videoCreditoDTO1.setId(1L);
        VideoCreditoDTO videoCreditoDTO2 = new VideoCreditoDTO();
        assertThat(videoCreditoDTO1).isNotEqualTo(videoCreditoDTO2);
        videoCreditoDTO2.setId(videoCreditoDTO1.getId());
        assertThat(videoCreditoDTO1).isEqualTo(videoCreditoDTO2);
        videoCreditoDTO2.setId(2L);
        assertThat(videoCreditoDTO1).isNotEqualTo(videoCreditoDTO2);
        videoCreditoDTO1.setId(null);
        assertThat(videoCreditoDTO1).isNotEqualTo(videoCreditoDTO2);
    }
}

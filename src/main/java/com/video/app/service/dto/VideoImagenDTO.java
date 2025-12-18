package com.video.app.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.video.app.domain.VideoImagen} entity.
 */
@Schema(description = "Imágenes que componen el video\nMáximo 10 imágenes por video")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VideoImagenDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    @Size(max = 255)
    private String filename;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    @Max(value = 9)
    private Integer orden;

    private Integer duracionIndividual;

    @NotNull
    private VideoDTO video;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getDuracionIndividual() {
        return duracionIndividual;
    }

    public void setDuracionIndividual(Integer duracionIndividual) {
        this.duracionIndividual = duracionIndividual;
    }

    public VideoDTO getVideo() {
        return video;
    }

    public void setVideo(VideoDTO video) {
        this.video = video;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VideoImagenDTO)) {
            return false;
        }

        VideoImagenDTO videoImagenDTO = (VideoImagenDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, videoImagenDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VideoImagenDTO{" +
            "id=" + getId() +
            ", filename='" + getFilename() + "'" +
            ", orden=" + getOrden() +
            ", duracionIndividual=" + getDuracionIndividual() +
            ", video=" + getVideo() +
            "}";
    }
}

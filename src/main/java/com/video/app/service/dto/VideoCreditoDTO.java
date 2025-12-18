package com.video.app.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.video.app.domain.VideoCredito} entity.
 */
@Schema(description = "Créditos del usuario para generar videos\nSe crea automáticamente al registrarse con 1 crédito disponible")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VideoCreditoDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    private Integer videosConsumidos;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    private Integer videosDisponibles;

    @NotNull
    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVideosConsumidos() {
        return videosConsumidos;
    }

    public void setVideosConsumidos(Integer videosConsumidos) {
        this.videosConsumidos = videosConsumidos;
    }

    public Integer getVideosDisponibles() {
        return videosDisponibles;
    }

    public void setVideosDisponibles(Integer videosDisponibles) {
        this.videosDisponibles = videosDisponibles;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VideoCreditoDTO)) {
            return false;
        }

        VideoCreditoDTO videoCreditoDTO = (VideoCreditoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, videoCreditoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VideoCreditoDTO{" +
            "id=" + getId() +
            ", videosConsumidos=" + getVideosConsumidos() +
            ", videosDisponibles=" + getVideosDisponibles() +
            ", user=" + getUser() +
            "}";
    }
}

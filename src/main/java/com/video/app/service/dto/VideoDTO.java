package com.video.app.service.dto;

import com.video.app.domain.enumeration.EstadoVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.beans.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.video.app.domain.Video} entity.
 */
@Schema(description = "Video generado por el usuario\nPuede contener hasta 10 imágenes y opcionalmente audio")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VideoDTO implements Serializable {

    private Long id;

    @Size(max = 255)
    private String titulo;

    @Size(max = 255)
    private String audioFilename;

    private Boolean tieneAudio;

    private Integer duracionTransicion;

    private EstadoVideo estado;

    private Instant fechaCreacion;

    private Instant fechaDescarga;

    private UserDTO user;

    private String formato;

    private String videoPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAudioFilename() {
        return audioFilename;
    }

    public void setAudioFilename(String audioFilename) {
        this.audioFilename = audioFilename;
    }

    public Boolean getTieneAudio() {
        return tieneAudio;
    }

    public void setTieneAudio(Boolean tieneAudio) {
        this.tieneAudio = tieneAudio;
    }

    public Integer getDuracionTransicion() {
        return duracionTransicion;
    }

    public void setDuracionTransicion(Integer duracionTransicion) {
        this.duracionTransicion = duracionTransicion;
    }

    public EstadoVideo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVideo estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaDescarga() {
        return fechaDescarga;
    }

    public void setFechaDescarga(Instant fechaDescarga) {
        this.fechaDescarga = fechaDescarga;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VideoDTO)) {
            return false;
        }

        VideoDTO videoDTO = (VideoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, videoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VideoDTO{" +
            "id=" + getId() +
            ", titulo='" + getTitulo() + "'" +
            ", audioFilename='" + getAudioFilename() + "'" +
            ", tieneAudio='" + getTieneAudio() + "'" +
            ", duracionTransicion=" + getDuracionTransicion() +
            ", estado='" + getEstado() + "'" +
            ", fechaCreacion='" + getFechaCreacion() + "'" +
            ", fechaDescarga='" + getFechaDescarga() + "'" +
            ", user=" + getUser() +
            ", formato='" + getFormato() + "'" +
            ", videoPath='" + getVideoPath() + "'" +
            "}";
    }
}

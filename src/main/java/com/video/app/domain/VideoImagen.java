package com.video.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Imágenes que componen el video
 * Máximo 10 imágenes por video
 */
@Table("video_imagen")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VideoImagen implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Size(max = 255)
    @Column("filename")
    private String filename;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    @Max(value = 9)
    @Column("orden")
    private Integer orden;

    @Column("duracion_individual")
    private Integer duracionIndividual;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "imagenes", "user" }, allowSetters = true)
    private Video video;

    @Column("video_id")
    private Long videoId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VideoImagen id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return this.filename;
    }

    public VideoImagen filename(String filename) {
        this.setFilename(filename);
        return this;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getOrden() {
        return this.orden;
    }

    public VideoImagen orden(Integer orden) {
        this.setOrden(orden);
        return this;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getDuracionIndividual() {
        return this.duracionIndividual;
    }

    public VideoImagen duracionIndividual(Integer duracionIndividual) {
        this.setDuracionIndividual(duracionIndividual);
        return this;
    }

    public void setDuracionIndividual(Integer duracionIndividual) {
        this.duracionIndividual = duracionIndividual;
    }

    public Video getVideo() {
        return this.video;
    }

    public void setVideo(Video video) {
        this.video = video;
        this.videoId = video != null ? video.getId() : null;
    }

    public VideoImagen video(Video video) {
        this.setVideo(video);
        return this;
    }

    public Long getVideoId() {
        return this.videoId;
    }

    public void setVideoId(Long video) {
        this.videoId = video;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VideoImagen)) {
            return false;
        }
        return getId() != null && getId().equals(((VideoImagen) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VideoImagen{" +
            "id=" + getId() +
            ", filename='" + getFilename() + "'" +
            ", orden=" + getOrden() +
            ", duracionIndividual=" + getDuracionIndividual() +
            "}";
    }
}

package com.video.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.video.app.domain.enumeration.EstadoVideo;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Video generado por el usuario
 * Puede contener hasta 10 imágenes y opcionalmente audio
 */
@Table("video")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Video implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Size(max = 255)
    @Column("titulo")
    private String titulo;

    @Size(max = 255)
    @Column("audio_filename")
    private String audioFilename;

    @Column("tiene_audio")
    private Boolean tieneAudio;

    @Column("duracion_transicion")
    private Integer duracionTransicion;

    @Column("estado")
    private EstadoVideo estado;

    @Column("fecha_creacion")
    private Instant fechaCreacion;

    @Column("fecha_descarga")
    private Instant fechaDescarga;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "video" }, allowSetters = true)
    private Set<VideoImagen> imagenes = new HashSet<>();

    @org.springframework.data.annotation.Transient
    private User user;

    @Column("user_id")
    private Long userId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Video id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public Video titulo(String titulo) {
        this.setTitulo(titulo);
        return this;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAudioFilename() {
        return this.audioFilename;
    }

    public Video audioFilename(String audioFilename) {
        this.setAudioFilename(audioFilename);
        return this;
    }

    public void setAudioFilename(String audioFilename) {
        this.audioFilename = audioFilename;
    }

    public Boolean getTieneAudio() {
        return this.tieneAudio;
    }

    public Video tieneAudio(Boolean tieneAudio) {
        this.setTieneAudio(tieneAudio);
        return this;
    }

    public void setTieneAudio(Boolean tieneAudio) {
        this.tieneAudio = tieneAudio;
    }

    public Integer getDuracionTransicion() {
        return this.duracionTransicion;
    }

    public Video duracionTransicion(Integer duracionTransicion) {
        this.setDuracionTransicion(duracionTransicion);
        return this;
    }

    public void setDuracionTransicion(Integer duracionTransicion) {
        this.duracionTransicion = duracionTransicion;
    }

    public EstadoVideo getEstado() {
        return this.estado;
    }

    public Video estado(EstadoVideo estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoVideo estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return this.fechaCreacion;
    }

    public Video fechaCreacion(Instant fechaCreacion) {
        this.setFechaCreacion(fechaCreacion);
        return this;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaDescarga() {
        return this.fechaDescarga;
    }

    public Video fechaDescarga(Instant fechaDescarga) {
        this.setFechaDescarga(fechaDescarga);
        return this;
    }

    public void setFechaDescarga(Instant fechaDescarga) {
        this.fechaDescarga = fechaDescarga;
    }

    public Set<VideoImagen> getImagenes() {
        return this.imagenes;
    }

    public void setImagenes(Set<VideoImagen> videoImagens) {
        if (this.imagenes != null) {
            this.imagenes.forEach(i -> i.setVideo(null));
        }
        if (videoImagens != null) {
            videoImagens.forEach(i -> i.setVideo(this));
        }
        this.imagenes = videoImagens;
    }

    public Video imagenes(Set<VideoImagen> videoImagens) {
        this.setImagenes(videoImagens);
        return this;
    }

    public Video addImagenes(VideoImagen videoImagen) {
        this.imagenes.add(videoImagen);
        videoImagen.setVideo(this);
        return this;
    }

    public Video removeImagenes(VideoImagen videoImagen) {
        this.imagenes.remove(videoImagen);
        videoImagen.setVideo(null);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public Video user(User user) {
        this.setUser(user);
        return this;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long user) {
        this.userId = user;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Video)) {
            return false;
        }
        return getId() != null && getId().equals(((Video) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Video{" +
            "id=" + getId() +
            ", titulo='" + getTitulo() + "'" +
            ", audioFilename='" + getAudioFilename() + "'" +
            ", tieneAudio='" + getTieneAudio() + "'" +
            ", duracionTransicion=" + getDuracionTransicion() +
            ", estado='" + getEstado() + "'" +
            ", fechaCreacion='" + getFechaCreacion() + "'" +
            ", fechaDescarga='" + getFechaDescarga() + "'" +
            "}";
    }
}

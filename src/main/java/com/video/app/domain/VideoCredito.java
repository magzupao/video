package com.video.app.domain;

import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Créditos del usuario para generar videos
 * Se crea automáticamente al registrarse con 1 crédito disponible
 */
@Table("video_credito")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VideoCredito implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    @Column("videos_consumidos")
    private Integer videosConsumidos;

    @NotNull(message = "must not be null")
    @Min(value = 0)
    @Column("videos_disponibles")
    private Integer videosDisponibles;

    @org.springframework.data.annotation.Transient
    private User user;

    @Column("user_id")
    private Long userId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VideoCredito id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVideosConsumidos() {
        return this.videosConsumidos;
    }

    public VideoCredito videosConsumidos(Integer videosConsumidos) {
        this.setVideosConsumidos(videosConsumidos);
        return this;
    }

    public void setVideosConsumidos(Integer videosConsumidos) {
        this.videosConsumidos = videosConsumidos;
    }

    public Integer getVideosDisponibles() {
        return this.videosDisponibles;
    }

    public VideoCredito videosDisponibles(Integer videosDisponibles) {
        this.setVideosDisponibles(videosDisponibles);
        return this;
    }

    public void setVideosDisponibles(Integer videosDisponibles) {
        this.videosDisponibles = videosDisponibles;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public VideoCredito user(User user) {
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
        if (!(o instanceof VideoCredito)) {
            return false;
        }
        return getId() != null && getId().equals(((VideoCredito) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VideoCredito{" +
            "id=" + getId() +
            ", videosConsumidos=" + getVideosConsumidos() +
            ", videosDisponibles=" + getVideosDisponibles() +
            "}";
    }
}

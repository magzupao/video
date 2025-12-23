package com.video.app.repository.rowmapper;

import com.video.app.domain.Video;
import com.video.app.domain.enumeration.EstadoVideo;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Video}, with proper type conversions.
 */
@Service
public class VideoRowMapper implements BiFunction<Row, String, Video> {

    private final ColumnConverter converter;

    public VideoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Video} stored in the database.
     */
    @Override
    public Video apply(Row row, String prefix) {
        Video entity = new Video();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setTitulo(converter.fromRow(row, prefix + "_titulo", String.class));
        entity.setAudioFilename(converter.fromRow(row, prefix + "_audio_filename", String.class));
        entity.setTieneAudio(converter.fromRow(row, prefix + "_tiene_audio", Boolean.class));
        entity.setDuracionTransicion(converter.fromRow(row, prefix + "_duracion_transicion", Integer.class));
        entity.setEstado(converter.fromRow(row, prefix + "_estado", EstadoVideo.class));
        entity.setFechaCreacion(converter.fromRow(row, prefix + "_fecha_creacion", Instant.class));
        entity.setFechaDescarga(converter.fromRow(row, prefix + "_fecha_descarga", Instant.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));

        // ✅ AGREGAR ESTAS 2 LÍNEAS:
        entity.setOutputFilename(converter.fromRow(row, prefix + "_output_filename", String.class));
        entity.setDownloadUrl(converter.fromRow(row, prefix + "_download_url", String.class));

        return entity;
    }
}

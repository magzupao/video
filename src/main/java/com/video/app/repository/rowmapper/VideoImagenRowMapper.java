package com.video.app.repository.rowmapper;

import com.video.app.domain.VideoImagen;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link VideoImagen}, with proper type conversions.
 */
@Service
public class VideoImagenRowMapper implements BiFunction<Row, String, VideoImagen> {

    private final ColumnConverter converter;

    public VideoImagenRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link VideoImagen} stored in the database.
     */
    @Override
    public VideoImagen apply(Row row, String prefix) {
        VideoImagen entity = new VideoImagen();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setFilename(converter.fromRow(row, prefix + "_filename", String.class));
        entity.setOrden(converter.fromRow(row, prefix + "_orden", Integer.class));
        entity.setDuracionIndividual(converter.fromRow(row, prefix + "_duracion_individual", Integer.class));
        entity.setVideoId(converter.fromRow(row, prefix + "_video_id", Long.class));
        return entity;
    }
}

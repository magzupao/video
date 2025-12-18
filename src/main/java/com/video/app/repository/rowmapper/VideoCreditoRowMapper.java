package com.video.app.repository.rowmapper;

import com.video.app.domain.VideoCredito;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link VideoCredito}, with proper type conversions.
 */
@Service
public class VideoCreditoRowMapper implements BiFunction<Row, String, VideoCredito> {

    private final ColumnConverter converter;

    public VideoCreditoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link VideoCredito} stored in the database.
     */
    @Override
    public VideoCredito apply(Row row, String prefix) {
        VideoCredito entity = new VideoCredito();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setVideosConsumidos(converter.fromRow(row, prefix + "_videos_consumidos", Integer.class));
        entity.setVideosDisponibles(converter.fromRow(row, prefix + "_videos_disponibles", Integer.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));
        return entity;
    }
}

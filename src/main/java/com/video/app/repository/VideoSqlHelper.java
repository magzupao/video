package com.video.app.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class VideoSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("titulo", table, columnPrefix + "_titulo"));
        columns.add(Column.aliased("audio_filename", table, columnPrefix + "_audio_filename"));
        columns.add(Column.aliased("tiene_audio", table, columnPrefix + "_tiene_audio"));
        columns.add(Column.aliased("duracion_transicion", table, columnPrefix + "_duracion_transicion"));
        columns.add(Column.aliased("estado", table, columnPrefix + "_estado"));
        columns.add(Column.aliased("fecha_creacion", table, columnPrefix + "_fecha_creacion"));
        columns.add(Column.aliased("fecha_descarga", table, columnPrefix + "_fecha_descarga"));
        columns.add(Column.aliased("user_id", table, columnPrefix + "_user_id"));

        // ✅ AGREGAR ESTAS 2 LÍNEAS:
        columns.add(Column.aliased("output_filename", table, columnPrefix + "_output_filename"));
        columns.add(Column.aliased("download_url", table, columnPrefix + "_download_url"));

        return columns;
    }
}

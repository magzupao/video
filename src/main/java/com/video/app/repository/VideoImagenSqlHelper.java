package com.video.app.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class VideoImagenSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("filename", table, columnPrefix + "_filename"));
        columns.add(Column.aliased("orden", table, columnPrefix + "_orden"));
        columns.add(Column.aliased("duracion_individual", table, columnPrefix + "_duracion_individual"));

        columns.add(Column.aliased("video_id", table, columnPrefix + "_video_id"));
        return columns;
    }
}

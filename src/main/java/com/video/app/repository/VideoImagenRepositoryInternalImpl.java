package com.video.app.repository;

import com.video.app.domain.VideoImagen;
import com.video.app.repository.rowmapper.VideoImagenRowMapper;
import com.video.app.repository.rowmapper.VideoRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the VideoImagen entity.
 */
@SuppressWarnings("unused")
class VideoImagenRepositoryInternalImpl extends SimpleR2dbcRepository<VideoImagen, Long> implements VideoImagenRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final VideoRowMapper videoMapper;
    private final VideoImagenRowMapper videoimagenMapper;

    private static final Table entityTable = Table.aliased("video_imagen", EntityManager.ENTITY_ALIAS);
    private static final Table videoTable = Table.aliased("video", "video");

    public VideoImagenRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        VideoRowMapper videoMapper,
        VideoImagenRowMapper videoimagenMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(VideoImagen.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.videoMapper = videoMapper;
        this.videoimagenMapper = videoimagenMapper;
    }

    @Override
    public Flux<VideoImagen> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<VideoImagen> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = VideoImagenSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(VideoSqlHelper.getColumns(videoTable, "video"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(videoTable)
            .on(Column.create("video_id", entityTable))
            .equals(Column.create("id", videoTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, VideoImagen.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<VideoImagen> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<VideoImagen> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private VideoImagen process(Row row, RowMetadata metadata) {
        VideoImagen entity = videoimagenMapper.apply(row, "e");
        entity.setVideo(videoMapper.apply(row, "video"));
        return entity;
    }

    @Override
    public <S extends VideoImagen> Mono<S> save(S entity) {
        return super.save(entity);
    }
}

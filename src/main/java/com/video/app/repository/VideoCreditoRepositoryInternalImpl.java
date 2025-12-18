package com.video.app.repository;

import com.video.app.domain.VideoCredito;
import com.video.app.repository.rowmapper.UserRowMapper;
import com.video.app.repository.rowmapper.VideoCreditoRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the VideoCredito entity.
 */
@SuppressWarnings("unused")
class VideoCreditoRepositoryInternalImpl extends SimpleR2dbcRepository<VideoCredito, Long> implements VideoCreditoRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final UserRowMapper userMapper;
    private final VideoCreditoRowMapper videocreditoMapper;

    private static final Table entityTable = Table.aliased("video_credito", EntityManager.ENTITY_ALIAS);
    private static final Table userTable = Table.aliased("jhi_user", "e_user");

    public VideoCreditoRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        UserRowMapper userMapper,
        VideoCreditoRowMapper videocreditoMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(VideoCredito.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.userMapper = userMapper;
        this.videocreditoMapper = videocreditoMapper;
    }

    @Override
    public Flux<VideoCredito> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<VideoCredito> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = VideoCreditoSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(UserSqlHelper.getColumns(userTable, "user"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(userTable)
            .on(Column.create("user_id", entityTable))
            .equals(Column.create("id", userTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, VideoCredito.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<VideoCredito> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<VideoCredito> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<VideoCredito> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<VideoCredito> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<VideoCredito> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private VideoCredito process(Row row, RowMetadata metadata) {
        VideoCredito entity = videocreditoMapper.apply(row, "e");
        entity.setUser(userMapper.apply(row, "user"));
        return entity;
    }

    @Override
    public <S extends VideoCredito> Mono<S> save(S entity) {
        return super.save(entity);
    }
}

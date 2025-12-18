package com.video.app.web.rest;

import static com.video.app.domain.VideoCreditoAsserts.*;
import static com.video.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.video.app.IntegrationTest;
import com.video.app.domain.User;
import com.video.app.domain.VideoCredito;
import com.video.app.repository.EntityManager;
import com.video.app.repository.UserRepository;
import com.video.app.repository.VideoCreditoRepository;
import com.video.app.service.VideoCreditoService;
import com.video.app.service.dto.VideoCreditoDTO;
import com.video.app.service.mapper.VideoCreditoMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link VideoCreditoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class VideoCreditoResourceIT {

    private static final Integer DEFAULT_VIDEOS_CONSUMIDOS = 0;
    private static final Integer UPDATED_VIDEOS_CONSUMIDOS = 1;

    private static final Integer DEFAULT_VIDEOS_DISPONIBLES = 0;
    private static final Integer UPDATED_VIDEOS_DISPONIBLES = 1;

    private static final String ENTITY_API_URL = "/api/video-creditos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VideoCreditoRepository videoCreditoRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private VideoCreditoRepository videoCreditoRepositoryMock;

    @Autowired
    private VideoCreditoMapper videoCreditoMapper;

    @Mock
    private VideoCreditoService videoCreditoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private VideoCredito videoCredito;

    private VideoCredito insertedVideoCredito;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VideoCredito createEntity(EntityManager em) {
        VideoCredito videoCredito = new VideoCredito()
            .videosConsumidos(DEFAULT_VIDEOS_CONSUMIDOS)
            .videosDisponibles(DEFAULT_VIDEOS_DISPONIBLES);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        videoCredito.setUser(user);
        return videoCredito;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VideoCredito createUpdatedEntity(EntityManager em) {
        VideoCredito updatedVideoCredito = new VideoCredito()
            .videosConsumidos(UPDATED_VIDEOS_CONSUMIDOS)
            .videosDisponibles(UPDATED_VIDEOS_DISPONIBLES);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        updatedVideoCredito.setUser(user);
        return updatedVideoCredito;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(VideoCredito.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        UserResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void initTest() {
        videoCredito = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedVideoCredito != null) {
            videoCreditoRepository.delete(insertedVideoCredito).block();
            insertedVideoCredito = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createVideoCredito() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);
        var returnedVideoCreditoDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(VideoCreditoDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the VideoCredito in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVideoCredito = videoCreditoMapper.toEntity(returnedVideoCreditoDTO);
        assertVideoCreditoUpdatableFieldsEquals(returnedVideoCredito, getPersistedVideoCredito(returnedVideoCredito));

        insertedVideoCredito = returnedVideoCredito;
    }

    @Test
    void createVideoCreditoWithExistingId() throws Exception {
        // Create the VideoCredito with an existing ID
        videoCredito.setId(1L);
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkVideosConsumidosIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        videoCredito.setVideosConsumidos(null);

        // Create the VideoCredito, which fails.
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkVideosDisponiblesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        videoCredito.setVideosDisponibles(null);

        // Create the VideoCredito, which fails.
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllVideoCreditos() {
        // Initialize the database
        insertedVideoCredito = videoCreditoRepository.save(videoCredito).block();

        // Get all the videoCreditoList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(videoCredito.getId().intValue()))
            .jsonPath("$.[*].videosConsumidos")
            .value(hasItem(DEFAULT_VIDEOS_CONSUMIDOS))
            .jsonPath("$.[*].videosDisponibles")
            .value(hasItem(DEFAULT_VIDEOS_DISPONIBLES));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVideoCreditosWithEagerRelationshipsIsEnabled() {
        when(videoCreditoServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(videoCreditoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVideoCreditosWithEagerRelationshipsIsNotEnabled() {
        when(videoCreditoServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(videoCreditoRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getVideoCredito() {
        // Initialize the database
        insertedVideoCredito = videoCreditoRepository.save(videoCredito).block();

        // Get the videoCredito
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, videoCredito.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(videoCredito.getId().intValue()))
            .jsonPath("$.videosConsumidos")
            .value(is(DEFAULT_VIDEOS_CONSUMIDOS))
            .jsonPath("$.videosDisponibles")
            .value(is(DEFAULT_VIDEOS_DISPONIBLES));
    }

    @Test
    void getNonExistingVideoCredito() {
        // Get the videoCredito
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingVideoCredito() throws Exception {
        // Initialize the database
        insertedVideoCredito = videoCreditoRepository.save(videoCredito).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the videoCredito
        VideoCredito updatedVideoCredito = videoCreditoRepository.findById(videoCredito.getId()).block();
        updatedVideoCredito.videosConsumidos(UPDATED_VIDEOS_CONSUMIDOS).videosDisponibles(UPDATED_VIDEOS_DISPONIBLES);
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(updatedVideoCredito);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, videoCreditoDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVideoCreditoToMatchAllProperties(updatedVideoCredito);
    }

    @Test
    void putNonExistingVideoCredito() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoCredito.setId(longCount.incrementAndGet());

        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, videoCreditoDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchVideoCredito() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoCredito.setId(longCount.incrementAndGet());

        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamVideoCredito() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoCredito.setId(longCount.incrementAndGet());

        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateVideoCreditoWithPatch() throws Exception {
        // Initialize the database
        insertedVideoCredito = videoCreditoRepository.save(videoCredito).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the videoCredito using partial update
        VideoCredito partialUpdatedVideoCredito = new VideoCredito();
        partialUpdatedVideoCredito.setId(videoCredito.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVideoCredito.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedVideoCredito))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the VideoCredito in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVideoCreditoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVideoCredito, videoCredito),
            getPersistedVideoCredito(videoCredito)
        );
    }

    @Test
    void fullUpdateVideoCreditoWithPatch() throws Exception {
        // Initialize the database
        insertedVideoCredito = videoCreditoRepository.save(videoCredito).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the videoCredito using partial update
        VideoCredito partialUpdatedVideoCredito = new VideoCredito();
        partialUpdatedVideoCredito.setId(videoCredito.getId());

        partialUpdatedVideoCredito.videosConsumidos(UPDATED_VIDEOS_CONSUMIDOS).videosDisponibles(UPDATED_VIDEOS_DISPONIBLES);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVideoCredito.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedVideoCredito))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the VideoCredito in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVideoCreditoUpdatableFieldsEquals(partialUpdatedVideoCredito, getPersistedVideoCredito(partialUpdatedVideoCredito));
    }

    @Test
    void patchNonExistingVideoCredito() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoCredito.setId(longCount.incrementAndGet());

        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, videoCreditoDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchVideoCredito() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoCredito.setId(longCount.incrementAndGet());

        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamVideoCredito() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoCredito.setId(longCount.incrementAndGet());

        // Create the VideoCredito
        VideoCreditoDTO videoCreditoDTO = videoCreditoMapper.toDto(videoCredito);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoCreditoDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the VideoCredito in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteVideoCredito() {
        // Initialize the database
        insertedVideoCredito = videoCreditoRepository.save(videoCredito).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the videoCredito
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, videoCredito.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return videoCreditoRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected VideoCredito getPersistedVideoCredito(VideoCredito videoCredito) {
        return videoCreditoRepository.findById(videoCredito.getId()).block();
    }

    protected void assertPersistedVideoCreditoToMatchAllProperties(VideoCredito expectedVideoCredito) {
        // Test fails because reactive api returns an empty object instead of null
        // assertVideoCreditoAllPropertiesEquals(expectedVideoCredito, getPersistedVideoCredito(expectedVideoCredito));
        assertVideoCreditoUpdatableFieldsEquals(expectedVideoCredito, getPersistedVideoCredito(expectedVideoCredito));
    }

    protected void assertPersistedVideoCreditoToMatchUpdatableProperties(VideoCredito expectedVideoCredito) {
        // Test fails because reactive api returns an empty object instead of null
        // assertVideoCreditoAllUpdatablePropertiesEquals(expectedVideoCredito, getPersistedVideoCredito(expectedVideoCredito));
        assertVideoCreditoUpdatableFieldsEquals(expectedVideoCredito, getPersistedVideoCredito(expectedVideoCredito));
    }
}

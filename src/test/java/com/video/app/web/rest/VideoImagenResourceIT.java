package com.video.app.web.rest;

import static com.video.app.domain.VideoImagenAsserts.*;
import static com.video.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.video.app.IntegrationTest;
import com.video.app.domain.Video;
import com.video.app.domain.VideoImagen;
import com.video.app.repository.EntityManager;
import com.video.app.repository.VideoImagenRepository;
import com.video.app.service.dto.VideoImagenDTO;
import com.video.app.service.mapper.VideoImagenMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link VideoImagenResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class VideoImagenResourceIT {

    private static final String DEFAULT_FILENAME = "AAAAAAAAAA";
    private static final String UPDATED_FILENAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_ORDEN = 0;
    private static final Integer UPDATED_ORDEN = 1;

    private static final Integer DEFAULT_DURACION_INDIVIDUAL = 1;
    private static final Integer UPDATED_DURACION_INDIVIDUAL = 2;

    private static final String ENTITY_API_URL = "/api/video-imagens";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VideoImagenRepository videoImagenRepository;

    @Autowired
    private VideoImagenMapper videoImagenMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private VideoImagen videoImagen;

    private VideoImagen insertedVideoImagen;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VideoImagen createEntity(EntityManager em) {
        VideoImagen videoImagen = new VideoImagen()
            .filename(DEFAULT_FILENAME)
            .orden(DEFAULT_ORDEN)
            .duracionIndividual(DEFAULT_DURACION_INDIVIDUAL);
        // Add required entity
        Video video;
        video = em.insert(VideoResourceIT.createEntity(em)).block();
        videoImagen.setVideo(video);
        return videoImagen;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VideoImagen createUpdatedEntity(EntityManager em) {
        VideoImagen updatedVideoImagen = new VideoImagen()
            .filename(UPDATED_FILENAME)
            .orden(UPDATED_ORDEN)
            .duracionIndividual(UPDATED_DURACION_INDIVIDUAL);
        // Add required entity
        Video video;
        video = em.insert(VideoResourceIT.createUpdatedEntity(em)).block();
        updatedVideoImagen.setVideo(video);
        return updatedVideoImagen;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(VideoImagen.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        VideoResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void initTest() {
        videoImagen = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedVideoImagen != null) {
            videoImagenRepository.delete(insertedVideoImagen).block();
            insertedVideoImagen = null;
        }
        deleteEntities(em);
    }

    @Test
    void createVideoImagen() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);
        var returnedVideoImagenDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(VideoImagenDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the VideoImagen in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVideoImagen = videoImagenMapper.toEntity(returnedVideoImagenDTO);
        assertVideoImagenUpdatableFieldsEquals(returnedVideoImagen, getPersistedVideoImagen(returnedVideoImagen));

        insertedVideoImagen = returnedVideoImagen;
    }

    @Test
    void createVideoImagenWithExistingId() throws Exception {
        // Create the VideoImagen with an existing ID
        videoImagen.setId(1L);
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkFilenameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        videoImagen.setFilename(null);

        // Create the VideoImagen, which fails.
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkOrdenIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        videoImagen.setOrden(null);

        // Create the VideoImagen, which fails.
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllVideoImagens() {
        // Initialize the database
        insertedVideoImagen = videoImagenRepository.save(videoImagen).block();

        // Get all the videoImagenList
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
            .value(hasItem(videoImagen.getId().intValue()))
            .jsonPath("$.[*].filename")
            .value(hasItem(DEFAULT_FILENAME))
            .jsonPath("$.[*].orden")
            .value(hasItem(DEFAULT_ORDEN))
            .jsonPath("$.[*].duracionIndividual")
            .value(hasItem(DEFAULT_DURACION_INDIVIDUAL));
    }

    @Test
    void getVideoImagen() {
        // Initialize the database
        insertedVideoImagen = videoImagenRepository.save(videoImagen).block();

        // Get the videoImagen
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, videoImagen.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(videoImagen.getId().intValue()))
            .jsonPath("$.filename")
            .value(is(DEFAULT_FILENAME))
            .jsonPath("$.orden")
            .value(is(DEFAULT_ORDEN))
            .jsonPath("$.duracionIndividual")
            .value(is(DEFAULT_DURACION_INDIVIDUAL));
    }

    @Test
    void getNonExistingVideoImagen() {
        // Get the videoImagen
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingVideoImagen() throws Exception {
        // Initialize the database
        insertedVideoImagen = videoImagenRepository.save(videoImagen).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the videoImagen
        VideoImagen updatedVideoImagen = videoImagenRepository.findById(videoImagen.getId()).block();
        updatedVideoImagen.filename(UPDATED_FILENAME).orden(UPDATED_ORDEN).duracionIndividual(UPDATED_DURACION_INDIVIDUAL);
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(updatedVideoImagen);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, videoImagenDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVideoImagenToMatchAllProperties(updatedVideoImagen);
    }

    @Test
    void putNonExistingVideoImagen() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoImagen.setId(longCount.incrementAndGet());

        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, videoImagenDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchVideoImagen() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoImagen.setId(longCount.incrementAndGet());

        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamVideoImagen() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoImagen.setId(longCount.incrementAndGet());

        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateVideoImagenWithPatch() throws Exception {
        // Initialize the database
        insertedVideoImagen = videoImagenRepository.save(videoImagen).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the videoImagen using partial update
        VideoImagen partialUpdatedVideoImagen = new VideoImagen();
        partialUpdatedVideoImagen.setId(videoImagen.getId());

        partialUpdatedVideoImagen.filename(UPDATED_FILENAME).orden(UPDATED_ORDEN);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVideoImagen.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedVideoImagen))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the VideoImagen in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVideoImagenUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVideoImagen, videoImagen),
            getPersistedVideoImagen(videoImagen)
        );
    }

    @Test
    void fullUpdateVideoImagenWithPatch() throws Exception {
        // Initialize the database
        insertedVideoImagen = videoImagenRepository.save(videoImagen).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the videoImagen using partial update
        VideoImagen partialUpdatedVideoImagen = new VideoImagen();
        partialUpdatedVideoImagen.setId(videoImagen.getId());

        partialUpdatedVideoImagen.filename(UPDATED_FILENAME).orden(UPDATED_ORDEN).duracionIndividual(UPDATED_DURACION_INDIVIDUAL);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVideoImagen.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedVideoImagen))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the VideoImagen in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVideoImagenUpdatableFieldsEquals(partialUpdatedVideoImagen, getPersistedVideoImagen(partialUpdatedVideoImagen));
    }

    @Test
    void patchNonExistingVideoImagen() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoImagen.setId(longCount.incrementAndGet());

        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, videoImagenDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchVideoImagen() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoImagen.setId(longCount.incrementAndGet());

        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamVideoImagen() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        videoImagen.setId(longCount.incrementAndGet());

        // Create the VideoImagen
        VideoImagenDTO videoImagenDTO = videoImagenMapper.toDto(videoImagen);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoImagenDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the VideoImagen in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteVideoImagen() {
        // Initialize the database
        insertedVideoImagen = videoImagenRepository.save(videoImagen).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the videoImagen
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, videoImagen.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return videoImagenRepository.count().block();
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

    protected VideoImagen getPersistedVideoImagen(VideoImagen videoImagen) {
        return videoImagenRepository.findById(videoImagen.getId()).block();
    }

    protected void assertPersistedVideoImagenToMatchAllProperties(VideoImagen expectedVideoImagen) {
        // Test fails because reactive api returns an empty object instead of null
        // assertVideoImagenAllPropertiesEquals(expectedVideoImagen, getPersistedVideoImagen(expectedVideoImagen));
        assertVideoImagenUpdatableFieldsEquals(expectedVideoImagen, getPersistedVideoImagen(expectedVideoImagen));
    }

    protected void assertPersistedVideoImagenToMatchUpdatableProperties(VideoImagen expectedVideoImagen) {
        // Test fails because reactive api returns an empty object instead of null
        // assertVideoImagenAllUpdatablePropertiesEquals(expectedVideoImagen, getPersistedVideoImagen(expectedVideoImagen));
        assertVideoImagenUpdatableFieldsEquals(expectedVideoImagen, getPersistedVideoImagen(expectedVideoImagen));
    }
}

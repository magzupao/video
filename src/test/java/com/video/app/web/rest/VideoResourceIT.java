package com.video.app.web.rest;

import static com.video.app.domain.VideoAsserts.*;
import static com.video.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.video.app.IntegrationTest;
import com.video.app.domain.User;
import com.video.app.domain.Video;
import com.video.app.domain.enumeration.EstadoVideo;
import com.video.app.repository.EntityManager;
import com.video.app.repository.UserRepository;
import com.video.app.repository.VideoRepository;
import com.video.app.service.VideoService;
import com.video.app.service.dto.VideoDTO;
import com.video.app.service.mapper.VideoMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link VideoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class VideoResourceIT {

    private static final String DEFAULT_TITULO = "AAAAAAAAAA";
    private static final String UPDATED_TITULO = "BBBBBBBBBB";

    private static final String DEFAULT_AUDIO_FILENAME = "AAAAAAAAAA";
    private static final String UPDATED_AUDIO_FILENAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_TIENE_AUDIO = false;
    private static final Boolean UPDATED_TIENE_AUDIO = true;

    private static final Integer DEFAULT_DURACION_TRANSICION = 1;
    private static final Integer UPDATED_DURACION_TRANSICION = 2;

    private static final EstadoVideo DEFAULT_ESTADO = EstadoVideo.EN_PROCESO;
    private static final EstadoVideo UPDATED_ESTADO = EstadoVideo.COMPLETADO;

    private static final Instant DEFAULT_FECHA_CREACION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_CREACION = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_FECHA_DESCARGA = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_DESCARGA = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/videos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepositoryMock;

    @Autowired
    private VideoMapper videoMapper;

    @Mock
    private VideoService videoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Video video;

    private Video insertedVideo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Video createEntity(EntityManager em) {
        Video video = new Video()
            .titulo(DEFAULT_TITULO)
            .audioFilename(DEFAULT_AUDIO_FILENAME)
            .tieneAudio(DEFAULT_TIENE_AUDIO)
            .duracionTransicion(DEFAULT_DURACION_TRANSICION)
            .estado(DEFAULT_ESTADO)
            .fechaCreacion(DEFAULT_FECHA_CREACION)
            .fechaDescarga(DEFAULT_FECHA_DESCARGA);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        video.setUser(user);
        return video;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Video createUpdatedEntity(EntityManager em) {
        Video updatedVideo = new Video()
            .titulo(UPDATED_TITULO)
            .audioFilename(UPDATED_AUDIO_FILENAME)
            .tieneAudio(UPDATED_TIENE_AUDIO)
            .duracionTransicion(UPDATED_DURACION_TRANSICION)
            .estado(UPDATED_ESTADO)
            .fechaCreacion(UPDATED_FECHA_CREACION)
            .fechaDescarga(UPDATED_FECHA_DESCARGA);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        updatedVideo.setUser(user);
        return updatedVideo;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Video.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        UserResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void initTest() {
        video = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedVideo != null) {
            videoRepository.delete(insertedVideo).block();
            insertedVideo = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createVideo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);
        var returnedVideoDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(VideoDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Video in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVideo = videoMapper.toEntity(returnedVideoDTO);
        assertVideoUpdatableFieldsEquals(returnedVideo, getPersistedVideo(returnedVideo));

        insertedVideo = returnedVideo;
    }

    @Test
    void createVideoWithExistingId() throws Exception {
        // Create the Video with an existing ID
        video.setId(1L);
        VideoDTO videoDTO = videoMapper.toDto(video);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTieneAudioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        video.setTieneAudio(null);

        // Create the Video, which fails.
        VideoDTO videoDTO = videoMapper.toDto(video);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        video.setEstado(null);

        // Create the Video, which fails.
        VideoDTO videoDTO = videoMapper.toDto(video);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkFechaCreacionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        video.setFechaCreacion(null);

        // Create the Video, which fails.
        VideoDTO videoDTO = videoMapper.toDto(video);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllVideos() {
        // Initialize the database
        insertedVideo = videoRepository.save(video).block();

        // Get all the videoList
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
            .value(hasItem(video.getId().intValue()))
            .jsonPath("$.[*].titulo")
            .value(hasItem(DEFAULT_TITULO))
            .jsonPath("$.[*].audioFilename")
            .value(hasItem(DEFAULT_AUDIO_FILENAME))
            .jsonPath("$.[*].tieneAudio")
            .value(hasItem(DEFAULT_TIENE_AUDIO))
            .jsonPath("$.[*].duracionTransicion")
            .value(hasItem(DEFAULT_DURACION_TRANSICION))
            .jsonPath("$.[*].estado")
            .value(hasItem(DEFAULT_ESTADO.toString()))
            .jsonPath("$.[*].fechaCreacion")
            .value(hasItem(DEFAULT_FECHA_CREACION.toString()))
            .jsonPath("$.[*].fechaDescarga")
            .value(hasItem(DEFAULT_FECHA_DESCARGA.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVideosWithEagerRelationshipsIsEnabled() {
        when(videoServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(videoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVideosWithEagerRelationshipsIsNotEnabled() {
        when(videoServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(videoRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getVideo() {
        // Initialize the database
        insertedVideo = videoRepository.save(video).block();

        // Get the video
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, video.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(video.getId().intValue()))
            .jsonPath("$.titulo")
            .value(is(DEFAULT_TITULO))
            .jsonPath("$.audioFilename")
            .value(is(DEFAULT_AUDIO_FILENAME))
            .jsonPath("$.tieneAudio")
            .value(is(DEFAULT_TIENE_AUDIO))
            .jsonPath("$.duracionTransicion")
            .value(is(DEFAULT_DURACION_TRANSICION))
            .jsonPath("$.estado")
            .value(is(DEFAULT_ESTADO.toString()))
            .jsonPath("$.fechaCreacion")
            .value(is(DEFAULT_FECHA_CREACION.toString()))
            .jsonPath("$.fechaDescarga")
            .value(is(DEFAULT_FECHA_DESCARGA.toString()));
    }

    @Test
    void getNonExistingVideo() {
        // Get the video
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingVideo() throws Exception {
        // Initialize the database
        insertedVideo = videoRepository.save(video).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the video
        Video updatedVideo = videoRepository.findById(video.getId()).block();
        updatedVideo
            .titulo(UPDATED_TITULO)
            .audioFilename(UPDATED_AUDIO_FILENAME)
            .tieneAudio(UPDATED_TIENE_AUDIO)
            .duracionTransicion(UPDATED_DURACION_TRANSICION)
            .estado(UPDATED_ESTADO)
            .fechaCreacion(UPDATED_FECHA_CREACION)
            .fechaDescarga(UPDATED_FECHA_DESCARGA);
        VideoDTO videoDTO = videoMapper.toDto(updatedVideo);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, videoDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVideoToMatchAllProperties(updatedVideo);
    }

    @Test
    void putNonExistingVideo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        video.setId(longCount.incrementAndGet());

        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, videoDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchVideo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        video.setId(longCount.incrementAndGet());

        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamVideo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        video.setId(longCount.incrementAndGet());

        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateVideoWithPatch() throws Exception {
        // Initialize the database
        insertedVideo = videoRepository.save(video).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the video using partial update
        Video partialUpdatedVideo = new Video();
        partialUpdatedVideo.setId(video.getId());

        partialUpdatedVideo.titulo(UPDATED_TITULO).audioFilename(UPDATED_AUDIO_FILENAME).estado(UPDATED_ESTADO);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVideo.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedVideo))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Video in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVideoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedVideo, video), getPersistedVideo(video));
    }

    @Test
    void fullUpdateVideoWithPatch() throws Exception {
        // Initialize the database
        insertedVideo = videoRepository.save(video).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the video using partial update
        Video partialUpdatedVideo = new Video();
        partialUpdatedVideo.setId(video.getId());

        partialUpdatedVideo
            .titulo(UPDATED_TITULO)
            .audioFilename(UPDATED_AUDIO_FILENAME)
            .tieneAudio(UPDATED_TIENE_AUDIO)
            .duracionTransicion(UPDATED_DURACION_TRANSICION)
            .estado(UPDATED_ESTADO)
            .fechaCreacion(UPDATED_FECHA_CREACION)
            .fechaDescarga(UPDATED_FECHA_DESCARGA);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVideo.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedVideo))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Video in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVideoUpdatableFieldsEquals(partialUpdatedVideo, getPersistedVideo(partialUpdatedVideo));
    }

    @Test
    void patchNonExistingVideo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        video.setId(longCount.incrementAndGet());

        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, videoDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchVideo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        video.setId(longCount.incrementAndGet());

        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamVideo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        video.setId(longCount.incrementAndGet());

        // Create the Video
        VideoDTO videoDTO = videoMapper.toDto(video);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(videoDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Video in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteVideo() {
        // Initialize the database
        insertedVideo = videoRepository.save(video).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the video
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, video.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return videoRepository.count().block();
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

    protected Video getPersistedVideo(Video video) {
        return videoRepository.findById(video.getId()).block();
    }

    protected void assertPersistedVideoToMatchAllProperties(Video expectedVideo) {
        // Test fails because reactive api returns an empty object instead of null
        // assertVideoAllPropertiesEquals(expectedVideo, getPersistedVideo(expectedVideo));
        assertVideoUpdatableFieldsEquals(expectedVideo, getPersistedVideo(expectedVideo));
    }

    protected void assertPersistedVideoToMatchUpdatableProperties(Video expectedVideo) {
        // Test fails because reactive api returns an empty object instead of null
        // assertVideoAllUpdatablePropertiesEquals(expectedVideo, getPersistedVideo(expectedVideo));
        assertVideoUpdatableFieldsEquals(expectedVideo, getPersistedVideo(expectedVideo));
    }
}

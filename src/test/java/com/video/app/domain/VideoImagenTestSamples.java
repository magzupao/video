package com.video.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VideoImagenTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static VideoImagen getVideoImagenSample1() {
        return new VideoImagen().id(1L).filename("filename1").orden(1).duracionIndividual(1);
    }

    public static VideoImagen getVideoImagenSample2() {
        return new VideoImagen().id(2L).filename("filename2").orden(2).duracionIndividual(2);
    }

    public static VideoImagen getVideoImagenRandomSampleGenerator() {
        return new VideoImagen()
            .id(longCount.incrementAndGet())
            .filename(UUID.randomUUID().toString())
            .orden(intCount.incrementAndGet())
            .duracionIndividual(intCount.incrementAndGet());
    }
}

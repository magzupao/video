package com.video.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VideoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Video getVideoSample1() {
        return new Video().id(1L).titulo("titulo1").audioFilename("audioFilename1").duracionTransicion(1);
    }

    public static Video getVideoSample2() {
        return new Video().id(2L).titulo("titulo2").audioFilename("audioFilename2").duracionTransicion(2);
    }

    public static Video getVideoRandomSampleGenerator() {
        return new Video()
            .id(longCount.incrementAndGet())
            .titulo(UUID.randomUUID().toString())
            .audioFilename(UUID.randomUUID().toString())
            .duracionTransicion(intCount.incrementAndGet());
    }
}

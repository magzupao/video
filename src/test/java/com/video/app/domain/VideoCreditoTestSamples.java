package com.video.app.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VideoCreditoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static VideoCredito getVideoCreditoSample1() {
        return new VideoCredito().id(1L).videosConsumidos(1).videosDisponibles(1);
    }

    public static VideoCredito getVideoCreditoSample2() {
        return new VideoCredito().id(2L).videosConsumidos(2).videosDisponibles(2);
    }

    public static VideoCredito getVideoCreditoRandomSampleGenerator() {
        return new VideoCredito()
            .id(longCount.incrementAndGet())
            .videosConsumidos(intCount.incrementAndGet())
            .videosDisponibles(intCount.incrementAndGet());
    }
}

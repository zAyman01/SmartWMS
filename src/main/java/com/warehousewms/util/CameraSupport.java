package com.warehousewms.util;

import com.github.sarxos.webcam.Webcam;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CameraSupport {
    private static final AtomicBoolean probed = new AtomicBoolean(false);
    private static volatile boolean available;

    private CameraSupport() {}

    public static boolean isCameraAvailable() {
        if (!probed.get()) {
            synchronized (CameraSupport.class) {
                if (!probed.get()) {
                    probe();
                }
            }
        }
        return available;
    }

    private static void probe() {
        try {
            Webcam webcam = Webcam.getDefault();
            available = (webcam != null);
            if (webcam != null) {
                webcam.close();
            }
        } catch (Throwable t) {
            available = false;
        }
        probed.set(true);
    }
}

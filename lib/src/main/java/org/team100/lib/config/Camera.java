package org.team100.lib.config;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * Represents all the cameras. Some may be mounted on one robot, some on
 * another. Keep this up to date when you move cameras around.
 * //TODO for delta you need correct serial numbers, put them here
 */
public enum Camera {
    /**
     * BETA front
    //  */
    // A("10000000caeaae82",
    //         new Transform3d(
    //                 new Translation3d(.1625, 0.0889, 0.762),
    //                 new Rotation3d(0, Math.toRadians(-18), 0))),
    /**
     * BETA back
     */
    B("1000000013c9c96c",
            new Transform3d(
                    new Translation3d(0.1825, -0.06985, 0.76),
                    new Rotation3d(0, Math.toRadians(-24), Math.PI))),
    /**
     * 
     */
    C("10000000a7c673d9",
            new Transform3d(
                    new Translation3d(0, 0, 0.75),
                    new Rotation3d(0, Math.toRadians(30), Math.PI))),
    /**
     * Delta shooter
     */
    //TODO get real y offset
    SHOOTER("10000000a7a892c0", 
            new Transform3d(
                    new Translation3d(-0.2778506, 0.03,  0.6254242),
                    new Rotation3d(0, Math.toRadians(-25), Math.toRadians(0)))),

    /**
     * Delta amp-placer
     */
    RIGHTAMP("10000000caeaae82",
    //TODO get real y offset
            new Transform3d(
                    new Translation3d(-0.2778506, -0.0818896, 0.6254242),
                    new Rotation3d(0, Math.toRadians(-20), -Math.PI/3))),

    /**
     * Delta amp-placer
     */
    LEFTAMP("100000004e0a1fb9",
    //TODO get real y offset
            new Transform3d(
                    new Translation3d(-0.2778506, 0.0818896, 0.6254242),
                    new Rotation3d(0, Math.toRadians(-20), Math.PI/3))),
    /**
     * Delta intake
     */
    GAME_PIECE("1000000013c9c96c",
            new Transform3d(
                    new Translation3d(-0.3, 0, 0.61),
                    new Rotation3d(0, Math.toRadians(31.5), Math.PI))),
    
    // G("10000000a7a892c0",
    //         new Transform3d(
    //                 new Translation3d(0, 0, 1),
    //                 new Rotation3d(0, Math.toRadians(30), 0))),           

    TEST1("test1",
            new Transform3d(
                    new Translation3d(),
                    new Rotation3d(0, Math.PI / 4, 0))),

    TEST2("test2",
            new Transform3d(
                    new Translation3d(1, 0, 0),
                    new Rotation3d())),

    TEST3("test3",
            new Transform3d(
                    new Translation3d(0, 0, 0),
                    new Rotation3d(0, Math.PI / 6, 0))),

    UNKNOWN(null, new Transform3d());

    private static Map<String, Camera> cameras = new HashMap<>();
    static {
        for (Camera i : Camera.values()) {
            cameras.put(i.m_serialNumber, i);
        }
    }
    private String m_serialNumber;
    private Transform3d m_Offset;

    private Camera(String serialNumber, Transform3d offset) {
        m_serialNumber = serialNumber;
        m_Offset = offset;
    }

    public static Camera get(String serialNumber) {
        if (cameras.containsKey(serialNumber))
            return cameras.get(serialNumber);
        return UNKNOWN;
    }

    public Transform3d getOffset() {
        return m_Offset;
    }
}

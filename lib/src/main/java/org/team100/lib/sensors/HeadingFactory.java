package org.team100.lib.sensors;

import org.team100.lib.config.Identity;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motion.drivetrain.module.SwerveModuleCollection;
import org.team100.lib.util.Util;

/**
 * Produces real or simulated gyros depending on identity.
 */
public class HeadingFactory {

    public static HeadingInterface get(
            SwerveKinodynamics kinodynamics,
            SwerveModuleCollection collection) {
        switch (Identity.instance) {
            case BLANK:
                // for simulation
                return new SimulatedHeading(kinodynamics, collection);
            default:
                try {
                    // the kauailabs library calls System.exit() in case
                    // it can't find this library, so check here first.
                    // this seems only to be a problem in some test or
                    // simulation scenarios.
                    // System.loadLibrary("vmxHaljni");
                    Gyro100 ahrsclass = new GyroFactory(Identity.instance).get();
                    return new Heading(ahrsclass);
                } catch (UnsatisfiedLinkError e) {
                    // fall back to simulated heading for testing.
                    Util.warn("No NavX Library!");
                    throw e;
                    // return new SimulatedHeading(kinodynamics, collection);
                }
        }
    }

    private HeadingFactory() {
        //
    }
}

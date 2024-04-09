package org.team100.lib.controller;

import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;

/**
 * Known-good controller settings.
 * 
 * joel 20240311 changed ptheta from 2 to 1.3
 */
public class DriveMotionControllerFactory {

    public static DriveMotionController fancyPIDF() {
        return new DrivePIDFController(false, 2.4, 1.3);
    }

    public static DriveMotionController straightPIDF() {
        return new DrivePIDFController(false, 4, 4);
    }

    public static DriveMotionController newNewPIDF() {
        return new DrivePIDFController(false, 5.5, 4);
    }

    public static DriveMotionController complementPIDF() {
        return new DrivePIDFController(false, 6, 6);
    }

    public static DriveMotionController goodPIDF() {
        return new DrivePIDFController(false, 1, 1.3);
    }

    public static DriveMotionController stageBase() {
        return new DrivePIDFController(false, 2, 1.3);
    }

    public static DriveMotionController autoPIDF() {
        return new DrivePIDFController(false, 1, 1.3);
    }

    public static DriveMotionController ffOnly() {
        return new DrivePIDFController(true, 2.4, 1.3);
    }

    public static DriveMotionController purePursuit(SwerveKinodynamics swerveKinodynamics) {
        return new DrivePursuitController(swerveKinodynamics);
    }

    public static DriveMotionController ramsete() {
        return new DriveRamseteController();
    }

    public static DriveMotionController testPIDF() {
        return new DrivePIDFController(false, 2.4, 2.4);
    }
    public static DriveMotionController testFFOnly() {
        return new DrivePIDFController(true, 2.4, 2.4);
    }

    public static DriveMotionController fasterCurves() {
        return new DrivePIDFController(true, 4.5, 4.5);
    }
    private DriveMotionControllerFactory() {
        //
    }

}

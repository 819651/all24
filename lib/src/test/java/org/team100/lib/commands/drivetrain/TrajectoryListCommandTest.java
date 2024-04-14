package org.team100.lib.commands.drivetrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.team100.lib.controller.HolonomicDriveController3;
import org.team100.lib.controller.State100;
import org.team100.lib.experiments.Experiment;
import org.team100.lib.experiments.Experiments;
import org.team100.lib.motion.drivetrain.Fixtured;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamicsFactory;
import org.team100.lib.testing.Timeless;
import org.team100.lib.timing.TimingConstraint;
import org.team100.lib.timing.TimingConstraintFactory;
import org.team100.lib.trajectory.TrajectoryMaker;
import org.team100.lib.trajectory.TrajectoryPlanner;
import org.team100.lib.util.Tire;
import org.team100.lib.util.Util;

import edu.wpi.first.math.kinematics.SwerveModuleState;

class TrajectoryListCommandTest extends Fixtured implements Timeless {
    boolean dump = false;
    private static final double kDelta = 0.001;
    private static final double kDtS = 0.02;

    TrajectoryPlanner planner = new TrajectoryPlanner();
    SwerveKinodynamics swerveKinodynamics = SwerveKinodynamicsFactory.get();

    List<TimingConstraint> constraints = new TimingConstraintFactory(swerveKinodynamics).allGood();
    TrajectoryMaker maker = new TrajectoryMaker(planner, constraints);

    @Test
    void testSimple() {
        // no wheel slip
        fixture.poseEstimator.f.mutable(Tire.kSaturationLabel, 0).set(Double.MAX_VALUE);

        Experiments.instance.testOverride(Experiment.UseSetpointGenerator, true);
        HolonomicDriveController3 control = new HolonomicDriveController3();
        TrajectoryListCommand c = new TrajectoryListCommand(
                fixture.drive,
                control,
                x -> List.of(maker.line(x)));
        c.initialize();
        assertEquals(0, fixture.drive.getPose().getX(), kDelta);
        c.execute();
        assertFalse(c.isFinished());
        // the trajectory takes a little over 2s
        for (double t = 0; t < 2.02; t += kDtS) {
            stepTime(kDtS);
            c.execute();
            fixture.drive.periodic(); // for updateOdometry

        }
        // at goal; wide tolerance due to test timing
        assertTrue(c.isFinished());
        assertEquals(1.031, fixture.drive.getPose().getX(), 0.05);
    }

    /**
     * See also DriveInALittleSquareTest.
     * This exists to produce useful output to graph.
     */
    @Test
    void testLowLevel() {
        HolonomicDriveController3 controller = new HolonomicDriveController3();
        TrajectoryListCommand command = new TrajectoryListCommand(
                fixture.drive,
                controller,
                x -> maker.square(x));
        Experiments.instance.testOverride(Experiment.UseSetpointGenerator, false);
        fixture.drive.periodic();
        command.initialize();
        do {
            stepTime(kDtS);
            fixture.drive.periodic();
            command.execute();
            double measurement = fixture.drive.moduleStates()[0].angle.getRadians();
            SwerveModuleState goal = fixture.swerveLocal.getDesiredStates()[0];
            State100 setpoint = fixture.swerveLocal.getSetpoints()[0];
            // this output is useful to see what's happening.
            if (dump)
                Util.printf("goal %5.3f setpoint x %5.3f setpoint v %5.3f measurement %5.3f\n",
                        goal.angle.getRadians(),
                        setpoint.x(),
                        setpoint.v(),
                        measurement);
        } while (!command.isFinished());
    }
}

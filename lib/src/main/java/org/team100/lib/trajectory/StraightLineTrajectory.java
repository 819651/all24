package org.team100.lib.trajectory;

import java.util.List;

import org.team100.lib.copies.TrajectoryConfig100;
import org.team100.lib.copies.TrajectoryGenerator100;
import org.team100.lib.experiments.Experiment;
import org.team100.lib.experiments.Experiments;
import org.team100.lib.motion.drivetrain.SwerveState;
import org.team100.lib.util.Util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.spline.Spline;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryParameterizer.TrajectoryGenerationException;

/** Make straight lines, rest-to-rest. */
public class StraightLineTrajectory {

    private final TrajectoryConfig100 m_config;

    public StraightLineTrajectory(TrajectoryConfig100 config) {
        m_config = config;
    }

    /**
     * Return a straight line trajectory from the start state to the end pose at
     * rest.
     */
    public Trajectory apply(SwerveState startState, Pose2d end) {
        if (Experiments.instance.enabled(Experiment.UseInitialVelocity))
            return movingToRest(startState, end);
        else
            return TrajectoryMaker.restToRest(m_config, startState.translation(), end.getTranslation());
    }

    private Trajectory movingToRest(SwerveState startState, Pose2d end) {
        if (Math.abs(startState.twist().dx) < 1e-6 && Math.abs(startState.twist().dy) < 1e-6)
            return TrajectoryMaker.restToRest(m_config, startState.translation(), end.getTranslation());
        Translation2d currentTranslation = startState.translation();
        Twist2d currentSpeed = startState.twist();
        Translation2d goalTranslation = end.getTranslation();
        Translation2d translationToGoal = goalTranslation.minus(currentTranslation);
        Rotation2d angleToGoal = translationToGoal.getAngle();

        double scalar = currentTranslation.getDistance(goalTranslation) * 1.2;

        Spline.ControlVector initial = new Spline.ControlVector(
                new double[] { currentTranslation.getX(), scalar * currentSpeed.dx },
                new double[] { currentTranslation.getY(), scalar * currentSpeed.dy });

        Spline.ControlVector last = new Spline.ControlVector(
                new double[] { goalTranslation.getX(), scalar * angleToGoal.getCos() },
                new double[] { goalTranslation.getY(), scalar * angleToGoal.getSin() });

        m_config.setStartVelocity(Math.hypot(currentSpeed.dx, currentSpeed.dy));
        m_config.setEndVelocity(0);

        try {
            return TrajectoryGenerator100.generateTrajectory(
                    initial,
                    List.of(),
                    last,
                    m_config);
        } catch (TrajectoryGenerationException e) {
            Util.warn("Trajectory Generation Exception");
            return new Trajectory();
        }
    }

}

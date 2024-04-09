package org.team100.frc2024.commands.drivetrain;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.team100.frc2024.RobotState100;
import org.team100.frc2024.RobotState100.IntakeState100;
import org.team100.frc2024.motion.intake.Intake;
import org.team100.lib.commands.Command100;
import org.team100.lib.controller.HolonomicDriveController100;
import org.team100.lib.controller.State100;
import org.team100.lib.experiments.Experiment;
import org.team100.lib.experiments.Experiments;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystem;
import org.team100.lib.motion.drivetrain.SwerveState;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.profile.Constraints100;
import org.team100.lib.profile.TrapezoidProfile100;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.util.Math100;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;

/**
 * //TODO just make this class use DriveWithProfile2, this should be more of a high level class
 * Creates a profile to the translation of a note and follows it
 */
public class DriveWithProfileNote extends Command100 {
    private final Supplier<Optional<Translation2d>> m_fieldRelativeGoal;
    private final SwerveDriveSubsystem m_swerve;
    private final HolonomicDriveController100 m_controller;
    private final SwerveKinodynamics m_limits;
    private final TrapezoidProfile100 xProfile;
    private final TrapezoidProfile100 yProfile;
    private final TrapezoidProfile100 thetaProfile;
    private final BooleanSupplier m_end;
    private int count;
    private Optional<Translation2d> previousGoal;
    private State100 xSetpoint;
    private State100 ySetpoint;
    private State100 thetaSetpoint;
    private final Telemetry t = Telemetry.get();
    private Timer m_timer;
    private Intake m_intake;

    /**
     * @param goal
     * @param drivetrain
     * @param controller
     * @param limits
     * @param end
     */
    public DriveWithProfileNote(
            Supplier<Optional<Translation2d>> fieldRelativeGoal,
            SwerveDriveSubsystem drivetrain,
            HolonomicDriveController100 controller,
            SwerveKinodynamics limits,
            BooleanSupplier end,
            Intake intake) {
        count = 0;
        previousGoal = null;
        m_swerve = drivetrain;
        m_fieldRelativeGoal = fieldRelativeGoal;
        if (end.getAsBoolean() == false) {
            m_end = end;
        } else {
            m_end = () -> false;
        }
        m_controller = controller;
        m_limits = limits;
        Constraints100 thetaContraints = new Constraints100(m_limits.getMaxAngleSpeedRad_S(),
                m_limits.getMaxAngleAccelRad_S2() / 4);
        Constraints100 driveContraints = new Constraints100(m_limits.getMaxDriveVelocityM_S(),
                m_limits.getMaxDriveAccelerationM_S2() / 2);
        xProfile = new TrapezoidProfile100(driveContraints, 0.01);
        yProfile = new TrapezoidProfile100(driveContraints, 0.01);
        thetaProfile = new TrapezoidProfile100(thetaContraints, 0.01);
        m_timer = new Timer();
        m_intake = intake;
        addRequirements(m_swerve);
    }

    @Override
    public void initialize100() {
        xSetpoint = m_swerve.getState().x();
        ySetpoint = m_swerve.getState().y();
        thetaSetpoint = m_swerve.getState().theta();
        m_timer.restart();
        // m_intake.intakeSmart();
        RobotState100.changeIntakeState(IntakeState100.INTAKE);

    }

    @Override
    public void execute100(double dt) {
        Optional<Translation2d> goal = m_fieldRelativeGoal.get();
        // if (!goal.isPresent()) {
        // if (previousGoal == null) {
        // return;
        // }
        // goal = previousGoal;
        // count++;
        // if (count == 50) {
        // return;
        // }
        // // return;
        // } else {
        // count = 0;
        // }

        if (!goal.isPresent()) {
            m_swerve.setChassisSpeeds(new ChassisSpeeds(), dt);
            t.log(Level.INFO, m_name, "Note detected", false);
            return;
        }
        t.log(Level.INFO, m_name, "Note detected", true);
        Rotation2d rotationGoal;
        if (Experiments.instance.enabled(Experiment.DriveToNoteWithRotation)) {
            rotationGoal = new Rotation2d(
                    goal.get().minus(m_swerve.getPose().getTranslation()).getAngle().getRadians() + Math.PI);
        } else {
            rotationGoal = m_swerve.getPose().getRotation();
        }
        Rotation2d currentRotation = m_swerve.getPose().getRotation();
        // take the short path
        double measurement = currentRotation.getRadians();
        rotationGoal = new Rotation2d(
                Math100.getMinDistance(measurement, rotationGoal.getRadians()));
        // make sure the setpoint uses the modulus close to the measurement.
        thetaSetpoint = new State100(
                Math100.getMinDistance(measurement, thetaSetpoint.x()),
                thetaSetpoint.v());

        State100 thetaGoal = new State100(rotationGoal.getRadians(), 0);
        State100 xGoalRaw = new State100(goal.get().getX(), 0, 0);
        xSetpoint = xProfile.calculate(dt, xSetpoint, xGoalRaw);
        State100 yGoalRaw = new State100(goal.get().getY(), 0, 0);
        ySetpoint = yProfile.calculate(dt, ySetpoint, yGoalRaw);
        thetaSetpoint = thetaProfile.calculate(dt, thetaSetpoint, thetaGoal);
        SwerveState goalState = new SwerveState(xSetpoint, ySetpoint, thetaSetpoint);
        Twist2d TwistGoal = m_controller.calculate(m_swerve.getState(), goalState);
        t.log(Level.DEBUG, "field", "target", new double[] {
                goal.get().getX(),
                goal.get().getY(),
                0 });
        previousGoal = goal;
        m_swerve.driveInFieldCoords(TwistGoal, dt);
    }

    @Override
    public boolean isFinished() {
        // if (!m_fieldRelativeGoal.get().isPresent() && (count >= 50 || previousGoal ==
        // null)) {
        // return true;
        // }
        // return m_end.getAsBoolean();

        return false;
    }

    @Override
    public void end(boolean interrupted) {
        m_swerve.stop();
        m_timer.stop();
    }
}

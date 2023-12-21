package org.team100.lib.commands.drivetrain;

import org.team100.lib.geometry.GeometryUtil;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystemInterface;
import org.team100.lib.util.Util;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * Makes a little square.
 * 
 * This is intended for tuning the steering control stack, because at each
 * corner of the square, the steering needs to respond quickly and precisely.
 * 
 * It samples a jerk-limited profile for driving, with feedforward to the
 * velocity servo but no positional feedback.
 * 
 * It sends the steering position servo fixed goals, so the servo profile is
 * used.
 */
public class DriveInALittleSquare extends Command {
    enum State {
        DRIVING,
        STEERING
    }

    private static final double kDriveLengthM = 1;
    private static final double kMaxVel = 1;
    private static final double kMaxAccel = 1;
    private final SwerveDriveSubsystemInterface m_swerve;
    final Timer m_timer;

    final TrapezoidProfile m_driveProfile;
    TrapezoidProfile.State speedM_S;
    Rotation2d m_goal;
    State m_state;
    private double prevTime;

    final TrapezoidProfile.State start = new TrapezoidProfile.State(0, 0);
    final TrapezoidProfile.State goal = new TrapezoidProfile.State(kDriveLengthM, 0);

    public DriveInALittleSquare(SwerveDriveSubsystemInterface swerve) {
        m_swerve = swerve;
        m_timer = new Timer();

        TrapezoidProfile.Constraints c = new TrapezoidProfile.Constraints(kMaxVel, kMaxAccel);
        m_driveProfile = new TrapezoidProfile(c);
        if (m_swerve.get() != null)
            addRequirements(m_swerve.get());
    }

    @Override
    public void initialize() {
        m_goal = GeometryUtil.kRotationZero;
        m_state = State.DRIVING;
        m_timer.restart();
        prevTime = 0;
        speedM_S = start;
        speedM_S = m_driveProfile.calculate(0, goal, speedM_S);
    }

    @Override
    public void execute() {
        double now = m_timer.get();
        double dt = now - prevTime;
        switch (m_state) {
            case DRIVING:
                if (m_timer.hasElapsed(m_driveProfile.totalTime())) {
                    // we were driving, but the timer elapsed, so switch to steering
                    m_state = State.STEERING;
                    m_goal = m_goal.plus(GeometryUtil.kRotation90);
                    speedM_S = new TrapezoidProfile.State(0, 0);
                } else {
                    // keep going
                    speedM_S = m_driveProfile.calculate(dt, goal, speedM_S);
                }
                break;
            case STEERING:
                if (Util.all(m_swerve.atGoal())) {
                    // we were steering, but all the setpoints have been reached, so switch to
                    // driving
                    m_state = State.DRIVING;
                    m_timer.restart();
                    now = 0;
                    speedM_S = new TrapezoidProfile.State(0, 0);
                    speedM_S = m_driveProfile.calculate(dt, goal, speedM_S);
                } else {
                    // wait to reach the setpoint
                }
                break;
        }
        prevTime = now;

        // there are four states here because state is mutable :-(
        SwerveModuleState[] states = new SwerveModuleState[] {
                new SwerveModuleState(speedM_S.position, m_goal),
                new SwerveModuleState(speedM_S.position, m_goal),
                new SwerveModuleState(speedM_S.position, m_goal),
                new SwerveModuleState(speedM_S.position, m_goal)
        };
        m_swerve.setRawModuleStates(states);

    }

    @Override
    public void end(boolean interrupted) {
        m_swerve.stop();
    }
}

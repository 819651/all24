package org.team100.lib.commands.drivetrain;

import java.util.function.Supplier;

import org.team100.lib.commands.Command100;
import org.team100.lib.controller.HolonomicDriveController100;
import org.team100.lib.controller.State100;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystem;
import org.team100.lib.motion.drivetrain.SwerveState;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.profile.Constraints100;
import org.team100.lib.profile.TrapezoidProfile100;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.util.Math100;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Twist2d;

/**
 * A copy of DriveToWaypoint to explore the new holonomic trajectory classes we
 * cribbed from 254.
 * 
 * Sanjans version, it ends now
 */
public class DriveWithProfile2 extends Command100 {
    // inject these, make them the same as the kinematic limits, inside the
    // trajectory supplier.
    private final Supplier<Pose2d> m_fieldRelativeGoal;
    private final SwerveDriveSubsystem m_swerve;
    private final HolonomicDriveController100 m_controller;
    private final SwerveKinodynamics m_limits;
    private final TrapezoidProfile100 xProfile;
    private final TrapezoidProfile100 yProfile;
    private final TrapezoidProfile100 thetaProfile;

    private State100 xSetpoint;
    private State100 ySetpoint;
    private State100 thetaSetpoint;

    private State100 m_xGoalRaw;
    private State100 m_yGoalRaw;
    private State100 m_thetaGoalRaw;

    private double newXSetpoint = 0;
    /**
     * @param goal
     * @param drivetrain
     * @param controller
     * @param limits
     */
    public DriveWithProfile2(
            Supplier<Pose2d> fieldRelativeGoal,
            SwerveDriveSubsystem drivetrain,
            HolonomicDriveController100 controller,
            SwerveKinodynamics limits) {
        m_fieldRelativeGoal = fieldRelativeGoal;
        m_swerve = drivetrain;
        m_controller = controller;
        m_limits = limits;
        Constraints100 thetaContraints = new Constraints100(m_limits.getMaxAngleSpeedRad_S(),m_limits.getMaxAngleAccelRad_S2());
        Constraints100 driveContraints = new Constraints100(m_limits.getMaxDriveVelocityM_S(),m_limits.getMaxDriveAccelerationM_S2());
        xProfile = new TrapezoidProfile100(driveContraints, 0.01);
        yProfile = new TrapezoidProfile100(driveContraints, 0.01);
        thetaProfile = new TrapezoidProfile100(thetaContraints, 0.01);
        addRequirements(m_swerve);
    }

    @Override
    public void initialize100() {
        // System.out.println("START X " + m_swerve.getState().x().v());
        System.out.println("DRIVE WITH PROFILE");
        xSetpoint = m_swerve.getState().x();
        ySetpoint = m_swerve.getState().y();
        thetaSetpoint = m_swerve.getState().theta();
    }

    @Override
    public void execute100(double dt) {
        Rotation2d currentRotation = m_swerve.getPose().getRotation();
        // take the short path
        double measurement = currentRotation.getRadians();
        Rotation2d bearing = new Rotation2d(
                Math100.getMinDistance(measurement, m_fieldRelativeGoal.get().getRotation().getRadians()));

        // make sure the setpoint uses the modulus close to the measurement.
        thetaSetpoint = new State100(
                Math100.getMinDistance(measurement, thetaSetpoint.x()),
                thetaSetpoint.v());
                
        m_thetaGoalRaw = new State100(bearing.getRadians(), 0);
        m_xGoalRaw = new State100(m_fieldRelativeGoal.get().getX(),0,0);
        xSetpoint = xProfile.calculate(0.02, xSetpoint, m_xGoalRaw);

       
        m_yGoalRaw = new State100(m_fieldRelativeGoal.get().getY(),0,0);
        ySetpoint = yProfile.calculate(0.02, ySetpoint, m_yGoalRaw);

        // State100 thetaGoalRaw = new State100(m_robotRelativeGoal.get().getRotation().getRadians(),0,0);
        thetaSetpoint = thetaProfile.calculate(0.02, thetaSetpoint, m_thetaGoalRaw);
        SwerveState goalState = new SwerveState(xSetpoint, ySetpoint, thetaSetpoint);
        Twist2d goal = m_controller.calculate(m_swerve.getState(), goalState);
        m_swerve.driveInFieldCoords(goal, 0.02);
               
    }

    @Override
    public boolean isFinished() {
        double xError = Math.abs(m_xGoalRaw.x() - m_swerve.getState().x().x());
        double yError = Math.abs(m_yGoalRaw.x() - m_swerve.getState().y().x());
        double thetaError = Math.abs(m_thetaGoalRaw.x() - m_swerve.getState().theta().x());

        // Telemetry.get().log(Level.DEBUG, "AH YES", "xError", xError);
        // Telemetry.get().log(Level.DEBUG, "AH YES", "yError", yError);
        // Telemetry.get().log(Level.DEBUG, "AH YES", "thetaError", thetaError);

        if(xError < 0.1){
            if(yError < 0.1){
                if(thetaError < 0.1){
                    return true;
                }
            }
        }

        return false;

    }

    @Override
    public void end(boolean interrupted) {
        m_swerve.stop();
    }

    
}

// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.team100.frc2024.motion;

import org.team100.frc2024.RobotState100;
import org.team100.frc2024.RobotState100.AmpState100;
import org.team100.frc2024.motion.amp.AmpSubsystem;

import edu.wpi.first.wpilibj2.command.Command;

public class ChangeAmpState extends Command {
  /** Creates a new ChangeAmpState. */
  AmpState100 m_state;
   AmpSubsystem m_amp; 
  public ChangeAmpState(AmpState100 state, AmpSubsystem amp) {
    // Use addRequirements() here to declare subsystem dependencies.
    m_state = state;
    m_amp = amp;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_amp.reset();
    RobotState100.changeAmpState(m_state);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    RobotState100.changeAmpState(AmpState100.NONE);

  }


  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}

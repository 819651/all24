package org.team100.frc2024.motion.intake;

import org.team100.lib.motion.simple.Speeding;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class Intake extends SubsystemBase implements Speeding  {
    public abstract void intake();
    public abstract void intakeSmart();

    public abstract void outtake();
    public abstract void stop();
    public abstract void runUpper();

}

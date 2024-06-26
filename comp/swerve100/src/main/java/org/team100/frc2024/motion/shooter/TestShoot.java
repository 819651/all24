package org.team100.frc2024.motion.shooter;

import edu.wpi.first.wpilibj2.command.Command;

public class TestShoot extends Command {
    private final Shooter m_shooter;

    public TestShoot(Shooter shooter) {
        m_shooter = shooter;
        addRequirements(m_shooter);
    }

    @Override
    public void execute() {
        m_shooter.forward();
        m_shooter.setAngle(0.445);
    }
}

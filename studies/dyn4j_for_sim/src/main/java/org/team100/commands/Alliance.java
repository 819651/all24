package org.team100.commands;

import org.team100.robot.RobotAssembly;

import edu.wpi.first.wpilibj2.command.Command;

/** Alliance coordinates the behavior of the member robots. */
public abstract class Alliance {

    public abstract void onEnd(RobotAssembly robot, Command command);
}

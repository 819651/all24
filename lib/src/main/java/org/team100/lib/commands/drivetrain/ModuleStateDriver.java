package org.team100.lib.commands.drivetrain;

import org.team100.lib.dashboard.Glassy;
import org.team100.lib.hid.DriverControl;

import edu.wpi.first.math.kinematics.SwerveModuleState;

public interface ModuleStateDriver extends Glassy {
    /**
     * @param input control units [-1,1]
     * @return module states
     */
    SwerveModuleState[] apply(DriverControl.Velocity input);
}

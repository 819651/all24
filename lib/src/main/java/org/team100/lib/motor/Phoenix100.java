package org.team100.lib.motor;

import java.util.function.Supplier;

import org.team100.lib.config.PIDConstants;
import org.team100.lib.util.Util;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

/** Utilities for CTRE Phoenix motors: Falcon, Kraken. */
public class Phoenix100 {

    public static void crash(Supplier<StatusCode> s) {
        StatusCode statusCode = s.get();
        if (statusCode.isError()) {
            throw new IllegalStateException(statusCode.toString());
        }
    }

    public static void warn(Supplier<StatusCode> s) {
        StatusCode statusCode = s.get();
        if (statusCode.isError()) {
            Util.warn(statusCode.toString());
        }
    }

    public static void baseConfig(TalonFXConfigurator conf) {
        TalonFXConfiguration base = new TalonFXConfiguration();
        crash(() -> conf.apply(base));
    }

    public static void motorConfig(TalonFXConfigurator conf, MotorPhase phase) {
        MotorOutputConfigs motorConfigs = new MotorOutputConfigs();
        motorConfigs.NeutralMode = NeutralModeValue.Brake;
        if (phase == MotorPhase.FORWARD) {
            motorConfigs.Inverted = InvertedValue.CounterClockwise_Positive;
        } else {
            motorConfigs.Inverted = InvertedValue.Clockwise_Positive;
        }
        crash(() -> conf.apply(motorConfigs));
    }

    /**
     * @see https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html
     * @see https://www.chiefdelphi.com/t/the-brushless-era-needs-sensible-default-current-limits/461056/51
     */
    public static void currentConfig(TalonFXConfigurator conf, double supply, double stator) {
        CurrentLimitsConfigs currentConfigs = new CurrentLimitsConfigs();
        currentConfigs.SupplyCurrentLimit = supply;
        currentConfigs.SupplyCurrentLimitEnable = true;
        currentConfigs.StatorCurrentLimit = stator;
        currentConfigs.StatorCurrentLimitEnable = true;
        crash(() -> conf.apply(currentConfigs));
    }

    public static void pidConfig(TalonFXConfigurator conf, PIDConstants pid) {
        Slot0Configs slot0Configs = new Slot0Configs();
        slot0Configs.kV = 0.0; // we use "arbitrary feedforward", not this.
        slot0Configs.kP = pid.getP();
        slot0Configs.kI = pid.getI();
        slot0Configs.kD = pid.getD();
        crash(() -> conf.apply(slot0Configs));
    }

    private Phoenix100() {
        //
    }

}

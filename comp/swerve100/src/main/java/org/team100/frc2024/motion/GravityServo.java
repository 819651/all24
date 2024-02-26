// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.team100.frc2024.motion;

import org.team100.lib.config.SysParam;
import org.team100.lib.controller.State100;
import org.team100.lib.encoder.AnalogEncoder100;
import org.team100.lib.encoder.Encoder100;
import org.team100.lib.encoder.SparkMaxEncoder;
import org.team100.lib.encoder.drive.NeoDriveEncoder;
import org.team100.lib.profile.Profile100;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.units.Angle100;
import org.team100.lib.units.Distance100;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;

//TODO I will fix this shitty shitty shitty class after SVR - Sanjan 
/** Add your docs here. */
public class GravityServo {

    String m_name;
    SysParam m_params;
    PIDController m_controller;
    Profile100 m_profile;
    CANSparkMax m_motor;
    Encoder100<Distance100> m_encoder;
    private final double m_period;
    double m_maxRadsM_S;
    Telemetry t = Telemetry.get();
    double m_gravityScale;
    int kCurrentLimit;



    private State100 m_goal = new State100(0, 0);
    private State100 m_setpoint = new State100(0, 0);

    public GravityServo(
        CANSparkMax motor,
        int currentLimit,
        String name,
        SysParam params,
        PIDController controller,
        Profile100 profile,
        int canID,
        double period,
        double gravityScale,
        Encoder100<Distance100> encoder
    ){
    
    
    m_period = period;
    m_motor = motor;
    m_motor.setIdleMode(IdleMode.kCoast);
    m_encoder = encoder;
    m_name = name;
    m_params = params;
    m_controller = controller;
    // m_controller.setIntegratorRange(-0.02, 0.02);
    m_controller.setTolerance(0.02);
    m_profile = profile;
    m_gravityScale = gravityScale;
    kCurrentLimit = currentLimit;
    m_motor.setSmartCurrentLimit(currentLimit);

    

    

    }

    public void reset(){
        // m_encoder.setPosition(0);
        m_controller.reset();
        m_setpoint = new State100(getPosition(), 0);
    }

    public double getPosition() {
        return m_encoder.getPosition();
    }

    public double getRawPosition() {
        return m_encoder.getPosition();
    }
    
    
    public void setPosition(double goal) {
        double measurement = m_encoder.getPosition();

        // use the modulus closest to the measurement.
        // note zero velocity in the goal.
        m_goal = new State100(goal, 0.0);

        m_setpoint = new State100(
                (m_setpoint.x()),
                m_setpoint.v());

        

        m_setpoint = m_profile.calculate(m_period, m_setpoint, m_goal);

        double u_FB = m_controller.calculate(measurement, m_setpoint.x());
        double u_FF = m_setpoint.v() * 0.006; //rot/s to rpm conversion

        double gravityTorque = 0.006 * Math.cos((m_encoder.getPosition() / m_params.gearRatio()));

        
        double staticFF = 0.01 * Math.signum(u_FF + u_FB);

        if ( Math.abs(m_goal.x() - measurement) < m_controller.getPositionTolerance() ){
            staticFF = 0;
        }

        double u_TOTAL = gravityTorque + u_FF + u_FB + staticFF;

        m_motor.set(u_TOTAL); 

        m_controller.setIntegratorRange(0, 0.1);

        t.log(Level.DEBUG, m_name, "u_FB", u_FB);
        t.log(Level.DEBUG, m_name, "u_FF", u_FF);
        t.log(Level.DEBUG, m_name, "static F", u_FF);
        t.log(Level.DEBUG, m_name, "gravity T", gravityTorque);
        t.log(Level.DEBUG, m_name, "DUTY CYCLE", m_motor.getAppliedOutput());

        t.log(Level.DEBUG, m_name, "u_TOTAL", u_TOTAL);
        t.log(Level.DEBUG, m_name, "Measurement", measurement);
        t.log(Level.DEBUG, m_name, "Goal", m_goal);
        t.log(Level.DEBUG, m_name, "Setpoint", m_setpoint);
        t.log(Level.DEBUG, m_name, "Setpoint Velocity", m_setpoint.v());
        t.log(Level.DEBUG, m_name, "Controller Position Error", m_controller.getPositionError());
        t.log(Level.DEBUG, m_name, "Controller Velocity Error", m_controller.getVelocityError());
        t.log(Level.DEBUG, m_name, "COOSIIINEEE", Math.cos((m_encoder.getPosition()/ m_params.gearRatio())));
        t.log(Level.DEBUG, m_name, "POSE * GEAR RAT", m_encoder.getPosition()/ m_params.gearRatio());

    }

    public void setDutyCycle(double value){
        m_motor.set(value);
    }

    public void setPositionWithSteadyState(double goal) {
        double measurement = m_encoder.getPosition();

        // use the modulus closest to the measurement.
        // note zero velocity in the goal.
        m_goal = new State100(goal, 0.0);

        m_setpoint = new State100(
                (m_setpoint.x()),
                m_setpoint.v());

        System.out.println("SETPOINT XXXXX" + m_setpoint.x());


        

        double diff = m_goal.x() - m_setpoint.x();

        m_setpoint = m_profile.calculate(m_period, m_setpoint, m_goal);

        double u_FB = m_controller.calculate(measurement, m_setpoint.x());
        // double u_FF = m_setpoint.v() * 2; //rot/s to rpm conversion

        double gravityTorque = 0.015 * Math.cos((m_encoder.getPosition() / m_params.gearRatio()));
        double u_TOTAL = gravityTorque + u_FB;
        
        // if(diff < 0.1){
        //     m_motor.set(0.05);
        // } else {
            m_motor.set(u_TOTAL); 
        // }


        m_controller.setIntegratorRange(0, 0.1);

        t.log(Level.DEBUG, m_name, "u_FB", u_FB);
        // t.log(Level.DEBUG, m_name, "u_FF", u_FF);
        t.log(Level.DEBUG, m_name, "GRAVITY", gravityTorque);
        t.log(Level.DEBUG, m_name, "u_TOTAL", u_TOTAL);
        t.log(Level.DEBUG, m_name, "Measurement", measurement);
        t.log(Level.DEBUG, m_name, "Goal", m_goal);
        t.log(Level.DEBUG, m_name, "Setpoint", m_setpoint);
        t.log(Level.DEBUG, m_name, "Setpoint Velocity", m_setpoint.v());
        t.log(Level.DEBUG, m_name, "Controller Position Error", m_controller.getPositionError());
        t.log(Level.DEBUG, m_name, "Controller Velocity Error", m_controller.getVelocityError());
        t.log(Level.DEBUG, m_name, "COOSIIINEEE", Math.cos((m_encoder.getPosition()/ m_params.gearRatio())));
        t.log(Level.DEBUG, m_name, "POSE * GEAR RAT", m_encoder.getPosition()/ m_params.gearRatio());

    }
    public void periodic(){
        t.log(Level.DEBUG, m_name, "Get Raw Position", getRawPosition());
        t.log(Level.DEBUG, m_name, "AMPS", m_motor.getOutputCurrent());
        t.log(Level.DEBUG, m_name, "ENCODEr", m_encoder.getPosition());




    }

    public void set(double value) {
       m_motor.set(value);

    }

    public void stop(){
        m_motor.set(0);
    }



   



}

package org.team100.lib.competition;

import java.util.OptionalInt;

import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.telemetry.Telemetry.Level;
import org.team100.lib.util.Util;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;

public class CompetitionData {

    public static Telemetry t;

    public static void startTelemetry() {
        t = Telemetry.get();
    }

    public static void startupData() {

        MatchType matchType = DriverStation.getMatchType();
        int matchNumber = DriverStation.getMatchNumber();

        switch (matchType) {
            case Practice:
                Util.println("PRACTICE MATCH: " + matchNumber);
                break;
            case Qualification:
                Util.println("QUALS MATCH: " + matchNumber);
                break;
            case Elimination:
                Util.println("ELIM MATCH: " + matchNumber);
                break;
            default:
                Util.println("");
                OptionalInt stationNumber = DriverStation.getLocation();
                Util.println("Station Number: " + stationNumber);

        }

    }

    public static void logMatchTime() {
        Telemetry.get().log(Level.DEBUG, "Competition Data", "Match Time Remaining", DriverStation.getMatchTime());

    }

    public static double getMatchTime() {
        return DriverStation.getMatchTime();
    }
}

package head_latest;

import battlecode.common.*;
import head_latest.bots.*;

public strictfp class RobotPlayer {
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        RobotAI ai = null;
        switch (rc.getType()) {
            case HEADQUARTERS: ai = new HeadquartersAI(rc); break;
            case CARRIER: ai = new CarrierAI(rc); break;
            case LAUNCHER: ai = new LauncherAI(rc); break;
            case BOOSTER: ai = new BoosterAI(rc); break;
            case DESTABILIZER: ai = new DestabilizerAI(rc); break;
            case AMPLIFIER: ai = new AmplifierAI(rc); break;
        }

        while (true) {
            try {
                ai.run();
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}

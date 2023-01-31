package head_v4_1;

import battlecode.common.*;
import head_v4_1.bots.*;

public strictfp class RobotPlayer {
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        RobotAI ai = null;
        int myID = rc.getID();
        switch (rc.getType()) {
            case HEADQUARTERS: ai = new HeadquartersAI(rc, myID); break;
            case CARRIER: ai = new CarrierAI(rc, myID); break;
            case LAUNCHER: ai = new LauncherAI(rc, myID); break;
            case BOOSTER: ai = new BoosterAI(rc, myID); break;
            case DESTABILIZER: ai = new DestabilizerAI(rc, myID); break;
            case AMPLIFIER: ai = new AmplifierAI(rc, myID); break;
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

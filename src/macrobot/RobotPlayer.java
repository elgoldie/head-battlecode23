package macrobot;

import battlecode.common.*;
import macrobot.bots.*;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
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

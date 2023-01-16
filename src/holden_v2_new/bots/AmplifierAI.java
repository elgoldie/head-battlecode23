package holden_v2_new.bots;

import battlecode.common.*;

public class AmplifierAI extends RobotAI {
    
    public AmplifierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        for (RobotInfo robot : rc.senseNearbyRobots(20, myTeam)) {
            if (robot.getType() == RobotType.AMPLIFIER) {
                
            }
        }
    }
}

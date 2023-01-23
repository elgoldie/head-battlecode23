/* package pathbot.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public int anchorCraftCooldown;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        anchorCraftCooldown = 0;
    }

    int build = 0;
    @Override
    public void run() throws GameActionException {
        super.run();
        if (build < 1 && rc.canBuildRobot(RobotType.CARRIER, rc.getLocation().add(Direction.NORTHEAST))) {
            rc.buildRobot(RobotType.CARRIER, rc.getLocation().add(Direction.NORTHEAST));
            build ++;
        }
        if (rc.getRoundNum() > 100) { rc.resign(); }
    }
}
 */
package head_v1.bots;

import battlecode.common.*;

public class BoosterAI extends RobotAI {
    
    public BoosterAI(RobotController rc, int id) {
        super(rc, id);
    }

    @Override
    public void run(int turn) throws GameActionException {
        System.out.println(turn);
    }
}

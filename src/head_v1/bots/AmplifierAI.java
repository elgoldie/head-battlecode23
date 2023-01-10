package head_v1.bots;

import battlecode.common.*;

public class AmplifierAI extends RobotAI {
    
    public AmplifierAI(RobotController rc, int id) {
        super(rc, id);
    }

    @Override
    public void run(int turn) throws GameActionException {
        System.out.println(turn);
    }
}

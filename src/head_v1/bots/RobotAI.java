package head_v1.bots;

import java.util.Random;

import battlecode.common.*;

public abstract class RobotAI {

    public RobotController rc;
    public int id;

    public Random rng;

    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public RobotAI(RobotController rc, int id) {
        this.rc = rc;
        this.id = id;
        this.rng = new Random(rc.getID());
    }
    
    public void run(int turn) throws GameActionException { }
}

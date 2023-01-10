package head_v1.bots;

import java.util.Random;

import battlecode.common.*;
import head_v1.util.Communication;

public abstract class RobotAI {

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

    public Team myTeam;
    public Team enemyTeam;

    public RobotController rc;
    public Communication comm;
    public int id;

    public int gameTurn;
    public int aliveTurns;

    public MapLocation spawnLocation;
    public MapLocation[] hqLocations;

    public String assignment = "idle";
    public String command = "assign";

    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        this.rng = new Random(id);
        this.comm = new Communication(rc);

        this.gameTurn = rc.getRoundNum() - 1;
        this.aliveTurns = 0;
        this.spawnLocation = rc.getLocation();

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
        this.hqLocations = comm.readLocationArray(0);
    }
    
    public void run() throws GameActionException {
        comm.clearCache();
        gameTurn += 1;
        aliveTurns += 1;
        // read all
    }

    public void wander() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}

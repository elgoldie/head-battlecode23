package head_v1.bots;

import java.util.Random;

import battlecode.common.*;
import head_v1.util.Communication;

public abstract class RobotAI {

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
    
    public Random rng;
    public int seed;

    public Team myTeam;
    public Team enemyTeam;

    public RobotController rc;
    public Communication comm;
    public int id;

    public int aliveTurns;

    public MapLocation spawnLocation;
    public MapLocation[] hqLocations;

    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        
        this.rng = new Random(id);
        this.seed = rng.nextInt();

        this.comm = new Communication(rc);
        
        this.aliveTurns = 0;
        this.spawnLocation = rc.getLocation();

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();

        this.hqLocations = comm.readLocationArray(0);
    }
    
    public void run() throws GameActionException {
        aliveTurns += 1;
    }

    public void wander() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) rc.move(dir);
    }

    public void tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public void tryMoveOrWander(Direction dir) throws GameActionException {
        if (rc.canMove(dir))
            rc.move(dir);
        else
            wander();
    }

    public MapLocation closestHeadquarters() throws GameActionException {
        if (hqLocations == null) {
            return spawnLocation;
        }
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = 0; i < hqLocations.length; i++) {
            int newDist = hqLocations[i].distanceSquaredTo(rc.getLocation());
            if (newDist < dist) {
                dist = newDist;
                loc = hqLocations[i];
            }
        }
        return loc;
    }
}

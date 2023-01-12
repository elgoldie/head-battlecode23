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

    public static final Direction[] orthogonalDirections = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST
    };

    public static final Direction[] diagonalDirections = {
        Direction.NORTHEAST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
        Direction.NORTHWEST
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
        
        if (rc.getType() != RobotType.HEADQUARTERS)
            scanForIslands();
        
        if (rc.canWriteSharedArray(0, 0) && comm.queueActive)
            comm.queueFlush();
    }

    public void wander() throws GameActionException {
        if (!tryMove(diagonalDirections[rng.nextInt(4)]))
            tryMove(orthogonalDirections[rng.nextInt(4)]);
    }

    public boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else {
            return false;
        }
    }

    public boolean tryMoveOrWander(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else {
            wander();
            return false;
        }
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

    public MapLocation closestIsland(Team team) throws GameActionException {
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;
        for (int index = 5; index < rc.getIslandCount() + 5; index++) {
            if (comm.readLocationFlags(index) == team.ordinal()) {
                MapLocation loc = comm.readLocation(index);
                int dist = loc.distanceSquaredTo(rc.getLocation());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = loc;
                }
            }
        }
        return closest;
    }

    public void scanForIslands() throws GameActionException {        
        for (int index : rc.senseNearbyIslands()) {
            // if island is undiscovered
            int newFlag = rc.senseTeamOccupyingIsland(index).ordinal();
            
            if (comm.hasNoLocation(index + 4)) {
                MapLocation islandLocation = rc.senseNearbyIslandLocations(index)[0];
                comm.writeLocation(index + 4, islandLocation, newFlag);
            } else if (comm.readLocationFlags(index + 4) != newFlag) {
                comm.writeLocationFlags(index + 4, newFlag);
            }
        }
    }
}

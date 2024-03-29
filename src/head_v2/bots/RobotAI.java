package head_v2.bots;

import java.util.Random;

import battlecode.common.*;
import head_v2.comm.Communication;
import head_v2.path.NaivePathfinding;
import head_v2.path.Pathfinding;

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
    public Pathfinding pathing;
    public int id;

    public int aliveTurns;

    public MapLocation spawnLocation;

    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        
        this.rng = new Random(id);
        this.seed = rng.nextInt();

        this.comm = new Communication(rc);
        this.pathing = new NaivePathfinding(rc);
        
        this.aliveTurns = 0;
        this.spawnLocation = rc.getLocation();

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
    }
    
    public void run() throws GameActionException {
        aliveTurns += 1;

        if (this.aliveTurns > 500) {
            rc.resign();
        }
        
        if (rc.getType() != RobotType.HEADQUARTERS) {
            scanForIslands();
            scanForWells();
        }

        for (RobotInfo robot : rc.senseNearbyRobots(20, enemyTeam)) {
            if (robot.getType() == RobotType.HEADQUARTERS) {
                comm.appendLocation(4, robot.getLocation());
            }
        }
        
        if (rc.canWriteSharedArray(0, 0) && comm.queueActive)
            comm.queueFlush();
    }

    public void postRun() throws GameActionException {
        comm.keepAliveFlush();
    }

    public void wander() throws GameActionException {
        if (!tryMove(diagonalDirections[rng.nextInt(4)]))
            tryMove(orthogonalDirections[rng.nextInt(4)]);
    }

    public void wanderOrthogonal() throws GameActionException {
        if (!tryMove(orthogonalDirections[rng.nextInt(4)]))
            tryMove(diagonalDirections[rng.nextInt(4)]);
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

    public MapLocation closestHeadquarters(Team team) throws GameActionException {

        int offset = comm.HQ_FRIENDLY_OFFSET;
        if (team != myTeam) {
            offset = comm.HQ_ENEMY_OFFSET;
        }

        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            MapLocation hqLoc = comm.readLocation(offset + i);
            if (hqLoc == null) break;
            int newDist = hqLoc.distanceSquaredTo(rc.getLocation());
            if (newDist < dist) {
                dist = newDist;
                loc = hqLoc;
            }
        }
        return loc;
    }

    public MapLocation closestIsland(Team team) throws GameActionException {
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;
        for (int index = comm.ISLAND_OFFSET; index < rc.getIslandCount() + comm.ISLAND_OFFSET; index++) {
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
            int commIndex = comm.ISLAND_OFFSET + index - 1;

            if (!comm.hasLocation(commIndex)) {
                MapLocation islandLocation = rc.senseNearbyIslandLocations(index)[0];
                comm.writeLocation(commIndex, islandLocation, newFlag);
            } else if (comm.readLocationFlags(commIndex) != newFlag) {
                comm.writeLocationFlags(commIndex, newFlag);
            }
        }
    }

    public void scanForWells() throws GameActionException {
        for (WellInfo well : rc.senseNearbyWells()) {
            if (well.getResourceType() == ResourceType.ADAMANTIUM && !comm.hasLocation(comm.WELL_ADAMANTIUM_OFFSET + 3)) {
                comm.queueAppend(comm.WELL_ADAMANTIUM_OFFSET, well.getMapLocation());
            } else if (well.getResourceType() == ResourceType.MANA && !comm.hasLocation(comm.WELL_MANA_OFFSET + 3)) {
                comm.queueAppend(comm.WELL_MANA_OFFSET, well.getMapLocation());
            }
        }
    }

    public int amountOfTypeNearby(int radiusSquared, Team team, RobotType type) throws GameActionException {
        int count = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(radiusSquared, team)) {
            if (robot.getType() == type) {
                count += 1;
            }
        }
        return count;
    }
}

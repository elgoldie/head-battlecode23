package head_v3.bots;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;
import head_v3.comm.Communication;
import head_v3.path.Pathfinding;
import head_v3.path.Symmetry;
import head_v3.path.WaypointPathfinding;

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

    public MapLocation destination;

    public void stepTowardsDestination(MapLocation loc) throws GameActionException {
        if (loc != destination) {
            destination = loc;
            pathing.initiate_pathfinding(destination);
        }

        Direction dir = pathing.findPath();
        tryMove(dir);
        // Direction direction = rc.getLocation().directionTo(loc);
        // if (direction == Direction.CENTER) return;

        // for (int i = 0; i < 8; i++) {
        //     if (rc.canMove(direction)) tryMove(direction);
        //     direction = direction.rotateLeft();
        // }
    }

    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        
        this.rng = new Random(id);
        this.seed = rng.nextInt();

        this.comm = new Communication(rc);
        this.pathing = new WaypointPathfinding(rc);
        
        this.aliveTurns = 0;
        this.spawnLocation = rc.getLocation();

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
    }
    
    public void run() throws GameActionException {
        aliveTurns += 1;
        
        if (rc.getType() != RobotType.HEADQUARTERS) {
            scanForIslands();
            scanForSymmetryConflicts();
        }

        rc.setIndicatorString(comm.read(61) + " " + comm.read(62) + " " + comm.read(63));
        
        if (rc.canWriteSharedArray(0, 0) && comm.queueActive)
            comm.queueFlush();
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

    public MapLocation closestHeadquarters() throws GameActionException {
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            MapLocation hqLoc = comm.readLocation(i);
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
        for (int index = 9; index < rc.getIslandCount() + 9; index++) {
            if (comm.readLocationFlags(index) == team.ordinal()) {
                MapLocation loc = comm.readLocation(index);
                if (loc == null) continue;
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
            
            if (comm.hasNoLocation(index + 8)) {
                MapLocation islandLocation = rc.senseNearbyIslandLocations(index)[0];
                comm.writeLocation(index + 8, islandLocation, newFlag);
            } else if (comm.readLocationFlags(index + 8) != newFlag) {
                comm.writeLocationFlags(index + 8, newFlag);
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

    public void eliminateSymmetry(Symmetry symmetry) throws GameActionException {
        comm.write(63 - symmetry.ordinal(), 1);
    }

    public Symmetry[] getValidSymmetries() throws GameActionException {
        Symmetry[] symmetries = new Symmetry[3];
        int count = 0;
        for (Symmetry symmetry : Symmetry.values()) {
            if (comm.read(63 - symmetry.ordinal()) == 0) {
                symmetries[count] = symmetry;
                count += 1;
            }
        }
        Symmetry[] result = new Symmetry[count];
        System.arraycopy(symmetries, 0, result, 0, count);
        return result;
    }

    public MapLocation[] getPossibleEnemyHQLocations() throws GameActionException {
        Symmetry[] symmetries = getValidSymmetries();
        MapLocation[] locations = new MapLocation[4 * symmetries.length];
        int count = 0;
        for (int hqIndex = 0; hqIndex < 4; hqIndex++) {
            MapLocation hqLoc = comm.readLocation(hqIndex);
            if (hqLoc == null) break;
            for (int i = 0; i < symmetries.length; i++) {
                locations[i + hqIndex * symmetries.length] = symmetries[i].transform(rc, hqLoc);
                count += 1;
            }
        }
        MapLocation[] result = new MapLocation[count];
        System.arraycopy(locations, 0, result, 0, count);
        return result;
    }

    public void scanForSymmetryConflicts() throws GameActionException {
        ArrayList<MapLocation> actualEnemyHQLocations = new ArrayList<>();
        for (RobotInfo robot : rc.senseNearbyRobots(1000, enemyTeam)) {
            if (robot.getType() == RobotType.HEADQUARTERS) {
                actualEnemyHQLocations.add(robot.getLocation());
            }
        }

        MapLocation[] myHQs = comm.readLocationsNonNull(0, 4);
        for (Symmetry symmetry : getValidSymmetries()) {
            for (MapLocation myHQ : myHQs) {
                MapLocation whereItShouldBe = symmetry.transform(rc, myHQ);
                if (rc.canSenseLocation(whereItShouldBe) && !actualEnemyHQLocations.contains(whereItShouldBe)) {
                    eliminateSymmetry(symmetry);
                    break;
                }
            }
        }
    }
}

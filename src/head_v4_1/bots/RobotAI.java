package head_v4_1.bots;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;
import head_v4_1.comm.Communication;
import head_v4_1.path.Pathfinding;
import head_v4_1.path.Symmetry;
import head_v4_1.path.NaivePathfinding;

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

    public MapLocation destination;

    /**
     * Makes a step towards the destination using the pathfinding object.
     * @param loc The new destination
     * @return Whether the step was successful
     * @throws GameActionException
     */
    public boolean stepTowardsDestination(MapLocation loc) throws GameActionException {
        // detect if the destination has changed
        if (!loc.equals(destination)) {
            destination = loc;
            pathing.initPathfinding(destination);
        }

        // make a step
        Direction dir = pathing.findPath();
        for (RobotInfo robot : rc.senseNearbyRobots(-1, enemyTeam)) {
            if (robot.getType() == RobotType.HEADQUARTERS && rc.getLocation().add(dir).distanceSquaredTo(robot.getLocation()) <= 9) {
                return false;
            }
        }
        return tryMove(dir);
    }

    protected final int[] attackPriorities = {
        1, // HEADQUARTERS
        4, // CARRIER
        6, // LAUNCHER
        2, // DESTABILIZER
        3, // BOOSTER
        5, // AMPLIFIER
    };

    /**
     * Returns a value heuristic that determines attack priority.
     * @param robot The robot to evaluate
     * @return The value of the robot
     */
    public MapLocation getAttackTarget(int radiusSquared) throws GameActionException {
        MapLocation bestRobot = null;
        int bestPriority = 0;
        int bestHealth = 0;

        for (RobotInfo robot : rc.senseNearbyRobots(radiusSquared, enemyTeam)) {
            if (robot.type == RobotType.HEADQUARTERS) continue;
            else if (attackPriorities[robot.type.ordinal()] < bestPriority) continue;

            if (bestRobot == null
            || attackPriorities[robot.type.ordinal()] > bestPriority
            || robot.health < bestHealth) {
                bestRobot = robot.location;
                bestPriority = attackPriorities[robot.type.ordinal()];
                bestHealth = robot.health;
            }
        }
        return bestRobot;
    }


    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        
        this.rng = new Random(id);
        this.seed = rng.nextInt();

        this.comm = new Communication(rc);
        // this.pathing = new WaypointPathfinding(rc);
        this.pathing = new NaivePathfinding(rc);
        
        this.aliveTurns = 0;

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
    }
    
    /**
     * Run every turn.
     * @throws GameActionException
     */
    public void run() throws GameActionException {
        aliveTurns += 1;
        
        // headquarters don't need to scan, since they don't move
        if (rc.getType() != RobotType.HEADQUARTERS) {
            scanForIslands();
            scanForSymmetryConflicts();
            scanForWells();
        
            // flush the queue
            if (rc.canWriteSharedArray(0, 0) && comm.queueActive)
                comm.queueFlush();
        }
    }

    /**
     * Wanders in a random direction, prioritizing diagonals.
     * @throws GameActionException
     */
    public void wander() throws GameActionException {
        if (!tryMove(diagonalDirections[rng.nextInt(4)]))
            tryMove(orthogonalDirections[rng.nextInt(4)]);
    }

    /**
     * Wanders in a random direction, prioritizing orthogonals.
     * @throws GameActionException
     */
    public void wanderOrthogonal() throws GameActionException {
        if (!tryMove(orthogonalDirections[rng.nextInt(4)]))
            tryMove(diagonalDirections[rng.nextInt(4)]);
    }

    /**
     * Tries to move in a given direction and returns the result.
     * @param dir The direction to move in
     * @return Whether the move succeeded
     * @throws GameActionException
     */
    public boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to move in a given direction, and if it fails, wanders.
     * @param dir The direction to move in
     * @return Whether the move succeeded
     * @throws GameActionException
     */
    public boolean tryMoveOrWander(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else {
            wander();
            return false;
        }
    }

    /**
     * Returns the average position of nearby robots of a given team and type.
     * @param team The team of the robots
     * @param type The type of the robots
     * @return The average position
     * @throws GameActionException
     */
    public MapLocation averagePositionOfNearbyRobots(Team team, RobotType type) throws GameActionException {
        int x = 0;
        int y = 0;
        int count = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(-1, team)) {
            if (type == null || robot.type == type) {
                x += robot.location.x;
                y += robot.location.y;
                count += 1;
            }
        }
        if (count == 0) return rc.getLocation();
        return new MapLocation(x / count, y / count);
    }

    public MapLocation averagePositionOfNearbyRobots(Team team) throws GameActionException {
        return averagePositionOfNearbyRobots(team, null);
    }

    /**
     * Returns the closest friendly headquarters.
     * @return The headquarters' coordinates
     * @throws GameActionException
     */
    public MapLocation closestHeadquarters() throws GameActionException {
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = comm.HQ_OFFSET; i < comm.HQ_OFFSET + 4; i++) {
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

    /**
     * Returns the closest island controlled by a given team (according to the comm system).
     * @param team The team to check for
     * @return The closest island controlled by the given team
     * @throws GameActionException
     */
    public MapLocation closestIsland(Team team) throws GameActionException {
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;
        // start at 4 because the first 4 locations are reserved for HQs
        for (int index = comm.ISLAND_OFFSET; index < comm.ISLAND_OFFSET + rc.getIslandCount(); index++) {
            if (comm.readFlags(index) == team.ordinal()) {
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

    /**
     * Scans for islands and updates the comm system accordingly.
     * @throws GameActionException
     */
    public void scanForIslands() throws GameActionException {        
        for (int index : rc.senseNearbyIslands()) {

            int newFlag = rc.senseTeamOccupyingIsland(index).ordinal();
            
            if (!comm.hasLocation(index - 1 + comm.ISLAND_OFFSET)) {
                MapLocation islandLocation = rc.senseNearbyIslandLocations(index)[0];
                comm.writeLocation(index - 1 + comm.ISLAND_OFFSET, islandLocation, newFlag);
            } else if (comm.readFlags(index - 1 + comm.ISLAND_OFFSET) != newFlag) {
                comm.writeFlags(index - 1 + comm.ISLAND_OFFSET, newFlag);
            }
        }
    }

    /**
     * Marks a symmetry as invalid in the comm system.
     * @param symmetry The symmetry to mark as invalid
     * @throws GameActionException
     */
    public void eliminateSymmetry(Symmetry symmetry) throws GameActionException {
        comm.write(63 - symmetry.ordinal(), 1);
    }

    /**
     * Returns an array of all possible symmetries, according to the comm system.
     * @return An array of all possible symmetries
     * @throws GameActionException
     */
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

    /**
     * Returns an array of all possible enemy HQ locations, given the current
     * knowledge of the map's symmetries.
     * @return An array of all possible enemy HQ locations
     * @throws GameActionException
     */
    public MapLocation[] getPossibleEnemyHQLocations() throws GameActionException {
        Symmetry[] symmetries = getValidSymmetries();
        MapLocation[] locations = new MapLocation[4 * symmetries.length];
        int count = 0;
        for (int hqIndex = comm.HQ_OFFSET; hqIndex < comm.HQ_OFFSET + 4; hqIndex++) {
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

    /**
     * Scans for symmetry conflicts based on nearby headquarters
     * and eliminates them.
     * @throws GameActionException
     */
    public void scanForSymmetryConflicts() throws GameActionException {
        ArrayList<MapLocation> actualEnemyHQLocations = new ArrayList<>();
        for (RobotInfo robot : rc.senseNearbyRobots(-1, enemyTeam)) {
            if (robot.getType() == RobotType.HEADQUARTERS) {
                actualEnemyHQLocations.add(robot.getLocation());
            }
        }

        MapLocation[] myHQs = comm.readLocationsUntilNull(0, 4);
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

    public void scanForWells() throws GameActionException {
        for (WellInfo well : rc.senseNearbyWells()) {
            comm.appendWell(well);
        }
    }
}

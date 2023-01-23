package head_latest.bots;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    private enum SpawningState {
        // insert different spawning states
        OPENING, // game opening? turns 1 and 2
        EARLYGAME, // early game ()
        MIDGAME // mid game
        // saving resources mode?
    }

    public SpawningState state;

    public int anchorCraftCooldown;
    public int myIndex;
    public RobotType typeToSpawn;

    public ArrayList<MapLocation> spawningLocations;

    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        state = SpawningState.OPENING;
        anchorCraftCooldown = 0;
        myIndex = -1;

        spawningLocations = new ArrayList<>();
    }

    public ArrayList<MapLocation> getFreeSpawningLocations() throws GameActionException {
        int radius = rc.getType().actionRadiusSquared;
        MapLocation[] allLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), radius);
        RobotInfo[] blockedLocations = rc.senseNearbyRobots(radius);

        ArrayList<MapLocation> freeLocations = new ArrayList<>(Arrays.asList(allLocations));
        for (RobotInfo robot : blockedLocations)
            freeLocations.remove(robot.location);

        return freeLocations;
    }

    /**
     * Tries to build a robot, and removes the location from the spawning locations
     * @param type the type of robot to build
     * @param loc the location to build the robot
     * @return true if the robot was built, false otherwise
     * @throws GameActionException
     */
    public boolean tryToBuildRobot(RobotType type, MapLocation loc) throws GameActionException {
        if (rc.canBuildRobot(type, loc)) {
            rc.buildRobot(type, loc);
            spawningLocations.remove(loc);
            return true;
        }
        return false;
    }

    // SPAWNING BEHAVIORS

    /**
     * Spawning behavior on turns 1 and 2. Turn 1 spawns 4 carriers, 1 launcher.
     * Turn 2 spawns 2 launchers.
     * @throws GameActionException
     */
    public void behaviorOpeningSpawning() throws GameActionException {
        if (rc.getRoundNum() == 1) {
            for (int i = 0; i < 4; i++) {
                MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
                tryToBuildRobot(RobotType.CARRIER, newLoc);
            }
            MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
            tryToBuildRobot(RobotType.LAUNCHER, newLoc);
        } else {
            for (int i = 0; i < 2; i++) {
                MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
                tryToBuildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
    }

    /**
     * Spawning behavior on for early game. Spawns carriers and launcher with equal
     * frequency (attempts to spawn up to 5 robots per turn)
     * @throws GameActionException
     */
    public void behaviorEarlyGameSpawning() throws GameActionException {
        
        MapLocation newLoc;
        for (int i = 0; i < 5; i++) {
            newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
            if (rng.nextInt(2) == 0) {
                typeToSpawn = RobotType.CARRIER;
            } else {
                typeToSpawn = RobotType.LAUNCHER;
            }
            tryToBuildRobot(typeToSpawn, newLoc);
        }
    }

    /**
     * Spawning behavior on for mid game. Spawns carriers and launcher with in a 1:4
     * ratio (attempts to spawn up to 5 robots per turn)
     * @throws GameActionException
     */
    public void behaviorMidGameSpawning() throws GameActionException {

        MapLocation newLoc;
        for (int i = 0; i < 5; i++) {
            newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
            if (rng.nextInt(5) == 0) {
                if (rng.nextInt(8) == 0) {
                    typeToSpawn = RobotType.CARRIER;
                } else {
                    typeToSpawn = RobotType.LAUNCHER;
                }
                tryToBuildRobot(typeToSpawn, newLoc);
            }
        }
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        spawningLocations = getFreeSpawningLocations();

        rc.setIndicatorString(state.toString());
        anchorCraftCooldown -= 1;

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            myIndex = comm.appendLocation(comm.HQ_OFFSET, rc.getLocation());

        } else {

            // handle distress signals
            int amountEnemies = rc.senseNearbyRobots(20, enemyTeam).length;
            if (amountEnemies >= 3) {
                // distress signal
                comm.writeFlags(myIndex, 1);
            } else {
                // safe signal
                comm.writeFlags(myIndex, 0);
            }
        }

        if (rc.getRobotCount() > 10 && anchorCraftCooldown <= 0 && rc.getNumAnchors(Anchor.STANDARD) == 0) {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                // System.out.println("I just built an anchor!");
                anchorCraftCooldown = 50;
            }
        }

        if (rc.canBuildAnchor(Anchor.ACCELERATING)) {
            rc.buildAnchor(Anchor.ACCELERATING);
        }

        if (rc.getRobotCount() >= 30) {
            state = SpawningState.MIDGAME;
        } else if (rc.getRoundNum() >= 3) {
            state = SpawningState.EARLYGAME;
        }

        if (!spawningLocations.isEmpty()) {
            switch (state) {
                case OPENING:
                    behaviorOpeningSpawning();
                    break;
                case EARLYGAME:
                    behaviorEarlyGameSpawning();
                    break;
                case MIDGAME:
                    behaviorMidGameSpawning();
                    break;
            }
        }

        // if (rc.getRoundNum() >= 200) rc.resign();
    }
}
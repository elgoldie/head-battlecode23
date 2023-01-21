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
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        state = SpawningState.OPENING;
        anchorCraftCooldown = 0;
        myIndex = -1;
    }

    public boolean canAffordToSpawnType(RobotType type) throws GameActionException {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) >= type.buildCostAdamantium
            && rc.getResourceAmount(ResourceType.MANA) >= type.buildCostMana
            && rc.getResourceAmount(ResourceType.ELIXIR) >= type.buildCostElixir;
    }
    
    public ArrayList<MapLocation> getFreeSpawningLocations() throws GameActionException {
        int radius = rc.getType().actionRadiusSquared;
        MapLocation[] allLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), radius);
        RobotInfo[] blockedLocations = rc.senseNearbyRobots(radius);

        ArrayList<MapLocation> freeLocations = new ArrayList<>(Arrays.asList(allLocations));
        for (RobotInfo robot : blockedLocations) freeLocations.remove(robot.location);
        
        return freeLocations;
    }


    // SPAWNING BEHAVIORS

    /**
     * Spawning behavior on turns 1 and 2. Turn 1 spawns 4 carriers, 1 launcher. Turn 2 spawns 2 launcher
     * @throws GameActionException
     */
    public void behaviorOpeningSpawning() throws GameActionException {
        ArrayList<MapLocation> spawningLocations = getFreeSpawningLocations();
        if (spawningLocations.size() == 0) {
            return;
        }
        if (rc.getRoundNum() == 1) {
            for (int i =0; i < 4; i++) {
                MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                }
                spawningLocations = getFreeSpawningLocations();
            }
            spawningLocations = getFreeSpawningLocations();
            MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        } else {
            for (int i =0; i < 3; i++) {
                MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
                if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                    rc.buildRobot(RobotType.LAUNCHER, newLoc);
                }
                spawningLocations = getFreeSpawningLocations();
            }
        }

    }


    /**
     * Spawning behavior on for early game. Spawns carriers and launcher with equal frequency (attempts to spawn up to 5 robots per turn)
     * @throws GameActionException
     */
    public void behaviorEarlyGameSpawning() throws GameActionException {
        ArrayList<MapLocation> spawningLocations = getFreeSpawningLocations();
        if (spawningLocations.size() == 0) {
            return;
        }
        MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));

        for (int i=0; i < 5; i++) {
            newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
            if (rng.nextInt(2) == 0) {
                typeToSpawn = RobotType.CARRIER;
            } else {
                typeToSpawn = RobotType.LAUNCHER;
            }
            if (rc.canBuildRobot(typeToSpawn, newLoc)) {
                rc.buildRobot(typeToSpawn, newLoc);
            }
        }
    }

        /**
     * Spawning behavior on for mid game. Spawns carriers and launcher with in a 1:3 ratio (attempts to spawn up to 5 robots per turn)
     * @throws GameActionException
     */
    public void behaviorMidGameSpawning() throws GameActionException {
        ArrayList<MapLocation> spawningLocations = getFreeSpawningLocations();
        if (spawningLocations.size() == 0) {
            return;
        }
        MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));

        for (int i=0; i < 5; i++) {
            newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
            if (rng.nextInt(5) == 0){
                if (rng.nextInt(5) == 0) {
                    typeToSpawn = RobotType.CARRIER;
                } else {
                    typeToSpawn = RobotType.LAUNCHER;
                }
                if (rc.canBuildRobot(typeToSpawn, newLoc)) {
                    rc.buildRobot(typeToSpawn, newLoc);
                }
            }

        }
    }

    










    @Override
    public void run() throws GameActionException {
        super.run();
        rc.setIndicatorString(state.toString());
        anchorCraftCooldown -= 1;

        // if (myIndex == 0 && rc.getRoundNum() % 10 == 0) {
        //     System.out.println(comm.dispArray());
        // }

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            myIndex = comm.appendLocation(comm.HQ_OFFSET, rc.getLocation());
            
        } else {

            // handle distress signals
            int amountEnemies = rc.senseNearbyRobots(20, enemyTeam).length;
            if (amountEnemies >= 3) {
                // distress signal
                comm.writeLocationFlags(myIndex, 1);
            } else {
                // safe signal
                comm.writeLocationFlags(myIndex, 0);
            }
        }
        
        ArrayList<MapLocation> spawningLocations = getFreeSpawningLocations();
        if (spawningLocations.size() == 0) {
            return;
        }
        MapLocation newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));

        if (rc.getRobotCount() > 10 && anchorCraftCooldown <= 0 && rc.getNumAnchors(Anchor.STANDARD) == 0) {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                System.out.println("I just built an anchor!");
                anchorCraftCooldown = 50;
            }
        }

        if (rc.getRobotCount() >= 30) {
            state = SpawningState.MIDGAME;
        } else if (rc.getRoundNum() >= 3) {
            state = SpawningState.EARLYGAME;
        } 


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









        // RobotType typeToSpawn = RobotType.CARRIER;
        // if (rc.getRoundNum() == 1) {
        //     for (int i =0; i < 4; i++) {
        //         newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
        //         if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
        //             rc.buildRobot(RobotType.CARRIER, newLoc);
        //         }
        //         spawningLocations = getFreeSpawningLocations();
        //     }
        //     spawningLocations = getFreeSpawningLocations();
        //     newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
        //     if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
        //         rc.buildRobot(RobotType.LAUNCHER, newLoc);
        //     }
        // } else if (rc.getRoundNum() == 2) {
        //     for (int i =0; i < 3; i++) {
        //         newLoc = spawningLocations.get(rng.nextInt(spawningLocations.size()));
        //         if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
        //             rc.buildRobot(RobotType.LAUNCHER, newLoc);
        //         }
        //         spawningLocations = getFreeSpawningLocations();
        //     }
        // } else {
        //     if (rng.nextInt(4) == 0) {
        //         typeToSpawn = RobotType.CARRIER;
        //     } else {
        //         typeToSpawn = RobotType.LAUNCHER;
        //     }
        // }

        // if (rc.canBuildRobot(typeToSpawn, newLoc)) {
        //     rc.buildRobot(typeToSpawn, newLoc);
        // }

        // if (rc.getRoundNum() >= 200) rc.resign();
    }
}
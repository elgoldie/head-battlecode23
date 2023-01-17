package head_latest.bots;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public int anchorCraftCooldown;
    public int myIndex;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
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

    @Override
    public void run() throws GameActionException {
        super.run();
        anchorCraftCooldown -= 1;

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            myIndex = comm.appendLocation(0, rc.getLocation());
            
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

        RobotType typeToSpawn = RobotType.CARRIER;
        if (rc.getRoundNum() <= 4) {
            typeToSpawn = RobotType.CARRIER;
        } else if (rc.getRoundNum() <= 7) {
            typeToSpawn = RobotType.LAUNCHER;
        } else {
            if (rng.nextInt(4) == 0) {
                typeToSpawn = RobotType.CARRIER;
            } else {
                typeToSpawn = RobotType.LAUNCHER;
            }
        }

        if (rc.canBuildRobot(typeToSpawn, newLoc)) {
            rc.buildRobot(typeToSpawn, newLoc);
        }

        // if (rc.getRoundNum() >= 200) rc.resign();
    }
}

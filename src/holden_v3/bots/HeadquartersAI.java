package holden_v3.bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public boolean savingUpForAnchor;
    public int myIndex;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        savingUpForAnchor = false;
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

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            myIndex = comm.appendLocation(0, rc.getLocation());
        } else {
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

        if (!savingUpForAnchor) {

            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc))
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            else if (rc.canBuildRobot(RobotType.CARRIER, newLoc))
                rc.buildRobot(RobotType.CARRIER, newLoc);
            else
                return;
            
            savingUpForAnchor = rng.nextInt(5) == 0;
        
        } else if (rc.canBuildAnchor(Anchor.STANDARD)) {

            rc.buildAnchor(Anchor.STANDARD);
            savingUpForAnchor = rng.nextInt(5) == 0;
        }
    }
}

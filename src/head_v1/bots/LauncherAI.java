package head_v1.bots;

import battlecode.common.*;

public class LauncherAI extends RobotAI {
    
    public LauncherAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    public int enemyValue(RobotInfo robot) {
        return -robot.health;
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        int radius = rc.getType().actionRadiusSquared;

        int maxValue = Integer.MIN_VALUE;
        RobotInfo target = null;
        for (RobotInfo robot : rc.senseNearbyRobots(radius, enemyTeam)) {
            int value = enemyValue(robot);
            if (value > maxValue) {
                maxValue = value;
                target = robot;
            }
        }

        if (target != null && rc.canAttack(target.location)) {
            rc.setIndicatorString("Attacking");        
            rc.attack(target.location);
        }

        // MapLocation island = closestIsland(myTeam);
        // MapLocation headquarters = closestHeadquarters();
        // boolean atIsland = rc.getLocation().equals(island);
        // if (island == null) {
        //     wander();
        //     return;
        // }

        // if (!rc.getLocation().equals(island)) {
        //     Direction dir = rc.getLocation().directionTo(island);
        //     tryMove(dir);
        // } else {
        //     int launcherCount = 0;
        //     for (RobotInfo i : rc.senseNearbyRobots(4, myTeam)) {
        //         if (i.type == RobotType.LAUNCHER) {
        //             launcherCount += 1;
        //         }
        //     }
        //     if (!atIsland) {
        //         if (rng.nextInt(4) != 0) {
        //             Direction dirHQ = rc.getLocation().directionTo(headquarters);
        //             tryMove(dirHQ);
        //         } 
        //     }    
        // }
        

        wander();
    }
}

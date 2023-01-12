package head_v1.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public WellInfo startingAdamantium = null;
    public WellInfo startingMana = null;

    public MapLocation optimalSpawnAdamantium = null;
    public MapLocation optimalSpawnMana = null;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            comm.appendLocation(0, rc.getLocation());
            // finding closest starting wells
            // WellInfo[] wells = rc.senseNearbyWells();
            // int bestDistanceAdamantium = Integer.MAX_VALUE;
            // int bestDistanceMana = Integer.MAX_VALUE;
            // if (wells != null) {
            //     for (WellInfo well : wells) {
            //         int distance = rc.getLocation().distanceSquaredTo(well.getMapLocation());
            //         if (well.getResourceType() == ResourceType.ADAMANTIUM) {
            //             if (distance < bestDistanceAdamantium) {
            //                 bestDistanceAdamantium = distance;
            //                 startingAdamantium = well;
            //             }
            //         } else {
            //             if (distance < bestDistanceMana) {
            //                 bestDistanceMana = distance;
            //                 startingMana = well;
            //             }
            //         }
            //     }
    
            //     // finding optimal spawn locations

            //     // find closest square to optimal spawn location
            //     if (startingAdamantium != null) {
            //         Direction dirAdamantium = startingAdamantium.getMapLocation().directionTo(rc.getLocation());
            //         MapLocation attemptedSpawnAdamantium = startingAdamantium.getMapLocation();
            //         while (optimalSpawnAdamantium == null) {
            //             //System.out.println("Adamantium Spawn search attempted");
            //             if (attemptedSpawnAdamantium.isWithinDistanceSquared(rc.getLocation(), 9)) {
            //                 optimalSpawnAdamantium = attemptedSpawnAdamantium;
            //                 System.out.println("Adamantium Spawn search complete");
            //             } else {
            //                 attemptedSpawnAdamantium.subtract(dirAdamantium);
            //             }
                        

                        
            //         }
            //     }
            //     if (startingMana != null) {
            //         Direction dirMana = startingMana.getMapLocation().directionTo(rc.getLocation());
            //         MapLocation attemptedSpawnMana = startingMana.getMapLocation();
            //         while (optimalSpawnMana == null) {
            //             //System.out.println("Mana Spawn search attempted");
            //             if (attemptedSpawnMana.isWithinDistanceSquared(rc.getLocation(), 9)) {
            //                 optimalSpawnMana = attemptedSpawnMana;
            //                 System.out.println("Mana Spawn search complete");
            //             } else {
            //                 attemptedSpawnMana.subtract(dirMana);
            //             }  
            //         }    
            //     }   
            //     // spawn accordingly
            //     if (rc.canBuildRobot(RobotType.CARRIER, optimalSpawnAdamantium)) {
            //         rc.buildRobot(RobotType.CARRIER, optimalSpawnAdamantium);
            //     }

            // }
            



        } else if (rc.getRoundNum() == 2) {
            this.hqLocations = comm.readLocationArray(0);
            for (MapLocation loc : hqLocations) {
                System.out.println(loc);
            }
        //     if (rc.canBuildRobot(RobotType.CARRIER, optimalSpawnAdamantium)) {
        //         rc.buildRobot(RobotType.CARRIER, optimalSpawnAdamantium);
        //     }
        // } else if (rc.getRoundNum() == 3) {
        //     if (rc.canBuildRobot(RobotType.CARRIER, optimalSpawnAdamantium)) {
        //         rc.buildRobot(RobotType.CARRIER, optimalSpawnAdamantium);
        //     }
        // } else if (rc.getRoundNum() == 4) {
        //     if (startingMana != null) {
        //         if (rc.canBuildRobot(RobotType.CARRIER, optimalSpawnMana)) {
        //             rc.buildRobot(RobotType.CARRIER, optimalSpawnMana);
        //         }
        //     }
        // } else if (rc.getRoundNum() <= 7) {
        //     if (rc.canBuildRobot(RobotType.LAUNCHER, rc.getLocation().add(Direction.NORTH))) {
        //         rc.buildRobot(RobotType.LAUNCHER, rc.getLocation().add(Direction.NORTH));
        //     }
        }


        
        


        
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if (rc.getRobotCount()>50) {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                // If we can build an anchor do it!
                rc.buildAnchor(Anchor.STANDARD);
                rc.setIndicatorString("Building anchor! " + rc.getAnchor());
            }
            if (rng.nextInt(4) ==0 && rc.getAnchor() != null) {
                if (rng.nextBoolean()) {
                    // Let's try to build a carrier.
                    rc.setIndicatorString("Trying to build a carrier (greater 50)");
                    if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                        rc.buildRobot(RobotType.CARRIER, newLoc);
                    }
                } else {
                    // Let's try to build a launcher.
                    rc.setIndicatorString("Trying to build a launcher (greater 50)");
                    if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                        rc.buildRobot(RobotType.LAUNCHER, newLoc);
                    }
                }
            }

        } else {
            if (rng.nextBoolean()) {
                // Let's try to build a carrier.
                rc.setIndicatorString("Trying to build a carrier (less 50)");
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                }
            } else {
                // Let's try to build a launcher.
                rc.setIndicatorString("Trying to build a launcher (less 50)");
                if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                    rc.buildRobot(RobotType.LAUNCHER, newLoc);
                }
            }
        }
    }
}
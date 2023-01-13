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
            WellInfo[] wells = rc.senseNearbyWells();
            int bestDistanceAdamantium = Integer.MAX_VALUE;
            int bestDistanceMana = Integer.MAX_VALUE;
            if (wells != null) {
                for (WellInfo well : wells) {
                    int distance = rc.getLocation().distanceSquaredTo(well.getMapLocation());
                    if (well.getResourceType() == ResourceType.ADAMANTIUM) {
                        if (distance < bestDistanceAdamantium) {
                            bestDistanceAdamantium = distance;
                            startingAdamantium = well;
                        }
                    } else {
                        if (distance < bestDistanceMana) {
                            bestDistanceMana = distance;
                            startingMana = well;
                        }
                    }
                }

                System.out.println("Best Adamantium");
                System.out.println(startingAdamantium);

                System.out.println("Best Mana");
                System.out.println(startingMana);

    
                // finding optimal spawn locations

                // find closest square to optimal spawn location
                if (startingAdamantium != null) {
                    System.out.println("searching for adamantium");
                    Direction dirAdamantium = startingAdamantium.getMapLocation().directionTo(rc.getLocation());
                    System.out.println(dirAdamantium);
                    MapLocation attemptedSpawnAdamantium = startingAdamantium.getMapLocation();
                    while (optimalSpawnAdamantium == null) {
                        System.out.println("Attempted location");
                        System.out.println(attemptedSpawnAdamantium);
                        if (attemptedSpawnAdamantium.isWithinDistanceSquared(rc.getLocation(), 9)) {
                            optimalSpawnAdamantium = attemptedSpawnAdamantium;
                            System.out.println("Adamantium Spawn search complete");
                        } else {
                            attemptedSpawnAdamantium = attemptedSpawnAdamantium.add(dirAdamantium);
                        }
                        

                        
                    }
                }
                if (startingMana != null) {
                    System.out.println("searching for mana");
                    Direction dirMana = startingMana.getMapLocation().directionTo(rc.getLocation());
                    MapLocation attemptedSpawnMana = startingMana.getMapLocation();
                    while (optimalSpawnMana == null) {
                        //System.out.println("Mana Spawn search attempted");
                        if (attemptedSpawnMana.isWithinDistanceSquared(rc.getLocation(), 9)) {
                            optimalSpawnMana = attemptedSpawnMana;
                            System.out.println("Mana Spawn search complete");
                        } else {
                            attemptedSpawnMana = attemptedSpawnMana.add(dirMana);
                        }  
                    }    
                }   
                //spawn accordingly
                if (optimalSpawnAdamantium != null) {
                    if (rc.canBuildRobot(RobotType.CARRIER, optimalSpawnAdamantium)) {
                        System.out.println("spawning adamantium");
                        rc.buildRobot(RobotType.CARRIER, optimalSpawnAdamantium);
                    }
                } else if (optimalSpawnMana != null) {
                        if (rc.canBuildRobot(RobotType.CARRIER, optimalSpawnMana)) {
                            System.out.println("spawning mana");
                            rc.buildRobot(RobotType.CARRIER, optimalSpawnMana);
                        }
                    } else {
                    Direction dir = directions[rng.nextInt(directions.length)];
                    MapLocation newLoc = rc.getLocation().add(dir);
                    if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                        System.out.println("spawning random");
                        rc.buildRobot(RobotType.CARRIER, newLoc);
                    }
                }
            } 
            

        } else if (rc.getRoundNum() == 2) {
            System.out.println("round2");
            this.hqLocations = comm.readLocationArray(0);
            for (MapLocation loc : hqLocations) {
                System.out.println(loc);
            }
            if (optimalSpawnAdamantium != null) {
                Direction dir = directions[rng.nextInt(directions.length)];
                do {
                    dir = directions[rng.nextInt(directions.length)];
                } while (!rc.canBuildRobot(RobotType.CARRIER, optimalSpawnAdamantium.add(dir)));
                    System.out.println("spawning adamantium");
                    rc.buildRobot(RobotType.CARRIER, optimalSpawnAdamantium.add(dir));
            } else if (optimalSpawnMana != null) {
                Direction dir = directions[rng.nextInt(directions.length)];
                do {
                    dir = directions[rng.nextInt(directions.length)];
                } while (!rc.canBuildRobot(RobotType.CARRIER, optimalSpawnMana.add(dir)));
                        System.out.println("spawning mana");
                        rc.buildRobot(RobotType.CARRIER, optimalSpawnMana.add(dir));
                    
            } else {
                Direction dir = directions[rng.nextInt(directions.length)];
                MapLocation newLoc = rc.getLocation().add(dir);
                do {
                dir = directions[rng.nextInt(directions.length)];
                newLoc = rc.getLocation().add(dir);
                } while (!rc.canBuildRobot(RobotType.CARRIER, newLoc));
                    System.out.println("spawning random");
                    rc.buildRobot(RobotType.CARRIER, newLoc);   
            }
        } else if (rc.getRoundNum() == 3) {
            System.out.println("round3");
            if (optimalSpawnAdamantium != null) {
                Direction dir = directions[rng.nextInt(directions.length)];
                do {
                    dir = directions[rng.nextInt(directions.length)];
                } while (!rc.canBuildRobot(RobotType.CARRIER, optimalSpawnAdamantium.add(dir)));
                    System.out.println("spawning adamantium");
                    rc.buildRobot(RobotType.CARRIER, optimalSpawnAdamantium.add(dir));
            } else if (optimalSpawnMana != null) {
                Direction dir = directions[rng.nextInt(directions.length)];
                do {
                    dir = directions[rng.nextInt(directions.length)];
                } while (!rc.canBuildRobot(RobotType.CARRIER, optimalSpawnMana.add(dir)));
                        System.out.println("spawning mana");
                        rc.buildRobot(RobotType.CARRIER, optimalSpawnMana.add(dir));
                    
            } else {
                Direction dir = directions[rng.nextInt(directions.length)];
                MapLocation newLoc = rc.getLocation().add(dir);
                do {
                dir = directions[rng.nextInt(directions.length)];
                newLoc = rc.getLocation().add(dir);
                } while (!rc.canBuildRobot(RobotType.CARRIER, newLoc));
                    System.out.println("spawning random");
                    rc.buildRobot(RobotType.CARRIER, newLoc);   
            }
        } else if (rc.getRoundNum() == 4) {
            if (startingMana != null) {
                Direction dir = directions[rng.nextInt(directions.length)];
                do {
                    dir = directions[rng.nextInt(directions.length)];
                } while (!rc.canBuildRobot(RobotType.CARRIER, optimalSpawnMana.add(dir)));
                        System.out.println("spawning mana");
                        rc.buildRobot(RobotType.CARRIER, optimalSpawnMana.add(dir));
            }
        } else if (rc.getRoundNum() <= 7) {
            Direction dir = directions[rng.nextInt(directions.length)];
            MapLocation newLoc = rc.getLocation().add(dir);
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        } else {
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
}
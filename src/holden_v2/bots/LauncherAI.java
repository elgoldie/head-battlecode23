package holden_v2.bots;

import java.util.Map;

import battlecode.common.*;

public class LauncherAI extends RobotAI {
    
    public boolean offense;
    public String indicatorString;
    public MapLocation destination;
    public MapLocation center = new MapLocation(rc.getMapWidth()/2,rc.getMapHeight()/2);
    
    public LauncherAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        offense = false;
        indicatorString = "";
        // if (rng.nextInt(3) == 0) {
        //     offense = true;
        //     indicatorString = "defense";
        // } else {
        //     offense = false;
        //     indicatorString = "offense";
            
        // }
    }

    

    public MapLocation closestEnemyHeadquarters() throws GameActionException {
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = 4; i < 8; i++) {
            MapLocation hqLoc = comm.readLocation(i);
            if (hqLoc == null) break;
            int newDist = rc.getLocation().distanceSquaredTo(hqLoc);
            if (newDist < dist) {
                dist = newDist;
                loc = hqLoc;
            }
        }
        return loc;
    }

    // public MapLocation randomEnemyHeadquarters() throws GameActionException {
    //     MapLocation loc = null;
    //     int dist = Integer.MAX_VALUE;
    //     for (int i = 4; i < 8; i++) {
    //         MapLocation hqLoc = comm.readLocation(i);
    //         if (hqLoc == null) break;
    //         int newDist = rc.getLocation().distanceSquaredTo(hqLoc);
    //         if (newDist < dist) {
    //             dist = newDist;
    //             loc = hqLoc;
    //         }
    //     }
    //     return loc;
    // }

    public int enemyValue(RobotInfo robot) {
        if (robot.getType() == RobotType.HEADQUARTERS) return Integer.MIN_VALUE;
        return -robot.health;
    }

    // public boolean baseDefenseMovement() throws GameActionException {
        

    //     MapLocation loc = null;
    //     int dist = Integer.MAX_VALUE;
    //     for (int i = 0; i < 4; i++) {
    //         MapLocation hqLoc = comm.readLocation(i);
    //         if (hqLoc == null) break;
    //         if (comm.readLocationFlags(i) == 0) continue;
    //         int newDist = hqLoc.distanceSquaredTo(rc.getLocation());
    //         if (newDist < dist) {
    //             dist = newDist;
    //             loc = hqLoc;
    //         }
    //         //we need a better resource for our flags and shit eom
    //     }
    //     if (loc == null) {
    //         // meaning: is not actively seeking to defend a base
    //         pathing.initiate_pathfinding(center); // replace this

    //         // TODO figure out what launcher behavior is if not defending base. E.g. work with Holden to figure out where it thinks enemy HQ is.
    //         return false;
    //     } else {
    //         if (loc != this.destination) {
    //             this.destination = loc;
    //             pathing.initiate_pathfinding(this.destination);
    //         }
    //         return true;
    //         //tryMoveOrWander(pathing.findPath(loc));
    //         //return true;
    //     }
    // }

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


        /* current logic:
        if visible target: 
            chase the target
        else if base defense mode is on:
            pathfind towards hq emitting distress signal 
        else if enemy HQ location known:
            pathfind towards location of interest (enemy HQ > enemy island > friendly island)
        else:
            wander (done inside baseDefenseMovement)

        MOST IMPORTANT THING:
            if not attempting to chase a target in range, NEED TO SET this.destination AND INITIATE PATHFINDING inside the 
            baseDefenseMovement() function. You'll probably want to change that to not return a boolean. 
        */
        if (target != null) {
            indicatorString += "| attacking"; 
            if (rc.canAttack(target.location)) {      
                rc.attack(target.location);
            } else {
                tryMoveOrWander(pathing.findPath(target.location));
            }
        }

        else { if (!offense) {
            MapLocation enemyHQ = closestEnemyHeadquarters();
            if (enemyHQ != null) {
                MapLocation loc = enemyHQ;
                if (loc != this.destination) {
                    this.destination = loc;
                    pathing.initiate_pathfinding(this.destination);
                }
                indicatorString += "| moving to enemyHQ"; 
                Direction dir;
                while (rc.isMovementReady() && !pathing.hasArrived()) {
                    dir = pathing.findPath();
                    if (dir == Direction.CENTER  || !rc.canMove(dir)) {
                        //System.out.println(dir);
                        // if can't move/something else has gone wrong
                        break;
                    }
                    //System.out.println(dir);
                    rc.move(dir);
                }
                return;
            } else {
                //System.out.println("in offense");
                MapLocation enemyIsland = closestIsland(enemyTeam);
                if (enemyIsland == null) {
                    //System.out.println("no enemy island");
                    MapLocation island = closestIsland(myTeam);
                //System.out.println("pathing to friendly island");
                if (island == null) {
                    //System.out.println("no friendly island");
                    MapLocation loc = center;
                if (loc != this.destination) {
                    this.destination = loc;
                    pathing.initiate_pathfinding(this.destination);
                }
                    Direction dir;
                    //WHILE PATHFINDING TO A DESTINATION IS INITIATED, this is the cycle you use to move as much as possible.
                    // This should probably be put into its own method inside robotAI instead. This is why I like the assignment/command system.
                    // copy this to whereever i need it
                    while (rc.isMovementReady() && !pathing.hasArrived()) {
                        dir = pathing.findPath();
                        if (dir == Direction.CENTER || !rc.canMove(dir)) {
                            // if can't move/something else has gone wrong
                            break;
                        }
                        rc.move(dir);
        
                    };
                } else {
                    //System.out.println("pathing to friendly island");
                    MapLocation loc = island;
                    if (loc != this.destination) {
                        this.destination = loc;
                        pathing.initiate_pathfinding(this.destination);
                    }
                    indicatorString += "| our island pathing"; 
                    Direction dir;
                    while (rc.isMovementReady() && !pathing.hasArrived()) {
                        dir = pathing.findPath();
                        if (dir == Direction.CENTER  || !rc.canMove(dir)) {
                            //System.out.println(dir);
                            // if can't move/something else has gone wrong
                            break;
                        }
                        //System.out.println(dir);
                        rc.move(dir);
                    }
                }
                } else {
                    //System.out.println("pathing to enemy island");
                    MapLocation loc = enemyIsland;
                    if (loc != this.destination) {
                        this.destination = loc;
                        pathing.initiate_pathfinding(this.destination);
                    }
                    indicatorString += "| enemy island pathing"; 
                    Direction dir;
                    while (rc.isMovementReady() && !pathing.hasArrived()) {
                        dir = pathing.findPath();
                        if (dir == Direction.CENTER  || !rc.canMove(dir)) {
                            //System.out.println(dir);
                            // if can't move/something else has gone wrong
                            break;
                        }
                        //System.out.println(dir);
                        rc.move(dir);
                    }
                }
            } 
        }
    //     } else {
    //         MapLocation enemyHQ = closestEnemyHeadquarters();
    //         if (enemyHQ != null) {
    //             MapLocation loc = enemyHQ;
    //             if (loc != this.destination) {
    //                 this.destination = loc;
    //                 pathing.initiate_pathfinding(this.destination);
    //             }
    //             indicatorString += "| moving to enemyHQ"; 
    //             Direction dir;
    //             while (rc.isMovementReady() && !pathing.hasArrived()) {
    //                 dir = pathing.findPath();
    //                 if (dir == Direction.CENTER  || !rc.canMove(dir)) {
    //                     //System.out.println(dir);
    //                     // if can't move/something else has gone wrong
    //                     break;
    //                 }
    //                 //System.out.println(dir);
    //                 rc.move(dir);
    //             }
    //             return;
    //         } else {
    //             wander();
    //         }
    // }

        // else {
        //     Direction dir;
        //     //WHILE PATHFINDING TO A DESTINATION IS INITIATED, this is the cycle you use to move as much as possible.
        //     // This should probably be put into its own method inside robotAI instead. This is why I like the assignment/command system.
        //     // copy this to whereever i need it
        //     while (rc.isMovementReady() && !pathing.hasArrived()) {
        //         dir = pathing.findPath();
        //         if (dir == Direction.CENTER) {
        //             // if can't move/something else has gone wrong
        //             break;
        //         }
        //         rc.move(dir);

        //     }
            
        // }
        rc.setIndicatorString(indicatorString);
    }
}
}

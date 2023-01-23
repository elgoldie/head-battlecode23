/* package pathbot.bots;

import java.util.Random;

import battlecode.common.*;
import pathbot.path.*;

public abstract class RobotAI {

    
    public Random rng;
    public int seed;

    public Team myTeam;
    public Team enemyTeam;

    public RobotController rc;
    public Pathfinding paths;

    public int id;

    public int aliveTurns;

    public MapLocation spawnLocation;

    public RobotAI(RobotController rc, int id) throws GameActionException {
        System.out.println("head_v2_pf done");
        this.rc = rc;
        this.id = id;
        
        this.rng = new Random(id);
        this.seed = rng.nextInt();

        this.paths = new WaypointPathfinding(rc);
        
        this.aliveTurns = 0;
        this.spawnLocation = rc.getLocation();

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
    }
    
    public void run() throws GameActionException {
        aliveTurns += 1;
    }

}
 */
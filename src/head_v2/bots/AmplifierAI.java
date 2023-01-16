package head_v2.bots;

import battlecode.common.*;

public class AmplifierAI extends RobotAI {

    public int myCommIndex;
    public int targetCommIndex;
    public MapLocation targetLocation;

    public int launcherQuotaLow;
    public int launcherQuotaHigh;
    
    public AmplifierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        launcherQuotaLow = 1;
        launcherQuotaHigh = 1;
    }

    public void runWithTarget() throws GameActionException {

        Direction dir = pathing.findPath(targetLocation);
        tryMove(dir);

        int numLaunchers = getNumberOfLaunchersNearby();
        int flags = comm.readLocationFlags(myCommIndex);
        boolean isSignalingFull = (flags & 4) != 0;
        if (!isSignalingFull && numLaunchers > launcherQuotaHigh) {
            comm.writeLocationFlags(myCommIndex, flags ^ 4);
        } else if (isSignalingFull && numLaunchers < launcherQuotaLow) {
            comm.writeLocationFlags(myCommIndex, flags ^ 4);
        }

        comm.keepAlive(myCommIndex);
        comm.keepAlive(targetCommIndex);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        if (targetLocation == null) {
            for (int targetIndex = comm.ISLAND_OFFSET; targetIndex < comm.FLEET_OFFSET; targetIndex++) {
                if (comm.hasLocation(targetIndex)) {
                    myCommIndex = comm.FLEET_OFFSET;
                    takeAssignment(myCommIndex, targetIndex);
                    runWithTarget();
                    break;
                }
            }
        } else {
            runWithTarget();
        }
    }

    public int getNumberOfLaunchersNearby() throws GameActionException {
        int count = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(20, myTeam)) {
            if (robot.type == RobotType.LAUNCHER) {
                count++;
            }
        }
        return count;
    }

    public void takeAssignment(int myIndex, int targetIndex) throws GameActionException {
        myCommIndex = myIndex;
        targetCommIndex = targetIndex;
        targetLocation = comm.readLocation(targetIndex);
        targetLocation = new MapLocation(9, 13);
        comm.writeLocation(myCommIndex, targetLocation);
    }
}

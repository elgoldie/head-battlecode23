package pathbot.path;

import battlecode.common.*;

public interface ArrivalCondition {
    public boolean hasArrived(MapLocation myloc, MapLocation destination);
}
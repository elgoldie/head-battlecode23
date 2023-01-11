package macrobot.util;

public class FreeMath {

    public static int abs(int x) { return x < 0 ? -x : x; }
    public static double abs(double x) { return x < 0 ? -x : x; }

    public static int min(int x, int y) { return x < y ? x : y; }
    public static double min(double x, double y) { return x < y ? x : y; }

    public static int max(int x, int y) { return x > y ? x : y; }
    public static double max(double x, double y) { return x > y ? x : y; }

    public static int signum(int x) { return x < 0 ? -1 : x > 0 ? 1 : 0; }
    public static double signum(double x) { return x < 0 ? -1 : x > 0 ? 1 : 0; }
}

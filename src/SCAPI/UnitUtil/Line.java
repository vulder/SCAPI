package SCAPI.UnitUtil;
import java.util.List;

import bwapi.Position;

public class Line {
    private double slope;
    private double intercept;

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public static Line fromObservations(List<Position> observations) {
        double a = 0;
        double b = 0;

        int sum_xy = 0;
        int sum_xx = 0;
        int sum_x = 0;
        int sum_y = 0;
        int n = observations.size();

        // Slope:
        for (Position pos : observations) {
            sum_xy += pos.getX() * pos.getY();
            sum_xx += pos.getX() * pos.getX();
            sum_x += pos.getX();
            sum_y += pos.getY();
        }

        a = (sum_xy - ((sum_x * sum_y) / n)) / (double) ((sum_xx - (sum_x ^ 2)) / (double) n);

        b = (sum_y - (a * sum_x)) / (double) n;
        return new Line(a, b);
    }

    Line(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public double eval(double x) {
        return slope * x + intercept;
    }
}

package SCAPI.UnitUtil;
import java.util.List;

import bwapi.Game;
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
	
	public static Line fromPoints(Vector from, Vector to) {
		Position tPos = to.toPosition();
		Position fPos = from.toPosition();

		double a =
				(tPos.getY() - fPos.getY()) /
				(tPos.getX() - fPos.getX());
		double b =
				to.toPosition().getY() - a * to.toPosition().getX();
				
		return new Line(a, b);
	}

	public static Line fromObservations(List<Position> observations) {
		double a = 0;
		double b = 0;

		int sum_x = 0;
		int sum_y = 0;
		int n = observations.size();

		for (Position pos : observations) {
			sum_x += pos.getX();
			sum_y += pos.getY();
		}
		
		double avg_x = sum_x / n;
		double avg_y = sum_y / n;
		
		double sum_xy_avg = 0;
		double sum_x_avg_2 = 0;
		for (Position pos : observations) {
			sum_xy_avg += (pos.getX() - avg_x) * (pos.getY() - avg_y);
			sum_x_avg_2 += Math.pow(pos.getX() - avg_x, 2);
		}
		
		b = sum_xy_avg / sum_x_avg_2;
		a = avg_y - b * avg_x;

		return new Line(b, a);
	}

	public Line(double slope, double intercept) {
		this.slope = slope;
		this.intercept = intercept;
	}

	public double eval(double x) {
		return slope * x + intercept;
	}
	
	public Position at(double x) {
		return new Position((int) x, (int)eval(x));
	}
	
	public void draw(Game state, double from, double to, bwapi.Color c) {
		Position fPos = new Position((int)from, (int)eval(from));
		Position tPos = new Position((int)to, (int)eval(to));
		state.drawLineMap(fPos, tPos, c);
		state.drawTextMap(tPos, String.format("f(x) = %f * x + %f", slope, intercept));
	}

}

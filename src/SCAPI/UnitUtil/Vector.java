package SCAPI.UnitUtil;

import bwapi.Game;
import bwapi.Position;

public class Vector {
	protected Position origin;
	protected Position pos;

	/**
	 * @param pos
	 */
	public Vector(Position pos) {
		this(new Position(0, 0), pos);
	}

	/**
	 * @param origin
	 * @param pos
	 */
	public Vector(Position origin, Position pos) {
		this.origin = origin;
		this.pos = pos;
	}
	
	public Vector(Vector origin, Vector pos) {
		this.origin = origin.toPosition();
		this.pos = pos.toPosition();
	}
	
	public int product(Vector rhs) {
		Position p_lhs = toPosition();
		Position p_rhs = rhs.toPosition();
		return p_lhs.getX() * p_rhs.getX() + p_lhs.getY() + p_rhs.getY();
	}
	
	public double length() {
		Position p = toPosition();
		return Math.sqrt(p.getX()^2 + p.getY()^2);
	}
	
	public Vector normalized() {
		return scale(Math.pow(length(), -1));
	}
	
	public double getAngle() {
		Position l = toOrigin().toPosition();
		double phi = Math.atan2(l.getY(), l.getX());
		return ((phi < 0) ? phi + 2 * Math.PI : phi) * 180 / Math.PI;
	}

	/**
	 * @param rhs
	 * @return
	 */
	public Vector add(Vector rhs) {
		return new Vector(origin, new Position(pos.getX() + rhs.pos.getX(), pos.getY() + rhs.pos.getY()));
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector add(int x, int y) {
		return add(new Vector(new Position(x, y)));
	}
	
	/**
	 * @param scale
	 * @return
	 */
	public Vector scale(double scale) {
		Vector v = new Vector(origin, new Position(
				(int) ((origin.getX() + pos.getX()) * scale),
				(int) ((origin.getY() + pos.getY()) * scale)));
		return v;
	}
	
	public Vector toOrigin() {
		Position p = toPosition();
		return new Vector(
				new Position(0, 0),
				new Position(p.getX() - origin.getX(), p.getY() - origin.getY())
				);
	}

	/**
	 * @param angle
	 * @return
	 */
	public Vector rotate(double angle) {
		Position p = toOrigin().toPosition();
		double newAngle = Math.toRadians(angle);
		
		Position rotated = new Position(
				(int) (p.getX() * Math.cos(newAngle) - p.getY() * Math.sin(newAngle)),
				(int) (p.getX() * Math.sin(newAngle) + p.getY() * Math.cos(newAngle)));
		Position shifted = new Position(
				rotated.getX() + origin.getX(),
				rotated.getY() + origin.getY());

		return new Vector(origin, shifted);
	}

	/**
	 * @param rhs
	 * @return
	 */
	public Vector sub(Vector rhs) {
		return this.add(rhs.scale(-1.0));
	}

	/**
	 * @return
	 */
	public Position toPosition() {
		return pos;
	}

	/**
	 * @param state
	 * @param c
	 */
	public void draw(Game state, bwapi.Color c) {
		state.drawLineMap(origin, pos, c);
		state.drawCircleMap(pos, 2, c);
		state.drawTextMap(toPosition(), ""+getAngle());
	}
	
	public String toString() {
		return toPosition().toString();
	}
}

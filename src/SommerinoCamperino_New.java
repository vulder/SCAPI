import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import SCAPI.UnitUtil.Line;
import SCAPI.UnitUtil.Vector;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class SommerinoCamperino_New extends DefaultBWListener {

	private Mirror mirror = new Mirror();
	private Game game;
	private Player self;
	private BaseLocation enemyLoc;
	private BaseLocation playerLoc;

	enum State {
		INIT, NORMAL
	}

	private State state = State.INIT;

	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	@Override
	public void onStart() {
		game = mirror.getGame();
		self = game.self();
		BWTA.readMap();
		BWTA.analyze();
		
		List<BaseLocation> locs = BWTA.getStartLocations();
		Iterator<BaseLocation> it = locs.iterator();
		playerLoc = BWTA.getStartLocation(self);
		
		while (it.hasNext()) {
			BaseLocation l = it.next();
			if (l != playerLoc) {
				enemyLoc = l;
			}
		}
	}

	protected void attackEverythingInSight() {
		Player enemy = game.enemy();
		List<Unit> units = self.getUnits();

		HashSet<Unit> s = new HashSet<Unit>();
		s.addAll(enemy.getUnits());

		units.sort(new Comparator<Unit>() {
			@Override
			public int compare(Unit o1, Unit o2) {
				int n1 = game.getUnitsInRadius(o1.getPosition(), o1.getType().groundWeapon().maxRange()).size();
				int n2 = game.getUnitsInRadius(o1.getPosition(), o1.getType().groundWeapon().maxRange()).size();
				return n2 - n1;
			}
		});

		for (Unit u : units) {
			HashSet<Unit> newS = new HashSet<Unit>();
			List<Unit> unitsInRange = game.getUnitsInRadius(u.getPosition(), u.getType().groundWeapon().maxRange());

			if (unitsInRange.isEmpty()) {
				continue;
			}

			for (Unit ru : unitsInRange) {
				if (s.contains(ru)) {
					newS.add(ru);
				}
			}

			if (!newS.isEmpty())
				s = newS;
		}

		if (s.isEmpty())
			return;

		Unit eu = (Unit) s.iterator().next();
		if (!eu.isVisible())
			return;

		for (Unit myUnit : self.getUnits()) {
			if (myUnit.getLastCommandFrame() >= game.getFrameCount() || myUnit.isAttackFrame())
				continue;
			if (myUnit.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit)
				continue;
			myUnit.attack(eu);
		}
	}

	private boolean spreadUnits(Game state, List<Unit> units, int distance) {
		if (units.size() < 2)
			return true;

		List<Position> wantPositions = new LinkedList<Position>();
		boolean requiredActions = false;

		for (int i = 0; i < units.size(); i++) {
			Unit u0 = units.get(i);
			Unit closest = u0;
			int minDistance = distance;
			for (int j = i + 1; j < units.size(); j++) {
				Unit u1 = units.get(j);

				int curDistance = u0.getDistance(u1);
				if (curDistance >= distance)
					continue;

				if (minDistance >= curDistance) {
					minDistance = curDistance;
					closest = u1;
				}
			}

			Position wantPos = new Position(
					u0.getX() - (closest.getX() - u0.getX()),
					u0.getY() - (closest.getY() - u0.getY()));

			wantPositions.add(wantPos);

			if (minDistance < distance)
				requiredActions = true;
			if (u0.isIdle()) {
				Vector v0 = new Vector(u0.getPosition(), wantPos);
				v0 = v0.rotate(Math.random() * 360);
				u0.move(v0.toPosition());
			}
		}
		int offset = 0;
		for (Position pos : wantPositions) {
			state.drawCircleMap(pos, 2, bwapi.Color.Cyan);
		}
		return requiredActions;
	}

	private Line unitAlignment = null;
	private Line wantAlignment = null;

	private boolean unitsAreAligned(Game state, List<Unit> units, Position pos) {
		if (units.size() < 2)
			return true;

		units.sort(new Comparator<Unit>() {
			@Override
			public int compare(Unit o1, Unit o2) {
				int comp = o1.getX() - o2.getX();
				if (comp == 0)
					comp = o1.getY() - o2.getY();
				return comp;
			}
		});

		List<Position> positions = new LinkedList<Position>();
		for (Unit u : units) {
			positions.add(u.getPosition());
		}

		unitAlignment = Line.fromObservations(positions);

		Unit u0 = units.get(0);
		Unit u1 = units.get(units.size() - 1);

		
		Position p0 = unitAlignment.at(u0.getX());
		Position p1 = unitAlignment.at(u1.getX());
				
		Vector align = new Vector(p0, p1);
		Vector mid = align.scale(0.5);
		
		Vector dir;
		if (pos != null)
			dir = new Vector(mid.toPosition(), pos);
		else
			dir = new Vector(mid.toPosition(), mid.add(64, 64).toPosition());

		Vector v1 = dir.rotate(90);
		Vector v2 = dir.rotate(270);
		
		wantAlignment = Line.fromPoints(v1, v2);
		wantAlignment.draw(state,  u0.getX(), u1.getX(), bwapi.Color.Green);
		unitAlignment.draw(state, u0.getX(), u1.getX(), bwapi.Color.Yellow);
		
		double slopeDelta = Math.abs(wantAlignment.getSlope() - unitAlignment.getSlope());
		state.drawTextScreen(10, 50, String.format("Slope-Delta: %f", slopeDelta));
		return slopeDelta < 0.5;
	}
	
	private void alignUnits(Game state, List<Unit> units) {
		if (unitAlignment == null || wantAlignment == null)
			return;
		for (Unit u : units) {
			if (!u.isIdle())
				continue;
			
			
			Position u_pos = u.getPosition();

			double a_g = wantAlignment.getSlope();
			double b_g = wantAlignment.getIntercept();
			double a_f = (-1) * a_g;
			double b_f = u_pos.getY() - a_f * u_pos.getX();

			double x = (b_f - b_g) / (a_g - a_f);
			
			Position to = wantAlignment.at(x);
			u.move(to);
		}
	}

	private void attackMoveTo(Game state, List<Unit> units, Position to) {
		for (Unit u : units) {
			if (u.isIdle()) {
				u.attack(new Position(u.getPosition().getX() + to.getX(), u.getPosition().getY() + to.getY()));
			}
		}
	}

	private void drawEnemies(Game state, Player enemy) {
		for (Unit e : enemy.getUnits()) {
			if (!e.isVisible())
				continue;
			state.drawBoxMap(e.getX() - 2, e.getY() - 2, e.getX() + 2, e.getY() + 2, bwapi.Color.Red);
		}
	}

	private void drawOrders(Game state, List<Unit> units) {
		for (Unit u : units) {
			if (u.isIdle())
				continue;

			Position pos = u.getOrderTargetPosition();
			bwapi.Color c = bwapi.Color.Green;

			UnitCommandType ty = u.getLastCommand().getUnitCommandType();
			if (ty == UnitCommandType.Attack_Move || ty == UnitCommandType.Attack_Unit)
				c = bwapi.Color.Red;

			game.drawLineMap(u.getPosition(), pos, c);
		}

	}

	@Override
	public void onFrame() {
		try {
			game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
			game.drawTextScreen(400, 10, "State: " + state);

			Player enemy = game.enemy();
			drawEnemies(game, enemy);
			drawOrders(game, self.getUnits());

			
			switch (state) {
			case INIT:
				Position pos = null;
				if (enemyLoc != null)
					pos = enemyLoc.getPosition();
				
				if (!unitsAreAligned(game, self.getUnits(), pos))
					alignUnits(game, self.getUnits());
				else {
					if (!spreadUnits(game, self.getUnits(), 15)) {
						state = State.NORMAL;
					}
				}
				break;
			case NORMAL:
				attackEverythingInSight();
				attackMoveTo(game, self.getUnits(), new Position(0, 15));
				break;
			}
		} catch (Throwable t) {
			game.sendText("Exception caught");
			t.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SommerinoCamperino_New().run();
	}
}
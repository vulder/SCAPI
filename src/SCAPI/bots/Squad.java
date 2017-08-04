package SCAPI.bots;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import SCAPI.UnitUtil.UnitControl;
import SCAPI.UnitUtil.Vector;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Squad {
	private List<Unit> members;
	final private Game state;
	private Color debugColor;

	public Squad(Game state, List<Unit> units, Color c) {
		assert !units.isEmpty();
		this.state = state;
		this.members = units;
		this.debugColor = c;
	}
	
	private Vector getTargetDirection(Position target) {
		Position focal = UnitControl.focalPosition(UnitControl.asPositions(members));
		Vector targetDir = new Vector(focal, target);
		return targetDir;
	}
	
	private Position getTargetPosition(Unit u, Vector dir) {
		return new Vector(u.getPosition()).add(dir).toPosition();
	}
	
	public boolean isMoving() {
		for (Unit m : members) {
			if (m.isMoving())
				return true;
		}
		return false;
	}
	
	public boolean isAttacking() {
		for (Unit m : members) {
			if (m.isAttacking() || m.isAttackFrame())
				return true;
		}
		return false;
	}
	
	public boolean isCloseTo(Position pos) {
		int maxdelta = 0;
		for (Unit m : members) {
			UnitType ty = m.getType();
			maxdelta = Math.max(maxdelta, ty.sightRange());
		}
		 
		Position focal = focalPosition();
		Vector dv = new Vector(focal, pos);
		return dv.length() <= maxdelta;
	}
	
	private void sort(final Position target) {
		members.sort(new Comparator<Unit>() {
			@Override
			public int compare(Unit o1, Unit o2) {
				int d1 = o1.getDistance(target);
				int d2 = o2.getDistance(target);
				return d1 - d2;
			}
		});
	}
	
	public Position focalPosition() {
		return UnitControl.focalPosition(UnitControl.asPositions(members));
		
	}
	
	public Vector distanceTo(Squad s) {
		return new Vector(focalPosition(), s.focalPosition());
	}
	
	public void move(Position target) {
		Vector targetDir = getTargetDirection(target);
		if (UnitControl.spreadUnits(state, members, 35))
		    return;

		sort(target);
		for (Unit member : members) {
			Position memberTarget = getTargetPosition(member, targetDir);
			member.move(memberTarget);
		}
	}
	
	public void attack(Position target) {
		Vector targetDir = getTargetDirection(target);
		if (UnitControl.spreadUnits(state, members, 35))
		    return;
		    
		sort(target);
		for (Unit member : members) {
			Position memberTarget = getTargetPosition(member, targetDir);
			member.attack(memberTarget);
		}
	}
	
	public void update() {
		members.removeIf(new Predicate<Unit>() {
			@Override
			public boolean test(Unit t) {
				return t.getHitPoints() <= 0 || !t.exists();
			}
		});
	}
	
	public void draw() {
		for (Unit m : members) {
			state.drawCircleMap(m.getPosition(), 5, debugColor);
		}
	}
}

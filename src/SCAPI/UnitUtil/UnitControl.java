package SCAPI.UnitUtil;

import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;

import java.util.LinkedList;
import java.util.List;

import SCAPI.Config;
import bwapi.Color;

public class UnitControl {

    /**
     * Smart Attack only issues an attack command if the unit would not get
     * interrupted by it.
     * 
     * @param attacker
     * @param target
     */
    public static void smartAttack(Unit attacker, Unit target) {
        if (!canIssueAttackCommand(attacker, target)) {
            return;
        }

        attacker.attack(target);

        if (Config.getDebug()) {
            Config.getGameRef().drawLineMap(attacker.getPosition().getX() + 3, attacker.getPosition().getY() + 3,
                    target.getPosition().getX() + 3, target.getPosition().getY() + 3, Color.Red);
        }
    }

    /**
     * Checks if the <code>Unit</code> can issues an attack command.
     * 
     * @param attacker
     * @param target
     * @return true if attack cmd is possible
     */
    public static boolean canIssueAttackCommand(Unit attacker, Unit target) {
        UnitCommand currentCmd = attacker.getLastCommand();
        UnitCommandType cmdType = currentCmd.getUnitCommandType();

        if (cmdType == UnitCommandType.Attack_Unit) {
            if (currentCmd.getTarget() == target) {
                return false;
            }
        }

        return !commandWillInterruptAttack(attacker);
    }

    /**
     * @param state
     * @param units
     * @param distance
     * @return
     */
    public static boolean spreadUnits(Game state, List<Unit> units, int distance) {
	if (units.size() < 2)
	    return true;

	List<Position> wantPositions = new LinkedList<Position>();
	boolean requiredActions = false;

	for (int i = 0; i < units.size() - 1; i++) {
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

	    Vector a = new Vector(u0.getPosition());
	    Vector b = new Vector(closest.getPosition());
	    Vector ab = new Vector(b, a);

	    int radius = Math.max(u0.getType().height(), u0.getType().width());
	    ab = new Vector(a.toPosition(), a.add(ab).toPosition());
	    ab = ab.scale(2);

	    if (minDistance < distance)
		requiredActions = true;

	    int tries = 0;
	    while (tries < 5) {
		List<Unit> uir = state.getUnitsInRadius(ab.toPosition(),
			radius);
		if (uir.size() == 0) {
		    if (u0.isIdle()) {
			wantPositions.add(ab.toPosition());
			u0.move(ab.toPosition());
		    }
		    break;
		}
		ab = ab.rotate(64);
		tries++;
	    }
	}
	for (Position pos : wantPositions) {
	    state.drawCircleMap(pos, 2, bwapi.Color.White);
	}
	return requiredActions;
    }
    
    /**
     * Find the unit that is closest to the given unit.
     * 
     * @param u
     * @param units
     * @return the unit that has the closest distance to u.
     */
    public static Unit closestTo(Unit u, List<Unit> units) {
    	assert units != null;
    	assert !units.isEmpty();

    	Unit closest = units.iterator().next();
    	int minDistance = u.getDistance(closest);
    	for (Unit c : units) {
    		int distance = u.getDistance(c);
    		if (minDistance > distance) {
    			minDistance = distance;
    			closest = c;
    		}
    	}
    	return closest;
    }
    
	/**
	 * Convert a list of units into a list of positions.
	 * 
	 * @param units
	 * @return a list of positions
	 */
	public static List<Position> asPositions(List<Unit> units) {
		List<Position> pos = new LinkedList<Position>();
		for (Unit u : units) {
			pos.add(u.getPosition());
		}
		return pos;
	}

	/**
	 * Return the focal point of a list of positions.
	 * @param positions
	 * @return the focal point of the given position list.
	 */
	public static Position focalPosition(List<Position> positions) {
		int sum_x = 0;
		int sum_y = 0;

		for (Position p : positions) {
			sum_x += p.getX();
			sum_y += p.getY();
		}
		return new Position(sum_x / positions.size(), sum_y / positions.size());
	}
	
	/**
	 * @param self
	 * @param units
	 * @return
	 */
	public static boolean containsEnemies(Player self, List<Unit> units) {
		for (Unit u : units) {
			if (u.getPlayer() != self)
				return true;
		}
		return false;
	}
    
    /**
     * Checks if the <code>Unit</code> can issue an move command.
     * 
     * @param unit
     * @param position
     * @return true if move cmd is possible
     */
    public static boolean canIssueMoveCommand(Unit unit, Position position) {
        UnitCommand currentCmd = unit.getLastCommand();
        UnitCommandType cmdType = currentCmd.getUnitCommandType();

        if (cmdType == UnitCommandType.Move) {
            if (!unit.isMoving()) {
                return true;
            } else {
                return unit.getDistance(position) < 20;
            }
        }

        return !commandWillInterruptAttack(unit);
    }

    private static boolean commandWillInterruptAttack(Unit unit) {
        // unit is attacking
        if (unit.isAttackFrame() || unit.isStartingAttack()) {
            return true;
        }

        return false;
    }
    
    /**
     * Calculate the center position to all given units.
     * 
     * @param units
     * @return
     */
    public static Position calcCenter(List<Unit> units) {
        if (units.isEmpty()) {
            return new Position(0, 0);
        }
        if (units.size() == 1) {
            return units.get(0).getPosition();
        }

        int minX = units.get(0).getX(), maxX = units.get(0).getX();
        int minY = units.get(0).getY(), maxY = units.get(0).getY();

        for (Unit unit : units) {
            Position p = unit.getPosition();
            if (p.getX() < minX) {
                minX = p.getX();
            }
            if (p.getX() > maxX) {
                maxX = p.getX();
            }
            if (p.getY() < minY) {
                minY = p.getY();
            }
            if (p.getY() > maxY) {
                maxY = p.getY();
            }
        }

        return new Position((minX + maxX) / 2, (minY + maxY) / 2);
    }
}

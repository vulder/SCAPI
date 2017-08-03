package SCAPI.UnitUtil;

import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.Game;
import bwapi.Position;

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

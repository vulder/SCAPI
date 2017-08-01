package UnitUtil;

import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.Game;
import bwapi.Position;
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
}

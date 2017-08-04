package SCAPI.UnitUtil;

import java.util.List;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;

public class UnitDebug {

    /**
     * Draws unit debug data to the screen.
     * 
     * @param game reference
     * @param self reference
     */
    public static void drawUnitDebug(Game game, Player self) {
        drawUnitDebug(game, self, 0);
    }

    /**
     * Draws unit debug data to the screen.
     * 
     * @param game reference
     * @param self reference
     * @param yOffset
     */
    public static void drawUnitDebug(Game game, Player self, int yOffset) {
        // Drawing own Units

        for (Unit myUnit : self.getUnits()) {
            game.drawTextScreen(10, 20 + yOffset, unitToFancyString(myUnit));
            yOffset += 10;
        }
    }
    
    /**
     * Draw debug infos for all units.
     * 
     * @param game
     * @param withInfo
     *            Draw with info text
     */
    public static void drawUnitHelpAll(Game game, boolean withInfo) {
        for (Unit myUnit : game.self().getUnits()) {
            drawUnitHelp(game, myUnit, withInfo);
        }
    }
    
    /**
     * Draw debug infos for unit.
     * 
     * @param game
     * @param unit
     * @param withInfo
     *            Draw with info text
     */
    public static void drawUnitHelp(Game game, Unit unit, boolean withInfo) {
        // draw text
        if (withInfo) {
            int x = unit.getX() - 10;
            int y = unit.getY() + 5;

            game.drawTextMap(x, y, getUnitInfo(unit));
        }

        // draw lines
        if (unit.isIdle())
            return;

        Position pos = unit.getOrderTargetPosition();
        bwapi.Color c = bwapi.Color.Green;

        UnitCommandType ty = unit.getLastCommand().getUnitCommandType();
        if (ty == UnitCommandType.Attack_Move || ty == UnitCommandType.Attack_Unit)
            c = bwapi.Color.Red;

        game.drawLineMap(unit.getPosition(), pos, c);
    }

    /**
     * Draws unit command debug data to the screen.
     * 
     * @param game
     * @param yOffset
     */
    public static void drawUnitCommands(Game game) {
        drawUnitCommands(game, 0);
    }
    
    /**
     * Draws unit command debug data to the screen.
     * 
     * @param game
     * @param yOffset
     */
    public static void drawUnitCommands(Game game, int yOffset) {
        Player self = game.self();
        
        for (Unit myUnit : self.getUnits()) {
            game.drawTextScreen(10, 20 + yOffset, unitCommandStr(myUnit));
            yOffset += 10;
        }
        
    }
    
	public static void drawEnemies(Game state, Player enemy) {
		for (Unit e : enemy.getUnits()) {
			if (!e.isVisible())
				continue;
			state.drawBoxMap(e.getX() - 2, e.getY() - 2, e.getX() + 2, e.getY() + 2, bwapi.Color.Red);
		}
	}

	public static void drawOrders(Game state, List<Unit> units) {
		for (Unit u : units) {
			if (u.isIdle())
				continue;

			Position pos = u.getOrderTargetPosition();
			bwapi.Color c = bwapi.Color.Green;

			UnitCommandType ty = u.getLastCommand().getUnitCommandType();
			if (ty == UnitCommandType.Attack_Move || ty == UnitCommandType.Attack_Unit)
				c = bwapi.Color.Red;

			state.drawLineMap(u.getPosition(), pos, c);
		}
	}

    private static String unitToFancyString(Unit unit) {
        StringBuilder SB = new StringBuilder();
        Unit target = unit.getOrderTarget();

        SB.append(String.format("ID: %2d (Health: %3d/%3d)", unit.getID(), unit.getHitPoints(),
                unit.getType().maxHitPoints()));

        if (target == null) {
            SB.append(String.format(" attacks -> nothing"));
        } else {
            SB.append(String.format(" attacks -> Unit: %2d T:%s (Health: %3d/%3d)", target.getID(),
                    target.getType().toString(), target.getHitPoints(), target.getType().maxHitPoints()));
        }

        return SB.toString();
    }
    
    private static String unitCommandStr(Unit unit) {
        StringBuilder SB = new StringBuilder();
        
        UnitCommand cmd = unit.getLastCommand();
        
        String targetStr = "nothing";
        
        if (cmd.getTarget() != null) {
            targetStr = cmd.getTarget().toString();
        }
        if (cmd.getTargetPosition() != null) {
            targetStr = cmd .getTargetPosition().toString();
        }

        String cmdString = String.format("%14s -> %s", cmd.getUnitCommandType(), targetStr);
        
        
        SB.append(String.format("ID: %2d doing: %s", unit.getID(), cmdString));
        
        return SB.toString();
    }
    
    private static String getUnitInfo(Unit unit) {
        StringBuilder SB = new StringBuilder();

        SB.append(String.format("ID%2d (Health %3d/%3d)\n", unit.getID(), unit.getHitPoints(),
                unit.getType().maxHitPoints()));
        SB.append(String.format("CMD %s\n", unit.getLastCommand().getUnitCommandType()));

        return SB.toString();
    }
}

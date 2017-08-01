package SCAPI.UnitUtil;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;

public class UnitDebug {

    public static void drawUnitDebug(Game game, Player self) {
        // Drawing own Units
        int yOffset = 0;

        for (Unit myUnit : self.getUnits()) {
            game.drawTextScreen(10, 20 + yOffset, unitToFancyString(myUnit));
            yOffset += 10;
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
}

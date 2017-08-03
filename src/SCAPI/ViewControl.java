package SCAPI;

import bwapi.Game;
import bwapi.Position;
import SCAPI.UnitUtil.UnitControl;

public class ViewControl {

    public static void screenAutoFollow(Game game) {
        Position center = UnitControl.calcCenter(game.self().getUnits());

        game.setScreenPosition(center.getX() - 300, center.getY() - 200);
    }
}

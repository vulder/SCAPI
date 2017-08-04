package SCBots17.BotLmao.BotAcc;

import java.util.Comparator;
import java.util.List;

import bwapi.*;

public class BotToolsLmao {

  public static void UnitCheck(Game game) {
    game.drawTextScreen(50, 70, ("BotLmao's Units: " + game.self().getUnits().size()));
    game.drawTextScreen(50, 50, ("Enemies (Visible) Units: " + game.enemy().getUnits().size()));
  }
    
  public static int getSide(Game game) {
    if (game.self().getUnits().get(0).getPosition().getY() < 450) {
      return 1;
    }
    else {
      game.sendText("b" + game.self().getUnits().get(0).getPosition().getY());
      return -1;
    }
    
  }
  
  public static void showUnitNumbers(Game game) {
    for (int i = 0; i < game.self().getUnits().size(); i++) {
      game.drawText(bwapi.CoordinateType.Enum.Map, game.self().getUnits().get(i).getX(), game.self().getUnits().get(i).getY() - 3 , "Unit No. " + i);
    }
  }
  
  public static void showFirebatInfo(Game game, int minRadius, int maxRadius, Unit fb) {
    for (int i = 0; i < game.self().getUnits().size(); i++) {
      game.drawCircleMap(game.self().getUnits().get(i).getPosition(), minRadius, Color.Red);
      game.drawCircleMap(game.self().getUnits().get(i).getPosition(), maxRadius, Color.Green);
      game.drawTextScreen( 50, 110 + 20*i, ("Distance to Firebat ( Unit No." + i + " ) : " + game.self().getUnits().get(i).getDistance(fb)));
    }    
  }
  
  public static void BotonUnitDiscover(Game game, Unit unit, List<Unit> eus, List<Unit> enus) {
    if (unit.getPlayer() == game.self()) {
      eus.add(unit);
    } else if (unit.getPlayer() == game.enemy()) {
      enus.add(unit);
    }
    enus.sort(new Comparator<Unit>() {
  		@Override
   	public int compare(Unit o1, Unit o2) {
    int compare = 0;
    compare = o1.getInitialHitPoints() - o2.getInitialHitPoints();
    return compare;
  		}
    });
  }
  
  public static void drawOrders(Game game, List<Unit> units) {
    for (Unit u : units) {
      if (u.isIdle())
        continue;
      
      Position pos = u.getOrderTargetPosition();
      bwapi.Color c = bwapi.Color.Green;
      
      UnitCommandType ty = u.getLastCommand().getUnitCommandType();
      if (ty == UnitCommandType.Attack_Move || ty == UnitCommandType.Attack_Unit) {
        c = bwapi.Color.Red; 
      }
      game.drawLineMap(u.getPosition(), pos, c);
    }
  }
   
}

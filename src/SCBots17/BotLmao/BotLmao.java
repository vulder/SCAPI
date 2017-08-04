package SCBots17.BotLmao;


import java.util.*;

import bwapi.*;

public class BotLmao extends DefaultBWListener {

  private Mirror mirror = new Mirror();

  private Game game;
  
  List<Unit> enus;
  List<Unit> eus;

  List<TilePosition> posPlayerUnits;

  int EnemyCount;
  int WalkTimeCounter;
  int SideFactor;
    
  @Override
  public void onStart() {
    game = mirror.getGame();
    eus = new ArrayList<Unit>();
    enus = new ArrayList<Unit>();
    posPlayerUnits = new ArrayList<TilePosition>();
    EnemyCount = 0;
    WalkTimeCounter = 0;
    SideFactor = 0;
    SideFactor = SCBots17.BotLmao.BotAcc.BotToolsLmao.getSide(game);
    game.sendText("BotLmao has initialised");
    
  }
    
  public void onUnitDiscover(Unit unit) {
    SCBots17.BotLmao.BotAcc.BotToolsLmao.BotonUnitDiscover(game, unit, eus, enus);
  }

  public void onFrame() {
    
    EnemyCount = game.enemy().getUnits().size();
    
    SCBots17.BotLmao.BotAcc.BotToolsLmao.UnitCheck(game);
    
    SCBots17.BotLmao.BotAcc.BotToolsLmao.showUnitNumbers(game);
    
    posPlayerUnits = new LinkedList<TilePosition>();
    
    SCBots17.BotLmao.BotAcc.BotToolsLmao.drawOrders(game, eus);
    
    for (int i = 0; i < eus.size(); i++) {
      posPlayerUnits.add(eus.get(i).getTilePosition());
    }
    
    if (game.mapFileName().equals("04_Zehnkampf_Multi.scx")) {
      game.drawTextScreen(50, 90, ("Using Nullformation"));
      SCBots17.BotLmao.BotAcc.BotFormationsLmao.useNullFormation();
    } else if (game.mapFileName().equals("01_BattleRoyale.scx")) {
      game.drawTextScreen(50, 90, ("Using Lineformation"));
      SCBots17.BotLmao.BotAcc.BotFormationsLmao.useLineFormation(game, EnemyCount, SideFactor, eus, enus, posPlayerUnits);
    } else if (game.mapFileName().equals("02_Zweikampf.scx")) {
      game.drawTextScreen(50, 90, ("Using Backtrackformation"));
      SCBots17.BotLmao.BotAcc.BotFormationsLmao.useBacktrackFormation(game, EnemyCount, SideFactor, eus, enus, posPlayerUnits);
    } 
    else if (game.mapFileName().equals("99_Lan_MarineFirebat.scx")) {
      game.drawTextScreen(50, 90, ("Using Backtrackformation"));
      SCBots17.BotLmao.BotAcc.BotFormationsLmao.useBacktrack2Formation(game, EnemyCount, SideFactor, eus, enus, posPlayerUnits);
    }
    else {
      game.drawTextScreen(50, 90, ("No specific Map formation. Defaulting to Lineformation"));
      SCBots17.BotLmao.BotAcc.BotFormationsLmao.useLineFormation(game, EnemyCount, SideFactor, eus, enus, posPlayerUnits);
    }
  }
 
  public void useAmbushFormation() {
    for (int i = 0; i < eus.size(); i++) {
      if (EnemyCount > 0) {
        if (eus.get(i).getLastCommandFrame() >= game.getFrameCount() || eus.get(i).isAttackFrame()) {
        } 
        else if (eus.get(i).getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
        } 
        else {
          if (enus.get(i).isVisible()) {
            eus.get(i).attack(enus.get(i));
          }
        }
      } 
      else if ((i % 2) == 0) {
        eus.get(i).move(new TilePosition(this.posPlayerUnits.get(i).getX() - 5, this.posPlayerUnits.get(i).getY()).toPosition());
      }
      else {
        if (this.posPlayerUnits.get(i).getX() <= 35) {
          eus.get(i).move(new TilePosition(this.posPlayerUnits.get(i).getX() + 5, this.posPlayerUnits.get(i).getY()).toPosition());
        } else {
          eus.get(i).move(new TilePosition(this.posPlayerUnits.get(i).getX() - 5, this.posPlayerUnits.get(i).getY() + 5).toPosition());
        }
      } 
    }
  }

  // Unver�nderbar
  public void run() {
    mirror.getModule().setEventListener(this);
    mirror.startGame();
  }

  public static void main(String[] args) {
    new BotLmao().run();
  }
}
package SCBots17.BotLmao.BotAcc;

import java.util.*;

import bwapi.*;



public class BotFormationsLmao {
 
  public static void useNullFormation() {
    
  }
    
  public static void useLineFormation(Game game, int EnemyCount, int SideFactor, List<Unit> eus, List<Unit> enus, List<TilePosition> posPlayerUnits) {
    for (int j = 0; j < EnemyCount + 1; j++) {
      if (game.enemy().getUnits().size() > 0) {
        for (int i = 0; i < eus.size(); i++) {
          if (eus.get(i).getLastCommandFrame() >= game.getFrameCount() || eus.get(i).isAttackFrame()) {
          } 
          else if (eus.get(i).getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
          } 
          else {
            if (enus.get(j).isVisible()) {
              eus.get(i).attack(enus.get(j));
            }
          }
        }
      }
      
      else {
        for (int i = 0; i < eus.size(); i++) {  
          if (i == 0) {
            eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX(), posPlayerUnits.get(i).getY() + 16*SideFactor).toPosition());
          } else {
            if (eus.get(i - 1).exists()) {
              eus.get(i).move(new TilePosition(posPlayerUnits.get(i - 1).getX() + 10*SideFactor, posPlayerUnits.get(i - 1).getY() + 16*SideFactor).toPosition());
            } else {
              eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX(), posPlayerUnits.get(i - 1).getY() + 16*SideFactor).toPosition());
            }
          }
        }
      }
    }
  }
    
  public static void useBacktrackFormation(Game game, int EnemyCount, int SideFactor, List<Unit> eus, List<Unit> enus, List<TilePosition> posPlayerUnits) {
    if (game.enemy().getUnits().size() <= 0) {
      for (int i = 0; i < eus.size(); i++) {
        if (SideFactor == 1) {
          if ((i % 2) == 0) {
            eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX() - 2*SideFactor, posPlayerUnits.get(i).getY() + 10*SideFactor).toPosition());
          }
          else {
            eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX() + 5*SideFactor, posPlayerUnits.get(i).getY() + 10*SideFactor).toPosition());
          }
        }
        else {
          if ((i % 2) == 0) {
            eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX() + 2*SideFactor, posPlayerUnits.get(i).getY() + 10*SideFactor).toPosition());
          }
          else {
            eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX() - 2*SideFactor, posPlayerUnits.get(i).getY() + 10*SideFactor).toPosition());
          }
        }
        
      }   
    }
    else {
      List<Unit> firebats = new LinkedList<Unit>();
      for (Unit u : enus) {
        if (u.getType() == UnitType.Terran_Firebat) {
          firebats.add(u);
        }
      }
      
      for (int j = 0; j < enus.size(); j++) {
        if (!firebats.get(j).exists()) {
          firebats.remove(j);
        }
        if (firebats.size() > 0) {
          for (int i = 0; i < eus.size(); i++) {
            Unit eu = eus.get(i);
            Unit enu = enus.get(j);
            UnitType enuTy = enu.getType();
            
            if (enuTy != UnitType.Terran_Firebat) {
              j = j+1;
              enu = enus.get(j);
            }
            
            int maxRadius = 110;
            int minRadius = 68;
            
            BotToolsLmao.showFirebatInfo(game, minRadius, maxRadius, eu);
            
            if (enu.getDistance(eu) <= maxRadius && enu.getDistance(eu) >= minRadius) {
              if ((i % 2) == 0) {
                eu.move((new TilePosition(posPlayerUnits.get(i).getX() - 3*SideFactor, posPlayerUnits.get(i).getY() - 10*SideFactor)).toPosition());
              }
              else {
                eu.move((new TilePosition(posPlayerUnits.get(i).getX() + 3*SideFactor, posPlayerUnits.get(i).getY() - 10*SideFactor)).toPosition());
              }
            }
            else if (eu.getLastCommandFrame() >= game.getFrameCount() || eu.isAttackFrame()) {
            }
            else if (eu.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
            }
            else {
              if (enu.isVisible()) {
                eu.attack(enu);
              }
            }
          }
        }
        else {
          for (int i = 0; i < eus.size(); i++) {
            if (!enus.get(j).isVisible()) {
              eus.get(i).move(new TilePosition(eus.get(i).getX(), eus.get(i).getY() + 10*SideFactor).toPosition());
            }
            else {
              if (eus.get(i).getLastCommandFrame() >= game.getFrameCount() || eus.get(i).isAttacking()) {
              }
              else if (eus.get(i).getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
              }
              else {
                eus.get(i).attack(game.enemy().getUnits().get(j));
              }
            }
          }
        }
      }
    }
  }
    
  public static void useBacktrack2Formation(Game game, int EnemyCount, int SideFactor, List<Unit> eus, List<Unit> enus, List<TilePosition> posPlayerUnits) {
    if (game.enemy().getUnits().size() <= 0) {
      for (int i = 0; i < eus.size(); i++) {  
        if (i == 0) {
          eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX() - 5*SideFactor, posPlayerUnits.get(i).getY() + 10*SideFactor).toPosition());
        } else {
          //              if (eus.size() != 1 && eus.get(i - 1).exists()) {
          //                eus.get(i).move(new TilePosition(posPlayerUnits.get(i - 1).getX() - 5, posPlayerUnits.get(i - 1).getY() + 10).toPosition());
          //              } else {
          eus.get(i).move(new TilePosition(posPlayerUnits.get(i).getX() - 5*SideFactor, posPlayerUnits.get(i - 1).getY() + 10*SideFactor).toPosition());
          //              }
        }         
      }
    }
    else {
      List<Unit> firebats = new LinkedList<Unit>();
      for (Unit u : enus) {
        if (u.getType() == UnitType.Terran_Firebat) {
          firebats.add(u);
        }
      }
      
      //        firebats.sort(new Comparator<Unit>() {
      //        @Override
      //        public int compare(Unit o1, Unit o2) {
      //        int compare = 0;
      //        compare = o2.getDistance(eus.get(0)) - o1.getDistance(eus.get(0));
      //        return compare;
      //        }
      //        });
      
      for (int j = 0; j < enus.size(); j++) {
        if (!firebats.get(j).exists()) {
          firebats.remove(j);
        }
        if (firebats.size() > 0) {
          for (int i = 0; i < eus.size(); i++) {
            Unit eu = eus.get(i);
            Unit enu = enus.get(j);
            UnitType enuTy = enu.getType();
            
            if (enuTy != UnitType.Terran_Firebat) {
              j = j+1;
              enu = enus.get(j);
            }
            
            int maxRadius = 50;
            int minRadius = 40;
            
            BotToolsLmao.showFirebatInfo(game, minRadius, maxRadius, enu);
            
            if (enu.getDistance(eu) <= maxRadius && enu.getDistance(eu) >= minRadius && eu.getType() != UnitType.Terran_Firebat) {
              if ((i % 2) == 0) {
                eu.move((new TilePosition(posPlayerUnits.get(i).getX() - 3*SideFactor, posPlayerUnits.get(i).getY() - 5*SideFactor)).toPosition());
              }
              else {
                eu.move((new TilePosition(posPlayerUnits.get(i).getX() + 3*SideFactor, posPlayerUnits.get(i).getY() - 5*SideFactor)).toPosition());
              }
            }
            else if (eu.getLastCommandFrame() >= game.getFrameCount() || eu.isAttackFrame()) {
            }
            else if (eu.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
            }
            else {
              if (enu.isVisible()) {
                eu.attack(enu);
              }
            }
          }
        }
        else {
          for (int i = 0; i < eus.size(); i++) {
            if (!enus.get(j).isVisible()) {
              eus.get(i).move(new TilePosition(eus.get(i).getX(), eus.get(i).getY() + 10*SideFactor).toPosition());
            }
            else {
              if (eus.get(i).getLastCommandFrame() >= game.getFrameCount() || eus.get(i).isAttacking()) {
              }
              else if (eus.get(i).getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
              }
              else {
                eus.get(i).attack(game.enemy().getUnits().get(j));
              }
            }
          }
        }
      }
    }
  }
    
    
}

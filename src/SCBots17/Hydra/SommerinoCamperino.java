package SCBots17.Hydra;


import java.util.List;

import SCAPI.UnitUtil.UnitControl;
import SCAPI.UnitUtil.UnitDebug;
import SCAPI.UnitUtil.Vector;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class SommerinoCamperino extends DefaultBWListener {
    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private Player enemy;
    
    
    
    

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
	if (unit.getPlayer() == self) {
	    
	    TilePosition cur = unit.getTilePosition();
	    //allUnitsStop();
	    unit.attack(
		  new TilePosition(cur.getX(), cur.getY() + 16).toPosition());
	}
    }

    @Override
    public void onStart() {
	game = mirror.getGame();
	self = game.self();
	enemy = game.enemy();
	
    }
    
   
    
    
    
    
    
   
	
    private String getDBStringForUnit(Unit u) {
	StringBuilder SB = new StringBuilder();
	
	SB.append("ID: " +  u.getID());
	SB.append(" T: " + u.getType());
	
	return SB.toString();
    }
    

    
    
    protected boolean attacksPrio(Unit u, Unit prio) {
	if(u.isAttacking() || u.isStartingAttack()) {
	    if(u.getOrderTarget().equals(prio)) {
		return true;
	    }
	}
	   return false;
	
    }
    
    
    
    
    
    
    private void allUnitsStop() {
	for (Unit myUnit : self.getUnits()) {

	    myUnit.holdPosition();

	}
    }
    
    
    
    protected Unit getPrioTarget(List<Unit> enemyUnits) {
	Unit target = enemyUnits.get(0);
	
	    if (enemyUnits.isEmpty()) {
		return null;
	    } else {
		int i = 0;
		while (i < enemyUnits.size()) {
		    if (enemyUnits.get(i)
			    .getType() == UnitType.Terran_Firebat) {
			target = enemyUnits.get(i);
			game.sendText("Prio Target found:"
				+ enemyUnits.get(i).getType());
			break;
		    } else {
			i++;
		    }
		}
	    }
		
	   
	
	return target;
    }

    public boolean attacksPrioTarget(Unit u, Unit prio) {
	if (u.isAttacking() || u.isStartingAttack()) {
	    if (u.getOrderTarget().equals(prio)) {
		return true;
	    }
	}
	return false;
    }
    
    
   


    
    
    
    
    
    
    
    

    protected boolean attackEverythingInSight() {

	Player enemy = game.enemy();
	List<Unit> eus = enemy.getUnits();
	if (eus.isEmpty())
	    return false;
	allUnitsStop();

	Unit eu = getPrioTarget(eus);
	

	for (Unit myUnit : self.getUnits()) {

	    if (attacksPrio(myUnit, eu) == true) {

		if (myUnit.getLastCommandFrame() >= game.getFrameCount()
			|| myUnit.isAttackFrame()) {
		    continue;
		}
		if (myUnit.getLastCommand()
			.getUnitCommandType() == UnitCommandType.Attack_Unit) {
		    continue;
		}
		game.sendText("I'm coming for you!");
	    } else {

		myUnit.attack(eu);
		
		
	    }

	}
	return true;
    }
    
    
    public void healthcheck() {
	int i = 0;

	for (Unit myUnit : self.getUnits()) {

	    i = i + 1;
	    for (Unit myEnemy : enemy.getUnits()) {
		if (myUnit.isInWeaponRange(myEnemy)) {
		    String health = Integer.toString(myUnit.getHitPoints());
		    game.sendText("Unit " + getDBStringForUnit(myUnit) + " has "
			    + health + " Health");

		    if (myUnit.getHitPoints() <= 12) {

			if (myUnit.isInWeaponRange(myEnemy)) {
			    if (myEnemy.getType() == UnitType.Terran_Firebat) {
				
				    if (myUnit.getDistance(myEnemy) < 100) {

					if (myUnit.getY() > myEnemy.getY()) {
					    Position n = new Position(
						    myUnit.getX(),
						    myUnit.getY() + 20);

					    myUnit.move(n);
					    game.sendText("Moving down");
					    

					} else {
					    Position n = new Position(
						    myUnit.getX(),
						    myUnit.getY() - 20);

					    myUnit.move(n);
					    game.sendText("Moving Up");
					    
					}
				    }
				}

			    }

			}
		    }
		}
	    }
	}
   

    
    

    

    
    public void spread(List<Unit> myUnits) {
	for (int i = 1; i < myUnits.size(); i++) {

	    if ((myUnits.get(i).getX() < myUnits.get(i - 1).getX() - 10)
		    && (myUnits.get(i).getY() < myUnits.get(i - 1).getY()
			    - 15)) {

		Position p = new Position(myUnits.get(i).getX() - 600,
			myUnits.get(i).getY() + 5);

		myUnits.get(i).move(p);

		game.sendText(i + "Nach links");
		game.sendText(i + "Nach unten");

	    } else if ((myUnits.get(i).getX() < myUnits.get(i - 1).getX() + 10)
		    && (myUnits.get(i).getY() < myUnits.get(i - 1).getY()
			    - 15)) {

		Position p = new Position(myUnits.get(i).getX() + 600,
			myUnits.get(i).getY() + 5);

		myUnits.get(i).move(p);

		game.sendText(i + "Nach rechts");
		game.sendText(i + "Nach unten");

	    } else if ((myUnits.get(i).getX() < myUnits.get(i - 1).getX() - 10)
		    && (myUnits.get(i).getY() < myUnits.get(i - 1).getY()
			    - 15)) {

		Position p = new Position(myUnits.get(i).getX() - 600,
			myUnits.get(i).getY() - 5);

		myUnits.get(i).move(p);

		game.sendText(i + "Nach links");
		game.sendText(i + "Nach oben");

	    } else if ((myUnits.get(i).getX() < myUnits.get(i - 1).getX() + 10)
		    && (myUnits.get(i).getY() < myUnits.get(i - 1).getY()
			    - 15)) {

		Position p = new Position(myUnits.get(i).getX() + 600,
			myUnits.get(i).getY() - 5);

		myUnits.get(i).move(p);

		game.sendText(i + "Nach rechts");
		game.sendText(i + "Nach oben");

	    } else if (myUnits.get(i).getY() < myUnits.get(i - 1).getY() - 10) {

		Position p = new Position(myUnits.get(i).getX(),
			myUnits.get(i).getY() + 5);

		myUnits.get(i).move(p);

		game.sendText(i + "Nach unten");

	    } else if (myUnits.get(i).getY() < myUnits.get(i - 1).getY() + 10) {

		Position p = new Position(myUnits.get(i).getX(),
			myUnits.get(i).getY() - 5);

		myUnits.get(i).move(p);

		game.sendText(i + "Nach oben");

	    } else if (myUnits.get(i).getX() < myUnits.get(i - 1).getX() + 10) {

		Position p = new Position(myUnits.get(i).getX() + 600,
			myUnits.get(i).getY());

		myUnits.get(i).move(p);

		game.sendText(i + "Nach rechts");

	    } else if (myUnits.get(i).getX() < myUnits.get(i - 1).getX() - 10) {

		Position p = new Position(myUnits.get(i).getX() - 600,
			myUnits.get(i).getY());

		myUnits.get(i).move(p);

		game.sendText(i + "Nach links");
	    }

	    else {
		myUnits.get(i).holdPosition();
	    }

	}
    }
    
    
    private void zehnkampf(List<Unit> myUnits) {
	int z = 100;
	int x = 0;
	game.sendText("Hello");
	
	
	
	
	
	for(int i = 0; i < (myUnits.size())/2; i++) {
	    z = z - 20;
	    Position p = new Position(myUnits.get(i- 1).getX() + 10 , myUnits.get(i).getY() - z + 10);
	    myUnits.get(i).move(p);
	    game.sendText(i + "up");
	}
	for(int i = myUnits.size()/2 ; i < myUnits.size(); i++) {
	    x = x + 20;
	    Position n = new Position(myUnits.get(i- 1).getX() + 10 , myUnits.get(i).getY() - x - 10);
	    myUnits.get(i).move(n);
	    game.sendText(i + "up");
	}
 
    }
    
    
    
		
	    
   
    
    private void drawOrders(Game state, List<Unit> units) {
	for (Unit u : units) {
	    if (u.isIdle())
		continue;

	    Position pos = u.getOrderTargetPosition();
	    bwapi.Color c = bwapi.Color.Green;

	    UnitCommandType ty = u.getLastCommand().getUnitCommandType();
	    if (ty == UnitCommandType.Attack_Move
		    || ty == UnitCommandType.Attack_Unit)
		c = bwapi.Color.Red;

	    game.drawLineMap(u.getPosition(), pos, c);
	}

    }

    @Override
    public void onUnitDiscover(Unit u) {
	try {
	    Vector v = new Vector(u.getPosition());
	    v.draw(game, bwapi.Color.Yellow, "detected");
	    
	
	if (u.getPlayer() != self) {
	    game.sendText("Now tha's where you were ");
	    List<Unit> np = self.getUnits();
	    zehnkampf(np);
	    attackEverythingInSight();
	    }
	} catch(Throwable t) {
	    t.printStackTrace();
	    game.sendText("Caught exception");
	}
    }

    @Override
    public void onUnitDestroy(Unit u) {
	if (u.getPlayer() == self) {
	    game.sendText("Damn you all!");
	} else {
	    
	    attackEverythingInSight();
	}
    }

    
    
    @Override
    public void onFrame() {
	
	/*if (game.getFrameCount() < 11) {
	    List<Unit> mU = self.getUnits();
	    spread(mU);

	} else if (game.getFrameCount() == 24) {
	    for (Unit myUnit : self.getUnits()) {
		TilePosition cur = myUnit.getTilePosition();
		myUnit.attack(new TilePosition(cur.getX(), cur.getY() + 16)
			.toPosition());
	    }
	}*/

	game.drawTextScreen(10, 10,
		"Playing as " + self.getName() + " - " + self.getRace());

	UnitDebug.drawUnitDebug(game, self);
	
	drawOrders(game, self.getUnits());

	//healthcheck();
	

    }
    
    
    

    public static void main(String[] args) {
	new SommerinoCamperino().run();
    }
}
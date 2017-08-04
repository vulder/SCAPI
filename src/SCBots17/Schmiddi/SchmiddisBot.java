package SCBots17.Schmiddi;
import java.util.LinkedList;
import java.util.List;

import SCAPI.ViewControl;
import SCAPI.UnitUtil.Line;
import SCAPI.UnitUtil.UnitDebug;
import SCAPI.UnitUtil.Vector;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class SchmiddisBot extends DefaultBWListener {
    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private java.util.List<Unit> myUnits;
    private java.util.List<Unit> enUnits;
    private Player enemy;
    
    private int waitBeforeAttack=240;
    private int waitBeforeGo=0;
    private int waitBetweenSteps=24;
    
    public Position[] positionsToGo;

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }


    @Override
    public void onStart() {
	waitBeforeAttack=240;
	waitBeforeGo=240;
	waitBetweenSteps=24;
	game = mirror.getGame();
	enemy=game.enemy();
	self = game.self();
	myUnits=self.getUnits();
	enUnits=enemy.getUnits();
	
	game.sendText("HI");
	getTogether();
	
	
    }
    
    private void getTogether() {
	positionsToGo = new Position[myUnits.size()];
	int unitToMove=1;

	int xLetztes=myUnits.size()/2;
	
	for (int x = 1; x < myUnits.size() / 2+1; x++) {

	    for (int y = 1; y < myUnits.size() / 5+1; y++) {

		Position newPos = new Position(myUnits.get(0).getX() + 32 * (x),
			myUnits.get(0).getY() + 64 * (y-1));
		
		List<TilePosition> tiles = BWTA.getShortestPath(
			myUnits.get(unitToMove).getTilePosition(),
			newPos.toTilePosition());
		
		if (tiles.size() < 1000) {
		    myUnits.get(unitToMove).move(newPos);
		}
		unitToMove++;
	    }

	}
	Position newPosition = new Position(
		myUnits.get(0).getX() + 32 * xLetztes, myUnits.get(0).getY());
	myUnits.get(unitToMove).move(newPosition);
	
    }
   
    public void onUnitDestroy(Unit u) {
        if (u.getPlayer() == self) {
            game.sendText("Das war gewollt!!");
        } else {
            game.sendText("Jetzt schon tot?");
        }
    }
    
    private void attackClosestEnemy(Unit u)
    {
	float distance=1000000000;
	Unit enemy=u;
	for(Unit e :enUnits)
	{
	    if(u.getDistance(e)<distance&&e.getPlayer()==game.enemy())
	    {
		distance=u.getDistance(e);
		enemy=e;
	    }
	}
	u.attack(enemy);
    }
    private void attack()
   {
       if(waitBeforeAttack>0)
       {
	   waitBeforeAttack--;
	   return;
	   
       }
       for(Unit u :myUnits)
       {
	   if (u.getLastCommandFrame() >= game.getFrameCount()
		    || u.isAttackFrame())
		continue;
	   if (u.getLastCommand()
		    .getUnitCommandType() == UnitCommandType.Attack_Unit)
		continue;

	   if(u.isHoldingPosition()||u.isIdle())
	   {
	       attackClosestEnemy(u);
	   }
	   for(Unit e:enUnits)
	   {
	       WeaponType enWT=e.getType().groundWeapon();

	       if(u.getDistance(e)-20>enWT.maxRange())
	       {
		   attackClosestEnemy(u);
	       }

	   }
	   
       }
   }
    private void allAttack()
    {
	for(Unit u:myUnits)
	{
	    attackClosestEnemy(u);
	    
	}
    }
    
    
    private void InWeaponRange()
    {	

	for(Unit e:enUnits)
	{
	    for(Unit u:myUnits)
	    {
		if(u.isInWeaponRange(e))
		    allAttack();
	    }
	}
	
    }
    @Override
    public void onFrame() {
	ViewControl.screenAutoFollow(game);
	
	game.drawTextScreen(10, 10,
		"Playing as " + self.getName() + " - " + self.getRace());

	
	UnitDebug.drawUnitHelpAll(game,false);

	attack();
	InWeaponRange();
	//go();

    }

    public static void main(String[] args) {
	new SchmiddisBot().run();
    }
}
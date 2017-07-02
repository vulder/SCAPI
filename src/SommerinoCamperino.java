import java.util.List;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class SommerinoCamperino extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
        if (unit.getPlayer() == self) {
        	TilePosition cur = unit.getTilePosition();
        	unit.attack(new TilePosition(cur.getX(), cur.getY() + 64).toPosition());
        }
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
    }
    
    protected boolean attackEverythingInSight() {
    	Player enemy = game.enemy();
    	
    	List<Unit> eus = enemy.getUnits();
    	if (!eus.isEmpty()) {
    		Unit eu = eus.get(0);
    		for (Unit myUnit : self.getUnits()) {
            	if (myUnit.getLastCommandFrame() >= game.getFrameCount() || myUnit.isAttackFrame())
        			continue;
            	if (myUnit.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit)
            		continue;
        		game.sendText("I'm coming for you!");
        		System.out.println("Target: " + eu.toString());
        		
        		myUnit.attack(eu);
        	}
    		return true;
    	} 
    	return false;
    }
    
    @Override
    public void onUnitDiscover(Unit u) {
    	System.out.println("Found new unit " + u.getType());
    	if (u.getPlayer() != self) {
    		game.sendText("Got you!");
    		attackEverythingInSight();
    	}
    }
    
    @Override
    public void onUnitDestroy(Unit u) {
    	if (u.getPlayer() == self) {
    		game.sendText("OH SHIT!");
    	} else {
    		attackEverythingInSight();
    	}
    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
    }

    public static void main(String[] args) {
        new SommerinoCamperino().run();
    }
}
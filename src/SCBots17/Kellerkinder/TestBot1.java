package SCBots17.Kellerkinder;



import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import java.util.Comparator;
import java.util.List;

import SCAPI.UnitUtil.UnitDebug;
//import SommerinoCamperino.State;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    private boolean bat = false;
    private boolean erster = false;
    private String laufen = "rechts";

    // private Position p;
    private Unit en1 = null;
    private Unit en2 = null;
    int a;
    private Position p;
    public List<Unit> units;

    enum State {
	INIT, NORMAL, ORDER, ATT
    }

    private State AIstate = State.INIT;

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
	System.out.println("New unit discovered " + unit.getType());
	// if (unit.getType() != UnitType.Buildings) {
	// if (unit.getPlayer() == self) {
	//// p = unit.getPosition();
	//// a = p.getY() + 15;
	// units.add(unit);
	//
	// } /*else {
	// unit.move(new Position(p.getX() - 40, p.getY()));
	// erster = true;
	//
	// }*/

    }

    @Override
    public void onStart() {
	game = mirror.getGame();
	self = game.self();
	en1 = null;
	en2 = null;	
	erster = true;
	units.clear();
	AIstate = State.INIT;	
	// Use BWTA to analyze map
	// This may take a few minutes if the map is processed first time!
	System.out.println("Analyzing map...");
	BWTA.readMap();
	BWTA.analyze();
	System.out.println("Map data ready");

	int i = 0;
	for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
	    System.out.println("Base location #" + (++i)
		    + ". Printing location's region polygon:");
	    for (Position position : baseLocation.getRegion().getPolygon()
		    .getPoints()) {
		System.out.print(position + ", ");
	    }
	}

    }

    public void Sort() {
	if (units.size() == 2 && (units.get(0).getX() != units.get(1).getX())) {
	    
	}
	units.sort(new Comparator<Unit>() {
	    public int compare(Unit u0, Unit u1) {
		int comp = u0.getX() - u1.getX();
		if (comp == 0) {
		    comp = u0.getY() - u1.getY();
		}
		return comp;
	    }
	});
	
	if (game.enemy().getUnits().size() == 2 && (game.enemy().getUnits().get(0).getX() != game.enemy().getUnits().get(1).getX())) {
	   
	}
	units.sort(new Comparator<Unit>() {
	    public int compare(Unit u0, Unit u1) {
		int comp = u0.getX() - u1.getX();
		if (comp == 0) {
		    comp = u0.getY() - u1.getY();
		}
		return comp;
	    }
	});

	// erster=true;
	AIstate = State.ORDER;
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

    private Unit getFirebatIfAvailable(List<Unit> units) {
	for (Unit u : units) {
	    UnitType ty = u.getType();
	    if (ty == UnitType.Terran_Firebat)		
		return u;
	}

	return null;
    }

    @Override
    public void onFrame() {
	List<Unit> units = self.getUnits();
	List<Unit> eus = game.enemy().getUnits();
	drawOrders(game, units);
	
	
	switch (AIstate)
	{
	case INIT:
	    game.sendText("I will DESTROY you!");
	    units = self.getUnits();
	    sortUnits(game, units);
	    sortUnits(game, eus);	    
	    AIstate = State.NORMAL;
	    break;
	
	case ORDER:
	    game.sendText("Kellerkind");
	 }

	Unit fb = getFirebatIfAvailable(eus);
	if (fb != null) {
	    Color fbColor = Color.Green;
	    en1 = fb;
	    p = fb.getPosition();
	    // game.sendText("MIMIMIMI");

	    for (Unit u : units) {		
		if (!u.isIdle())
			continue;
		    if (u.getLastCommandFrame() >= game.getFrameCount()
			    || u.isAttackFrame())
			continue;

		    if (u.getLastCommand()
			    .getUnitCommandType() == UnitCommandType.Attack_Unit)
			continue;
		    
		    if (u.isInWeaponRange(en1)) {
//		    game.sendText("MIMIMIMI");
//		    game.sendText("LAUF FOREST LAUF!!!1!!1");
//		    u.move(new Position(u.getX() + 100, u.getY() + 100));
//		    game.sendText("Puh, die Kellerkinder k�nnen nicht mehr! ");
			u.attack(fb);
	    		fbColor = Color.Red;
		}
		else
		{		    
		    if(en1 == null)
		    {	
			for(int i = 0; i < eus.size();i++)
			    if(eus.get(i).getType() == UnitType.Terran_Marine)
			    {
				u.attack(eus.get(i));
				continue;
			    }
		    }	
		    
		}		   
	    }
	    
	    game.drawCircleMap(fb.getPosition(), 4, fbColor);
	}
	
	UnitDebug.drawUnitDebug(game, self, 30);
	
	if(self.isVictorious())
	{
	    
	    game.sendText("DIE KELLERKINDER HABEN DICH GEREKT!!!11!!!!111!1!1!1111111");
	    game.sendText("LOL ROFL LEL XD");
	    
	}
	if (self.isDefeated()) {
	    game.sendText("Die Kellerkinder kommen wieder!!!!!!!!!");
	}
    }

    public void onUnitDiscover(Unit u) {
//	game.sendText("there is no cow level");
    }

    public void onUnitDestroy(Unit u) {
	if (bat) {
	    if (u.getPlayer() != self) {
		en2 = null;
	    }
	} else if (u.getPlayer() == game.enemy()) {
	    game.sendText("DESTROYED!");
	    game.sendText("NOOb!");
	    en1 = null;
	    for (Unit unit : units) {
		unit.attack(en2);
	    }
	}

    }

    
    private void sortUnits(Game state, List<Unit> units) {
   	if (units.size() < 2)
   	    return;

   	switch (AIstate) {
   	case INIT:
   	case ORDER:
   	     AIstate = State.NORMAL;
   	    break;
   	default:
   	    return;
   	}

   	units.sort(new Comparator<Unit>() {
   	    @Override   	    
   	    public int compare(Unit o1, Unit o2) {
   		int compare = 0;
   		compare = o1.getX() - o2.getX();
   		if (compare == 0)
   		    compare = o1.getY() - o2.getY();
   		return compare;
   	    }
   	});

   	Unit u0 = units.get(0);
   	Unit u1 = units.get(units.size() - 1);

   	p = u0.getPosition();
//   	game.sendText(p.getX() + " " + p.getY());
   	if (u0.isIdle())
   	    u0.move(new Position(p.getX(), p.getY()));

   	Position p0 = u0.getPosition();
   	Position newPos = new Position(p0.getX()+ 25, p0.getY());

   	game.drawCircleMap(newPos, 1, bwapi.Color.Red);
//   	game.sendText("U1:" + u1.getPosition().toString() + " NewPos:"
//   		+ newPos.toString());
   	if (u1.isIdle()) {
   	    u1.move(newPos);
   	}
   	AIstate = State.NORMAL;
       }
    
    public static void main(String[] args) {
	new TestBot1().run();
    }
}

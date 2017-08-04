package SCBots17.Kellerkind;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import SCAPI.UnitUtil.Vector;
import SCAPI.UnitUtil.Line;
import SCAPI.UnitUtil.UnitDebug;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class Bot extends DefaultBWListener {
    
    static class Line {
	private double slope;
	private double intercept;

	public double getSlope() {
	    return slope;
	}

	public double getIntercept() {
	    return intercept;
	}

	public static Line fromObservations(List<Position> observations) {
	    double a = 0;
	    double b = 0;

	    int sum_xy = 0;
	    int sum_xx = 0;
	    int sum_x = 0;
	    int sum_y = 0;
	    int n = observations.size();

	    for (Position pos : observations) {
		sum_xy += pos.getX() * pos.getY();
		sum_xx += pos.getX() * pos.getX();
		sum_x += pos.getX();
		sum_y += pos.getY();
	    }

	    a = (sum_xy - ((sum_x * sum_y) / n))
		    / (double) ((sum_xx - (sum_x ^ 2)) / (double) n);

	    b = (sum_y - (a * sum_x)) / (double) n;
	    return new Line(a, b);
	}

	Line(double slope, double intercept) {
	    this.slope = slope;
	    this.intercept = intercept;
	}

	double eval(double x) {
	    return slope * x + intercept;
	}
    }
    
    
    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private int setXParallel = -50, posX, hey = 0;
    private boolean checkPosition = true;
    private boolean b = true;
    private boolean exchange = true;
    
    
    

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
	Player enemy = game.enemy();
	if (unit.getPlayer() == self ) {
	List<Unit> eus = enemy.getUnits();
	for(Unit u : eus) {
	    if(u.getType() == UnitType.Terran_Firebat) {
		game.sendText("I have detected a firebat in the beginning: " + u.getType());
	    }else {
		game.sendText("This is no Firebat in the beginning! " + u.getType());
	    }
	}
	    game.sendText("setXParallel: " + String.valueOf(setXParallel));
	    setXParallel = setXParallel + 150;
	}
    }

    @Override
    public void onStart() {
	game = mirror.getGame();
	self = game.self();
	
	     setXParallel = -50;
	     posX = 0;
	     checkPosition = true;
	     b = true;
	     exchange = true;
    }
    
    private void drawEnemies(Game state, Player enemy) {
	for (Unit e : enemy.getUnits()) {
	    if (!e.isVisible())
		continue;
	    state.drawBoxMap(e.getX() - 2, e.getY() - 2, e.getX() + 2,
		    e.getY() + 2, bwapi.Color.Red);
	}
    }

    private void drawOrders(Game state, List<Unit> units) {
	int i = 0;
	for (Unit u : units) {
	    i++;
	    game.drawTextScreen(new Position(200, 40 + i * 15),
		    String.format("U: isIdle? %b", u.isIdle()));
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
    
    
    
    protected void attackEverythingInSight() {
	Player enemy = game.enemy();
	List<Unit> units = self.getUnits();
	List<Unit> eus = enemy.getUnits();

	HashSet<Unit> s = new HashSet<Unit>();
	s.addAll(enemy.getUnits());
	boolean finish = false;
	
	
	units.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit o1, Unit o2) {
		int n1 = game.getUnitsInRadius(o1.getPosition(),
			o1.getType().groundWeapon().maxRange()).size();
		int n2 = game.getUnitsInRadius(o1.getPosition(),
			o1.getType().groundWeapon().maxRange()).size();
		return n2 - n1;
	    }
	});

	for (Unit u : units) {
	    HashSet<Unit> newS = new HashSet<Unit>();
	    List<Unit> unitsInRange = game.getUnitsInRadius(u.getPosition(),
		    u.getType().groundWeapon().maxRange());

	    if (unitsInRange.isEmpty()) {
		continue;
	    }

	    for (Unit ru : unitsInRange) {
		if (s.contains(ru)) {
		    newS.add(ru);
		}
	    }

	    if (!newS.isEmpty())
		s = newS;
	}

	if (s.isEmpty())
	    return;

	Unit eu = (Unit) s.iterator().next();
	if (!eu.isVisible())
	    return;
		
	for (Unit myUnit : self.getUnits()) {
	    if (myUnit.getLastCommandFrame() >= game.getFrameCount()
		    || myUnit.isAttackFrame())
		continue;
	    if (myUnit.getLastCommand()
		    .getUnitCommandType() == UnitCommandType.Attack_Unit)
		continue;
	    game.sendText("Attack In Sight");
	    for(Unit ue : eus) {
		if(checkPosition) {
		     posX = myUnit.getX() - 20;
		     checkPosition = false;
		}
		
		if(myUnit.getX() <= posX) {
		   finish =  true;
		}
		
		game.drawTextScreen(10,  100, "pos: " + myUnit.getX());
		game.drawTextScreen(50,  100, "aim: " + posX);
		if(self.getUnits().size() != 1) {
		    if(ue.getType() == UnitType.Terran_Firebat && myUnit.getDistance(ue) <= ue.getType().groundWeapon().maxRange() + 150 && myUnit.getX() > posX && !finish) {
			game.sendText("moving " + String.valueOf(hey) + " times");
        			if(exchange) {
        			    myUnit.move(new Position(myUnit.getX() - 100, myUnit.getY() - 75));	
        			    exchange = false;
        			}else {
        			    myUnit.move(new Position(myUnit.getX() - 100, myUnit.getY() + 50));	
        			    exchange = true;
        			}
        		    }else {
        			myUnit.attack(eu);
        		    }
        		    
		    }
		else {
		    if(ue.getType() == UnitType.Terran_Firebat && myUnit.getDistance(ue) <= ue.getType().groundWeapon().maxRange() + 150 && myUnit.getX() > posX) {
			if(myUnit.getY() > ue.getY()) {
		    		myUnit.move(new Position(myUnit.getX() , myUnit.getY() + 50));	
			}else {
			    myUnit.move(new Position(myUnit.getX() , myUnit.getY() - 50));
			}
			
			
		    }else {
			myUnit.attack(eu);
		    }
		}
        }
	}
}
    
    private void attackMoveTo(Game state, List<Unit> units, Position to) {
	for (Unit u : units) {
	    if (u.isIdle()) {
		u.attack(new Position(u.getPosition().getX() + to.getX(),
			u.getPosition().getY() + to.getY()));
	    }
	}
    }

    @Override
    public void onUnitDiscover(Unit u) {
	
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
	setXParallel = -50;
    }
    
    private boolean spreadUnits(Game state, List<Unit> units, int distance) {
   	if (units.size() < 2)
   	    return true;

   	List<Position> wantPositions = new LinkedList<Position>();
   	boolean requiredActions = false;

   	for (int i = 0; i < units.size(); i++) {
   	    Unit u0 = units.get(i);
   	    Unit closest = u0;
   	    int minDistance = distance;
   	    for (int j = i + 1; j < units.size(); j++) {
   		Unit u1 = units.get(j);

   		int curDistance = u0.getDistance(u1);
   		if (curDistance >= distance)
   		    continue;

   		if (minDistance >= curDistance) {
   		    minDistance = curDistance;
   		    closest = u1;
   		}
   	    }

   	    Position wantPos = new Position(
   		    u0.getY() - (closest.getY() - u0.getY()),
   		    u0.getX() - (closest.getX() - u0.getX()));

   	    
   	    wantPositions.add(wantPos);

   	    if (minDistance < distance)
   		requiredActions = true;
   	    if (u0.isIdle())
   		u0.move(wantPos);
   	}
   	int offset = 0;
   	for (Position pos : wantPositions) {
   	    state.drawCircleMap(pos, 2, bwapi.Color.Cyan);
   	    state.drawTextScreen(10, 30 + offset, pos.toString());
   	    offset += 10;
   	}
   	return requiredActions;
       }
    
    private void alignUnits(Game state, List<Unit> units) {
	if (units.size() < 2)
	    return;

	units.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit o1, Unit o2) {
		int comp = o1.getX() - o2.getX();
		if (comp == 0)
		    comp = o1.getY() - o2.getY();
		return comp;
	    }
	});

	List<Position> positions = new LinkedList<Position>();
	for (Unit u : units) {
	    positions.add(u.getPosition());
	}

	Line current = Line.fromObservations(positions);
	Unit u0 = units.get(0);
	Unit u1 = units.get(units.size() - 1);

	state.drawLineMap(
		new Position(u0.getX(), (int) current.eval(u0.getX())),
		new Position(u1.getX(), (int) current.eval(u1.getX())),
		bwapi.Color.Orange);
    }
    
    
    
    @Override
    public void onFrame() {
	game.drawTextScreen(10, 10,
		"Playing as " + self.getName() + " - " + self.getRace());
	Player enemy = game.enemy();
	drawEnemies(game, enemy);
	drawOrders(game, self.getUnits());
	
	UnitDebug.drawUnitDebug(game, self, 60);

	if(self.getUnits().size() <= 2) {
        	if(b) {
        	    alignUnits(game, self.getUnits());
        	    if (!spreadUnits(game, self.getUnits(), 100)) {
        		b = false;
        	    }else {
        		attackMoveTo(game, self.getUnits(), new Position(0, 15));
        		attackEverythingInSight();
        	    }
        	    
        	}
	}
	
	
    } 

    public static void main(String[] args) {
	new Bot().run();
    }
}
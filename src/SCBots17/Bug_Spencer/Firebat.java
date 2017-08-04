package SCBots17.Bug_Spencer;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import SCAPI.UnitUtil.UnitDebug;
import SCAPI.UnitUtil.Vector;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class Firebat extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    int countFrame;

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
	System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
	countFrame = 0;
	game = mirror.getGame();
	self = game.self();

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
	    System.out.println();
	}

    }

    @Override
    public void onFrame() {
	UnitDebug.drawUnitCommands(game, 40);

	countFrame++;
	// game.setTextSize(10);
	game.drawTextScreen(10, 10,
		"Playing as " + self.getName() + " - " + self.getRace());

	StringBuilder units = new StringBuilder("My units:\n");

	// iterate through my units
	for (Unit myUnit : self.getUnits()) {
	    units.append(myUnit.getType()).append(" ")
		    .append(myUnit.getTilePosition()).append("\n");

	    // if there's enough minerals, train an SCV
	    if (myUnit.getType() == UnitType.Terran_Command_Center
		    && self.minerals() >= 50) {
		myUnit.train(UnitType.Terran_SCV);
	    }

	    // if it's a worker and it's idle, send it to the closest mineral
	    // patch
	    if (myUnit.getType().isWorker() && myUnit.isIdle()) {
		Unit closestMineral = null;

		// find the closest mineral
		for (Unit neutralUnit : game.neutral().getUnits()) {
		    if (neutralUnit.getType().isMineralField()) {
			if (closestMineral == null
				|| myUnit.getDistance(neutralUnit) < myUnit
					.getDistance(closestMineral)) {
			    closestMineral = neutralUnit;
			}
		    }
		}

		// if a mineral patch was found, send the worker to gather it
		if (closestMineral != null) {
		    myUnit.gather(closestMineral, false);
		}
	    }
	}
	// draw my units on screen
	game.drawTextScreen(10, 25, units.toString());
	move();
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
		    u0.getX() - (closest.getX() - u0.getX()),
		    u0.getY() - (closest.getY() - u0.getY()));
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

    public Unit getTarget() {
	Player enemy = game.enemy();

	List<Unit> eunits = enemy.getUnits();
	List<Unit> firebats = new LinkedList<Unit>();

	eunits.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit o1, Unit o2) {
		int compare = 0;

		int hp1 = o1.getInitialHitPoints();
		int hp2 = o2.getInitialHitPoints();

		compare = hp1 - hp2;

		if (compare == 0) {
		    int d1 = o1.getDistance(new Position(0, 0));
		    int d2 = o2.getDistance(new Position(0, 0));
		    compare = d1 - d2;
		}

		return compare;
	    }
	});

	for (Unit u : eunits) {
	    if (u.getType() == UnitType.Terran_Firebat)
		firebats.add(u);
	}

	if (firebats.size() > 0)
	    return firebats.iterator().next();

	if (eunits.size() > 0)
	    return eunits.iterator().next();
	return null;
    }

    public Unit minHealth() {
	Player enemy = game.enemy();
	List<Unit> eus = enemy.getUnits();
	Unit eu = eus.get(0);
	for (Unit newTarget : enemy.getUnits()) {
	    if (eu.getHitPoints() > newTarget.getHitPoints()) {
		eu = newTarget;
	    }
	}
	return eu;
    }

    private void moveDown(Player enemy) {
	if (countFrame < 10) {
	    List<Unit> myUnits = self.getUnits();
	    // spreadUnits(game,myUnits,100);
	    myUnits.get(0).move(new Position(myUnits.get(0).getX() - 30,
		    myUnits.get(0).getY()));
	    myUnits.get(1).move(new Position(myUnits.get(1).getX() + 30,
		    myUnits.get(1).getY()));
	    // if (countFrame == 29)
	    // {
	    // for(Unit myUnit : self.getUnits()) {
	    // myUnit.holdPosition();
	    // }

	} else if (countFrame >= 10 && game.enemy().visibleUnitCount() == 0) {
	    for (Unit myUnit : self.getUnits()) {
		if (enemy.visibleUnitCount() == 0) {
		    myUnit.move(
			    new Position(myUnit.getX(), myUnit.getY() + 20));
		} else {
		    myUnit.holdPosition();
		}

		// if(myUnit.getBottom() < myUnit.getTop()) {
		// myUnit.move(new Position(myUnit.getX(), myUnit.getY() - 20));
		// }else {
		// myUnit.move(new Position(myUnit.getX(), myUnit.getY() + 20));
		//
		// }
	    }
	}
    }
    
    public boolean hasTarget(UnitCommand cmd) {
	return cmd.getUnitCommandType() == UnitCommandType.Attack_Unit;
    }

    public void move() {
	WeaponType wTy = UnitType.Terran_Firebat.groundWeapon();
	Player enemy = game.enemy();
	UnitDebug.drawUnitHelpAll(game, true);
	List<Unit> eunits = enemy.getUnits();
	List<Unit> firebats = new LinkedList<Unit>();
	for (Unit u : eunits) {
	    if (u.getType() == UnitType.Terran_Firebat)
		firebats.add(u);
	}

	Unit target = getTarget();
	if (target == null)
	    moveDown(enemy);
	else
	    for (Unit myUnit : self.getUnits()) {
		Vector eVector = new Vector(target.getPosition(),
			myUnit.getPosition());
		Vector myVector = new Vector(myUnit.getPosition());
		eVector.draw(game, bwapi.Color.Blue, "eVector");
		myVector.draw(game, bwapi.Color.Green, "myVector");
		game.drawCircleMap(myUnit.getPosition(), wTy.maxRange() + 34,
			Color.Cyan);
		game.drawCircleMap(myUnit.getPosition(),
			myUnit.getType().groundWeapon().maxRange() + 34,
			Color.Purple);
		game.drawCircleMap(target.getPosition(),
			target.getType().groundWeapon().maxRange(), Color.Cyan);
		System.out.println(target.getType());

		UnitCommand command = myUnit.getLastCommand();

		int distanceToTarget = target.getDistance(myUnit);
		int maxDistance = wTy.maxRange() + 34;

		boolean tgtIsFirebat = target
			.getType() == UnitType.Terran_Firebat;
		boolean cmdIsAttackUnit = command
			.getUnitCommandType() == UnitCommandType.Attack_Unit;


		Unit cmdTarget = hasTarget(command) ? command.getTarget() : null;
		
		boolean cmdTgtIsFirebat = (cmdTarget != null)
			&& cmdTarget.getType() == UnitType.Terran_Firebat;
		boolean haveFirebats = !firebats.isEmpty();
		boolean cmdTgtAlmostDead = (cmdTarget != null)
			&& cmdTarget.getHitPoints() <= 5;

		if (distanceToTarget < maxDistance) {
		    Vector moveVector = myVector.add(eVector);
		    moveVector.draw(game, bwapi.Color.Red, "moveVector");
		    if(myUnit.hasPath(moveVector.toPosition())){
			    myUnit.move(moveVector.toPosition());
		    }else if(!cmdIsAttackUnit){
			eVector.draw(game, bwapi.Color.Blue, "eVector");
			myUnit.attack(target);
		    }
		}

		else if ((!cmdIsAttackUnit && tgtIsFirebat) || (cmdIsAttackUnit
			&& !cmdTgtIsFirebat && haveFirebats)) {
		    myUnit.attack(target);
		    game.sendText("attacke!");
		} else if ((!cmdIsAttackUnit || cmdTgtAlmostDead && cmdTgtIsFirebat)
			&& !haveFirebats) {
		    myUnit.attack(target);
		    game.sendText("nrmal");
		} else {
		    game.sendText("here "
			    + myUnit.getLastCommand().getTarget().getType()
			    + "life: " + command.getTarget().getHitPoints());
		    // game.drawBoxMap(myUnit.getPosition(),
		    // target.getPosition(), Color.Yellow);

		}
	    }
    }

    public static void main(String[] args) {
	new Firebat().run();
    }

}
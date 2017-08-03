import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import SCAPI.UnitUtil.Line;
import SCAPI.UnitUtil.Vector;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class SommerinoCamperino_New extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private BaseLocation enemyLoc;
    private BaseLocation playerLoc;

    enum State {
	INIT, NORMAL
    }

    private State state = State.INIT;
    private HashMap<Unit, State> unitStates = new HashMap<Unit, State>();

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }

    @Override
    public void onStart() {
	state = State.INIT;
	game = mirror.getGame();
	self = game.self();
	BWTA.readMap();
	BWTA.analyze();

	List<BaseLocation> locs = BWTA.getStartLocations();
	playerLoc = BWTA.getStartLocation(self);
	for (BaseLocation loc : BWTA.getStartLocations()) {
	    if (loc != playerLoc) {
		enemyLoc = loc;
	    }
	}
    }

    protected void attackEverythingInSight() {
	Player enemy = game.enemy();
	List<Unit> units = self.getUnits();

	HashSet<Unit> s = new HashSet<Unit>();
	s.addAll(enemy.getUnits());

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
	    myUnit.attack(eu);
	}
    }

    private boolean spreadUnits(Game state, List<Unit> units, int distance) {
	if (units.size() < 2)
	    return true;

	List<Position> wantPositions = new LinkedList<Position>();
	boolean requiredActions = false;

	for (int i = 0; i < units.size() - 1; i++) {
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

	    Vector a = new Vector(u0.getPosition());
	    Vector b = new Vector(closest.getPosition());
	    Vector ab = new Vector(b, a);

	    int radius = Math.max(u0.getType().height(), u0.getType().width());
	    ab = new Vector(a.toPosition(), a.add(ab).toPosition());
	    ab = ab.scale(2);

	    if (minDistance < distance)
		requiredActions = true;

	    int tries = 0;
	    while (tries < 5) {
		List<Unit> uir = state.getUnitsInRadius(ab.toPosition(),
			radius);
		if (uir.size() == 0) {
		    if (u0.isIdle()) {
			wantPositions.add(ab.toPosition());
			u0.move(ab.toPosition());
		    }
		    break;
		}
		ab = ab.rotate(64);
		tries++;
	    }
	}
	for (Position pos : wantPositions) {
	    state.drawCircleMap(pos, 2, bwapi.Color.White);
	}
	return requiredActions;
    }

    private Line unitAlignment = null;
    private Line wantAlignment = null;

    private boolean unitsAligned(Line ua, Line wa) {
	double slopeDelta = Math.abs(wa.getSlope() - ua.getSlope());
	double interceptDelta = Math.abs(wa.getIntercept() - ua.getIntercept());
	return slopeDelta < 0.6 && interceptDelta < 40;
    }

    private boolean refreshAlignment(Game state, List<Unit> units,
	    Position pos) {
	if (units.size() < 2)
	    return true;

	List<Position> positions = new LinkedList<Position>();
	for (Unit u : units) {
	    positions.add(u.getPosition());
	}

	unitAlignment = Line.fromObservations(positions);

	Unit u0 = units.get(0);
	Unit u1 = units.get(units.size() - 1);

	Position p0 = unitAlignment.at(u0.getX());
	Position p1 = unitAlignment.at(u1.getX());

	Vector align = new Vector(p0, p1);
	Vector mid = align.scale(0.5);

	Vector dir;
	if (pos != null)
	    dir = new Vector(mid.toPosition(), pos);
	else
	    dir = new Vector(mid.toPosition(), mid.add(0, 64).toPosition());

	Vector v1 = dir.rotate(90);
	Vector v2 = dir.rotate(270);

	wantAlignment = Line.fromPoints(v1, v2);
	wantAlignment = Line.fromPoints(v1, v2);
	wantAlignment.draw(state, u0.getX(), u1.getX(), bwapi.Color.Green);
	unitAlignment.draw(state, u0.getX(), u1.getX(), bwapi.Color.Yellow);

	double slopeDelta = Math
		.abs(wantAlignment.getSlope() - unitAlignment.getSlope());
	double interceptDelta = Math.abs(
		wantAlignment.getIntercept() - unitAlignment.getIntercept());
	state.drawTextScreen(10, 50,
		String.format("Slope-Delta: %f", slopeDelta));
	return slopeDelta < 0.5 && interceptDelta < 6;
    }

    private void alignUnits(Game state, List<Unit> units) {
	if (unitAlignment == null || wantAlignment == null)
	    return;
	for (Unit u : units) {
	    if (!u.isIdle())
		continue;

	    Position u_pos = u.getPosition();

	    double a_g = wantAlignment.getSlope();
	    double b_g = wantAlignment.getIntercept();
	    double a_f = (-1) * a_g;
	    double b_f = u_pos.getY() - a_f * u_pos.getX();

	    if (a_f == a_g)
		continue;
	    double x = (b_f - b_g) / (a_g - a_f);

	    Position to = wantAlignment.at(x);
	    Vector v = new Vector(u_pos, to);
	    v.draw(state, Color.Red, "align");
	    u.move(v.toPosition());
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

    private void drawEnemies(Game state, Player enemy) {
	for (Unit e : enemy.getUnits()) {
	    if (!e.isVisible())
		continue;
	    state.drawBoxMap(e.getX() - 2, e.getY() - 2, e.getX() + 2,
		    e.getY() + 2, bwapi.Color.Red);
	}

	Vector v = new Vector(playerLoc.getPosition(), enemyLoc.getPosition());
	v.draw(game, bwapi.Color.Orange, "enemy");
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

    private void alignUnitsTo(Game state, Line to, List<Unit> units,
	    int distance) {
	int i = 0;
	Position p = null;
	for (Unit u : units) {
	    if (!u.isIdle())
		continue;
	    if (p == null)
		p = u.getPosition();
	    UnitType ty = u.getType();
	    int radius = Math.max(ty.height(), ty.width());
	    Position np = to.at(p.getX() + i * (distance + radius));
	    
	    u.move(np);
	    i++;
	}
    }

    private List<Unit> getInWeaponRange(Unit u, List<Unit> units) {
	List<Unit> us = new LinkedList<Unit>();
	
	for (Unit e : units) {
	    if (e.isInWeaponRange(u)) {
		us.add(e);
	    }
	}
	return us;
    }
    
    private List<Position> asPositions(List<Unit> units) {
	List<Position> pos = new LinkedList<Position>();
	for (Unit u : units) {
	    pos.add(u.getPosition());
	}
	return pos;
    }
    
    private Position avgPoint(List<Position> positions) {
	int sum_x = 0;
	int sum_y = 0;
	
	for (Position p : positions) {
	    sum_x += p.getX();
	    sum_y += p.getY();
	}
	return new Position(sum_x / positions.size(), sum_y / positions.size());
    }
    
    private boolean canKite(Unit u, List<Unit> uir) {
	if (uir.isEmpty())
	    return false;
	
	UnitType ty = u.getType();
	double uspd = ty.topSpeed();
	
	boolean canKite = false;
	for (Unit eu : uir) {
	    UnitType ety = eu.getType();
	    double euspd = eu.getType().topSpeed();
	    double urng = (eu.isFlying()) ? ty.airWeapon().maxRange() :
					    ty.groundWeapon().maxRange();
	    double eurng = (u.isFlying()) ? ety.airWeapon().maxRange() :
					    ety.groundWeapon().maxRange();
	    
	    canKite = canKite || (uspd >= euspd) && (urng > eurng);	    
	}
	return canKite;
    }

    private void maybeKite(Game state, List<Unit> units, List<Unit> enemies) {
	for (final Unit u : units) {
	    enemies.sort(new Comparator<Unit>() {
		@Override
		public int compare(Unit o1, Unit o2) {
		    int d1 = u.getDistance(o1.getPosition());
		    int d2 = u.getDistance(o1.getPosition());
		    return d1 - d2;
		}
	    });

	    List<Unit> uir = getInWeaponRange(u, enemies);
	    Color kiteCandidate = Color.Blue;
	    if (canKite(u, uir)) {
		kiteCandidate = Color.Orange;
		Position dangerZone = avgPoint(asPositions(uir));
		game.drawCircleMap(dangerZone, 15, Color.Teal);
	    }
	    for (Unit kc : uir) {
		game.drawCircleMap(kc.getPosition(), 10, kiteCandidate);
	    }
	}
    }

    @Override
    public void onFrame() {
	try {
	    game.drawTextScreen(10, 10,
		    "Playing as " + self.getName() + " - " + self.getRace());
	    game.drawTextScreen(400, 10, "State: " + state);

	    Player enemy = game.enemy();
	    List<Unit> units = self.getUnits();
	    Comparator<Unit> cp = new Comparator<Unit>() {
		@Override
		public int compare(Unit o1, Unit o2) {
		    Position origin = new Position(0, 0);
		    int d1 = o1.getDistance(origin);
		    int d2 = o2.getDistance(origin);
		    return d1 - d2;
		}
	    };

	    List<Unit> enemy_units = enemy.getUnits();

	    drawEnemies(game, enemy);
	    drawOrders(game, units);

	    switch (state) {
	    case INIT:
		refreshAlignment(game, units, enemyLoc.getPosition());
		alignUnitsTo(game, wantAlignment, units, 10);
		if (unitsAligned(unitAlignment, wantAlignment)) {
		    state = State.NORMAL;
		}
		break;
	    case NORMAL:
		maybeKite(game, units, enemy.getUnits());
		attackEverythingInSight();
		attackMoveTo(game, units, new Position(0, 64));
		break;
	    }
	} catch (

	Throwable t) {
	    game.sendText("Exception caught");
	    t.printStackTrace();
	}
    }

    public static void main(String[] args) {
	new SommerinoCamperino_New().run();
    }
}
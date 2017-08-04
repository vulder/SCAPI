package SCBots17.Bot;

import java.util.Comparator;

import java.util.LinkedList;
import java.util.List;

import SCAPI.UnitUtil.Line;
import SCAPI.UnitUtil.Vector;
//import SommerinoCamperino_New.State;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

//import SCAPI.UnitUtil.UnitControl;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import java.util.List;
import java.util.List;

public class SommerinoCamperino extends DefaultBWListener {
    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;

    enum State {
	ATTACK, NORMAL, HOLDPOSITION, DISCOVEREDENEMY, ALIGN, RUNAWAY

    }

    private State state = State.NORMAL;

    public void run() {
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
	if (unit.getPlayer() == self) {
	}

    }

    @Override
    public void onStart() {
	game = mirror.getGame();
	self = game.self();
	BWTA.readMap();
	BWTA.analyze();
    }

    protected boolean attackEverythingInSight() {
	Player enemy = game.enemy();
	List<Unit> eus = enemy.getUnits();

	if (eus.isEmpty())
	    return false;
	eus.sort(new Comparator<Unit>() {

	    @Override
	    public int compare(Unit o1, Unit o2) {
		int x = 0;
		int y = 0;
		int a = 0;
		for (Unit myUnits : self.getUnits()) {
		    x = x + myUnits.getX();
		    y = y + myUnits.getY();
		    a++;
		}
		x = x / a;
		y = y / a;

		game.drawCircleMap(x, y, 10, Color.Blue);

		UnitType ty1 = o1.getType();
		UnitType ty2 = o2.getType();
		int compare = 0;
		int r1 = ty1.groundWeapon().maxRange();
		int r2 = ty2.groundWeapon().maxRange();
		compare = r1 - r2;
		if (compare == 0) {
		    int d1 = o1.getDistance(new Position(x, y));
		    int d2 = o2.getDistance(new Position(x, y));
		    compare = d1 - d2;
		}
		return compare;
	    }

	});
	Unit eu = eus.get(0);

	int a = eus.get(0).getX();
	int b = eus.get(0).getY();
	game.drawCircleMap(a, b, 15, Color.Cyan);
	for (Unit myUnit : self.getUnits()) {
	    if (myUnit.getLastCommandFrame() >= game.getFrameCount()
		    || myUnit.isAttackFrame())
		continue;
	    if (myUnit.getLastCommand()
		    .getUnitCommandType() == UnitCommandType.Attack_Unit)
		continue;

	    myUnit.attack(eu);
	}
	return true;
    }

    @Override
    public void onUnitDiscover(Unit discoveredUnit) {

	if (discoveredUnit.getPlayer() == self)
	    return;

	if (!discoveredUnit.isVisible(self))
	    return;
	// game.sendText("Got you");
	// state = State.DISCOVEREDENEMY;
    }

    @Override
    public void onUnitDestroy(Unit u) {
	game.sendText("" + game.enemy().deadUnitCount());
	game.sendText("" + game.enemy().getKillScore());
	if (u.getPlayer() == self) {

	} else {
	    attackEverythingInSight();
	}
    }

    @Override
    public void onFrame() {
	// game.sendText("" + state.name());
	// System.out.println("" + state.name());
	// List<Unit> eus = game.enemy().getUnits();

	List<Unit> units = self.getUnits();
	drawOrders(game, self.getUnits());
	Vector v1 = new Vector(new Position(982, 300));
	Vector v2 = new Vector(new Position(800, 300));
	Line l = Line.fromPoints(v1, v2);
	l.draw(game, 800, 928, Color.Blue);

	if (state == State.ALIGN) {
	    alignUnits(game, units, l);
	}

	for (Unit myUnit : self.getUnits()) {
	    // System.out.println(myUnit.getOrder());
	    WeaponType wty = myUnit.getType().groundWeapon();
	    if (game.enemy().visibleUnitCount() == 0) {

		state = State.NORMAL;
		Vector v = new Vector(units.get(0).getPosition(),
			new Position(myUnit.getX(), myUnit.getY() + 16));
		v.draw(game, Color.Grey, "Vector");
		moveCommand(v);

		return;
	    }

	    float mR = wty.maxRange();
	    for (Unit enemy : game.enemy().getUnits()) {

		double enemyDistance = myUnit.getDistance(enemy);
		// System.out.println(enemyDistance);
		if (enemyDistance <= mR) {
		    state = State.ATTACK;
		}
		if (state == State.ATTACK) {
		    attackEverythingInSight();
		}

		// WeaponType wty_en = enemy.getType().groundWeapon();
		// float mR_en = wty_en.maxRange();

		if (enemyDistance < mR + 700 && state != State.ATTACK&&state!=State.HOLDPOSITION) {
		    state = State.ALIGN;
		    myUnit.stop();
		    game.sendText(state.name());
		}
		if (enemyDistance < mR + 400 && state != State.ATTACK) {
		    state = State.HOLDPOSITION;
		}
		if (state == State.HOLDPOSITION) {
		    alignUnits(game, units, l);
		    myUnit.holdPosition();
		   
		    System.out.println(game.countdownTimer());
		}
		if (state != State.ATTACK && state != State.HOLDPOSITION) {
		    state = State.DISCOVEREDENEMY;
		}

		if (state == State.DISCOVEREDENEMY) {
		    Vector v = new Vector(units.get(0).getPosition(),
			    enemy.getPosition());
		    moveCommand(v);
		}

		// if (state == State.NORMAL) {
		// Vector v = new Vector(units.get(0).getPosition(),
		// new Position(myUnit.getX(), myUnit.getY() + 16));
		// // moveCommand(v);
		// }
		// }

	    }

	}
    }

    private boolean alignUnits(Game state, List<Unit> units, Line to) {
	if (to == null)
	    return true;

	boolean aligned = true;
	for (Unit u : units) {
	    if (!u.isIdle() || u.isHoldingPosition())
		continue;
	    // if (u.isHoldingPosition()) {
	    //
	    // }

	    Position u_pos = u.getPosition();

	    Position v1 = to.at(0);
	    Position v2 = to.at(200);

	    Vector v = new Vector(v1, v2);

	    v = v.rotate(95);
	    Line l = Line.fromPoints(new Vector(v1), v);

	    double a_g = to.getSlope();
	    double b_g = to.getIntercept();
	    double a_f = l.getSlope();
	    double b_f = u_pos.getY() - a_f * u_pos.getX();

	    if (a_f == a_g)
		continue;
	    double x = (b_f - b_g) / (a_g - a_f);

	    Position to_pos = to.at(x);
	    if (to_pos != u_pos) {
		aligned = false;
	    }
	    Vector vn = new Vector(u_pos, to_pos);
	    vn.draw(state, Color.Red, "align");

	    List<Unit> uir = state.getUnitsInRadius(vn.toPosition(), 6);
	    // System.out.println("" + u.getOrder() + "" +
	    // u.getSecondaryOrder());
	    if (uir.size() == 0)
		u.move(vn.toPosition());
	    else
		u.move(vn.rotate(90).toPosition());

	}
	// game.sendText("ALIGNED");
	return aligned;

    }

    private void moveCommand(Vector v) {
	for (Unit u : self.getUnits()) {

	    Vector uv = new Vector(u.getPosition());
	    Vector mv = uv.add(v);
	    Position p = mv.toPosition();

	    // mv.draw(game, Color.Cyan, "Vector");
	    u.move(p);
	}
    }

    private void drawOrders(Game state, List<Unit> units) {
	for (Unit u : units) {
	    if (u.isIdle())
		continue;

	    // game.sendText("" + u.getOrderTimer());
	    Position pos = u.getOrderTargetPosition();
	    bwapi.Color c = bwapi.Color.Green;

	    UnitCommandType ty = u.getLastCommand().getUnitCommandType();
	    if (ty == UnitCommandType.Attack_Move
		    || ty == UnitCommandType.Attack_Unit)
		c = bwapi.Color.Red;
	    game.drawLineMap(u.getPosition(), pos, c);
	}
    }

    public static void main(String[] args) {
	new SommerinoCamperino().run();
    }
}
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import SCAPI.UnitUtil.Line;
import SCAPI.UnitUtil.UnitControl;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class SommerinoCamperino_New extends DefaultBWListener {


    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;

    enum State {
        INIT, NORMAL
    }

    private State state = State.INIT;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
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
        if (!eus.isEmpty()) {
            Unit eu = eus.get(0);
            for (Unit myUnit : self.getUnits()) {
                if (myUnit.getLastCommandFrame() >= game.getFrameCount() || myUnit.isAttackFrame())
                    continue;
                if (myUnit.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit)
                    continue;

                myUnit.attack(eu);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onUnitDiscover(Unit u) {
        if (!u.isVisible())
            return;
    }

    @Override
    public void onUnitDestroy(Unit u) {
        if (u.getPlayer() == self) {
            game.sendText("OH SHIT!");
        } else {
            attackEverythingInSight();
        }
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

            Position wantPos = new Position(u0.getX() - (closest.getX() - u0.getX()),
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

    private void alignUnits(Game state, List<Unit> units, BaseLocation pos) {
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

        state.drawLineMap(new Position(u0.getX(), (int) current.eval(u0.getX())),
                new Position(u1.getX(), (int) current.eval(u1.getX())), bwapi.Color.Orange);
    }

    private void attackMoveTo(Game state, List<Unit> units, BaseLocation to) {
        for (Unit u : units) {
            if (!u.isIdle())
                continue;

            u.attack(to.getPosition());
        }
    }

    private void drawEnemies(Game state, Player enemy) {
        for (Unit e : enemy.getUnits()) {
            if (!e.isVisible())
                continue;
            state.drawBoxMap(e.getX() - 2, e.getY() - 2, e.getX() + 2, e.getY() + 2, bwapi.Color.Red);
        }
    }

    private void drawOrders(Game state, List<Unit> units) {
        for (Unit u : units) {
            if (u.isIdle())
                continue;

            Position pos = u.getOrderTargetPosition();
            bwapi.Color c = bwapi.Color.Green;

            UnitCommandType ty = u.getLastCommand().getUnitCommandType();
            if (ty == UnitCommandType.Attack_Move || ty == UnitCommandType.Attack_Unit)
                c = bwapi.Color.Red;

            game.drawLineMap(u.getPosition(), pos, c);
        }

    }

    @Override
    public void onFrame() {
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
        game.drawTextScreen(400, 10, "State: " + state);

        Player enemy = game.enemy();
        drawEnemies(game, enemy);
        drawOrders(game, self.getUnits());

        switch (state) {
        case INIT:
            alignUnits(game, self.getUnits(), BWTA.getStartLocation(enemy));
            if (!spreadUnits(game, self.getUnits(), 6))
                state = State.NORMAL;
            break;
        case NORMAL:
            if (!attackEverythingInSight()) {
                attackMoveTo(game, self.getUnits(), BWTA.getStartLocation(enemy));
            }
            break;
        }
    }

    public static void main(String[] args) {
        new SommerinoCamperino_New().run();
    }
}
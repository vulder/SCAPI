package SCAPI.bots;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import SCAPI.ViewControl;
import SCAPI.UnitUtil.UnitControl;
import SCAPI.UnitUtil.UnitDebug;
import SCAPI.UnitUtil.Vector;
import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

public class Palazo extends DefaultBWListener {

	private Mirror mirror = new Mirror();
	private Game game;
	private Player self;
	private HashMap<Unit, UnitStrategy> unitStrategies;
	private Iterator<TilePosition> startLocs;
	private TilePosition scoutTarget;
	private List<Squad> squads;

	public Palazo() {
		unitStrategies = new HashMap<Unit, UnitStrategy>();
		squads = new LinkedList<Squad>();
	}

	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	public void updateScoutTarget() {
		if (!startLocs.hasNext())
			startLocs = game.getStartLocations().iterator();

		scoutTarget = null;
		while (startLocs.hasNext()) {
			TilePosition loc = startLocs.next();
			if (loc != self.getStartLocation()) {
				scoutTarget = loc;
				break;
			}
		}

		if (scoutTarget == null)
			updateScoutTarget();
	}

	@Override
	public void onStart() {
		game = mirror.getGame();
		self = game.self();
		BWTA.readMap();
		BWTA.analyze();

		startLocs = game.getStartLocations().iterator();
		updateScoutTarget();
	}

	@Override
	public void onUnitCreate(Unit unit) {
		if (unit.getPlayer() != self)
			return;

		UnitType ty = unit.getType();
		if (ty == UnitType.Terran_Marine) {
			unitStrategies.put(unit, new RangedStrategy(game, unit));
		} else if (ty == UnitType.Terran_Firebat) {
			unitStrategies.put(unit, new MeleeStrategy(game, unit));
		}
	}

	private void scoutAggressive(Position scoutPos, Squad s, List<Squad> squads) {
		if (s == null)
			return;

		if (!s.isAttacking() && !s.isMoving())
			s.attack(scoutPos, squads);
	}

	private void scout(List<Unit> units) {
		assert scoutTarget != null;

		Position tpos = scoutTarget.toPosition();
		Position npos = new Position(tpos.getX() - 16, tpos.getY() - 16);

		boolean squadsAtScoutTarget = true;

		Squad lastSquad = null;
		for (Squad s : squads) {
			boolean isClose = s.isCloseTo(npos);
			if (!isClose) {
				if (lastSquad != null) {
					Vector nposv = new Vector(npos);
					Vector delta = lastSquad.distanceTo(s);
					npos = nposv.add(delta).toPosition();
				}

				lastSquad = s;
				squadsAtScoutTarget = false;
				scoutAggressive(npos, s, squads);
			}
		}

		if (squadsAtScoutTarget) {
			updateScoutTarget();
		}
	}

	private void updateSquads(List<Unit> units) {
		List<Unit> marine_list = new LinkedList<Unit>();
		List<Unit> firebat_list = new LinkedList<Unit>();
		for (Unit u : units) {
			if (u.getType() == UnitType.Terran_Marine)
				marine_list.add(u);
			if (u.getType() == UnitType.Terran_Firebat)
				firebat_list.add(u);
		}

		squads.clear();
		if (!marine_list.isEmpty())
			squads.add(new Squad(game, marine_list, Color.Orange));
		if (!firebat_list.isEmpty())
			squads.add(new Squad(game, firebat_list, Color.Blue));

	}

	@Override
	public void onFrame() {
		try {
			ViewControl.screenAutoFollow(game);

			List<Unit> units = self.getUnits();
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
			List<Unit> enemies = new LinkedList<Unit>();
			for (Player enemy : game.enemies()) {
				UnitDebug.drawEnemies(game, enemy);
				enemies.addAll(enemy.getUnits());
			}
			UnitDebug.drawOrders(game, units);
			updateSquads(units);

			boolean needScouting = enemies.isEmpty();
			boolean inCombat = !needScouting;
			if (needScouting)
				scout(units);

			if (inCombat) {
				HashMap<Unit, Set<Unit>> attackPlan = new HashMap<Unit, Set<Unit>>();
				for (UnitStrategy strat : unitStrategies.values())
					strat.update(enemies, attackPlan);
			}

			for (Squad s : squads) {
				s.update();
				s.draw();
			}
			game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
		} catch (Throwable t) {
			game.sendText("Exception caught");
			t.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Palazo().run();
	}
}
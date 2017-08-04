package SCAPI.bots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import SCAPI.UnitUtil.UnitControl;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;

public class MeleeStrategy extends UnitStrategy {
	private Unit unit;
		
	public MeleeStrategy(Game state, Unit u) {
		super(state);
		this.unit = u;
	}
	
	@Override
	public void update(List<Unit> enemies, HashMap<Unit, Set<Unit>> attackPlan) {
		assert !enemies.isEmpty();
		Unit closest;
		List<Unit> candidates = new ArrayList<Unit>(enemies);
		Set<Unit> unitPlan;
		int damage;
		do {
			closest = UnitControl.closestTo(unit, candidates);
			unitPlan = attackPlan.getOrDefault(closest, new HashSet<Unit>());
			damage = totalDamage(unitPlan);
			candidates.remove(closest);
		} while (damage >= closest.getHitPoints() && !candidates.isEmpty());
		smartAttack(unit, closest);
	}
}

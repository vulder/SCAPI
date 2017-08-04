package SCAPI.bots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import SCAPI.UnitUtil.UnitControl;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;

public class RangedStrategy extends UnitStrategy {
	private Unit unit;

	public RangedStrategy(Game state, Unit u) {
		super(state);
		this.state = state;
		this.unit = u;
	}

	private Unit optimalTarget(List<Unit> enemies, HashMap<Unit, Set<Unit>> attackPlan) {
		UnitType ty = unit.getType();
		int range = ty.groundWeapon().maxRange() + 8;

		List<Unit> uir = state.getUnitsInRadius(unit.getPosition(), range);
		uir.removeIf(new Predicate<Unit>() {
			@Override
			public boolean test(Unit t) {
				return t.getType().isCritter();
			}
		});
		uir.removeIf(new Predicate<Unit>() {
			@Override
			public boolean test(Unit t) {
				return t.getPlayer() == state.self();
			}
		});
		uir.sort(new Comparator<Unit>() {
			@Override
			public int compare(Unit o1, Unit o2) {
				return o1.getHitPoints() - o2.getHitPoints();
			}
		});

		Unit bestTarget;
		Set<Unit> unitPlan;
		if (!uir.isEmpty()) {
			Iterator<Unit> uirIt = uir.iterator();
			int damage;
			do {
				bestTarget = uirIt.next();
				unitPlan = attackPlan.getOrDefault(bestTarget, new HashSet<Unit>());
				damage = totalDamage(unitPlan);
			} while (damage >= bestTarget.getHitPoints() && uirIt.hasNext());
		} else {
			List<Unit> candidates = new ArrayList<Unit>(enemies);
			int damage;
			do {
				bestTarget = UnitControl.closestTo(unit, candidates);
				unitPlan = attackPlan.getOrDefault(bestTarget, new HashSet<Unit>());
				damage = totalDamage(unitPlan);
				candidates.remove(bestTarget);
			} while (damage >= bestTarget.getHitPoints() && !candidates.isEmpty());
		}

		unitPlan = attackPlan.getOrDefault(bestTarget, new HashSet<Unit>());
		unitPlan.add(bestTarget);
		attackPlan.put(bestTarget, unitPlan);

		return bestTarget;
	}

	@Override
	public void update(List<Unit> enemies, HashMap<Unit, Set<Unit>> attackPlan) {
		assert !enemies.isEmpty();

		Unit closest = optimalTarget(enemies, attackPlan);
		smartKite(unit, closest);
	}
}

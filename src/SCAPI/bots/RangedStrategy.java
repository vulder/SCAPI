package SCAPI.bots;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import SCAPI.UnitUtil.UnitControl;
import SCAPI.UnitUtil.Vector;
import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;

public class RangedStrategy extends UnitStrategy {
	private Unit unit;

	public RangedStrategy(Game state, Unit u) {
		super(state);
		this.state = state;
		this.unit = u;
	}

	private Unit optimalTarget(List<Unit> enemies) {
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

	    if (!uir.isEmpty()) {
		return uir.iterator().next();
	    } else {
		return UnitControl.closestTo(unit, enemies);		
	    }
	}

	@Override
	public void update(List<Unit> enemies) {
		assert !enemies.isEmpty();

		Unit closest = optimalTarget(enemies);
		smartKite(unit, closest);
	}
}

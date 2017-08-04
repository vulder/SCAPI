package SCAPI.bots;

import java.util.List;

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
	
	@Override
	public void update(List<Unit> enemies) {
		assert !enemies.isEmpty();
				
		Unit closest = UnitControl.closestTo(unit, enemies);
		smartKite(unit, closest);
	}
}

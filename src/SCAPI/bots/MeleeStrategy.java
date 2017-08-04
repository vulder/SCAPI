package SCAPI.bots;

import java.util.List;

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
	public void update(List<Unit> enemies) {
		assert !enemies.isEmpty();
		Unit closest = UnitControl.closestTo(unit, enemies);
		smartAttack(unit, closest);
	}
}

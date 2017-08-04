package SCAPI.bots;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import SCAPI.UnitUtil.Vector;
import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;

public class UnitStrategy {
	protected Game state;

	public UnitStrategy(Game state) {
		this.state = state;
	}

	protected void smartAttack(Unit src, Unit tgt) {
		if (src.getLastCommandFrame() > state.getFrameCount() || src.isAttackFrame())
			return;

		boolean weaponRdy = src.getGroundWeaponCooldown() == 0;
		if (!weaponRdy)
			return;

		if (src.isStartingAttack())
			return;

		UnitCommand curCmd = src.getLastCommand();
		UnitCommandType ty = curCmd.getUnitCommandType();
		boolean attacksSameTarget = ty == UnitCommandType.Attack_Unit && curCmd.getTarget().getID() == tgt.getID();
		if (attacksSameTarget)
			return;

		state.drawTextMap(src.getPosition(), "PENG");
		src.attack(tgt);

	}

	protected void smartMove(Unit src, Position tgt) {
		if (!tgt.isValid())
			return;

		if (src.getLastCommandFrame() >= state.getFrameCount() || src.isAttackFrame())
			return;

		UnitCommand curCmd = src.getLastCommand();
		UnitCommandType ty = curCmd.getUnitCommandType();

		if (ty == UnitCommandType.Move && curCmd.getTargetPosition() == tgt && src.isMoving())
			return;

		src.move(tgt);
		state.drawTextMap(src.getPosition(), "CYA");
	}
	
	public int totalDamage(Iterable<Unit> units) {
		int sum = 0;
		for (Unit u : units) {
			sum += u.getType().groundWeapon().damageAmount();
		}
		return sum;
	}
	
	protected void smartKite(Unit src, Unit tgt) {
		UnitType ty = src.getType();
		UnitType ety = tgt.getType();
		int range = ty.groundWeapon().maxRange();
		int e_range = ety.groundWeapon().maxRange();

		if (range <= e_range) {
			smartAttack(src, tgt);
			return;
		}

		boolean canKite = true;
		int distance = src.getDistance(tgt);
		double speed = ty.topSpeed();
		double tte = Math.max(0, (distance - range) / speed);

		if (tte >= tgt.getGroundWeaponCooldown())
			canKite = false;

		if (canKite) {
			Vector fromEnemy = new Vector(tgt.getPosition(), src.getPosition());
			Vector ourPosition = new Vector(src.getPosition());
			Vector movePosition = ourPosition.add(fromEnemy.normalized());
			smartMove(src, movePosition.toPosition());
		} else {
			smartAttack(src, tgt);
		}
	}

	public void update(List<Unit> enemies, HashMap<Unit, Set<Unit>> attackPlan) {
	}
}

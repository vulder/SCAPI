package SCBots17.splittacus;import java.util.LinkedList;
import java.util.List;

import SCAPI.UnitUtil.UnitControl;
import bwapi.*;

public class SommerinoCamperino extends DefaultBWListener{
    
    //Main variables
    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private int count = 0;
    
    //own variables
    private String state = "Init";
    public List <Unit> attackingUnits = new LinkedList<Unit>();
    public List <Unit> attackingUnits2 = new LinkedList<Unit>();
    public List <Unit> tmp = new LinkedList<Unit>();
    public Unit bestEnemy;
    public Unit bestEnemy2;
    
    //Default methods - do not touch!
    public void run(){
	mirror.getModule().setEventListener(this);
	mirror.startGame();
    }
    
    public void onStart(){
	game = mirror.getGame();
	self = game.self();
	
	count = 0;  //Framecounter has to be 0 at the start!
	state = "Init";
	
	//Cheats
	//game.setLocalSpeed(100);
	game.sendText("black sheep wall");
    }
    
    public static void main(String[] args){
	new SommerinoCamperino().run();
    }
    
    //do not touch methods above!
    
    //run every frame
    public void onFrame() {
	try {
	//status and debug information
	//auofollow();
	debugUnits();
	debugArrows();
	state();
	drawBestEnemy();
	drawBestEnemy2();
	debugCenterPosition();
	debugCenterPosition2();
	markUntis();
	markUntis2();
	fps();
	//System.err.println("State: " + state);
	
	//#################################################################################################################################################################################
	//Main logic
	
	//update the attackingunits - remove dead units
	tmp = new LinkedList<Unit>();
	
	//get units to remove
	for(Unit u: attackingUnits) {
	    if(u.getHitPoints() < 0 || !(u.exists())) {
		tmp.add(u);
	    }
	}
	
	//remove the units from the list
	for(Unit tmpu : tmp) {
	    attackingUnits.remove(tmpu);
	}
	
	//second
	tmp = new LinkedList<Unit>();
	
	//get units to remove
	for(Unit u: attackingUnits2) {
	    if(u.getHitPoints() < 0 || !(u.exists())) {
		tmp.add(u);
	    }
	}

	//remove the units from the list
	for(Unit tmpu : tmp) {
	    attackingUnits2.remove(tmpu);
	}
	
	//if there are only 5 own units left, then it does not make sense to split them up - if i do not have any firebats left also do not split
	if(self.getUnits().size() < 5 ) { //one could add this - || !ihaveFirebat()
	    attackingUnits = self.getUnits(); //all units are in attackingUnits - not in attackingUnits2
	    attackingUnits2 = new LinkedList<Unit>(); //Empty attackingUnits2 list
	}
	
	switch(state) {
		case "Init": 
		    //Assign the attack groups - random
		    
		    /*
		    boolean first = true;
		    for (Unit u: self.getUnits()) {
			if (first) {
			    attackingUnits.add(u);
			    first = false;
			} else {
			    attackingUnits2.add(u);
			    first = true;
			}
		    }
		    */
		    
		    /*
		    //Assign the attack groups - Firebats in one
		    for(Unit u :self.getUnits()) {
			if(u.getType() == UnitType.Terran_Firebat) {
			    attackingUnits.add(u);
			}
			else {
			    attackingUnits2.add(u);
			}
		    }
		    */
		    
		      
		    for(Unit u: self.getUnits()) {
			attackingUnits.add(u);
		    }
		    
		    
		    

		   //**********************************************************************************************************************************************************************
		   //battle formation - also change the count in "Start"
		   //walkInV();
		   //walkInV_upsidedown();
		   //turle();
		   //walkInLine_old();
		   //turtle_bat();
		   
		   //choose the best formation
//		   if(ihaveFirebat()) {
//		       turtle_bat();
//		   }
//		   else {
//		       walkInLine_old();
//		   }
		    
		    walkInLine_old();

		   state = "Start"; //Eigentlich Start
		   break;
		case "nix":
		    break;
		   
		case "Start": //only executed until sorting is done
		    count++;
		    if(!(isWalking()) || count > 80) { //if sorting is complete - no unit is moving - if the is walking method does not work after 100 frames go to idle (80 is good for first level)
			count = 0;
			state = "Idle";
		    }
		    break;
		    
		    
		case "StartBattle":
		    if(!thereIsEnemy()) //if there are no enemies left...
			state = "Idle";
		    
		    //first attackingUnit
		    bestEnemy = bestEnemy(attackingUnits);
		    attack(attackingUnits, bestEnemy); //all units
		    
		    //second attackingUnit
		    bestEnemy2 = bestEnemy(attackingUnits2);
		    
		    //we need to check if there are firebats (then both attack groups should focus ONE enemy)
		    if(thereAreEnemyFirebats())
			bestEnemy2 = bestEnemy; //attackingUnits2 will attack the same target as attackingUnits
		    
		    attack(attackingUnits2, bestEnemy2);
		    
		    state = "InBattle";
		    break;
		    
		    
		case "InBattle":
		    if(!thereIsEnemy()) //if there are no enemies left...
			state = "Idle";
		    
		    //get the best enemy for the first attackingUnits
		    bestEnemy = bestEnemy(attackingUnits);
		    
		    //iterate over all attackingUnits
		    for(int i = 0; i < attackingUnits.size(); i++) {
    			if(!(isAttackingTheBest(attackingUnits.get(i),bestEnemy)))  //if one particular unit is not attacking the best possible target
    			    attackBestEnemy(attackingUnits.get(i),bestEnemy); //execute a new attack command for this unit only 
		    }
		    
		    //get the best enemy for the second attackingUnits2
		    bestEnemy2 = bestEnemy(attackingUnits2);
		    
		    //we need to check if there are firebats (then both attack groups should focus ONE enemy)
		    if(thereAreEnemyFirebats())
			bestEnemy2 = bestEnemy; //attackingUnits2 will attack the same target as attackingUnits
		    
		    //iterate over all attackingUnits2
		    for(int i = 0; i < attackingUnits2.size(); i++) {
    			if(!(isAttackingTheBest(attackingUnits2.get(i),bestEnemy2)))  //if one particular unit is not attacking the best possible target
    			    attackBestEnemy(attackingUnits2.get(i),bestEnemy2); //execute a new attack command for this unit only
		    }
		    
		    break;
		    
		
		case "Idle": //might be useful later
		    state = "Search";
		    break;
		    
		    
		case "Search":
		    if(thereIsEnemy()) { //go to attack mode if there is an enemy
			state = "StartBattle";
			break;
		    }
		    if(isIdleAll()) //only issue new search command if the last one was processed - do no spam the waypoint list!
			//**********************************************************************************************************************************************************************
			searchAll(); //searchAll_up() searchAll()
		    break;
		    
		    
		default:
		    searchAll();
	}
	}catch(Exception e) {
	    e.printStackTrace();
	}
	
	//End Main logic
	//#################################################################################################################################################################################
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //debug methods
    
    //get the current fps
    public void fps() {
	game.drawTextScreen(200, 10, "FPS: "+ game.getFPS());
    }
    
    //draw the center pos to all attackingUnits
    public void debugCenterPosition() {
	if(attackingUnits == null)
	    return;
	Position centerP = calcCenter(attackingUnits);
	if (centerP != null) {
	    game.drawCircleMap(centerP, 3, bwapi.Color.Orange, true);
	}
    }
    
  //draw the center pos to all attackingUnits
    public void debugCenterPosition2() {
	if(attackingUnits2 == null)
	    return;
	Position centerP = calcCenter(attackingUnits2);
	if (centerP != null) {
	    game.drawCircleMap(centerP, 3, bwapi.Color.Blue, true);
	}
    }
    
    //draw a circle arround attackingUnits
    public void markUntis() {
	for(Unit u: attackingUnits) {
	    game.drawCircleMap(u.getPosition(), 5, bwapi.Color.Orange);
	}
    }
    
  //draw a circle arround attackingUnits2
    public void markUntis2() {
	for(Unit u: attackingUnits2) {
	    game.drawCircleMap(u.getPosition(), 5, bwapi.Color.Blue);
	}
    }
    
    
    //draw a circle arround the best enemy
    public void drawBestEnemy() {
	if(bestEnemy != null)
	    game.drawCircleMap(bestEnemy.getPosition(), 4, bwapi.Color.Yellow,true);
    }
    
    //draw a circle arround the best enemy2
    public void drawBestEnemy2() {
	if(bestEnemy2 != null)
	    game.drawCircleMap(bestEnemy2.getPosition(), 4, bwapi.Color.Green,true);
    }
    
    //track a unit and auto follow - BROKEN!
    public void auofollow() {
	Position centerP = null;
	if(!attackingUnits.isEmpty())
	    centerP = calcCenter(attackingUnits);
	if (centerP != null) {
	    game.setScreenPosition(new Position(centerP.getX()-300, centerP.getY()-200));
	}
    }
    
    //debug info about units
    public void debugUnits() {
	game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
	Player enemy = game.enemy();
        drawEnemies(game, enemy);
        drawOrders(self.getUnits());
    }
    
    private void drawEnemies(Game state, Player enemy) {
        for (Unit e : enemy.getUnits()) {
            if (!e.isVisible())
                continue;
            state.drawBoxMap(e.getX() - 2, e.getY() - 2, e.getX() + 2, e.getY() + 2, bwapi.Color.Red);
        }
    }

    //draw lines to all units
    private void drawOrders(List<Unit> units) {
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
    
    //draw debug arrows
    public void debugArrows() {
   	for(Unit myUnit : self.getUnits()) {
   	    if(!(myUnit.isIdle())) {
   		if(myUnit.isAttackFrame() || myUnit.isAttacking()|| myUnit.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit) {
   		    game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), myUnit.getTargetPosition().getX(), myUnit.getTargetPosition().getY(), bwapi.Color.Red);
   		}
   		else {
   		    game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), myUnit.getTargetPosition().getX(), myUnit.getTargetPosition().getY(), bwapi.Color.Green);
   		}
   	    }
   	}
    }
    
    //state
    public void state() {
	game.drawTextScreen(10, 30, "State: " + state);
    }
    
    //debug end
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //on units
    
    public void onUnitCreate(Unit unit) {
	if(unit.getPlayer() == self) {
	    //this is the default unit if a new unit is created
	}
    }
    
    public void onUnitdestroy(Unit u){
	if(u.getPlayer() == self){
	    game.sendText("Es wurde eine unserer Einheiten get�tet!");
	}
    }
    
    //on units end
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //abilities
    
    /**
     * This unit is the v attack formation
     */
    public void walkInV() {
	List <Unit> eus = self.getUnits();

	eus.get(0).stop(true);
	
	for(int i = 1; i < eus.size(); i = i+2) {
	    eus.get(i).move(new Position(eus.get(0).getPosition().getX()+25*i, eus.get(0).getPosition().getY()-25*i));
	    eus.get(i).stop(true);
	    
	    //no err if mod not 0
	    if(eus.get(i+1) != null) {
		eus.get(i+1).move(new Position(eus.get(0).getPosition().getX()-25*i, eus.get(0).getPosition().getY()-25*i));
		eus.get(i+1).stop(true);
	    }
	    
	}
    }
    
    /**
     * This unit is the v attack formation upside down
     */
    public void walkInV_upsidedown() {
	List <Unit> eus = self.getUnits();

	eus.get(0).stop(true);
	
	for(int i = 1; i < eus.size(); i = i+2) {
	    eus.get(i).move(new Position(eus.get(0).getPosition().getX()+25*i, eus.get(0).getPosition().getY()+25*i));
	    eus.get(i).stop(true);
	    
	    //no err if mod not 0
	    if(eus.get(i+1) != null) {
		eus.get(i+1).move(new Position(eus.get(0).getPosition().getX()-25*i, eus.get(0).getPosition().getY()+25*i));
		eus.get(i+1).stop(true);
	    }
	    
	}
    }
    
    
    /**
     * turtle formation
     */
    public void turle() {
	List <Unit> eus = self.getUnits();

	//size
	int size = 30;
	
	//special case for the first two units
	eus.get(0).stop(true);
	
	if(eus.size() > 1){
	    eus.get(1).move(new Position(eus.get(0).getPosition().getX(), eus.get(0).getPosition().getY()-size));
	    eus.get(1).stop(true);
	}
	
	for(int i = 2; i < eus.size(); i = i+2) {
	    eus.get(i).move(new Position(eus.get(0).getPosition().getX()+size*i, eus.get(0).getPosition().getY()));
	    eus.get(i).stop(true);
	    
	  //no err if mod not 0
	    if(eus.get(i+1) != null) {
		eus.get(i+1).move(new Position(eus.get(0).getPosition().getX()+size*i, eus.get(0).getPosition().getY()-size));
		eus.get(i+1).stop(true);
	    }	}
    }
    
    
    /**
     * do the turtle, but sort the firebats to the front
     */
    public void turtle_bat() {
	List <Unit> eus = self.getUnits();
	
	//size
	int size_x = 150; //150
	int size_y = 75;
	
	boolean b = true;
	
	for(int i = 0; i < eus.size(); i++) {
	    if(eus.get(i).getType() == UnitType.Terran_Firebat) {
		if(b) {
		    eus.get(i).move(new Position(eus.get(i).getPosition().getX()+size_x, eus.get(i).getPosition().getY()+size_y));
		    eus.get(i).stop(true);
		    b = false;
		}
		else {
		    eus.get(i).move(new Position(eus.get(i).getPosition().getX()-size_x, eus.get(i).getPosition().getY()+size_y));
		    eus.get(i).stop(true);
		    b = true;
		}
	    }
	}
    }
    
    /**
     * do the turtle, but sort the firebats to the front
     */
    public void turtle_bat_up() {
	List <Unit> eus = self.getUnits();
	
	//size
	int size_x = 150; //150
	int size_y = 75;
	
	boolean b = true;
	
	for(int i = 0; i < eus.size(); i++) {
	    if(eus.get(i).getType() == UnitType.Terran_Firebat) {
		if(b) {
		    eus.get(i).move(new Position(eus.get(i).getPosition().getX()+size_x, eus.get(i).getPosition().getY()-size_y));
		    eus.get(i).stop(true);
		    b = false;
		}
		else {
		    eus.get(i).move(new Position(eus.get(i).getPosition().getX()-size_x, eus.get(i).getPosition().getY()-size_y));
		    eus.get(i).stop(true);
		    b = true;
		}
	    }
	}
    }
    
    
    /**
     * This is a "search" method - at the moment all units move forward
     */
    public void searchAll() {
	for(Unit myUnit : self.getUnits()) {
		 myUnit.move(new Position(myUnit.getPosition().getX(), myUnit.getPosition().getY()+50), true);
	    }
	}
    
    /**
     * This is a "search" method - at the moment all units move forward
     */
    public void searchAll_up() {
	for(Unit myUnit : self.getUnits()) {
		 myUnit.move(new Position(myUnit.getPosition().getX(), myUnit.getPosition().getY()-50), true);
	    }
	}
    
    /**
     * All units should walk in a line if this method is called - old version
     */
    public void walkInLine_old() {
	//debug
	game.sendText("We are trying to walk in a line!");
	
	//do this for first player - special case
	Unit first = self.getUnits().get(0);
	first.stop();
	
	for(int i = 1; i < self.getUnits().size();i++) {
	    self.getUnits().get(i).move(new Position(self.getUnits().get(0).getPosition().getX()+(i*25), self.getUnits().get(0).getPosition().getY()+i));
	    self.getUnits().get(i).stop(true);
	}
    }
    
    
    /**
     * Attack units in list
     * @param attackUnits - list of all units, that should attack
     * @param best possible target
     */
    public void attack(List <Unit> attackUnits, Unit best) {
            for (Unit myUnit : attackUnits) {
                if (myUnit.getLastCommandFrame() >= game.getFrameCount() || myUnit.isAttackFrame())
                    continue;
                if (myUnit.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit)
                    continue;
                myUnit.attack(best);
            }
    }
    
    /**
     * Attacks the best enemy
     * @param ourUnit (single)
     * @param bestEnemy
     */
    public void attackBestEnemy(Unit ourUnit, Unit bestEnemy) {
	ourUnit.attack(bestEnemy);
    }
    
    
    //end abilities methods
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //checks and calculations
    
    /**
     * Test if there is a firebat in our unit list
     * @return
     */
    public boolean ihaveFirebat() {
	for(Unit u: self.getUnits()) {
	    if( u.getType() == UnitType.Terran_Firebat){
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Test if there are firebat enemy units
     * @return thereAreEnemyFirebats
     */
    public boolean thereAreEnemyFirebats() {
	for(Unit u: game.enemy().getUnits()) {
	    if( u.getType() == UnitType.Terran_Firebat){
		return true;
	    }
	}
	return false;
    }
    
    /**
     * 
     * @param ourUnit - the unit to test if attacking the best
     * @param enemy - best enemy
     * @return true if they are attacking the best enemy, false if not
     */
    public boolean isAttackingTheBest(Unit ourUnit, Unit enemy) {
	int eID = enemy.getID();
	
	try {
	    if (ourUnit.getOrderTarget() != null
		&& ourUnit.getOrderTarget().getID() != eID) {
	    return false;
	}

	if (ourUnit.getTarget() != null && ourUnit.getTarget().getID() != eID) {
	    return false;
	}
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	}
	
	
	
	return true;
    }
    
    /**
     * Find out the best possible target
     * @return Unit bestEnemy
     */
    public Unit bestEnemy(List <Unit> attack) {	
	Player enemyp = game.enemy();
	List<Unit> eus = enemyp.getUnits();
	
	Position centerP = calcCenter(attack);
	
	boolean existsFireBat = false;
	Unit nearTarget = eus.get(0);
	
	for(Unit enemy : eus) {
	    if(enemy.getType() == UnitType.Terran_Firebat) {
		nearTarget = enemy;
		existsFireBat = true;
		break;
	    }
	}
	
	if (centerP != null) {
	    int nearTargetD = nearTarget.getDistance(centerP);

	    for (Unit enemy : eus) {
		if (existsFireBat) {
		    if (enemy.getType() == UnitType.Terran_Firebat
			    && enemy.getDistance(centerP) < nearTargetD) {
			nearTarget = enemy;
			nearTargetD = enemy.getDistance(centerP);
		    }
		} else {
		    if (enemy.getDistance(centerP) < nearTargetD) {
			nearTarget = enemy;
			nearTargetD = enemy.getDistance(centerP);
		    }
		}
	    }
	}
	
	return nearTarget; //if there is no Firebat --> "random" enemy
    }
    
    /**
     * Tests if all units are idle
     * @return boolean allUnitsAreIdle
     */
    public boolean isIdleAll() {
	boolean idle = false;
	for(Unit myUnit : self.getUnits()) {
	    if(myUnit.isIdle() || myUnit.isHoldingPosition()) {
		idle = true;
	    }
	    else {
		idle = false;
	    }
	}
	return idle;
    }
    
  /**
   * Test if any unit is moving/walking
   * @return boolean anyUnitIsWalking
   */
    public boolean isWalking() {
	for(Unit myUnit : self.getUnits()) {
	    if(myUnit.isMoving()) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Test if there is an enemy at all
     * @return boolean thereIsEnemy
     */
    public boolean thereIsEnemy() {
	Player enemy = game.enemy();
	List<Unit> eus = enemy.getUnits();
	if(eus.size() > 0) 
	    return true;
	return false;
	
    }
    
    /**
     * Calculate the center position to all given Units
     * @param units
     * @return
     */
    private Position calcCenter(List<Unit> units) {
	if (units.isEmpty()) {
	    return null;
	}
	if (units.size() == 1) {
	    return units.get(0).getPosition();
	}
	int minX = units.get(0).getX(), maxX = units.get(0).getX();
	int minY = units.get(0).getY(), maxY = units.get(0).getY();

	for (Unit unit : units) {
	    Position p = unit.getPosition();
	    if (p.getX() < minX) {
		minX = p.getX();
	    }
	    if (p.getX() > maxX) {
		maxX = p.getX();
	    }
	    if (p.getY() < minY) {
		minY = p.getY();
	    }
	    if (p.getY() > maxY) {
		maxY = p.getY();
	    }
	}
	return new Position((minX + maxX) / 2, (minY + maxY) / 2);
    }
    
    //end checks and calculations
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
}
package SCBots17.HereGoesNothing;

import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

import bwapi.*;

enum State
{
    PREPARATION, 
    SEARCHING_ENEMY,
    ATTACK,
    DODGE_MANEUVER,
}

enum unitState
{
    PREPARATION,
    DEAD,
}


public class HereGoesNothing extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private State gamestate = State.PREPARATION;
    private HashMap<Unit, unitState> unitStates;
    private Player enemy;
    
    private Unit currentTarget = null;
    private Unit closestEnemyUnit = null;
    private Position averagePosition = new Position(0, 0);
    
    private Position initialFormationReference = new Position(0, 0);
    private int formationDistance = 30;
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        if (unit.getPlayer() == self) {
            unitStates.put(unit, unitState.PREPARATION);
        }
    }

    @Override
    public void onStart() {
	try {
        game = mirror.getGame();
        self = game.self();

        // initially set a random enemy
        enemy = game.enemy();
        
        gamestate = State.PREPARATION;
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	}

    }
  
    // ==================================  OWN METHODS BEGIN  =============================================
    
    protected void log(String message)
    {
	// Just a wrapper for 'game.sendText()'
	
	// send message to chat
	game.sendText(message);
    }
    
    protected void sortByHealth(List<Unit> units)
    {
	/* This sorts the given List of Units in ascending order
	 * by Hitpoints
	 */
	
	// Using Java's Lambdas, because I love C++ Lambdas
	units.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit u1, Unit u2)
	    {
		int value1 = u1.getHitPoints();
		int value2 = u2.getHitPoints();
		
		return value2 - value1;
	    }
	});
    }
    
    protected void sortByWeaponRange(List<Unit> units)
    {
	/* This sorts the given List of Units in ascending order
	 * by their respective ground weapon range
	 */
	
	// Using Java's Lambdas, because I love C++ Lambdas
	units.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit u1, Unit u2)
	    {
		int value1 = u1.getType().groundWeapon().maxRange();
		int value2 = u2.getType().groundWeapon().maxRange();
		
		return value1 - value2;
	    }
	});
	
    }
    
    protected void sortByDistance(List<Unit> units, Vector2D v)
    {
	/* This sorts the given List of Units in ascending order
	 * by their respective distance to the specified Vector v.
	 */
	
	// Don't know but Java needs it
	final Vector2D vec = v;
	
	// Using Java's Lambdas, because I love C++ Lambdas
	units.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit u1, Unit u2)
	    {
		int value1 = u1.getDistance(new Position((int)vec.getX(), (int)vec.getY()));
		int value2 = u2.getDistance(new Position((int)vec.getX(), (int)vec.getY()));
		
		return value1 - value2;
	    }
	});
	
    }
    
    protected void sortByXPos(List<Unit> units)
    {
	/* This sorts the given List of Units in ascending order
	 * by their respective x coordinate.
	 */
	
	// Using Java's Lambdas, because I love C++ Lambdas
	units.sort(new Comparator<Unit>() {
	    @Override
	    public int compare(Unit u1, Unit u2)
	    {
		int value1 = u1.getPosition().getX();
		int value2 = u2.getPosition().getX();
		
		return value1 - value2;
	    }
	});
    }
        
    protected void move(Vector2D v, Unit u)
    {
	// don't do anything if already moving
	if (u.isMoving())
	    return;
	
	// don't do anything if the length of the specified Vector is zero
	if ((int)v.getLength() == 0)
	    return;
	
	// calculating desired absolute position on Map
	Position desiredPosition = new Position(u.getPosition().getX() + (int)v.getX(), u.getPosition().getY() + (int)v.getY());
	// sending move command	
	u.move(desiredPosition);
	
	// logging
	//log("move(): \tMoving to: " + desiredPosition.toString());
    }
    
    protected void moveTo(Vector2D v, Unit u)
    {
	// don't do anything if already moving
	if (u.isMoving())
	    return;
	    
	// Convert Vector to Position
	Position desiredPosition = new Position((int)v.getX(), (int)v.getY());
	// sending move command
	u.move(desiredPosition);
	
	// logging
	//log("moveTo(): \tMoving to: " + desiredPosition.toString());
    }
    
    protected void groupedMove(Vector2D v, List<Unit> units)    
    {
	// send a move command to every unit in the specified List
	for (Unit u : units)
	    move(v, u);
    }
    
    protected void groupedMoveTo(Vector2D v, List<Unit> units)
    {
	/* This Method makes a List of units move to a specified
	 * Position, using a Position Vector.
	 * 
	 */
	
	// iterating over every Unit in the list
	for (Unit u : units)
	{
	    // calling the actual function
	    moveTo(v, u);
	}
	
    }
    
    protected void getInFormation()
    {
	// get own Units
	List<Unit> ownUnits = self.getUnits();
	
	// sort the units so that there won't be as much commotion
	sortByXPos(ownUnits);
	
	/* We want to get approximately in line with the first one
	 * 
	 * We don't require any checking before we send the move command,
	 * because (currently) this method gets executed only once until the next state is entered
	 */
	
	// reference unit data
	Unit referenceUnit = ownUnits.get(0);
	int referenceX = referenceUnit.getPosition().getX() + 10;
	int referenceY = referenceUnit.getPosition().getY() + 10;

	
	// save initial position
	initialFormationReference = referenceUnit.getPosition();

	for (int i = 0; i < ownUnits.size(); i++)
	{
	    	ownUnits.get(i).move(new Position(referenceX + formationDistance * i, referenceY));
	}
	
	// logging
	log("getInFormation(): \tFormation");
    }

    protected boolean isInFormation()
    {
	// position tolerances on both sides
	int tolerance = 10;
	
	// get own Units
	List<Unit> ownUnits = self.getUnits();
	sortByXPos(ownUnits);
	
	int falseCount = 0;
	
	// check the position for everyUnit
	for (int i = 0; i < ownUnits.size(); i++)
	{
	    // check if unit is in approximately in position
	    int posX = initialFormationReference.getX() + formationDistance * i;
	    if (!((ownUnits.get(i).getPosition().getX() <= (posX) + tolerance)
		    && (ownUnits.get(i).getPosition().getX() >= (posX) - tolerance)))
	    {
		falseCount++;
	    }
	}
	
	// change number for tolerances
	return (falseCount < 1);
    }
    
    protected void groupedTurnTowardsEnemy(List<Unit> units, Vector2D v)
    {
	// setting a reference Point
        Position referencePoint = new Position(averagePosition.getX(), averagePosition.getY());
        
        // calculate the vector
        Vector2D vectorToEnemy = new Vector2D(Math.abs(referencePoint.getX() - v.getX()), Math.abs(referencePoint.getY() - v.getY()));
        
        groupedTurn(vectorToEnemy, self.getUnits());
    }
    
    protected void averagePosition(List<Unit> units)
    {
	// just sum up all the x values and y values and divide them by the power of the list
	int avgX = 0;
	int avgY = 0;
	int size = units.size();
	
	for (Unit u : units)   
	{
	    avgX += u.getPosition().getX();
	    avgY += u.getPosition().getY();
	}
	avgX /= size;
	avgY /= size;
	
	averagePosition = new Position(avgX, avgY);
    }
    
    protected Vector2D getAveragePosition(Player player)
    {
	// get Units
	List<Unit> units = player.getUnits();
	
	if (units.isEmpty())
	    return new Vector2D(-1000, -1000);
	
	// calculate
	// just sum up all the x values and y values and divide them by the power of the list
	int avgX = 0;
	int avgY = 0;
	int size = units.size();
		
	for (Unit u : units)   
	{
	    avgX += u.getPosition().getX();
	    avgY += u.getPosition().getY();
	}
	avgX /= size;
	avgY /= size;
		
	return new Vector2D(avgX, avgY);
    }
    
    protected void attack(Unit own, Unit target)
    {
	// Just a wrapper for Unit.attack()
	
	// check for already existing attack commands (Don't know if one part of it is redundant)
	if ((own.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit
		|| own.isAttacking()) && (own.getLastCommand().getTarget() == target))
	    return;
	
	// Don't really know in detail what this does, but it sounds splendid :D
	if (!own.canAttack(target))
	    return;
	
	// send the command
	own.attack(target);
    }
    
    protected void groupedAttack(Unit target)    
    {
	// don't attack the same target multiple times
	if (currentTarget == target)
	    return;
	
	// just command every unit to attack one unit
	for (Unit u : self.getUnits())
	{
	    attack(u, target);
	}
	
	// saving the target that is getting attacked
	currentTarget = target;
    }
   
    protected void turn(Vector2D v, Unit u)
    {
	// This Method just moves the Unit slightly according to the the given vector v
	
	
	// scale the vector
        v.normalize();
        v.scalarMultiply(8.0);
        
        // send the command
        move(v, u);
    }

    protected void groupedTurn(Vector2D v, List<Unit> units)
    {
	// iterating over every given unit and applying turn
	for (Unit u : units)
	{
	    turn(v, u);
	}
    }
      
    protected void interruptAllCommands(List<Unit> units)
    {
	// this makes all the units stop their movement
	for (Unit u : units)
	    u.stop();
    }
    
    protected int getDistance(Vector2D v1, Vector2D v2)
    {
	// calculates the distance between two position vectors
	v1.subtract(v2);
	
	return Math.abs((int)v1.getLength());
    }
    
    protected boolean searchEnemyUnit()
    {
	/* Looks for enemy units and returns 'true'
	 * if there is one.
	 * 
	 * Additionally it iterates through all the enemies and considers the one with 
	 * the closest units (on average) when saving the 'currentTarget'.
	 * 
	 * Furthermore this Method saves a reference to the found enemy unit
	 * in the attribute 'currentTarget'. (This could be replace some day in the future). 
	 */
	
	List<Player> closestEnemy = game.enemies();
	
	closestEnemy.sort(new Comparator<Player>() {
	    @Override
	    public int compare(Player e1, Player e2)
	    {
		int value1 = getDistance(getAveragePosition(e1), getAveragePosition(self));
		int value2 = getDistance(getAveragePosition(e2), getAveragePosition(self));
		
		return value1 - value2;
	    }
	    
	});
	
	enemy = closestEnemy.get(0);
	
	// counting the enemy's Units
	if (game.enemy().visibleUnitCount() > 0)
	{
	    // calculate the average Position
	    averagePosition(self.getUnits());
	    
	    // saving a reference to the found enemy unit (currently just the first one)
	    //currentTarget = getFurthestUnit(enemy.getUnits(), averagePosition);
	    
	    // returning true and therefore indicating that we found a unit
	    return true;
	}
	
	// If no Enemy Unit was found
	return false;
    }

    protected List<Unit> getClosestRangeEnemyUnits()
    {
	// get the enemy Units
	List<Unit> enemyUnits = enemy.getUnits();
	
	// remove every Unit which is not visible
	for (int i = 0; i < enemyUnits.size(); i++)
	{
	    if (!enemyUnits.get(i).isVisible(self))
		enemyUnits.remove(i);
	}
	
	sortByWeaponRange(enemyUnits);
	
	// return the sorted list
	return enemyUnits;
    }

    protected List<Unit> getFirebats(List<Unit> units)
    {
	/* This is searching for any Firebat Units in the specified List
	 * and returns the ones that are found in a List.
	 * 
	 * If none is found it simply returns the first Unit in the List as a List.
	 */
	
	// check if the given list contains any Units
	if (units.isEmpty())
	    return units;
	
	List<Unit> firebats = new LinkedList<Unit>();
	
	for (Unit u : units)
	{
	    // check if Type is Firebat if so add it to the List
	    if (u.getType() == UnitType.Terran_Firebat)
		firebats.add(u);
	}
	
	// just return the other Units if no Firebats were found
	if (firebats.isEmpty())
	    return units;
	
	// return the result
	return firebats;
    }
    
    protected Unit getClosestUnit(List<Unit> units, Position p)
    {
	// set a reference point
	//Unit referenceUnit = self.getUnits().get(0);
	Vector2D referenceVector = new Vector2D(p.getX(), p.getY());
	
	// sort by distance to the reference point
	sortByDistance(units, referenceVector);
	
	// hopefully the sorting algorithm did a good job
	return units.get(0);
    }
    
    protected Unit getFurthestUnit(List<Unit> units, Position p)
    {
	// set a reference point
	//Unit referenceUnit = self.getUnits().get(0);
	Vector2D referenceVector = new Vector2D(p.getX(), p.getY());
	
	// sort by distance to the reference point
	sortByDistance(units, referenceVector);
	
	// hopefully the sorting algorithm did a good job
	return units.get(units.size() - 1);
	
    }
    
    protected void holdPosition(Unit u, Vector2D v)
    {
	// don't interrupt attack command
	if (u.getLastCommand().getUnitCommandType() == UnitCommandType.Attack_Unit)
	    return;
	
	// don't do anything if already holding position
	if (u.getLastCommand().getUnitCommandType() == UnitCommandType.Hold_Position)
	    return;
	
	// If the Vector is not specified we'll hold the current position, otherwise we firstly move according to 'v'
	if (v != null)
	{
	    // firstly move if the Vector is specified
	    moveTo(v, u);
	}
	
	// now hold the position (queue in, if still moving)
	u.holdPosition((v != null));
	
	// logging
	//log("holdPosition(): \tHolding Position" + ((v == null) ? (u.getPosition().toString()) : (v.toString())));
    }
    
    protected void groupedHoldPosition(List<Unit> units, Vector2D v)
    {
	// makes every Unit in the specified List hold its current Position
	for (Unit u : units)
	    // call the actual method
	    holdPosition(u, v);
    }
  
    protected void changeState(State newState)
    {
	// logging
	//log("Exiting " + gamestate.toString() + " ---- Entering " + newState.toString());
	
	// setting the new state
	gamestate = newState;
    }
    
    protected void unitChangeState(Unit u, unitState newState)
    {
	// setting the new state
	unitStates.put(u, newState);
    }

    
    private boolean stopFlag = false;
    protected void stateSwitch(State state)
    {
	switch (state)
	{
	// PREPARATION
	case PREPARATION:
	    
	    // get into formation
	    getInFormation();
	    
	    // changing the State because we only want this state for one cycle
	    changeState(State.SEARCHING_ENEMY);
	    
	    break;
	    
	// SEARCHING ENEMY
	case SEARCHING_ENEMY:

	    // check if the group is in Formation
	    if (isInFormation())
	    {
		if (!stopFlag)
		{
		    interruptAllCommands(self.getUnits());
		    stopFlag = true;
		}
	    	groupedMove(new Vector2D(0.0, -10.0), self.getUnits());
	    }
	    
	    // we want to enter a new state as soon as an enemy unit is visible
	    if (searchEnemyUnit())
		changeState(State.ATTACK);
	    
	    
	    break;
	    
	// ATTACK
	case ATTACK:
	    
	    // calculate the average position of all our units
	    averagePosition(self.getUnits());
	    
	    // get potential Firebats
	    List<Unit> potentialFirebats = getFirebats(enemy.getUnits());
	    
	    // get the closest enemy unit
	    closestEnemyUnit = getClosestUnit(potentialFirebats, averagePosition);
	    
	    game.drawCircleMap(closestEnemyUnit.getPosition(), 4, Color.Green);
	    
	    // concentrate the attack on one enemy unit
	    groupedAttack(closestEnemyUnit);
	    
	    
	    break;
	    
	case DODGE_MANEUVER:
	    
	    break;
	    
	// VERY VERY VERY unlikely to happen
	default:
	    log("What the F***?!");
	    break;
	}
    }
    
    protected void unitStateSwitch()
    {
	for (Unit u : self.getUnits())
	{
	    switch (unitStates.get(u))
	    {
	    case PREPARATION:
		
		break;
	    
	    case DEAD:
		
		// Don't do anything if dead
		
		break;
		
		
	    // VERY VERY VERY unlikely to happen
	    default:
		log("What the F***?!");
		break;
	    }
	}
    }
    
    // ==================================  OWN METHODS END  =============================================

    @Override
    public void onUnitDiscover(Unit u) {
	

    }

    @Override
    public void onUnitDestroy(Unit u) 
    {
	if (u.getPlayer() != self) 
	{
	    //log("RIP IN PEACE!!!");
	    changeState(State.SEARCHING_ENEMY);
	}   	
    }
    
    @Override
    public void onFrame() 
    {
	// unitStateSwitch();
	stateSwitch(gamestate);
	
	drawOrders(game, self.getUnits());
    }
    
    private void drawOrders(Game state, List<Unit> units) {
	for (Unit u : units) {
	    if (u.isIdle())
		continue;

	    Position pos = u.getOrderTargetPosition();
	    bwapi.Color c = bwapi.Color.Green;

	    UnitCommandType ty = u.getLastCommand().getUnitCommandType();
	    if (ty == UnitCommandType.Attack_Move
		    || ty == UnitCommandType.Attack_Unit)
		c = bwapi.Color.Red;

	    game.drawLineMap(u.getPosition(), pos, c);
	}

	// draw the average Position
	game.drawCircleMap(averagePosition, 4, Color.Orange);
	
	// draw the gamestate on the top left corner of the screen
	//game.drawTextMap(80, 80, "state: " + gamestate.toString();
    }

    public static void main(String[] args) {
        new HereGoesNothing().run();
    }
}

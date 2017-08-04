package SCBots17.HereGoesNothing;

import bwapi.*;

public class Vector2D {
    private double xComponent;
    private double yComponent;
    
    public Vector2D(double x, double y)
    {
	xComponent = x;
	yComponent = y;
    }
    
    public void add(Vector2D v)
    {
	xComponent += v.xComponent;
	yComponent += v.yComponent;
    }
    
    public void subtract(Vector2D v)
    {
	xComponent -= v.xComponent;
	yComponent -= v.yComponent;
    }
    
    public void scalarMultiply(double scalar)
    {
	xComponent *= scalar;
	yComponent *= scalar;
    }
    
    public double getLength()
    {
	return Math.sqrt((xComponent * xComponent) + yComponent * yComponent);
    }
    
    public void normalize()
    {
	double length = getLength();
	xComponent /= length;
	yComponent /= length;
    }
    
    public double getX()
    {
	return xComponent;
    }
    
    public double getY()
    {
	return yComponent;
    }
    
    public void draw(Game g, Position startPoint)
    {
	Position endPoint = new Position(startPoint.getX() + (int)getX(), startPoint.getY() + (int)getY());
	g.drawLineMap(startPoint, endPoint, bwapi.Color.Teal);
    }
    
    @Override
    public String toString()
    {
	return ("[" + (int)xComponent + ", " + (int)yComponent + "]");
    }
}

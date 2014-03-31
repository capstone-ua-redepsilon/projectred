package com.uacapstone.red.util;

public class Vector2 {
	private double x;
	private double y;
	
	Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public double getX()
	{
		return this.x;
	}
	
	public double getY()
	{
		return this.y;
	}
	
	public Vector2 scale(double factor)
	{
		return new Vector2(factor*x, factor*y);
	}
	
	public Vector2 negate()
	{
		return scale(-1);
	}
	
	public Vector2 add(Vector2 other)
	{
		return new Vector2(x + other.getX(), y + other.getY());
	}
	
	public Vector2 subtract(Vector2 other)
	{
		return add(other.scale(-1));
	}
}

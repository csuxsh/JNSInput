package com.blueocean.jnsinput;

public class Pos
{
	float x;
	float y;
	int tag;
	int action;

	public Pos(float x, float y, int action, int tag)
	{
		this.x = x;
		this.y = y;
		this.tag = tag;
		this.action = action;
	}
	public Pos(float x, float y, int action)
	{
		this.x = x;
		this.y = y;
		tag = 0xff;
		this.action = action;
	}
	public Pos(float x, float y)
	{
		this.x = x;
		this.y = y;
		tag = 0xff;
	}
	void setTag(int tag)
	{
		this.tag = tag;
	}

}
package com.bizosys.oneline.management;

public class Metric 
{
	public String name;
	public double val;

	public Metric(String name, double val) 
	{
		this.name = name;
		this.val = val;
	}

	public Metric(String name) 
	{
		this.name = name;
	}

}
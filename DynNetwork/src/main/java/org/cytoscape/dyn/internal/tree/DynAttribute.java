/*
 * DynNetwork plugin for Cytoscape 3.0 (http://www.cytoscape.org/).
 * Copyright (C) 2012 Sabina Sara Pfister
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.cytoscape.dyn.internal.tree;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.dyn.internal.util.KeyPairs;

/**
 * <code> DynAttribute </code> is the abstract class to set/request represents all 
 * dynamic attributes, i.e. a list of intervals containing the value of type T 
 * and the time interval.
 * 
 * @author sabina
 *
 * @param <T>
 */
public class DynAttribute<T>
{
	private Class<T> type;
	
	private List<DynInterval<T>> intervalList;
	
	private KeyPairs key;
	
	private List<DynAttribute<T>> children;
	
	public DynAttribute(Class<T> type)
	{
		this.type = type;
		intervalList = new ArrayList<DynInterval<T>>();
		children = new ArrayList<DynAttribute<T>>();
	}
	
	public DynAttribute(DynInterval<T> interval, KeyPairs key)
	{
		this(interval.getType());
		this.key = key;
		this.intervalList.add(interval);
		interval.setAttribute(this);
	}
		
	public void addInterval(DynInterval<T> interval)
	{
		intervalList.add(interval);
		interval.setAttribute(this);
	}
	
	public void removeInterval(DynInterval<T> interval)
	{
		intervalList.remove(interval);
	}
	
    public List<DynInterval<T>> getIntervalList()
    {
		return intervalList;
	}
    
    public List<DynInterval<T>> getRecursiveIntervalList(ArrayList<DynInterval<T>> list)
    {
    	for (DynInterval<T> interval : intervalList)
    		list.add(interval);
    	for (DynAttribute<T> attr : children)
    		attr.getRecursiveIntervalList(list);
    	return list;
    }

    public void setKey(long row, String column)
    {
    	this.key = new KeyPairs(column, row);
    }

    public KeyPairs getKey() 
    {
    	return key;
    }
	
	public String getColumn() 
	{
		return key.getColumn();
	}
	
	public long getRow() 
	{
		return key.getRow();
	}
	
	public void addChildren(DynAttribute<T> attr)
	{
		this.children.add(attr);
	}
	
	public void removeChildren(DynAttribute<T> attr)
	{
		if (this.children.contains(attr))
			this.children.remove(attr);
	}
	
	public void clear()
	{
		this.intervalList.clear();
		this.children.clear();
		this.key = null;
	}

	public Class<T> getType()
	{
		return type;
	}
	
    public double getMinTime()
    {
            double minTime = Double.POSITIVE_INFINITY;
            for (DynInterval<T> i : intervalList)
                    minTime = Math.min(minTime, i.getStart());
            return minTime;
    }

	public double getMaxTime()
    {
            double maxTime = Double.NEGATIVE_INFINITY;
            for (DynInterval<T> i : intervalList)
                    maxTime = Math.max(maxTime, i.getEnd());
            return maxTime;
    }

}
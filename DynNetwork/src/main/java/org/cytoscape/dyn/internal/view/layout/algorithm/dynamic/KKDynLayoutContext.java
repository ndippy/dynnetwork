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

package org.cytoscape.dyn.internal.view.layout.algorithm.dynamic;

import java.util.List;

import org.cytoscape.work.Tunable;

/**
 * <code> KKDynLayoutContext </code> encodes the parameters for the Dynamic Kamada Kawai Layout. 
 * 
 * @author Sabina Sara Pfister
 *
 */
public class KKDynLayoutContext 
{	
	/**
	 * Event type.
	 */
	@Tunable(description="Event type")
	public int m_event_type = 0;
	
	/**
	 * Attribute name.
	 */
	@Tunable(description="Event type")
	public String m_attribute_name;
	
	/**
	 * Event list.
	 */
	@Tunable(description="Event list")
	public  List<Double> m_event_list;
	
	/**
	 * Maximum number of iterations.
	 */
	@Tunable(description="Maximum number of iterations")
	public int m_max_iterations = 10;
	
	/**
	 * Iteration rate.
	 */
	@Tunable(description="Iteration rate")
	public double m_iteration_rate;
	
	/**
	 * Number of past events to consider.
	 */
	@Tunable(description="Number of past events")
	public int m_past_events = 0;
	
	/**
	 * Number of future events to consider.
	 */
	@Tunable(description="Number of future events")
	public int m_future_events = 0;
	
	/**
	 * Cancel layout algorithm execution.
	 */
	@Tunable(description="Cancel algorithm")
	public boolean m_cancel = true;
}

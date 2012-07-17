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

package org.cytoscape.dyn.internal.view.model;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.dyn.internal.model.DynNetwork;
import org.cytoscape.dyn.internal.model.tree.DynAttribute;
import org.cytoscape.dyn.internal.model.tree.DynInterval;
import org.cytoscape.dyn.internal.model.tree.DynIntervalTreeImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * <code> DynNetworkViewImpl </code> is the interface for the visualization of 
 * dynamic networks {@link DynNetworkView}.
 * 
 * @author Sabina Sara Pfister
 *
 * @param <T>
 */
public final class DynNetworkViewImpl<T> implements DynNetworkView<T>
{
	private final DynNetwork<T> dynNetwork;
	private final CyNetworkView view;
	private final VisualMappingManager cyStyleManager;
	
	private double currentTime;
	
	private List<DynInterval<T>> currentNodes;
	private final DynIntervalTreeImpl<T> nodePositionsTree;
	
	private final DynAttribute<T> node_X_Pos;
	private final DynAttribute<T> node_Y_Pos;
	private final DynAttribute<T> node_Z_Pos;

	public DynNetworkViewImpl(
			DynNetwork<T> dynNetwork,
			final CyNetworkViewManager networkViewManager,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final VisualMappingManager cyStyleManager)
	{
		this.currentTime = 0;
		this.dynNetwork = dynNetwork;
		this.cyStyleManager = cyStyleManager;
		
		this.view = cyNetworkViewFactory.createNetworkView(dynNetwork.getNetwork());
		networkViewManager.addNetworkView(view);
		cyStyleManager.setVisualStyle(cyStyleManager.getDefaultVisualStyle(), view);
		cyStyleManager.getDefaultVisualStyle().apply(view);
		
		this.currentNodes = new ArrayList<DynInterval<T>>();
		this.nodePositionsTree = new DynIntervalTreeImpl<T>();
		
		this.node_X_Pos = new DynAttribute<T>((Class<T>) Double.class);
		this.node_Y_Pos = new DynAttribute<T>((Class<T>) Double.class);
		this.node_Z_Pos = new DynAttribute<T>((Class<T>) Double.class);
	}
	
	@Override
	public int readVisualProperty(CyNode node, VisualProperty<Integer> vp) 
	{
		return view.getNodeView(node).getVisualProperty(vp).intValue();	
	}
	
	@Override
	public double readVisualProperty(CyNode node, VisualProperty<Double> vp) 
	{
		return view.getNodeView(node).getVisualProperty(vp).doubleValue();	
	}
	
	@Override
	public void writeVisualProperty(CyNode node, VisualProperty<Integer> vp, int value) 
	{
		view.getNodeView(node).setVisualProperty(vp,value);
	}
	
	@Override
	public void writeVisualProperty(CyNode node, VisualProperty<Double> vp, double value) 
	{
		view.getNodeView(node).setVisualProperty(vp,value);
	}
	
	@Override
	public void writeLockedVisualProperty(CyNode node, VisualProperty<Integer> vp, int value) 
	{
		view.getNodeView(node).setLockedValue(vp, value);
	}

	@Override
	public void writeLockedVisualProperty(CyNode node, VisualProperty<Double> vp, double value) 
	{
		view.getNodeView(node).setLockedValue(vp, value);
	}
	
	@Override
	public int readVisualProperty(CyEdge edge, VisualProperty<Integer> vp) 
	{
		return view.getEdgeView(edge).getVisualProperty(vp).intValue();
	}
	
	@Override
	public double readVisualProperty(CyEdge edge, VisualProperty<Double> vp) 
	{
		return view.getEdgeView(edge).getVisualProperty(vp).doubleValue();
	}

	@Override
	public void writeVisualProperty(CyEdge edge, VisualProperty<Integer> vp, int value) 
	{
		view.getEdgeView(edge).setVisualProperty(vp,value);
	}
	
	@Override
	public void writeVisualProperty(CyEdge edge, VisualProperty<Double> vp, double value) 
	{
		view.getEdgeView(edge).setVisualProperty(vp,value);
	}
	
	@Override
	public void writeLockedVisualProperty(CyEdge edge, VisualProperty<Integer> vp, int value) 
	{
		view.getEdgeView(edge).setLockedValue(vp,value);
	}
	
	@Override
	public void writeLockedVisualProperty(CyEdge edge, VisualProperty<Double> vp, double value) 
	{
		view.getEdgeView(edge).setLockedValue(vp,value);
	}
	
	@Override
	public synchronized void insertNodePositionX(CyNode node, DynInterval<T> interval)
	{
		this.node_X_Pos.addInterval(interval);
		interval.getAttribute().setKey(node.getSUID(), "node_X_Pos");
		nodePositionsTree.insert(interval, node.getSUID());
	}
	
	@Override
	public synchronized void insertNodePositionY(CyNode node, DynInterval<T> interval)
	{
		this.node_Y_Pos.addInterval(interval);
		interval.getAttribute().setKey(node.getSUID(), "node_Y_Pos");
		nodePositionsTree.insert(interval, node.getSUID());
	}
	
	@Override
	public synchronized void insertNodePositionZ(CyNode node, DynInterval<T> interval)
	{
		this.node_Z_Pos.addInterval(interval);
		interval.getAttribute().setKey(node.getSUID(), "node_Z_Pos");
		nodePositionsTree.insert(interval, node.getSUID());
	}
	
	@Override
	public synchronized void removeNodePositionX(CyNode node, DynInterval<T> interval)
	{
		this.node_X_Pos.removeInterval(interval);
		nodePositionsTree.remove(interval, node.getSUID());
	}
	
	@Override
	public synchronized void removeNodePositionY(CyNode node, DynInterval<T> interval)
	{
		this.node_Y_Pos.removeInterval(interval);
		nodePositionsTree.remove(interval, node.getSUID());
	}
	
	@Override
	public synchronized void removeNodePositionZ(CyNode node, DynInterval<T> interval)
	{
		this.node_Z_Pos.removeInterval(interval);
		nodePositionsTree.remove(interval, node.getSUID());
	}
	
	@Override
	public synchronized void removeAllIntervals()
	{
		nodePositionsTree.clear();
		this.node_X_Pos.clear();
		this.node_Y_Pos.clear();
		this.node_Z_Pos.clear();
		currentNodes.clear();
	}
	
	@Override
	public List<DynInterval<T>> getIntervals(CyNode node)
	{
		return nodePositionsTree.getIntervals(node.getSUID());
	}
	
	@Override
	public List<DynInterval<T>> searchNodePoistions(DynInterval<T> interval)
	{
		return nodePositionsTree.search(interval);
	}
	
	@Override
	public List<DynInterval<T>> searchNodePoistionsNot(DynInterval<T> interval)
	{
		return nodePositionsTree.searchNot(interval);
	}
	
	@Override
	public List<DynInterval<T>> searchChangedNodePositions(DynInterval<T> interval)
	{
		List<DynInterval<T>> tempList = nodePositionsTree.search(interval);
		List<DynInterval<T>> changedList = nonOverlap(currentNodes, tempList);
		currentNodes = tempList;
		return changedList;
	}

	@Override
	public void updateView() 
	{
		view.updateView();
	}

	@Override
	public DynNetwork<T> getNetwork() 
	{
		return this.dynNetwork;
	}
	
	@Override
	public CyNetworkView getNetworkView() 
	{
		return this.view;
	}

	@Override
	public double getCurrentTime() 
	{
		return currentTime;
	}

	@Override
	public void setCurrentTime(double currentTime) 
	{
		this.currentTime = currentTime;
	}	
	
	private List<DynInterval<T>> nonOverlap(List<DynInterval<T>> list1, List<DynInterval<T>> list2) 
	{
		List<DynInterval<T>> diff = new ArrayList<DynInterval<T>>();
		for (DynInterval<T> i : list1)
			if (!list2.contains(i))
				diff.add(i);
		for (DynInterval<T> i : list2)
			if (!list1.contains(i))
				diff.add(i);
		return diff;
	}
}

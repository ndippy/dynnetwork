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

package org.cytoscape.dyn.internal.view.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.dyn.internal.model.DynNetwork;
import org.cytoscape.dyn.internal.view.model.DynNetworkView;
import org.cytoscape.dyn.internal.view.model.DynNetworkViewManager;
import org.cytoscape.dyn.internal.view.task.BlockingQueue;
import org.cytoscape.dyn.internal.view.task.DynNetworkViewTask;
import org.cytoscape.dyn.internal.view.task.DynNetworkViewTaskGroup;
import org.cytoscape.dyn.internal.view.task.DynNetworkViewTaskIterator;
import org.cytoscape.dyn.internal.view.task.DynNetworkViewTransparencyTask;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/**
 * <code> DynCytoPanel </code> implements the a JPanel component in {@link CytoPanel} 
 * west to provide a time slider for controlling the dynamic visualization.
 * 
 * @author sabina
 *
 * @param <T>
 * @param <C>
 */
public final class DynCytoPanel<T,C> extends JPanel implements CytoPanelComponent, 
ChangeListener, ActionListener, SetCurrentNetworkViewListener, GroupCollapsedListener
//VisualStyleSetListener
{
	private static final long serialVersionUID = 1L;
	
	private final TaskManager<T,C> taskManager;
	private final BlockingQueue queue;
	private final CyApplicationManager appManager;
	private final DynNetworkViewManager<T> viewManager;
	
	private DynNetwork<T> network;
	private DynNetworkView<T> view;
	
	private double time;
	private double minTime;
	private double maxTime;
	private volatile int visibility = 0;
	private volatile boolean valueIsAdjusting = false;
	
	private int sliderMax;
	private DynNetworkViewTaskIterator<T,C> recursiveTask;

	private JPanel buttonPanel;
	private JPanel dynVizPanel;
	private JPanel featurePanel;
	private JPanel measurePanel;
	private JLabel currentTime;
	private JLabel nodeNumber;
	private JLabel edgeNumber;
	private JSlider slider;
	private JComboBox resolutionComboBox;
	private JButton forwardButton, backwardButton,stopButton;
	private JCheckBox seeAllCheck;
	private Hashtable<Integer, JLabel> labelTable;
	private DecimalFormat formatter,formatter2;

	public DynCytoPanel(
			final TaskManager<T,C> taskManager,
			final CyApplicationManager appManager,
			final DynNetworkViewManager<T> viewManager)
	{
		super();
		this.taskManager = taskManager;
		this.appManager = appManager;
		this.viewManager = viewManager;
		this.queue = new BlockingQueue();
		initComponents();
	}

	@Override
	public synchronized void stateChanged(ChangeEvent event)
	{
		if (event.getSource() instanceof JSlider)
			if (view!=null)
			{
				time = slider.getValue()*((maxTime-minTime)/sliderMax)+(minTime);
				currentTime.setText("Current time = " + formatter.format(time));
				if (!valueIsAdjusting)
					updateView();
			}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			if (recursiveTask!=null)
				recursiveTask.cancel();
			
			JButton source = (JButton)event.getSource();
			if (source.equals(forwardButton))
			{
				recursiveTask = new DynNetworkViewTaskIterator<T,C>(
						slider, +1, this, view, network, queue);
				new Thread(recursiveTask).start();
			}
			else if (source.equals(backwardButton))
			{
				recursiveTask = new DynNetworkViewTaskIterator<T,C>(
						slider, -1, this, view, network, queue);
				new Thread(recursiveTask).start();
			}
		}
		else if (event.getSource() instanceof JCheckBox)
		{
			JCheckBox source = (JCheckBox)event.getSource();
			if (source.isSelected())
				this.visibility = 30;
			else
				this.visibility = 0;
			if (!valueIsAdjusting)
				updateTransparency();
		}
		else if (event.getSource() instanceof JComboBox)
		{
			JComboBox source = (JComboBox)event.getSource();
			updateGui((double) slider.getValue()/sliderMax, ((NameIDObj)source.getSelectedItem()).id);
			if (!valueIsAdjusting)
				updateView();
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) 
	{
		if (recursiveTask!=null)
			recursiveTask.cancel();

		if (e.getNetworkView()!=null)
		{
			if (view!=null)
				view.setCurrentTime((double) slider.getValue()/sliderMax);
			
			view = viewManager.getDynNetworkView(e.getNetworkView());
			
			if (view!=null)
			{
				network = view.getNetwork();
				updateGui(view.getCurrentTime(), ((NameIDObj)resolutionComboBox.getSelectedItem()).id);
				updateView();
			}
		}
	}
	
	@Override
	public void handleEvent(GroupCollapsedEvent e)
	{
		if (recursiveTask!=null)
			recursiveTask.cancel();
		
		if (view!=null)
			updateGroup((CyGroup) e.getSource());
	}
	

//	@Override
//	public void handleEvent(VisualStyleSetEvent e)
//	{
//		visualStyle = e.getSource().getCurrentVisualStyle();
//	}
	
	public void reset() 
	{
		view = viewManager.getDynNetworkView(appManager.getCurrentNetworkView());
		if (view!=null)
		{
			network = view.getNetwork();
			initTransparency();
			updateGui(view.getCurrentTime(), ((NameIDObj)resolutionComboBox.getSelectedItem()).id);
			updateView();
		}
	}
	
	public double getMinTime() 
	{
		return minTime;
	}

	public double getMaxTime() 
	{
		return maxTime;
	}

	public int getVisibility() 
	{
		return visibility;
	}

	public int getSliderMax() 
	{
		return sliderMax;
	}

	public Component getComponent() 
	{
		return this;
	}

	public CytoPanelName getCytoPanelName() 
	{
		return CytoPanelName.WEST;
	}

	public String getTitle() 
	{
		return "Dynamic Network";
	}

	public Icon getIcon() 
	{
		return null;
	}
	
	public void setNodes(int nodes) 
	{
		nodeNumber.setText("Current nodes = " + formatter2.format(nodes) + "/" + formatter2.format(network.getNetwork().getNodeCount()));
	}

	public void setEdges(int edges) 
	{
		edgeNumber.setText("Current edges = " + formatter2.format(edges) + "/" + formatter2.format(network.getNetwork().getEdgeCount()));
	}

	public void setValueIsAdjusting(boolean valueIsAdjusting)
	{
		this.valueIsAdjusting = valueIsAdjusting;
	}
	
	private void initComponents()
	{
		formatter = new DecimalFormat("#0.000");
		currentTime = new JLabel("Current time = ");
		
		slider = new JSlider(JSlider.HORIZONTAL,0, 100, 0);
		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer( 0 ),new JLabel(formatter.format(Double.NEGATIVE_INFINITY)) );
		labelTable.put(new Integer( 100 ),new JLabel(formatter.format(Double.POSITIVE_INFINITY)) );
		slider.setLabelTable(labelTable);
		slider.setMajorTickSpacing(25);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(this);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		forwardButton = new JButton("Play >>");
		stopButton = new JButton("Stop");
		backwardButton = new JButton("<< Play");
		forwardButton.addActionListener(this);
		stopButton.addActionListener(this);
		backwardButton.addActionListener(this);
		buttonPanel.add(backwardButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(forwardButton);
		
		dynVizPanel = new JPanel();
		dynVizPanel.setLayout(new GridLayout(3,1));
		dynVizPanel.add(currentTime);
		dynVizPanel.add(slider);
		dynVizPanel.add(buttonPanel);
		
		NameIDObj[] items = { 
				new NameIDObj(10,"1/10"), 
				new NameIDObj(100,"1/100"), 
				new NameIDObj(1000,"1/1000"), 
				new NameIDObj(10000,"1/10000") };
		resolutionComboBox  = new JComboBox(items);
		resolutionComboBox.setSelectedIndex(1);
		resolutionComboBox.addActionListener(this);
		
		seeAllCheck = new JCheckBox("Display all",false);
		seeAllCheck.addActionListener(this);
		
		featurePanel = new JPanel();
		featurePanel.setLayout(new GridLayout(3,1));
		featurePanel.add(new JLabel("Time resolution"));
		featurePanel.add(resolutionComboBox);
		featurePanel.add(seeAllCheck);
		
		formatter2 = new DecimalFormat("#0");
		nodeNumber = new JLabel    ("Current nodes = ");
		edgeNumber = new JLabel    ("Current edges = ");
		
		measurePanel = new JPanel();
		measurePanel.setLayout(new GridLayout(10,1));
		measurePanel.add(nodeNumber);
		measurePanel.add(edgeNumber);
		
		dynVizPanel
		.setBorder(BorderFactory.createTitledBorder(null,
				"Dynamic Visualization",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("SansSerif", 1, 12),
				Color.darkGray));
		
		featurePanel
		.setBorder(BorderFactory.createTitledBorder(null,
				"Options",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("SansSerif", 1, 12),
				Color.darkGray));
		
		measurePanel
		.setBorder(BorderFactory.createTitledBorder(null,
				"Metrics",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("SansSerif", 1, 12),
				Color.darkGray));
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				   layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				           .addComponent(dynVizPanel, GroupLayout.DEFAULT_SIZE,
				        		   280 , Short.MAX_VALUE)
				           .addComponent(featurePanel, GroupLayout.DEFAULT_SIZE,
				        		   280, Short.MAX_VALUE)
				           .addComponent(measurePanel, GroupLayout.DEFAULT_SIZE,
				        		   280, Short.MAX_VALUE)
				);
				layout.setVerticalGroup(
				   layout.createSequentialGroup()
				      .addComponent(dynVizPanel, 150,
				    		  GroupLayout.DEFAULT_SIZE, 270)
				      .addComponent(featurePanel,  GroupLayout.DEFAULT_SIZE,
				    		  150 , Short.MAX_VALUE)
				      .addComponent(measurePanel, GroupLayout.DEFAULT_SIZE,
				    		   400, Short.MAX_VALUE)
				);

		this.setVisible(true);
	}
	
	private void initTransparency()
	{
		for (final View<CyNode> nodeView : view.getNetworkView().getNodeViews())
		{
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, visibility);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, visibility);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, visibility);
			
//			nodeView.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, visibility);
//			nodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, visibility);
//			nodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, visibility);
		}
		
		for (final View<CyEdge> edgeView : view.getNetworkView().getEdgeViews())
		{
//			edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY, visibility);
//			edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, visibility);
			
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, visibility);
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, visibility);
		}
	}
	
	private void updateView()
	{
		if (time==this.network.getMaxTime())
			taskManager.execute(new TaskIterator(1,new DynNetworkViewTask<T,C>(
					this, view, network, queue, time-0.0000001, time+0.0000001, visibility)));
		else
			taskManager.execute(new TaskIterator(1,new DynNetworkViewTask<T,C>(
					this, view, network, queue, time, time, visibility)));
	}
	
	private void updateGroup(CyGroup group)
	{
		if (time==maxTime)
			taskManager.execute(new TaskIterator(1,new DynNetworkViewTaskGroup<T>(
					view, network, queue, time-0.0000001, time+0.0000001, visibility, group)));
		else
			taskManager.execute(new TaskIterator(1,new DynNetworkViewTaskGroup<T>(
					view, network, queue, time, time, visibility, group)));
	}
	
	private void updateTransparency()
	{
		if (time==maxTime)
			taskManager.execute(new TaskIterator(1,new DynNetworkViewTransparencyTask<T>(
					view, network, queue, time-0.0000001, time+0.0000001, visibility)));
		else
			taskManager.execute(new TaskIterator(1,new DynNetworkViewTransparencyTask<T>(
					view, network, queue, time, time, visibility)));
	}
	
	private void updateGui(double absoluteTime, int value)
	{
		minTime = network.getMinTime();
		maxTime = network.getMaxTime();
		sliderMax = value;
		slider.setMaximum(value);
		slider.setValue((int) (absoluteTime*(double) sliderMax));
		
		time = slider.getValue()*((maxTime-minTime)/sliderMax)+(minTime);
		currentTime.setText("Current time = " + formatter.format(time));
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				labelTable.clear();
				labelTable.put(new Integer( 0 ),new JLabel(formatter.format(minTime)) );
				labelTable.put(new Integer( sliderMax ),new JLabel(formatter.format(maxTime)) );
				slider.setMaximum(sliderMax);
				slider.setLabelTable(labelTable);
				slider.setMajorTickSpacing((int) (0.5*sliderMax));
				slider.setMinorTickSpacing((int) (0.1*sliderMax));
				slider.setPaintTicks(true);
				slider.setPaintLabels(true);
			}
		});
	}

}

final class NameIDObj
{
	int id;
	String name;

	NameIDObj(int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public String toString()
	{
		return name;
	}
}


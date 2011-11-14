/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elijahmeeks.tubemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;

import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

//New Stuff
import java.io.File;
import java.io.IOException;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;


import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;

import java.util.HashMap;

import org.gephi.graph.api.*;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;

import org.gephi.ui.propertyeditor.NodeColumnNumbersEditor;

/**
 *
 * @author elijahmeeks
 */
public class TubeMapLayout extends AbstractLayout implements Layout {

    private Graph graph;
    private boolean converged;
    
    //////////////
    
                public static final String NETDISTANCE = "netdistance";
            private double centerX = 0.0;
            private double centerY = 0.0;
            private double distanceScale = 1000.0;
            private boolean useXY = false;
            private boolean logDistance = true;
            private String targetID = "";
            private AttributeColumn xCoord;
            private AttributeColumn yCoord;
            private boolean suitableTarget = false;

            //////////////////

    public TubeMapLayout(LayoutBuilder layoutBuilder, double size) {
        super(layoutBuilder);
    }

    public void initAlgo() {
        converged = false;
        graph = graphModel.getGraphVisible();
    }

    public void goAlgo() {
        graph = graphModel.getGraphVisible();
///////////////////////////////////////////////////////

        AttributeModel attributeModel = ((AttributeController) Lookup.getDefault().lookup(AttributeController.class)).getModel();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        
        if (gc.getModel().getGraphVisible() instanceof DirectedGraph) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn distanceCol = nodeTable.getColumn(NETDISTANCE);
        if (distanceCol == null) {
            distanceCol = nodeTable.addColumn(NETDISTANCE, "NetDistance", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

                HashMap<Integer, Node> indicies = new HashMap<Integer, Node>();

                int targetNode = 0;
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(index, s);
            if (s.getAttributes().getValue("Label").equals(targetID)) {
                suitableTarget = true;
                targetNode = index;
                if (useXY == false){
                    centerX = Double.valueOf(s.getAttributes().getValue(xCoord.getTitle()).toString());
                    centerY = Double.valueOf(s.getAttributes().getValue(yCoord.getTitle()).toString());
                }
                else {
                    centerX = Double.valueOf(s.getNodeData().x());
                    centerY = Double.valueOf(s.getNodeData().y());
                }
            }
            index++;
        }
        
        if (suitableTarget == true){

                            AbstractShortestPathAlgorithm algorithm;
                    if (gc.getModel().getGraphVisible() instanceof DirectedGraph) {
                        DirectedGraph pgraph = (DirectedGraph) gc.getModel().getGraphVisible();
                        algorithm = new BellmanFordShortestPathAlgorithm(pgraph, indicies.get(targetNode));
                        algorithm.compute();
                    } else {
                        Graph pgraph = gc.getModel().getGraphVisible();
                        algorithm = new DijkstraShortestPathAlgorithm(pgraph, indicies.get(targetNode));
                        algorithm.compute();
                    }
                    
                    HashMap<Node, Double> distanceHash = new HashMap<Node, Double>();                    
                    distanceHash = algorithm.getDistances();
                    
                    
                    for (Node s : graph.getNodes()) {
                        AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                        if(Double.isInfinite(distanceHash.get(s))) {
                            row.setValue(distanceCol, -999d);
                            distanceHash.put(s, (algorithm.getMaxDistance() * 1.1));
                        }
                        else{
                            row.setValue(distanceCol, new Double (distanceHash.get(s)));
                        }
                        double targetX = 999;
                        double targetY = 999;
                                
                if (useXY == false){
                        targetX = Double.valueOf(s.getAttributes().getValue(xCoord.getTitle()).toString());
                        targetY = Double.valueOf(s.getAttributes().getValue(yCoord.getTitle()).toString());
                }
                else {
                    targetX = Double.valueOf(s.getNodeData().x());
                    targetY = Double.valueOf(s.getNodeData().y());
                }

                
                        double distanceFrom = (distanceHash.get(s) / algorithm.getMaxDistance()) * distanceScale;
                        
                        if (logDistance == true) {
                            distanceFrom = Math.log(distanceHash.get(s))  * distanceScale;
                        }
                        
                        if(Double.isInfinite(distanceFrom)) {
                            distanceFrom = algorithm.getMaxDistance() * 2;
                        }

                            double differenceX = targetX - centerX;
                            double differenceY = targetY - centerY;
                        
                        //tranlsate latlong to pixels
                        
                        double initialHypotenuse = (Math.sqrt(Math.pow(differenceX, 2.0) + Math.pow(differenceY, 2.0)));
                        
                        //imagine a distanceFrom value of 600 and an initial hypotenuse of 5 degrees (500)
                        
                        double ratio = (distanceFrom / initialHypotenuse);
                        
                        differenceX = differenceX * ratio;
                        differenceY = differenceY * ratio;
                        
                        s.getNodeData().setX((float) (differenceY));
                        s.getNodeData().setY((float) (differenceX));
                        
                        if (distanceHash.get(s) == 0){
                            s.getNodeData().setX(0f);
                            s.getNodeData().setY(0f);
                        }

                        
                    }
        }
                    suitableTarget = false;
                    converged = true;

    }

    @Override
    public boolean canAlgo() {
        if(useXY == false) {
            return !converged && xCoord != null && yCoord != null;
        }
        else {
            return !converged;
        }
    }

    public void endAlgo() {
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class, 
                    NbBundle.getMessage(getClass(), "TubeMap.distanceScale.name"),
                    null,
                    "TubeMap.distanceScale.name",
                    NbBundle.getMessage(getClass(), "TubeMap.distanceScale.desc"),
                    "getSize", "setSize"));
            
            properties.add(LayoutProperty.createProperty(
                    this, String.class, 
                    NbBundle.getMessage(getClass(), "TubeMap.targetID.name"),
                    null,
                    "TubeMap.targetID.name",
                    NbBundle.getMessage(getClass(), "TubeMap.targetID.desc"),
                    "getTargetNode", "setTargetNode"));

            properties.add(LayoutProperty.createProperty(
                    this, boolean.class, 
                    NbBundle.getMessage(getClass(), "TubeMap.useXY.name"),
                    null,
                    "TubeMap.useXY.name",
                    NbBundle.getMessage(getClass(), "TubeMap.useXY.desc"),
                    "getUseXY", "setUseXY"));

            properties.add(LayoutProperty.createProperty(
                    this, boolean.class, 
                    NbBundle.getMessage(getClass(), "TubeMap.logMode.name"),
                    null,
                    "TubeMap.logMode.name",
                    NbBundle.getMessage(getClass(), "TubeMap.logMode.desc"),
                    "getLogMode", "setLogMode"));
            
            properties.add(LayoutProperty.createProperty(
                    this, AttributeColumn.class,
                    NbBundle.getMessage(getClass(), "TubeMap.xCoord.name"),
                    null,
                    NbBundle.getMessage(getClass(), "TubeMap.xCoord.desc"),
                    "getXCoord", "setXCoord", NodeColumnNumbersEditor.class));

            properties.add(LayoutProperty.createProperty(
                    this, AttributeColumn.class,
                    NbBundle.getMessage(getClass(), "TubeMap.yCoord.name"),
                    null,
                    NbBundle.getMessage(getClass(), "TubeMap.yCoord.desc"),
                    "getYCoord", "setYCoord", NodeColumnNumbersEditor.class));
            
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    public void resetPropertiesValues() {
    }

    public void setXCoord(AttributeColumn XCoord) {
        this.xCoord = XCoord;
    }

    public AttributeColumn getXCoord() {
        return this.xCoord;
    }

    public void setYCoord(AttributeColumn YCoord) {
        this.yCoord = YCoord;
    }

    public AttributeColumn getYCoord() {
        return this.yCoord;
    }

    
    public void setSize(Double size) {
        this.distanceScale = size;
    }
    
    public Double getSize() {
        return distanceScale;
    }
    
    public void setTargetNode(String nodeName) {
        this.targetID = nodeName;
    }

    public String getTargetNode() {
        return targetID;
    }
        
    public void setUseXY(boolean XY) {
        this.useXY = XY;
    }

    public boolean getUseXY() {
        return useXY;
    }

        public void setLogMode(boolean logMode) {
        this.logDistance = logMode;
    }

    public boolean getLogMode() {
        return logDistance;
    }

    
}

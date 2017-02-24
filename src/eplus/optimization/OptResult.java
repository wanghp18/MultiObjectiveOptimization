package eplus.optimization;

import java.util.ArrayList;

public class OptResult {
	
	double firstcost;
	double operationcost;
	ArrayList<String> componentSelection;
	
	public OptResult(){
		firstcost = 0.0;
		operationcost = 0.0;
		componentSelection = new ArrayList<String>();
	}
	
	public void setFirstCost(double first){
	    firstcost = first;
	}
	
	public void setOperationCost(double operation){
	    operationcost = operation;
	}
	
	public double getFirstCost(){
		return firstcost;
	}
	
	public double getOperationCost(){
		return operationcost;
	}
	
	public int getComponentLength(){
	    return componentSelection.size();
	}
	
	public String getComponent(int index){
	    return componentSelection.get(index);
	}
	
	public void addComponent(String component){
	    componentSelection.add(component);
	}
	
	public boolean equals(OptResult another){
	    int size = componentSelection.size();
	    for(int i=0; i<size; i++){
		if(!componentSelection.get(i).equals(another.getComponent(i))){
		    return false;
		}
	    }
	    return true;
	}
}

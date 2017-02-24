package eplus.optimization;

import java.util.ArrayList;
import java.util.List;

public class OptResultSet {
	List<OptResult> resultSet;
	//private Integer TOP = 3;
	
	public OptResultSet(){
		resultSet = new ArrayList<OptResult>();
	}
	
	public int getSize(){
	    return resultSet.size();
	}
	
	public void addResultSet(OptResult r){
		resultSet.add(r);
	}
	
	public List<OptResult> getResultSet(){
		return resultSet;
	}
	
	public OptResult getResult(int index){
	    return resultSet.get(index);
	}
	
	public void cleanResults(){
		resultSet.clear();
	}
}

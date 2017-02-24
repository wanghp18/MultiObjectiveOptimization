package test.servlet;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;

import engine.OptimizationMain;
import jmetal.util.JMException;

public class mainTest {
    
    public static void main(String[] args) throws ClassNotFoundException, JMException{
	OptimizationMain main = new OptimizationMain("ZDT1","NSGAII");
	
	HashMap<String, ArrayList<Double>> results = main.getResults();
	
	ArrayList<Double> obj1 = results.get("Obj1");
	ArrayList<Double> obj2 = results.get("Obj2");
	Double time = results.get("Time").get(0);
	System.out.println(time);
	
	JsonObject res = new JsonObject();
	JsonObject jObj1 = new JsonObject();
	JsonObject jObj2 = new JsonObject();
	
	for(int i=0; i<obj1.size(); i++){
	    //System.out.println("This is object 1 value: " + obj1.get(i) + " This is object 2 value: " + obj2.get(i));
	    jObj1.addProperty(i+"", obj1.get(i));
	    jObj2.addProperty(i+"", obj2.get(i));
	}
	res.add("Obj1", jObj1);
	res.add("Obj2", jObj2);
	System.out.println(res.toString());
    }

}

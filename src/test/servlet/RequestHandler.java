package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import engine.OptimizationMain;
import jmetal.util.JMException;

public class RequestHandler extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 8331757816988864444L;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	//JsonObject ret = new JsonObject();

	String problem = req.getParameter("problem");
	String algorithm = req.getParameter("algorithm");

	try {
	    OptimizationMain engine = new OptimizationMain(problem, algorithm);
	    HashMap<String, ArrayList<Double>> results = engine.getResults();

	    ArrayList<Double> obj1 = results.get("Obj1");
	    ArrayList<Double> obj2 = results.get("Obj2");
	    ArrayList<Double> timelist = results.get("Time");
	    
	    // DataTable data = new DataTable();
	    JsonObject res = new JsonObject();
	    JsonObject jObj1 = new JsonObject();
	    JsonObject jObj2 = new JsonObject();
	    JsonObject timeObj = new JsonObject();

	    for (int i = 0; i < obj1.size(); i++) {
		// System.out.println("This is object 1 value: " + obj1.get(i) +
		// " This is object 2 value: " + obj2.get(i));
		jObj1.addProperty(i + "", obj1.get(i));
		jObj2.addProperty(i + "", obj2.get(i));
	    }
	    
	    timeObj.addProperty("Time", timelist.get(0));
	    res.add("Obj1", jObj1);
	    res.add("Obj2", jObj2);
	    res.add("Time", timeObj);

	    resp.setContentType("json");
	    PrintWriter pw = resp.getWriter();
	    pw.print(res);
	    pw.flush();
	    pw.close();

	} catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (JMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}

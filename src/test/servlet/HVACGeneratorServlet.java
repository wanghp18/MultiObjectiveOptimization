package test.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import eplus.util.EnergyPlusData;

public class HVACGeneratorServlet extends HttpServlet{

    /**
     * 
     */
    private static final long serialVersionUID = 406139086181785612L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
	String path = request.getParameter("path");
	String sys = request.getParameter("hvac");
	response.setContentType("application/octet-stream");
	response.setHeader("Content-Disposition", "attachment;filename=HVAC_"+sys+".idf");
	File eplus = new File(path);
	FileInputStream fileIn = new FileInputStream(eplus);
	
	ServletOutputStream sos = response.getOutputStream();
	byte[] outputByte = new byte[4096];
	
	int c = 0;
	while((c=fileIn.read(outputByte,0,4096))!= -1){
	    sos.write(outputByte,0,c);
	}
	sos.flush();
	sos.close();
	fileIn.close();
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
	String hvac = request.getParameter("hvac");
	System.out.println(hvac);
	
	EnergyPlusData.replaceHVACSystem(hvac);

	JsonObject res = new JsonObject();
	res.addProperty("path", EnergyPlusData.resultFolder+"/" + EnergyPlusData.fileCounter + ".idf");
	response.setContentType("json");
	PrintWriter pw = response.getWriter();
	pw.print(res);
	pw.flush();
	pw.close();
    }

}

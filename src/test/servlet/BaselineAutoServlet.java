package test.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.JsonObject;

import engine.BaselineAutomationEngine;

public class BaselineAutoServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -5901159093028417299L;
    //private static final String UPLOAD_DIRECTORY = "upload";

    // upload settings
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 10; // 10 MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 50; // 50 MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50;// 50 MB
    
    //IDF file
    private static String baselinePath = "none";

    @Override
    protected void doGet(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {
	//get file
	String path = request.getParameter("path");
	response.setContentType("application/octet-stream");
	response.setHeader("Content-Disposition", "attachment;filename=Baseline.idf");
	File eplus = new File(path);
	FileInputStream fileIn = new FileInputStream(eplus);
	
	ServletOutputStream sos = response.getOutputStream();
	
	byte[] outputByte = new byte[4096];
	
	int c=0;
	while((c=fileIn.read(outputByte,0,4096))!=-1){
	    sos.write(outputByte,0,c);
	}
	sos.flush();
	sos.close();
	fileIn.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
	// checks if the request actually contains upload file
	if (!ServletFileUpload.isMultipartContent(request)) {
	    PrintWriter writer = response.getWriter();
	    if(request.getParameter("message").equals("download")){
		JsonObject res = new JsonObject();
		res.addProperty("path", baselinePath);
		response.setContentType("json");
		writer.print(res);
	    }else{
		writer.println("Error: Form must has enctype=multipart/form-data.");
	    }
	    writer.flush();
	    writer.close();
	    return;
	}
	// configures upload settings
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// sets memory threshold - beyond which files are stored in disk
		factory.setSizeThreshold(MEMORY_THRESHOLD);
		// sets temporary location to store files
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

		ServletFileUpload upload = new ServletFileUpload(factory);
		// sets maximum size of upload file
		upload.setFileSizeMax(MAX_FILE_SIZE);
		// sets maximum size of request (include file + form data)
		upload.setSizeMax(MAX_REQUEST_SIZE);

		// constructs the directory path to store upload file
		// this path is relative to application's directory
		String uploadPath = getServletContext().getRealPath("") + File.separator
			+ getRandom();
		System.out.println(uploadPath);
		baselinePath = uploadPath + File.separator + "Baseline_0.idf";
		// creates the directory if it does not exist
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
		    uploadDir.mkdirs();
		}
		try {
		    // parses the request's content to extract file data
		    List<FileItem> formItems = upload.parseRequest(request);
		    File idf = null;
		    String climate = null;
		    if (formItems != null && formItems.size() > 0) {
			// iterates over form's fields
			for (FileItem item : formItems) {
			    // processes only fields that are not form fields
			    if (!item.isFormField()) {
				String fileName = new File(item.getName()).getName();
				String filePath = uploadPath + File.separator
					+ fileName;
				System.out.println(filePath);
				File storeFile = new File(filePath);

				// saves the file on disk
				item.write(storeFile);
				request.setAttribute("message",
					"Upload has been done successfully!");
				if(fileName.endsWith(".idf")){
				    idf = storeFile;
				    
				}
			    }else{
				if(item.getFieldName().equals("Select Climate")){
				    climate = item.getString();
				    System.out.println(climate);
				}
			    }
			}
		    }
		    
		    BaselineAutomationEngine engine = new BaselineAutomationEngine(idf, climate);
		    JsonObject info = engine.getBaselineInfo();
		    response.setContentType("json");
		    PrintWriter pw = response.getWriter();
		    pw.print(info);
		    pw.flush();
		    pw.close();
		}catch(Exception ex){
		    request.setAttribute("message", "There was an error: " + ex.getMessage());
		}
    }
    

    private String getRandom() {
	String plainText = System.currentTimeMillis() + "" + Math.random();
	MessageDigest mdAlgorithm = null;
	try {
	    mdAlgorithm = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
	    e.printStackTrace();
	    return plainText;
	}
	mdAlgorithm.update(plainText.getBytes());
	byte[] digest = mdAlgorithm.digest();
	StringBuffer hexString = new StringBuffer();
	for (int i = 0; i < digest.length; i++) {
	    plainText = Integer.toHexString(0xFF & digest[i]);
	    if (plainText.length() < 2) {
		plainText = "0" + plainText;
	    }
	    hexString.append(plainText);
	}
	return hexString.toString();
    }

}

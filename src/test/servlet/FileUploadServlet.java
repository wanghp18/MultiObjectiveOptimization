package test.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eplus.data.ThermalZone;
import eplus.util.EnergyPlusData;

/**
 * File Upload Servlet receives request method return list of zones and their
 * properties
 * 
 * @author Weili
 *
 */
public class FileUploadServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6297071817277436365L;
    private static final String UPLOAD_DIRECTORY = "upload";

    // upload settings
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 10; // 10 MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 50; // 50 MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50;// 50 MB

    /**
     * Upon receiving file upload submission, parses the request to read upload
     * data and saves the file on disk
     */
    @Override
    protected void doPost(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {
	// checks if the request actually contains upload file
	if (!ServletFileUpload.isMultipartContent(request)) {
	    // if not, we stop here
	    PrintWriter writer = response.getWriter();
	    writer.println("Error: Form must has enctype=multipart/form-data.");
	    writer.flush();
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
		+ UPLOAD_DIRECTORY;
	System.out.println(uploadPath);

	// creates the directory if it does not exist
	File uploadDir = new File(uploadPath);
	if (!uploadDir.exists()) {
	    uploadDir.mkdirs();
	}
	try {
	    // parses the request's content to extract file data
	    List<FileItem> formItems = upload.parseRequest(request);
	    File idf = null;
	    File html = null;
	    if (formItems != null && formItems.size() > 0) {
		// iterates over form's fields
		for (FileItem item : formItems) {
		    // processes only fields that are not form fields
		    if (!item.isFormField()) {
			String fileName = new File(item.getName()).getName();
			String filePath = uploadPath + File.separator
				+ fileName;
			// System.out.println(filePath);
			File storeFile = new File(filePath);

			// saves the file on disk
			item.write(storeFile);
			request.setAttribute("message",
				"Upload has been done successfully!");
			if (fileName.endsWith(".html")) {
			    html = storeFile;
			} else if (fileName.endsWith(".idf")) {
			    idf = storeFile;
			}
		    }
		}
	    }
	    EnergyPlusData.setUserEplusFile(idf);
	    EnergyPlusData.setUpHVACBldg(html);
	    List<ThermalZone> zonelist = EnergyPlusData
		    .getHVACBldgThermalZoneList();
	    JsonObject data = processThermalZoneinJson(zonelist);
	    JsonObject dataForTable = processThermalZoneinJsonFormat(zonelist);
	    JsonObject dataAll = new JsonObject();
	    dataAll.add("google", data);
	    dataAll.add("own", dataForTable);
	    response.setContentType("json");
	    PrintWriter pw = response.getWriter();
	    pw.print(dataAll);
	    pw.flush();
	    pw.close();

	} catch (Exception ex) {
	    request.setAttribute("message",
		    "There was an error: " + ex.getMessage());
	}
    }

    private JsonObject processThermalZoneinJsonFormat(
	    List<ThermalZone> zonelist) {
	JsonObject ret = new JsonObject();
	JsonArray zoneNameList = new JsonArray();

	for (ThermalZone temp : zonelist) {

	    JsonObject zone = new JsonObject();
	    zone.addProperty("name", temp.getFullName());
	    zone.addProperty("block", temp.getBlock());
	    zone.addProperty("type", temp.getZoneType());
	    zone.addProperty("floor", temp.getFloor());
	    zone.addProperty("zoneid", temp.getZoneIdentification());
	    zone.addProperty("hvac", temp.getZoneCoolHeat());
	    zone.addProperty("vent", temp.getZoneVent());
	    zoneNameList.add(zone);

	}

	ret.add("zonename", zoneNameList);
	JsonArray zonePropertyList = new JsonArray();

	for (ThermalZone temp : zonelist) {
	    JsonObject zoneproperty = new JsonObject();
	    zoneproperty.addProperty("coolload", temp.getCoolingLoad());
	    zoneproperty.addProperty("heatload", temp.getHeatingLoad());
	    zoneproperty.addProperty("coolair", temp.getCoolingAirFlow());
	    zoneproperty.addProperty("heatair", temp.getHeatingAirFlow());
	    zoneproperty.addProperty("area", temp.getZoneArea());
	    zoneproperty.addProperty("grosswall", temp.getZoneGrossWallArea());
	    zoneproperty.addProperty("occupant", temp.getZoneOccupants());
	    zoneproperty.addProperty("lpd", temp.getZoneLPD());
	    zoneproperty.addProperty("epd", temp.getZoneEPD());

	    zonePropertyList.add(zoneproperty);

	}
	ret.add("property", zonePropertyList);
	return ret;
    }

    private JsonObject processThermalZoneinJson(List<ThermalZone> zonelist) {
	JsonObject ret = new JsonObject();

	JsonArray cols = new JsonArray();
	// build cols
	cols.add(buildJsonObjectforCol("name", "Zone name", "string"));
	cols.add(buildJsonObjectforCol("block", "Zone block", "string"));
	cols.add(buildJsonObjectforCol("type", "Zone type", "string"));
	cols.add(buildJsonObjectforCol("floor", "Zone floor", "string"));
	cols.add(buildJsonObjectforCol("zoneid", "Zone Id", "string"));
	cols.add(buildJsonObjectforCol("hvac", "Zone HVAC", "string"));
	cols.add(buildJsonObjectforCol("vent", "Zone ventilation", "string"));
	cols.add(buildJsonObjectforCol("coolload", "Zone cooling load",
		"number"));
	cols.add(buildJsonObjectforCol("heatload", "Zone heating load",
		"number"));
	cols.add(buildJsonObjectforCol("coolair", "Zone cooling design airflow",
		"number"));
	cols.add(buildJsonObjectforCol("heatair", "Zone heating design airflow",
		"number"));
	cols.add(buildJsonObjectforCol("area", "Zone floor area", "number"));
	cols.add(buildJsonObjectforCol("grosswall", "Zone gross wall area",
		"number"));
	cols.add(buildJsonObjectforCol("occupant", "Zone occupants", "number"));
	cols.add(buildJsonObjectforCol("lpd", "Zone lighting power density",
		"number"));
	cols.add(buildJsonObjectforCol("epd", "Zone equipment power density",
		"number"));

	ret.add("cols", cols);

	JsonArray rows = new JsonArray();
	for (ThermalZone temp : zonelist) {
	    JsonObject row = new JsonObject();
	    JsonArray rowlist = buildJsonObjectforRowList(temp);
	    row.add("c", rowlist);
	    rows.add(row);
	}
	ret.add("rows", rows);
	return ret;
    }

    private JsonArray buildJsonObjectforRowList(ThermalZone zone) {
	JsonArray rowlist = new JsonArray();
	JsonObject c1 = new JsonObject();
	c1.addProperty("v", zone.getFullName());
	JsonObject c2 = new JsonObject();
	c2.addProperty("v", zone.getBlock());
	JsonObject c3 = new JsonObject();
	c3.addProperty("v", zone.getZoneType());
	JsonObject c4 = new JsonObject();
	c4.addProperty("v", zone.getFloor());
	JsonObject c5 = new JsonObject();
	c5.addProperty("v", zone.getZoneIdentification());
	JsonObject c6 = new JsonObject();
	c6.addProperty("v", zone.getZoneCoolHeat());
	JsonObject c7 = new JsonObject();
	c7.addProperty("v", zone.getZoneVent());
	JsonObject c8 = new JsonObject();
	c8.addProperty("v", zone.getCoolingLoad());
	JsonObject c9 = new JsonObject();
	c9.addProperty("v", zone.getHeatingLoad());
	JsonObject c10 = new JsonObject();
	c10.addProperty("v", zone.getCoolingAirFlow());
	JsonObject c11 = new JsonObject();
	c11.addProperty("v", zone.getHeatingAirFlow());
	JsonObject c12 = new JsonObject();
	c12.addProperty("v", zone.getZoneArea());
	JsonObject c13 = new JsonObject();
	c13.addProperty("v", zone.getZoneGrossWallArea());
	JsonObject c14 = new JsonObject();
	c14.addProperty("v", zone.getZoneOccupants());
	JsonObject c15 = new JsonObject();
	c15.addProperty("v", zone.getZoneLPD());
	JsonObject c16 = new JsonObject();
	c16.addProperty("v", zone.getZoneEPD());
	rowlist.add(c1);
	rowlist.add(c2);
	rowlist.add(c3);
	rowlist.add(c4);
	rowlist.add(c5);
	rowlist.add(c6);
	rowlist.add(c7);
	rowlist.add(c8);
	rowlist.add(c9);
	rowlist.add(c10);
	rowlist.add(c11);
	rowlist.add(c12);
	rowlist.add(c13);
	rowlist.add(c14);
	rowlist.add(c15);
	rowlist.add(c16);
	return rowlist;
    }

    private JsonObject buildJsonObjectforCol(String id, String label,
	    String type) {
	JsonObject col = new JsonObject();
	col.addProperty("id", id);
	col.addProperty("label", label);
	col.addProperty("type", type);
	return col;
    }
}

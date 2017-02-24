package eplus.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import eplus.data.ThermalZone;

public class HVACSizing {

    private static Document doc;

    // All Summary tables
    private static ZoneSummaryParser zoneSummary;
    private static HeatingLoadParser heatingLoad;
    private static CoolingLoadParser coolingLoad;
    private static MechanicalVentilation miniVent;
    private static EndUseParser enduse;
    private static EquipmentSummary equipment;

    public HVACSizing(File html) {
	try {
	    doc = Jsoup.parse(html, "UTF-8");
	    preprocessTable();
	    zoneSummary = new ZoneSummaryParser(doc);
	    heatingLoad = new HeatingLoadParser(doc);
	    coolingLoad = new CoolingLoadParser(doc);
	    enduse = new EndUseParser(doc);
	    miniVent = new MechanicalVentilation(doc);
	    equipment = new EquipmentSummary(doc);
	} catch (IOException e) {
	    // do nothing
	}
    }

    private static void preprocessTable() {
	String report = null;
	Elements htmlNodes = doc.getAllElements();
	for (int i = 0; i < htmlNodes.size(); i++) {
	    if (htmlNodes.get(i).text().contains("Report:")) {
		report = htmlNodes.get(i + 1).text();
	    }
	    if (htmlNodes.get(i).hasAttr("cellpadding")) {
		String tableName = htmlNodes.get(i - 3).text();
		htmlNodes.get(i).attr("tableID", report + ":" + tableName);
	    }
	}
    }

    private static Double getZoneHeatingAirFlow(String zone) {
	String airflow = heatingLoad.getUserDefinedHeatingAirFlow(zone);
	if (airflow.equals("")) {
	    return 0.0;
	} else {
	    return Double.parseDouble(airflow);
	}
    }

    private static Double getZoneCoolingAirFlow(String zone) {
	String airflow = coolingLoad.getUserDefinedCoolingAirFlow(zone);
	if (airflow.equals("")) {
	    return 0.0;
	} else {
	    return Double.parseDouble(airflow);
	}
    }

    /**
     * The method must be called after the output was processed.
     */
    private static Double getZoneHeatingLoad(String zone) {
	String load = heatingLoad.getUserDefinedHeatingLoad(zone);
	if (load.equals("")) {
	    return 0.0;
	} else {
	    return Double.parseDouble(load);
	}
    }

    public List<ThermalZone> processThermalZone(List<ThermalZone> zonelist) {
	List<ThermalZone> tempList = new ArrayList<ThermalZone>();
	for (int i = 0; i < zonelist.size(); i++) {
	    ThermalZone temp = zonelist.get(i);
	    String zoneName = temp.getFullName();
	    if (!zoneName.contains("PLENUM")) {
		double coolLoad = getZoneCoolingLoad(zoneName);
		double heatLoad = getZoneHeatingLoad(zoneName);
		double coolAirFlow = getZoneCoolingAirFlow(zoneName);
		double heatAirFlow = getZoneHeatingAirFlow(zoneName);
		temp.setCoolingLoad(coolLoad);
		temp.setHeaingLoad(heatLoad);
		temp.setCoolingAirFlow(coolAirFlow);
		temp.setHeatingAirFlow(heatAirFlow);
		temp.setZoneArea(getZoneArea(zoneName));
		temp.setZoneGrossWallArea(getZoneGrossWallArea(zoneName));
		temp.setZoneOccupants(getZoneOccupants(zoneName));
		temp.setZoneLPD(getZoneLPD(zoneName));
		temp.setZoneEPD(getZoneEPD(zoneName));
		tempList.add(temp);
	    }
	}
	return tempList;
    }

    /**
     * The method must be called after the output was processed.
     */
    private static Double getZoneCoolingLoad(String zone) {
	String load = coolingLoad.getUserDefinedCoolingLoad(zone);
	if (load.equals("")) {
	    return 0.0;
	} else {
	    return Double.parseDouble(load);
	}
    }

    private static Double getZoneMinimumVentilation(String zone) {
	Double vent = miniVent.getMinimumVentilationRate(zone);
	return vent;
    }

    private static Double getZoneArea(String zone) {
	Double area = zoneSummary.getZoneArea(zone);
	return area;
    }

    private static Double getZoneGrossWallArea(String zone) {
	Double grossWallArea = zoneSummary.getZoneGrossWallArea(zone);
	return grossWallArea;
    }

    private static Double getZoneLPD(String zone) {
	Double lpd = zoneSummary.getZoneLPD(zone);
	return lpd;
    }

    private static Double getZoneOccupants(String zone) {
	Double occupants = zoneSummary.getZoneOccupants(zone);
	return occupants;
    }

    private static Double getZoneEPD(String zone) {
	Double epd = zoneSummary.getZoneEPD(zone);
	return epd;
    }
}

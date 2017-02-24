package eplus.hvac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jsoup.nodes.Document;

import eplus.data.EplusObject;
import eplus.data.KeyValuePair;
import eplus.data.ThermalZone;

public class DOAS implements HVACSystem {
    // recording all the required data for DOAS
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    // building object contains building information and energyplus data
    private EnergyPlusBuildingForHVACSystems building;
    
    private int numOfSupplySystem;
    private int numOfDemandSystem;
    
    public DOAS(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuildingForHVACSystems bldg) {
	objectLists = objects;
	building = bldg;
	processSystems();

    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }

    public void processSystems() {
	ArrayList<EplusObject> supplySideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandSideSystem = new ArrayList<EplusObject>();
	/*
	 * supply side relates to the condenser, typically one condenser link to
	 * several indoor terminal units
	 */
	ArrayList<EplusObject> supplySideSystemTemplate = objectLists
		.get("Supply Side System");
	/*
	 * demand side relates to the evaporator, one zone has one evaporator
	 */
	ArrayList<EplusObject> demandSideSystemTemplate = objectLists
		.get("Demand Side System");
	HashMap<String, ArrayList<ThermalZone>> doasMap = building.getVentilationMap();
	Set<String> doasMapSet = doasMap.keySet();
	Iterator<String> doasMapIterator = doasMapSet.iterator();

	// every zone has one set of the system
	int supplySystemCounter = 0;
	int demandSystemCounter = 0;
	while (doasMapIterator.hasNext()) {
	    supplySystemCounter ++;
	    String doasSys = doasMapIterator.next();
	    ArrayList<ThermalZone> zones = doasMap.get(doasSys);
	    ArrayList<String> zoneNames = new ArrayList<String>();
	    for (ThermalZone zone : zones) {
		demandSystemCounter ++;
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(),
			demandSideSystemTemplate));
		zoneNames.add(zone.getFullName());
	    }
	    supplySideSystem.addAll(processSupplyTemp(doasSys,
		    supplySideSystemTemplate, zoneNames));
	}
	numOfSupplySystem = supplySystemCounter;
	numOfDemandSystem = demandSystemCounter;
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
    }

    /**
     * process the HVAC supply air side system
     * 
     * @param zone
     * @param supplySideSystemTemplate
     * @return
     */
    private ArrayList<EplusObject> processSupplyTemp(String condenser,
	    ArrayList<EplusObject> supplySideSystemTemplate,
	    ArrayList<String> zoneNameList) {
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : supplySideSystemTemplate) {
	    EplusObject temp = eo.clone();

	    /*
	     * replace the special characters that contains floors
	     */
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(condenser);
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName().equalsIgnoreCase(
		    "AirLoopHVAC:ZoneSplitter")) {
		for (String s : zoneNameList) {
		    KeyValuePair splitterPair = new KeyValuePair(
			    "Outlet Node Name", s + " Zone Equip Inlet");
		    temp.addField(splitterPair);
		}
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneMixer")) {
		for (String s : zoneNameList) {
		    KeyValuePair mixerPair = new KeyValuePair(
			    "Intlet Node Name", s + " Return Outlet");
		    temp.addField(mixerPair);
		}
	    }
	    supplyTemp.add(temp);
	}
	return supplyTemp;
    }
    
    /**
     * process the demand side system
     * 
     * @param zone
     * @param zoneTemp
     * @return
     */
    private ArrayList<EplusObject> processDemandTemp(String zone,
	    ArrayList<EplusObject> zoneTemp) {
	ArrayList<EplusObject> demandTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : zoneTemp) {
	    EplusObject temp = eo.clone();
	    // check special characters to avoid useless loop inside the replace
	    // special characters
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(zone);
	    }
	    demandTemp.add(temp);
	}
	return demandTemp;
    }

    @Override
    public double getTotalLoad(Document doc) {
	return 0;
    }

    @Override
    public String getSystemName() {
	return "DOAS";
    }

    @Override
    public int getNumberOfSupplySystem() {
	return numOfSupplySystem;
    }

    @Override
    public int getNumberOfDemandSystem() {
	return numOfDemandSystem;
    }
}

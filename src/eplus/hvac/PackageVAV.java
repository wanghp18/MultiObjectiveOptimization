package eplus.hvac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import eplus.data.EplusObject;
import eplus.data.KeyValuePair;
import eplus.data.ThermalZone;

public class PackageVAV implements HVACSystem {
    // recording all the required data for DOAS
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    // building object contains building information and energyplus data
    private EnergyPlusBuildingForHVACSystems building;

    // supply air connection list
    private ArrayList<String> zoneSplitterList;
    private ArrayList<String> zoneMixerList;

    // plant demand side list
    private ArrayList<String> systemHeatingCoilList;
    private ArrayList<String> zoneHeatingCoilList;

    // plant supply side list
    private ArrayList<String> boilerList;

    // pump selection
    private String heatingPump;    
    
    private static final String sizingTable = "Component Sizing Summary%Coil:Cooling:DX:SingleSpeed";
    private static final String TAG = "tableID";
    
    private int numOfSupplySystem;
    private int numOfDemandSystem;

    public PackageVAV(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuildingForHVACSystems bldg) {
	objectLists = objects;
	building = bldg;

	// Set-up all the data structures
	zoneSplitterList = new ArrayList<String>();
	zoneMixerList = new ArrayList<String>();
	systemHeatingCoilList = new ArrayList<String>();
	zoneHeatingCoilList = new ArrayList<String>();
	boilerList = new ArrayList<String>();

	processSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    @Override
    public double getTotalLoad(Document doc){
	Elements coilList = doc.getElementsByAttributeValue(TAG, sizingTable).get(0).getElementsByTag("td");
	Double load = 0.0;
	for(int i=0; i<coilList.size(); i++){
	    if(coilList.get(i).text().contains("COOLING COIL")){
		load = load + Double.parseDouble(coilList.get(i+2).text());
	    }
	}
	System.out.println(load);
	return load;
    }

    private void processSystems() {
	ArrayList<EplusObject> supplySideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandSideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> plantSystem = new ArrayList<EplusObject>();

	ArrayList<EplusObject> supplySideSystemTemplate = objectLists
		.get("Supply Side System");
	ArrayList<EplusObject> demandSideSystemTemplate = objectLists
		.get("Demand Side System");
	ArrayList<EplusObject> plantSystemTemplate = objectLists.get("Plant");
	HashMap<String, ArrayList<ThermalZone>> floorMap = building
		.getVentilationMap();

	Set<String> floorMapSet = floorMap.keySet();
	Iterator<String> floorMapIterator = floorMapSet.iterator();

	int roomCounter = 0;
	int supplySystemCounter = 0;
	while (floorMapIterator.hasNext()) {
	    zoneSplitterList.clear();
	    zoneMixerList.clear();
	    String floor = floorMapIterator.next();
	    // first process the demand side system and their connection to
	    // plant and supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    supplySystemCounter++;
	    for (ThermalZone zone : zones) {
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(),
			demandSideSystemTemplate));
		// add the outdoor air object for demand zone
		//demandSideSystem.add(zone.getOutdoorAirObject());
		roomCounter++;
	    }
	    // then process the supply side system and their connections to
	    // plant
	    supplySideSystem.addAll(processSupplyTemp(floor,
		    supplySideSystemTemplate));
	}
	
	numOfSupplySystem = supplySystemCounter;
	numOfDemandSystem = roomCounter;
	plantSystem.addAll(processPlantTemp(plantSystemTemplate));
	//System.out.println("Counting the rooms: " + roomCounter);
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	objectLists.put("Plant", plantSystem);
	//System.out.println("Connect plans");
	processConnections();
    }

    /**
     * A method to process the system connections
     */
    private void processConnections() {
	ArrayList<EplusObject> plantSystem = objectLists.get("Plant");
	HVACSystemImplUtil.plantConnectionForSys5And6(plantSystem, boilerList,
		systemHeatingCoilList, zoneHeatingCoilList);
    }

    private ArrayList<EplusObject> processPlantTemp(
	    ArrayList<EplusObject> plantSideTemp) {

	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();

	// we use iterator because we will delete some objects in this loop
	// (pumps)
	boilerList.add("Boiler%");
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while (eoIterator.hasNext()) {
	    EplusObject temp = eoIterator.next().clone();

	    // select pumps from Templates based on the inputs
	    // choose hot water loop pumps
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Hot Water Loop HW Supply Pump")) {
		if (temp.getObjectName().equalsIgnoreCase(
			"HeaderedPumps:ConstantSpeed")) {
		    eoIterator.remove();
		    heatingPump = "HeaderedPumps:VariableSpeed";
		    continue;
		}
	    }

	    // this should be remove to the next loop later update the hot water
	    // loop branch information
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Hot Water Loop HW Supply Inlet Branch")) {
		// this is the number of the component 1 object type in branch
		temp.getKeyValuePair(3).setValue(heatingPump);
	    }

	    plantTemp.add(temp);
	}
	return plantTemp;
    }

    private ArrayList<EplusObject> processSupplyTemp(String floor,
	    ArrayList<EplusObject> supplySideSystemTemplate) {
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();

	for (EplusObject eo : supplySideSystemTemplate) {
	    EplusObject temp = eo.clone();

	    /*
	     * replace the special characters that contains floors
	     */
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(floor);
	    }

	    /*
	     * find the name of the coils' branch for plant connection purpose
	     */
	    if (temp.getObjectName().equals("Branch")) {
		String name = temp.getKeyValuePair(0).getValue();
		if (name.contains("Heating Coil")) {
		    systemHeatingCoilList.add(name);
		}
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName().equalsIgnoreCase(
		    "AirLoopHVAC:ZoneSplitter")) {
		for (String s : zoneSplitterList) {
		    KeyValuePair splitterPair = new KeyValuePair(
			    "Outlet Node Name", s);
		    temp.addField(splitterPair);
		}
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneMixer")) {
		for (String s : zoneMixerList) {
		    KeyValuePair mixerPair = new KeyValuePair(
			    "Intlet Node Name", s);
		    temp.addField(mixerPair);
		}
	    }
	    supplyTemp.add(temp);
	}
	return supplyTemp;
    }

    /**
     * Process the zones in the building
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
	// record the connection links in the HVAC system
	String zoneSplitter = zone + " Zone Equip Inlet";
	String zoneMixer = zone + " Return Outlet";
	// this is only for system type 7
	String reheatCoil = zone + " Reheat Coil HW Branch";

	// add the connection links to another data lists for later
	// processing
	zoneSplitterList.add(zoneSplitter);
	zoneMixerList.add(zoneMixer);
	zoneHeatingCoilList.add(reheatCoil);
	return demandTemp;
    }

    @Override
    public String getSystemName() {
	// TODO Auto-generated method stub
	return "Package VAV";
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

package eplus.hvac;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import eplus.data.EplusObject;
import eplus.data.KeyValuePair;

/**
 * This class mainly help to merge systems such that the ventilation and
 * heating/cooling part separated DOAS + VRF
 * 
 * @author Weili
 *
 */
public class SystemMerger implements HVACSystem {
    
    private HashMap<String, ArrayList<EplusObject>> heatCoolSystem;
    private HashMap<String, ArrayList<EplusObject>> ventSystem;
    private HashMap<String, ArrayList<EplusObject>> mergedSystem;

    private final int Cooling_Seq_Index = 3;
    private final int Heating_Seq_Index = 4;
    
    private static final String sizingTable = "Component Sizing Summary%AirConditioner:VariableRefrigerantFlow";
    private static final String TAG = "tableID";
    
    private int numOfSupplySystem;
    private int numOfDemandSystem;
    
    public SystemMerger(HVACSystem ventilation, HVACSystem heatcool) {
	ventSystem = ventilation.getSystemData();
	numOfSupplySystem = ventilation.getNumberOfSupplySystem();
	heatCoolSystem = heatcool.getSystemData();
	numOfDemandSystem = heatcool.getNumberOfDemandSystem();	
	mergedSystem = new HashMap<String, ArrayList<EplusObject>>();
	mergeSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	// TODO Auto-generated method stub
	return mergedSystem;
    }

    /**
     * This is mainly merging the demand side systems we will firstly keep the
     * format of heat and cool system we will delete ventilation system demand
     * side objects if necessary and merge them into one object
     */
    private void mergeSystems() {
	/* Change heat Cool System's ZoneHVAC:EquipmentList */
	ArrayList<EplusObject> heatCoolDemandSystem = heatCoolSystem
		.get("Demand Side System");
	ArrayList<EplusObject> ventDemandSystem = ventSystem
		.get("Demand Side System");

	mergedSystem.put("Demand Side System", new ArrayList<EplusObject>());
	for (int i = 0; i < heatCoolDemandSystem.size(); i++) {
	    EplusObject object = heatCoolDemandSystem.get(i);
	    /* 1. find equipment list case */
	    if (object.getObjectName().equalsIgnoreCase(
		    "ZoneHVAC:EquipmentList")) {
		String name = object.getKeyValuePair(0).getValue();// get its
								   // name

		// start iterating the ventilation loop to find the match object
		for (int j = 0; j < ventDemandSystem.size(); j++) {
		    EplusObject ventOb = ventDemandSystem.get(j);
		    if (ventOb.getObjectName().equalsIgnoreCase(
			    "ZoneHVAC:EquipmentList")
			    && ventOb.getKeyValuePair(0).getValue()
				    .equals(name)) {
			object.getKeyValuePair(Cooling_Seq_Index).setValue(
				"2.0");
			object.getKeyValuePair(Heating_Seq_Index).setValue(
				"2.0");
			for (int k = 1; k < ventOb.getSize(); k++) {
			    object.addField(ventOb.getKeyValuePair(k));
			}
		    }
		}
	    }
	    /* 2. if find NodeList case */
	    if (object.getObjectName().equalsIgnoreCase("NodeList")
		    && !object.getKeyValuePair(0).getValue()
			    .split(" ")[0].split("%")[3].equals("NONE")) {
		String name = object.getKeyValuePair(0).getValue();
		String zoneName = name.split(" ")[0];
		if (name.contains("Inlet")) {
		    KeyValuePair inlet = new KeyValuePair("Node 2 Name",
			    zoneName + " DOAS Supply Inlet");
		    object.addField(inlet);
		} else if (name.contains("Outlet")) {
		    KeyValuePair outlet = new KeyValuePair("Node 2 Name",
			    zoneName + " Zone Equip Inlet");
		    object.addField(outlet);
		}
	    }
	    mergedSystem.get("Demand Side System").add(object);
	}
	/* 3. remove DOAS demand system now! */
	ArrayList<EplusObject> reducedDemandComponents = new ArrayList<EplusObject>();
	for (int l = 0; l < ventDemandSystem.size(); l++) {
	    EplusObject ventObject = ventDemandSystem.get(l);
	    if (ventObject.getObjectName().equals("ZoneHVAC:EquipmentList")
		    || ventObject.getObjectName().equals(
			    "ZoneHVAC:EquipmentConnections")
		    || ventObject.getObjectName().equals("NodeList")||
		    ventObject.getObjectName().equals("Sizing:Zone")||
		    ventObject.getObjectName().equals("DesignSpecification:OutdoorAir")||
		    ventObject.getObjectName().equals("ZoneControl:Thermostat")) {
		if (inVentOnlyZones(ventObject.getKeyValuePair(0).getValue()
			.split(" ")[0])) {
		    reducedDemandComponents.add(ventObject);
		}
	    } else {
		reducedDemandComponents.add(ventObject);
	    }
	}
	if (reducedDemandComponents != null) {
	    mergedSystem.get("Demand Side System").addAll(
		    reducedDemandComponents);
	}
	simpleMerge();
    }

    private boolean inVentOnlyZones(String zoneName) {
	String[] zoneNameComponent = zoneName.split("%");
	// the new name indicates
	if (zoneNameComponent[4].equals("NONE")) {
	    return true;
	}
	return false;
    }

    private void simpleMerge() {
	/* Change heat Cool System's ZoneHVAC:EquipmentList */
	ArrayList<EplusObject> heatCoolSupplySystem = heatCoolSystem
		.get("Supply Side System");
	ArrayList<EplusObject> ventSupplySystem = ventSystem
		.get("Supply Side System");
	ArrayList<EplusObject> heatCoolScheduleSystem = heatCoolSystem
		.get("Schedule");
	ArrayList<EplusObject> ventScheduleSystem = ventSystem.get("Schedule");
	ArrayList<EplusObject> heatCoolGlobalSystem = heatCoolSystem
		.get("Global");
	ArrayList<EplusObject> ventGlobalSystem = ventSystem.get("Global");
	mergedSystem.put("Supply Side System", new ArrayList<EplusObject>());
	mergedSystem.get("Supply Side System").addAll(heatCoolSupplySystem);
	mergedSystem.get("Supply Side System").addAll(ventSupplySystem);
	mergedSystem.put("Schedule", new ArrayList<EplusObject>());
	if (heatCoolScheduleSystem != null) {
	    mergedSystem.get("Schedule").addAll(heatCoolScheduleSystem);
	}
	if (ventScheduleSystem != null) {
	    mergedSystem.get("Schedule").addAll(ventScheduleSystem);

	}
	mergedSystem.put("Global", new ArrayList<EplusObject>());
	if (heatCoolGlobalSystem != null) {
	    mergedSystem.get("Global").addAll(heatCoolGlobalSystem);

	}
	if (ventGlobalSystem != null) {
	    mergedSystem.get("Global").addAll(ventGlobalSystem);
	}
    }

    @Override
    public double getTotalLoad(Document doc) {
	Elements coilList = doc.getElementsByAttributeValue(TAG, sizingTable).get(0).getElementsByTag("td");
	Double load = 0.0;
	for(int i=0; i<coilList.size(); i++){
	    if(coilList.get(i).text().contains("VRF HEAT PUMP")){
		load = load + Double.parseDouble(coilList.get(i+1).text());
	    }
	}
	return load;
    }

    @Override
    public String getSystemName() {
	// TODO Auto-generated method stub
	return "DOAS+VRF";
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

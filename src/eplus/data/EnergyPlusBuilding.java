package eplus.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import baseline.util.ClimateZone;
import eplus.data.IdfReader.ValueNode;

public class EnergyPlusBuilding {

    /**
     * basic information about the building
     */
    private String buildingType;
    private Double totalFloorArea;
    private Double conditionedFloorArea;
    private Set<String> floorSet;
    private boolean electricHeating;

    private double numberOfSystem = 0.0;
    private double supplyReturnRatio = 0.0;
    private BaselineInfo info;

    /**
     * set point not met
     */
    private Double heatingSetPointNotMet;
    private Double coolingSetPointNotMet;

    /**
     * the required cooling and heating loads
     */
    private Double totalCoolingLoad;
    private Double totalHeatingLoad;

    /**
     * climate zone
     */
    private ClimateZone cZone;

    /**
     * the building thermal zone lists
     */
    private List<ThermalZone> thermalZoneList;
    // for creating HVAC system
    private HashMap<String, ArrayList<ThermalZone>> floorMap;
    private HashMap<String, Boolean> returnFanMap;
    private HashMap<String, HashMap<String, ArrayList<ValueNode>>> serviceHotWater;
    private boolean addedServiceWater = false;

    /**
     * EnergyPlus data
     */
    private IdfReader baselineModel;

    public EnergyPlusBuilding(String bldgType, ClimateZone zone,
	    IdfReader baselineModel, BaselineInfo infomation) {
	buildingType = bldgType;
	thermalZoneList = new ArrayList<ThermalZone>();
	floorMap = new HashMap<String, ArrayList<ThermalZone>>();
	returnFanMap = new HashMap<String, Boolean>();
	serviceHotWater = new HashMap<String, HashMap<String, ArrayList<ValueNode>>>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
	cZone = zone;
	this.baselineModel = baselineModel;
	electricHeating = false;
	this.info = infomation;
	if (info != null) {
	    info.setHeatSource("NaturalGas");
	}

	// remove unnecessary objects in the model
	this.baselineModel.removeEnergyPlusObject("Daylighting:Controls");
    }

    /**
     * Reload the data from energyplus output.
     */
    public void initializeBuildingData() {
	thermalZoneList.clear();
	floorMap.clear();
	floorSet = new HashSet<String>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
    }

    public void initialInfoForSystem(String system) {
	if (system.equals("System Type 7")) {
	    info.setSystemType("System Type 7");
	    info.setFanControlType("Variable Control Fans");
	    info.setChillerCOP(6.1);
	    info.setChillerIPLV(6.4);
	    info.setChillerCapacity(info.getCoolingCapacity());
	    info.setBoilerCapacity(info.getHeatingCpacity());
	} else if (system.equals("System Type 5")) {
	    info.setSystemType("System Type 5");
	    info.setFanControlType("Variable Control Fans");
	    info.setCoolingEER(10.78);
	} else if (system.equals("System Type 8")) {
	    info.setSystemType("System Type 8");
	    info.setFanControlType("Variable Control Fans");
	    info.setChillerCOP(6.1);
	    info.setChillerIPLV(6.4);
	    info.setChillerCapacity(info.getCoolingCapacity());

	} else if (system.equals("System Type 6")) {
	    info.setSystemType("System Type 6");
	    info.setFanControlType("Variable Control Fans");
	    info.setCoolingEER(10.78);
	} else if (system.equals("System Type 3")) {
	    info.setSystemType("System Type 3");
	    info.setFanControlType("Constant Control Fans");
	    info.setCoolingEER(10.78);
	    info.setUnitaryHeatingCOP(3.2);
	}
	info.setSupplyAirFlow(getSupplyAirFlow());
	info.setOutdoorAirFlow(getOutdoorAir());
    }

    public BaselineInfo getInfoObject() {
	return info;
    }

    /*
     * All Setter Methods
     */
    public void setTotalFloorArea(Double area) {
	totalFloorArea = area;
	if (info != null) {
	    info.setBuildingArea(area);
	}
    }

    public void setConditionedFloorArea(Double area) {
	conditionedFloorArea = area;
    }

    public void setHeatTimeSetPointNotMet(Double hr) {
	heatingSetPointNotMet = hr;
    }

    public void setCoolTimeSetPointNotMet(Double hr) {
	coolingSetPointNotMet = hr;
    }

    public void setElectricHeating() {
	electricHeating = true;
	if (info != null) {
	    info.setHeatSource("Electric");
	}
    }

    /**
     * add thermal zones to the data structure
     * 
     * @param zone
     */
    public void addThermalZone(ThermalZone zone) {
	thermalZoneList.add(zone);
    }

    /**
     * This method must be called prior to get floorMap, get total cooling load
     * and get total heating load and then check return fans
     * 
     * @return
     */
    public void processModelInfo() {
	// building the thermal zones
	for (ThermalZone zone : thermalZoneList) {
	    String block = zone.getBlock();
	    String floor = zone.getFloor();
	    String level = null;
	    if (floor == null && block == null) {
		level = null;
	    } else if (floor == null && block != null) {
		// plenum condition
		floorSet.add(block);
		level = block;
	    } else {
		floorSet.add(floor);
		level = block + ":" + floor;
	    }

	    if (level != null) {
		if (!floorMap.containsKey(level)) {
		    floorMap.put(level, new ArrayList<ThermalZone>());
		}
		floorMap.get(level).add(zone);
		totalCoolingLoad += zone.getCoolingLoad();
		totalHeatingLoad += zone.getHeatingLoad();
	    }
	}
	if (info != null) {
	    info.setCoolingCapacity(totalCoolingLoad);
	    info.setHeatingCpacity(totalHeatingLoad);
	}
    }

    /**
     * get the zone maximum flow rate
     * 
     * @param zoneName
     * @return
     */
    public Double getZoneMaximumFlowRate(String zoneName) {
	Double coolingFlowRate = 0.0;
	Double heatingFlowRate = 0.0;
	for (ThermalZone zone : thermalZoneList) {
	    if (zone.getFullName().equalsIgnoreCase(zoneName)) {
		coolingFlowRate = zone.getCoolingAirFlow();
		heatingFlowRate = zone.getHeatingAirFlow();
	    }
	}
	// System.out.println(coolingFlowRate + " " + heatingFlowRate);
	return Math.max(coolingFlowRate, heatingFlowRate);
    }

    /**
     * get the floor maximum flow rate from the database
     * 
     * @param floor
     * @return
     */
    public Double getFloorMaximumFlowRate(String floor) {
	ArrayList<ThermalZone> zoneList = floorMap.get(floor);
	Double coolingFlowRate = 0.0;
	Double heatingFlowRate = 0.0;

	for (ThermalZone zone : zoneList) {
	    coolingFlowRate += zone.getCoolingAirFlow();
	    heatingFlowRate += zone.getHeatingAirFlow();
	}
	return Math.max(coolingFlowRate, heatingFlowRate);
    }

    public Double getFloorMinimumVentilationRate(String floor) {
	ArrayList<ThermalZone> zoneList = floorMap.get(floor);
	Double flowVent = 0.0;

	for (ThermalZone zone : zoneList) {
	    flowVent += zone.getMinimumVentilation();
	}
	return flowVent;
    }

    /**
     * get the floor map
     * 
     * @return
     */
    public HashMap<String, ArrayList<ThermalZone>> getFloorMap() {
	return floorMap;
    }

    public IdfReader getBaselineModel() {
	return baselineModel;
    }

    public boolean getHeatingMethod() {
	return electricHeating;
    }

    public boolean hasReturnFan() {
	Set<String> returnFan = returnFanMap.keySet();
	Iterator<String> returnFanIterator = returnFan.iterator();
	while (returnFanIterator.hasNext()) {
	    String fan = returnFanIterator.next();
	    // System.out.println(fan+" " + returnFanMap.get(fan));
	    if (returnFanMap.get(fan)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * get the total cooling load
     * 
     * @return
     */
    public Double getTotalCoolingLoad() {
	return Math.round(totalCoolingLoad * 100.0) / 100.0;
    }

    /**
     * get the total heating load
     * 
     * @return
     */
    public Double getTotalHeatingLoad() {
	return Math.round(totalHeatingLoad * 100.0) / 100.;
    }

    /*
     * All getter methods
     */
    public Double getTotalFloorArea() {
	return totalFloorArea;
    }

    public Double getConditionedFloorArea() {
	return conditionedFloorArea;
    }

    public Integer getNumberOfFloor() {
	return floorSet.size();
    }

    public Double getHeatingSetPointNotMet() {
	return heatingSetPointNotMet;
    }

    public Double getCoolingSetPointNotMet() {
	return coolingSetPointNotMet;
    }

    public Double getSupplyReturnFanRatio() {
	return supplyReturnRatio / numberOfSystem;
    }

    /**
     * removes the HVAC objects and build service hot water model This method
     * firstly will trace any inputs that relates to service hot water system
     * Note the name of components in the service hot water must contain DHWSys
     * strings. then this method will remove the whole objects.
     * 
     * @param s
     */
    public void removeHVACObject(String s) {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> objectList = baselineModel
		.getObjectList(s);

	if (objectList != null) {
	    Set<String> elementCount = objectList.get(s).keySet();
	    Iterator<String> elementIterator = elementCount.iterator();
	    while (elementIterator.hasNext()) {
		String count = elementIterator.next();
		ArrayList<ValueNode> object = objectList.get(s).get(count);
		for (ValueNode v : object) {
		    if (v.getDescription().contains("Name")
			    && v.getAttribute().contains("SHWSys")) {
			if (!serviceHotWater.containsKey(s)) {
			    serviceHotWater
				    .put(s,
					    new HashMap<String, ArrayList<ValueNode>>());
			}
			serviceHotWater.get(s).put(count, object);
		    }
		}
	    }
	}

	addedServiceWater = false;
	baselineModel.removeEnergyPlusObject(s);
    }

    /**
     * allow other method to insert energyplus object to the model
     * 
     * @param name
     * @param objectValues
     * @param objectDes
     */
    public void insertEnergyPlusObject(String name, String[] objectValues,
	    String[] objectDes) {
	baselineModel.addNewEnergyPlusObject(name, objectValues, objectDes);
    }

    public void generateEnergyPlusModel(String filePath, String fileName,
	    String degree) {
	// merge the all the information before write out
	// 1. add service hot water back to the model
	if (!addedServiceWater) {
	    Set<String> objectList = serviceHotWater.keySet();
	    Iterator<String> objectIterator = objectList.iterator();
	    while (objectIterator.hasNext()) {
		String objectName = objectIterator.next();
		HashMap<String, ArrayList<ValueNode>> elementList = serviceHotWater
			.get(objectName);
		Set<String> elementSet = elementList.keySet();
		Iterator<String> elementIterator = elementSet.iterator();
		while (elementIterator.hasNext()) {
		    String element = elementIterator.next();
		    ArrayList<ValueNode> object = elementList.get(element);
		    baselineModel.addNewEnergyPlusObject(objectName, object);
		}
	    }
	    addedServiceWater = true;
	}

	HashMap<String, ArrayList<ValueNode>> buildingObject = baselineModel
		.getObjectListCopy("Building");
	buildingObject.get("0").get(1).setAttribute(degree);

	// 2. write out the model
	baselineModel.WriteIdf(filePath, fileName);
    }

    private double getSupplyAirFlow() {
	double supplyAir = 0.0;
	Set<String> floors = floorMap.keySet();
	Iterator<String> floorIterator = floors.iterator();
	while (floorIterator.hasNext()) {
	    String floor = floorIterator.next();
	    supplyAir = getFloorMaximumFlowRate(floor);
	}
	return supplyAir;
    }

    private double getOutdoorAir() {
	double outdoorAir = 0.0;
	Set<String> floors = floorMap.keySet();
	Iterator<String> floorIterator = floors.iterator();
	while (floorIterator.hasNext()) {
	    String floor = floorIterator.next();
	    outdoorAir = getFloorMinimumVentilationRate(floor);
	}
	return outdoorAir;
    }
}

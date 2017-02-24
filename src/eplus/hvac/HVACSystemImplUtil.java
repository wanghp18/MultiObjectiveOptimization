package eplus.hvac;

import java.util.ArrayList;

import eplus.data.EplusObject;
import eplus.data.KeyValuePair;

/**
 * This class helps to establish, modify, or implement control logic which are
 * specified in Standard 2007 and 2010
 * 
 * 
 * @author Weili
 *
 */
public final class HVACSystemImplUtil {

    /**
     * control the economizer, if economizer is needed, then modify the
     * energyplus object
     * 
     * @param eo
     * @param temperature
     */
    public static void economizer(EplusObject eo, double temperature) {
	int numOfFields = eo.getSize();
	for (int i = 0; i < numOfFields; i++) {
	    if (eo.getKeyValuePair(i).getKey()
		    .equals("Economizer Control Type")) {
		eo.getKeyValuePair(i).setValue("FixedDryBulb");
	    } else if (eo.getKeyValuePair(i).getKey()
		    .equals("Economizer Maximum Limit Dry-Bulb Temperature")) {
		eo.getKeyValuePair(i).setValue("" + temperature);
	    }
	}
    }

    /**
     * For system type 7 where plant is available on-site and require to
     * connect to air distribution system
     * 
     * This method connects all the chillers, towers, boilers, heating and cooling
     * coils in the plant side system
     * 
     * @param plantSystem
     * @param chillerList
     * @param towerList
     * @param boilerList
     * @param sysCooilngCoilList
     * @param sysHeatingCoilList
     * @param zoneHeatingCoilList
     */
    public static void plantConnectionForSys7(ArrayList<EplusObject> plantSystem,
	    ArrayList<String> chillerList, ArrayList<String> towerList,
	    ArrayList<String> boilerList, ArrayList<String> sysCooilngCoilList,
	    ArrayList<String> sysHeatingCoilList,
	    ArrayList<String> zoneHeatingCoilList) {

	// use for additional eplus objects
	for (EplusObject eo : plantSystem) {
	    String name = eo.getKeyValuePair(0).getValue();
	    if (name.equals("Hot Water Loop HW Demand Side Branches")) {
		insertHeatingCoils(2, eo, sysHeatingCoilList,
			zoneHeatingCoilList);
	    } else if (name.equals("Hot Water Loop HW Demand Splitter")
		    || name.equals("Hot Water Loop HW Demand Mixer")) {
		insertHeatingCoils(eo.getSize(), eo, sysHeatingCoilList,
			zoneHeatingCoilList);// insert to the last index
	    } else if (name
		    .equals("Chilled Water Loop ChW Demand Side Branches")) {
		insertCoolingCoils(2, eo, sysCooilngCoilList);
	    } else if (name.equals("Chilled Water Loop ChW Demand Splitter")
		    || name.equals("Chilled Water Loop ChW Demand Mixer")) {
		insertCoolingCoils(eo.getSize(), eo, sysCooilngCoilList);
	    } else if (name.equals("Hot Water Loop HW Supply Side Branches")
		    || name.equals("Hot Water Loop HW Supply Splitter")
		    || name.equals("Hot Water Loop HW Supply Mixer")) {
		insertBoilerRelatedInputs(2, eo, " HW Branch", boilerList);
	    } else if (name
		    .equals("Chilled Water Loop ChW Supply Side Branches")) {
		insertChillerRelatedInputs(2, eo, " ChW Branch", chillerList);
	    } else if (name.equals("Chilled Water Loop ChW Supply Splitter")
		    || name.equals("Chilled Water Loop ChW Supply Mixer")) {
		insertChillerRelatedInputs(3, eo, " ChW Branch", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Demand Side Branches")) {
		insertChillerRelatedInputs(2, eo, " CndW Branch", chillerList);
	    } else if (name.equals("Chilled Water Loop CndW Demand Splitter")
		    || name.equals("Chilled Water Loop CndW Demand Mixer")) {
		insertChillerRelatedInputs(3, eo, " CndW Branch", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Supply Side Branches")) {
		insertTowerRelatedInputs(2, eo, " CndW Branch", towerList);
	    } else if (name.equals("Chilled Water Loop CndW Supply Splitter")
		    || name.equals("Chilled Water Loop CndW Supply Mixer")) {
		insertTowerRelatedInputs(3, eo, " CndW Branch", towerList);
	    } else if (name.equals("Hot Water Loop HW Supply Setpoint Nodes")) {
		insertBoilerRelatedInputs(1, eo, " HW Outlet", boilerList);
	    } else if (name
		    .equals("Chilled Water Loop ChW Supply Setpoint Nodes")) {
		insertChillerRelatedInputs(1, eo, " ChW Outlet", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Supply Setpoint Nodes")) {
		insertTowerRelatedInputs(1, eo, " CndW Outlet", towerList);
	    } else if (name.equals("Hot Water Loop All Equipment")) {
		insertBoilerEquipmentList(eo, boilerList);
	    } else if (name.equals("Chilled Water Loop All Chillers")) {
		insertChillerEquipmentList(eo, chillerList);
	    } else if (name.equals("Chilled Water Loop All Condensers")) {
		insertTowerEquipmentList(eo, towerList);
	    }
	}
    }
    
    /**
     * For system type 8 where plant is available on-site and require to
     * connect to air distribution system
     * 
     * This method connects all the chillers, towers, boilers, heating and cooling
     * coils in the plant side system
     * 
     * @param plantSystem
     * @param chillerList
     * @param towerList
     * @param sysCooilngCoilList
     */
    public static void plantConnectionForSys8(ArrayList<EplusObject> plantSystem,
	    ArrayList<String> chillerList, ArrayList<String> towerList, ArrayList<String> sysCooilngCoilList) {

	// use for additional eplus objects
	for (EplusObject eo : plantSystem) {
	    String name = eo.getKeyValuePair(0).getValue();
	    if (name.equals("Chilled Water Loop ChW Demand Side Branches")) {
		insertCoolingCoils(2, eo, sysCooilngCoilList);
	    } else if (name.equals("Chilled Water Loop ChW Demand Splitter")
		    || name.equals("Chilled Water Loop ChW Demand Mixer")) {
		insertCoolingCoils(eo.getSize(), eo, sysCooilngCoilList);
	    } else if (name
		    .equals("Chilled Water Loop ChW Supply Side Branches")) {
		insertChillerRelatedInputs(2, eo, " ChW Branch", chillerList);
	    } else if (name.equals("Chilled Water Loop ChW Supply Splitter")
		    || name.equals("Chilled Water Loop ChW Supply Mixer")) {
		insertChillerRelatedInputs(3, eo, " ChW Branch", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Demand Side Branches")) {
		insertChillerRelatedInputs(2, eo, " CndW Branch", chillerList);
	    } else if (name.equals("Chilled Water Loop CndW Demand Splitter")
		    || name.equals("Chilled Water Loop CndW Demand Mixer")) {
		insertChillerRelatedInputs(3, eo, " CndW Branch", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Supply Side Branches")) {
		insertTowerRelatedInputs(2, eo, " CndW Branch", towerList);
	    } else if (name.equals("Chilled Water Loop CndW Supply Splitter")
		    || name.equals("Chilled Water Loop CndW Supply Mixer")) {
		insertTowerRelatedInputs(3, eo, " CndW Branch", towerList);
	    } else if (name
		    .equals("Chilled Water Loop ChW Supply Setpoint Nodes")) {
		insertChillerRelatedInputs(1, eo, " ChW Outlet", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Supply Setpoint Nodes")) {
		insertTowerRelatedInputs(1, eo, " CndW Outlet", towerList);
	    } else if (name.equals("Chilled Water Loop All Chillers")) {
		insertChillerEquipmentList(eo, chillerList);
	    } else if (name.equals("Chilled Water Loop All Condensers")) {
		insertTowerEquipmentList(eo, towerList);
	    }
	}
    }    

    /**
     * For system type 5-6 where plant is available on-site and require to
     * connect to air distribution system
     * 
     * This method connects all the chillers, towers, boilers, heating and cooling
     * coils in the plant side system
     * 
     * @param plantSystem
     * @param boilerList
     * @param sysHeatingCoilList
     * @param zoneHeatingCoilList
     */
    public static void plantConnectionForSys5And6(ArrayList<EplusObject> plantSystem,
	    ArrayList<String> boilerList,
	    ArrayList<String> sysHeatingCoilList,
	    ArrayList<String> zoneHeatingCoilList) {

	// use for additional eplus objects
	for (EplusObject eo : plantSystem) {
	    String name = eo.getKeyValuePair(0).getValue();
	    if (name.equals("Hot Water Loop HW Demand Side Branches")) {
		insertHeatingCoils(2, eo, sysHeatingCoilList,
			zoneHeatingCoilList);
	    } else if (name.equals("Hot Water Loop HW Demand Splitter")
		    || name.equals("Hot Water Loop HW Demand Mixer")) {
		insertHeatingCoils(eo.getSize(), eo, sysHeatingCoilList,
			zoneHeatingCoilList);// insert to the last index
	    }else if (name.equals("Hot Water Loop HW Supply Side Branches")
		    || name.equals("Hot Water Loop HW Supply Splitter")
		    || name.equals("Hot Water Loop HW Supply Mixer")) {
		insertBoilerRelatedInputs(2, eo, " HW Branch", boilerList);
	    } else if (name.equals("Hot Water Loop HW Supply Setpoint Nodes")) {
		insertBoilerRelatedInputs(1, eo, " HW Outlet", boilerList);
	    }  else if (name.equals("Hot Water Loop All Equipment")) {
		insertBoilerEquipmentList(eo, boilerList);
	    }
	}
    }
    
    
    /*
     * Group of helper functions which inserts the systems connections to the
     * specified fields
     */
    private static void insertHeatingCoils(int index, EplusObject eo,
	    ArrayList<String> systemHeatingCoilList,
	    ArrayList<String> zoneHeatingCoilList) {
	for (String s : zoneHeatingCoilList) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
	    eo.insertFiled(index, newPair);
	}

	for (String s : systemHeatingCoilList) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
	    eo.insertFiled(index, newPair);
	}
    }

    private static void insertCoolingCoils(int index, EplusObject eo,
	    ArrayList<String> sysCooilngCoilList) {
	for (String s : sysCooilngCoilList) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
	    eo.insertFiled(index, newPair);
	}
    }

    private static void insertBoilerRelatedInputs(int index, EplusObject eo,
	    String postfix, ArrayList<String> boilerList) {
	if (boilerList.isEmpty()) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", "Boiler%"
		    + postfix);
	    eo.insertFiled(index, newPair);
	} else {
	    for (String s : boilerList) {
		KeyValuePair newPair = new KeyValuePair("Branch Name", s
			+ postfix);
		eo.insertFiled(index, newPair);
	    }
	}
    }

    private static void insertChillerRelatedInputs(int index, EplusObject eo,
	    String postfix, ArrayList<String> chillerList) {
	if (chillerList.isEmpty()) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", "Chiller%"
		    + postfix);
	    eo.insertFiled(index, newPair);
	} else {
	    for (String s : chillerList) {
		KeyValuePair newPair = new KeyValuePair("Branch Name", s
			+ postfix);
		eo.insertFiled(index, newPair);
	    }
	}
    }

    private static void insertTowerRelatedInputs(int index, EplusObject eo,
	    String postfix, ArrayList<String> towerList) {
	if (towerList.isEmpty()) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", "TOWER%"
		    + postfix);
	    eo.insertFiled(index, newPair);
	} else {
	    for (String s : towerList) {
		KeyValuePair newPair = new KeyValuePair("Branch Name", s
			+ postfix);
		eo.insertFiled(index, newPair);
	    }
	}
    }

    private static void insertBoilerEquipmentList(EplusObject eo,
	    ArrayList<String> boilerList) {
	if (boilerList.isEmpty()) {
	    KeyValuePair objectType = new KeyValuePair("Equipment Object Type",
		    "Boiler:HotWater");
	    KeyValuePair name = new KeyValuePair("Equipment Name", "Boiler%");
	    eo.addField(objectType);
	    eo.addField(name);
	} else {
	    for (String s : boilerList) {
		KeyValuePair objectType = new KeyValuePair(
			"Equipment Object Type", "Boiler:HotWater");
		KeyValuePair name = new KeyValuePair("Equipment Name", s);
		eo.addField(objectType);
		eo.addField(name);
	    }
	}
    }

    private static void insertChillerEquipmentList(EplusObject eo,
	    ArrayList<String> chillerList) {
	if (chillerList.isEmpty()) {
	    KeyValuePair objectType = new KeyValuePair("Equipment Object Type",
		    "Chiller:Electric:EIR");
	    KeyValuePair name = new KeyValuePair("Equipment Name", "Chiller%");
	    eo.addField(objectType);
	    eo.addField(name);
	} else {
	    for (String s : chillerList) {
		KeyValuePair objectType = new KeyValuePair(
			"Equipment Object Type", "Chiller:Electric:EIR");
		KeyValuePair name = new KeyValuePair("Equipment Name", s);
		eo.addField(objectType);
		eo.addField(name);
	    }
	}
    }

    private static void insertTowerEquipmentList(EplusObject eo,
	    ArrayList<String> towerList) {
	if (towerList.isEmpty()) {
	    KeyValuePair objectType = new KeyValuePair("Equipment Object Type",
		    "CoolingTower:TwoSpeed");
	    KeyValuePair name = new KeyValuePair("Equipment Name", "Tower%");
	    eo.addField(objectType);
	    eo.addField(name);
	} else {
	    for (String s : towerList) {
		KeyValuePair objectType = new KeyValuePair(
			"Equipment Object Type", "CoolingTower:TwoSpeed");
		KeyValuePair name = new KeyValuePair("Equipment Name", s);
		eo.addField(objectType);
		eo.addField(name);
	    }
	}
    }

}

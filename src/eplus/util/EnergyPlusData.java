package eplus.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eplus.data.ThermalZone;
import eplus.hvac.EnergyPlusBuildingForHVACSystems;

public final class EnergyPlusData {
    static final EnergyPlusBuildingForHVACSystems hvacbldg = new EnergyPlusBuildingForHVACSystems();
    public static final String resultFolder = "C:\\Users\\Weili\\workspace\\MyWebProject\\WebContent\\results";
    public static int fileCounter = 1;

    public static void setUpHVACBldg(File html) {
	hvacbldg.processEnergyPlusFile();
	hvacbldg.processOutputs(html);
    }
    
    public static void setUserEplusFile(File f){
	hvacbldg.setUserEplus(f);
    }
    
    public static List<ThermalZone> getHVACBldgThermalZoneList(){
	return hvacbldg.getThermalZones();
    }
    
    public static void replaceHVACSystem(String sys){
	try {
	    hvacbldg.replaceHVACEnergyPlus(sys);
	    hvacbldg.generateEnergyPlusModel(resultFolder,fileCounter+"");
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }
}

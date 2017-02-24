package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import baseline.generator.Generator;
import baseline.idfdata.BaselineInfo;
import baseline.util.BaselineUtils;
import baseline.util.ClimateZone;

public class BaselineAutomationEngine {

    private JsonObject baselineInfoData;

    private static File eplusFolder;

    private final String weatherFileLink = "USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw";
    
    private static int order = 0;

    public BaselineAutomationEngine(File design, String climate) throws IOException {
	File weatherFile = new File(getClass().getClassLoader().getResource(weatherFileLink).getFile());
	eplusFolder = design.getParentFile();
	BaselineUtils.setAbsoluteDir("C:\\Users\\Weili\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MyWebProject\\WEB-INF\\classes\\");
	
	File wea = null;
	try {
	    wea = copyWeatherFile(weatherFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	ClimateZone zone = null;
	ClimateZone[] zones = ClimateZone.values();
	for (ClimateZone z : zones) {
	    if (z.toString().equals(climate)) {
		zone = z;
	    }
	}
	System.out.println(design.getAbsolutePath());
	System.out.println(wea.getName());
	Generator generator = new Generator(design, wea, zone, "Office",
		false, "DesignBuilder");
	BaselineInfo info = generator.getBaselineInfo();
	baselineInfoData = new JsonObject();
	order = 1;
	JsonArray infoList = new JsonArray();
	infoList.add(createInfoJson("Building Condition Area", info.getBuildingArea()+"", "m2"));
	infoList.add(createInfoJson("System Type", info.getSystemType(), " "));
	infoList.add(createInfoJson("System Desination", info.getSystemDesignation(), " "));
	infoList.add(createInfoJson("Number of System", info.getNumOfSystem()+"", " "));
	infoList.add(createInfoJson("Total Cooling Capacity", info.getCoolingCapacity()+"", "W"));
	infoList.add(createInfoJson("Total Heating Capacity", info.getHeatingCpacity()+"", "W"));
	infoList.add(createInfoJson("Cooling EER", info.getCoolingEER()+"", "Btu/W-h"));
	infoList.add(createInfoJson("Cooling IEER", info.getCoolingIEER()+"", "Btu/W-h"));
	infoList.add(createInfoJson("Heating Efficiency", info.getHeatingEfficiency()+"", "%"));
	infoList.add(createInfoJson("Unitary Heating COP", info.getUnitaryHeatingCOP()+"", " "));
	infoList.add(createInfoJson("Fan Control Type", info.getFanControlType()+"", " "));
	infoList.add(createInfoJson("Supply Air Flow", info.getSupplyAirFlow()+"", "m3/s"));
	infoList.add(createInfoJson("Outdoor Air Flow", info.getOutdoorAirFlow()+"", "m3/s"));
	infoList.add(createInfoJson("Demand Control", info.isDemandControl()+"", " "));
	infoList.add(createInfoJson("Economizer Shutoff", info.getHasEconomizer()+"", " C"));
	infoList.add(createInfoJson("Energy Recovery", info.isEnergyRecovery()+"", " "));
	infoList.add(createInfoJson("Energy Recovery Effect", info.getRecoveryEffect()+"", " "));
	infoList.add(createInfoJson("Supply Fan Power", info.getFanPower()+"", "W"));
	infoList.add(createInfoJson("Return Fan Power", info.getReturnFanPower()+"", "W"));
	infoList.add(createInfoJson("Number of Chiller", info.getNumChiller()+"", " "));
	infoList.add(createInfoJson("Chiller Capacity", info.getChillerCapacity()+"", "W"));
	infoList.add(createInfoJson("Chiller COP", info.getChillerCOP()+"", " "));
	infoList.add(createInfoJson("Chiller IPLV", info.getChillerIPLV()+"", " "));
	infoList.add(createInfoJson("Number of CHW Pump", info.getNumCHWPump()+"", " "));
	infoList.add(createInfoJson("CHW Pump Water Flow", info.getChwPumpFlow()+"", "m3/s"));
	infoList.add(createInfoJson("CW Pump Water Flow", info.getChwPumpFlow()+"", "m3/s"));
	infoList.add(createInfoJson("Boiler Capacity", info.getBoilerCapacity()+"", "W"));
	infoList.add(createInfoJson("Boiler Efficiency", info.getBoilerEfficiency()+"", "%"));
	infoList.add(createInfoJson("HW Pump Water Flow", info.getHwPumpFlow()+"", "m3/s"));
	baselineInfoData.add("results", infoList);
	
    }
    
    public JsonObject getBaselineInfo(){
	return baselineInfoData;
    }
    
    public static String getFilePath(){
	return eplusFolder.getAbsolutePath()+File.separator + "Baseline_0.idf";
    }
    
    private JsonObject createInfoJson(String name, String value, String unit){
	JsonObject temp = new JsonObject();
	temp.addProperty("Item", order+"");
	temp.addProperty("Output", name);
	temp.addProperty("Value", value);
	temp.addProperty("Unit", unit);
	order++;
	return temp;
    }

    /*
     * copy weather file into a directory
     */
    private File copyWeatherFile(File weatherFile) throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(weatherFile));
	StringBuilder sb = new StringBuilder();
	System.out.println("I am at this another step: " + sb.getClass().getName());

	File file = new File(
		eplusFolder.getAbsolutePath() + "\\" + "weather.epw");
	try {
	    String line = br.readLine();
	    while (line != null) {
		sb.append(line);
		sb.append(System.lineSeparator());
		line = br.readLine();
	    }
	} finally {
	    FileWriter results = null;
	    try {
		results = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(results);
		pw.append(sb.toString());
		pw.close();
	    } catch (IOException e) {
		// some warning??
	    }
	    // close the file
	    br.close();
	}
	return file;
    }

}

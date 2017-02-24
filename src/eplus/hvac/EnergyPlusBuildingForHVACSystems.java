package eplus.hvac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eplus.data.DesignBuilderThermalZone;
import eplus.data.EplusObject;
import eplus.data.IdfReader;
import eplus.data.ThermalZone;
import eplus.html.HVACSizing;
import eplus.optimization.OptResult;
import eplus.optimization.OptResultSet;

public class EnergyPlusBuildingForHVACSystems {
    /**
     * the building thermal zone lists
     */
    private List<ThermalZone> thermalZoneList;
    private HashMap<String, ArrayList<ThermalZone>> ventilationMap;
    private HashMap<String, ArrayList<ThermalZone>> zoneSystemMap;

    private OptResultSet optResults;
    /**
     * EnergyPlus data
     */
    private IdfReader energyplusModel;

    /**
     * process related data
     */
    private Document doc;
    private static final String FILE_NAME = "HVACObjects.txt";
    private String[] objectList;
    private double totalArea = 0.0;

    private File userEplus;

    public EnergyPlusBuildingForHVACSystems() {

	thermalZoneList = new ArrayList<ThermalZone>();
	ventilationMap = new HashMap<String, ArrayList<ThermalZone>>();
	zoneSystemMap = new HashMap<String, ArrayList<ThermalZone>>();
	optResults = new OptResultSet();
    }

    public void processEnergyPlusFile() {
	energyplusModel = new IdfReader();
	energyplusModel.setFilePath(userEplus.getAbsolutePath());
	try {
	    energyplusModel.readEplusFile();
	    System.out.println("Success!");
	} catch (IOException e) {
	    e.printStackTrace();
	}
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
	    if (!zone.getFullName().contains("PLENUM")) {
		String hvac = zone.getZoneCoolHeat();
		String vent = zone.getZoneVent();
		if (!vent.equalsIgnoreCase("NONE")
			&& !vent.equalsIgnoreCase("EXT")) {
		    if (!ventilationMap.containsKey(vent)) {
			ventilationMap.put(vent, new ArrayList<ThermalZone>());
		    }
		    ventilationMap.get(vent).add(zone);
		}

		if (!hvac.equalsIgnoreCase("NONE")) {
		    if (!zoneSystemMap.containsKey(hvac)) {
			zoneSystemMap.put(hvac, new ArrayList<ThermalZone>());
		    }
		    zoneSystemMap.get(hvac).add(zone);
		}
	    }
	}
    }

    public HashMap<String, ArrayList<ThermalZone>> getVentilationMap() {
	return ventilationMap;
    }

    public HashMap<String, ArrayList<ThermalZone>> getZoneSystemMap() {
	return zoneSystemMap;
    }

    public List<ThermalZone> getThermalZones() {
	return thermalZoneList;
    }

    public int getNumberOfZone() {
	return thermalZoneList.size();
    }

    public String getZoneNamebyIndex(int index) {
	return thermalZoneList.get(index).getFullName();
    }

    public IdfReader getIdfData() {
	return energyplusModel;
    }

    public void setUserEplus(File eplus) {
	userEplus = eplus;
    }

    public void generateEnergyPlusModel(String filePath, String fileName) {
	// System.out.println(file);
	energyplusModel.WriteIdf(filePath, fileName);
    }

    public void processOutputs(File htmloutput) {
	try {
	    doc = Jsoup.parse(htmloutput, "UTF-8");
	    preprocessTable();
	} catch (IOException e) {
	    // do nothing
	}
	totalArea = getBuildingArea();
	extractThermalZones(htmloutput);
    }

    public void addOptimizationResult(OptResult opt) {
	optResults.addResultSet(opt);
    }

    public OptResultSet getOptimizationResults() {
	return optResults;
    }

    public OptResult duplicatedSimulationCase(OptResult result) {
	for (int i = 0; i < optResults.getSize(); i++) {
	    if (result.equals(optResults.getResult(i))) {
		return optResults.getResult(i);
	    }
	}
	return null;
    }

    public double getTotalBuildingArea() {
	return totalArea;
    }

    public void writeOutResults() {
	int row = optResults.getResultSet().size();

	try {
	    FileWriter writer = new FileWriter(
		    "E:\\02_Weili\\02_ResearchTopic\\Optimization\\output.txt");
	    for (int i = 0; i < row; i++) {
		OptResult r = optResults.getResult(i);
		writer.append(i + "@");
		writer.append(r.getOperationCost() + "@");
		writer.append(r.getFirstCost() + "@");
		for (int j = 0; j < r.getComponentLength(); j++) {
		    writer.append(r.getComponent(j));
		    writer.append("@");
		}
		writer.append("\n");
	    }
	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void replaceHVACEnergyPlus(String sysName) throws IOException {
	removeHVAC();
	HVACSystem system;
	if (sysName.equals("VAV")) {
	    PackagedVAVFactory factory = new PackagedVAVFactory(this);
	    system = factory.getSystem();
	} else if (sysName.equals("VRF")) {
	    VRFSystemFactory factory = new VRFSystemFactory(this);
	    system = factory.getSystem();
	} else {
	    VRFSystemFactory vrfFactory = new VRFSystemFactory(this);
	    DOASFactory doasFactory = new DOASFactory(this);
	    system = new SystemMerger(doasFactory.getSystem(),
		    vrfFactory.getSystem());
	}
	insertSystem(system);
    }

    /**
     * Merge the system with baseline model, this should be called after
     */
    private void insertSystem(HVACSystem system) {
	HashMap<String, ArrayList<EplusObject>> hvac = system.getSystemData();
	Set<String> hvacSet = hvac.keySet();
	Iterator<String> hvacIterator = hvacSet.iterator();
	while (hvacIterator.hasNext()) {
	    String partSystem = hvacIterator.next();
	    ArrayList<EplusObject> objectList = hvac.get(partSystem);
	    for (EplusObject eo : objectList) {
		// System.out.println(eo.getObjectName());
		String[] objectValues = new String[eo.getSize()];
		String[] objectDes = new String[eo.getSize()];
		// loop over the key-value pairs
		for (int i = 0; i < objectValues.length; i++) {
		    objectValues[i] = eo.getKeyValuePair(i).getValue();
		    objectDes[i] = eo.getKeyValuePair(i).getKey();
		}
		// add the object to the baseline model

		energyplusModel.addNewEnergyPlusObject(eo.getObjectName(),
			objectValues, objectDes);
	    }
	}
    }

    private void removeHVAC() throws IOException {
	processObjectLists();
	for (String s : objectList) {
	    energyplusModel.removeEnergyPlusObject(s);
	}
    }

    private void extractThermalZones(File html) {
	Elements thermalZoneSummary = doc.getElementsByAttributeValue("tableID",
		"Input Verification and Results Summary:Zone Summary");
	Elements zoneList = thermalZoneSummary.get(0).getElementsByTag("tr");
	thermalZoneList.clear();// clear the thermal zone list first
	int conditionIndex = 2;
	for (int i = 1; i < zoneList.size(); i++) {
	    Elements info = zoneList.get(i).getElementsByTag("td");
	    if (info.get(conditionIndex).text().equalsIgnoreCase("YES")) {
		String zoneName = info.get(0).text();
		ThermalZone temp = null;
		temp = new DesignBuilderThermalZone(zoneName);
		thermalZoneList.add(temp);
	    }
	}
	HVACSizing sizing = new HVACSizing(html);

	sizing.processThermalZone(thermalZoneList);
	processModelInfo();
	System.out.println("Success 4");
    }

    private void preprocessTable() {
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

    // HVAC objects list is read from local list file
    private void processObjectLists() throws IOException {
	File file = new File(
		getClass().getClassLoader().getResource(FILE_NAME).getFile());
	BufferedReader br = new BufferedReader(new FileReader(file));

	try {
	    StringBuilder sb = new StringBuilder();
	    String line = br.readLine();

	    while (line != null) {
		sb.append(line);
		sb.append("%");
		line = br.readLine();
	    }
	    objectList = sb.toString().split("%");
	} finally {
	    br.close();
	}
    }

    /**
     * gets the building area
     * 
     * @return
     */
    private double getBuildingArea() {
	double area;
	Elements tables = doc.getElementsByTag("table");
	for (Element table : tables) {
	    Elements texts = table.getAllElements();
	    for (int i = 0; i < texts.size(); i++) {
		if (texts.get(i).getElementsByTag("td").text()
			.equals("Total Building Area")) {
		    area = Double.parseDouble(texts.get(i + 1).text());
		    return area;
		}
	    }
	}
	return -1;
    }
}

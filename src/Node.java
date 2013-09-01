import java.util.HashMap;
import java.util.Map;

public class Node {

	private String nodeId = null;
	private String nodename = null;
	private double latitude = -1;
	private double longitude = -1;
	private double transmissionPower = -1; // power in mw
	private double centerFreqVal = -1; // frequency in MHz
	private double busyToneFreqVal = -1; // frequencey in MHz

	private double sicPowerMax = -1; // power in mw
	private double sicPowerMin = -1; // power in mw

	private int csmaBackOffMax = -1;
	private int sicBackOffMax = -1;

	private int csmaBackOff = -1;
	private int sicBackOff = -1;

	private PowerUtilities.STATE csmabackoffState = PowerUtilities.STATE.IDLE;
	private PowerUtilities.STATE sicbackoffState = PowerUtilities.STATE.IDLE;

	private Map<String, Double> nodeidVsDistance = new HashMap<String, Double>();
	private Map<String, Double> nodeidVsPathLoss = new HashMap<String, Double>();

	private int pktTransmittedInNode = 0;

	private boolean sicpowerComputed = false;

	private int slotsForSIC = 0;

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodename(String nodename) {
		this.nodename = nodename;
	}

	public String getNodename() {
		return nodename;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setTransmissionPower(double transmissionPower) {
		this.transmissionPower = transmissionPower;
	}

	public double getTransmissionPower() {
		return transmissionPower;
	}

	public void setCenterFreqVal(double centerFreqVal) {
		this.centerFreqVal = centerFreqVal;
	}

	public double getCenterFreqVal() {
		return centerFreqVal;
	}

	public void setBusyToneFreqVal(double busyToneFreqVal) {
		this.busyToneFreqVal = busyToneFreqVal;
	}

	public double getBusyToneFreqVal() {
		return busyToneFreqVal;
	}

	public void setSicPowerMax(double sicPower) {
		this.sicPowerMax = sicPower;
	}

	public double getSicPowerMax() {
		return sicPowerMax;
	}

	public void setSicPowerMin(double sicPowerMin) {
		this.sicPowerMin = sicPowerMin;
	}

	public double getSicPowerMin() {
		return sicPowerMin;
	}

	public void setBackoffvalue(int backoffvalue) {
		this.csmaBackOff = backoffvalue;
	}

	public int getBackoffvalue() {
		return csmaBackOff;
	}

	public void setSicBackOff(int sicBackOff) {
		this.sicBackOff = sicBackOff;
	}

	public int getSicBackOff() {
		return sicBackOff;
	}

	public void setNodeidVsDistance(String key, Double value) {
		nodeidVsDistance.put(key, value);
	}

	public double getDistanceForNodeId(String key) {
		return nodeidVsDistance.get(key);
	}

	public void setCsmabackoffState(PowerUtilities.STATE csmabackoffState) {
		this.csmabackoffState = csmabackoffState;
	}

	public PowerUtilities.STATE getCsmabackoffState() {
		return csmabackoffState;
	}

	public void setSicbackoffState(PowerUtilities.STATE sicbackoffState) {
		this.sicbackoffState = sicbackoffState;
	}

	public PowerUtilities.STATE getSicbackoffState() {
		return sicbackoffState;
	}

	public Map<String, Double> getNodeidVsPathLoss() {
		return nodeidVsPathLoss;
	}

	public Map<String, Double> getNodeidVsDistanceMap() {
		return nodeidVsDistance;
	}

	public void setNodeidVsPathLoss(String key, Double value) {
		nodeidVsPathLoss.put(key, value);
	}

	public double getNodeidVsPathLoss(String key) {
		return nodeidVsPathLoss.get(key);
	}

	public void setCsmaBackOffMax(int csmaBackOffMax) {
		this.csmaBackOffMax = csmaBackOffMax;
	}

	public int getCsmaBackOffMax() {
		return csmaBackOffMax;
	}

	public void setSicBackOffMax(int sicBackOffMax) {
		this.sicBackOffMax = sicBackOffMax;
	}

	public int getSicBackOffMax() {
		return sicBackOffMax;
	}

	public void setPktTransmittedInNode() {
		pktTransmittedInNode++;
	}

	public void resetPktTransmittedInNode() {
		pktTransmittedInNode = 0;
	}

	public int getPktTransmittedInNode() {
		return pktTransmittedInNode;
	}

	public boolean sicPowerComputed() {
		return sicpowerComputed;
	}

	public void setSicPowerComputed(boolean computed) {
		sicpowerComputed = computed;
	}

	public void setSlotsForSIC(int slotsForSIC) {
		this.slotsForSIC = slotsForSIC;
	}

	public int getSlotsForSIC() {
		return slotsForSIC;
	}

	public void decSlotsForSIC() {
		slotsForSIC = slotsForSIC - 1;
	}
	
	public boolean waitInIdle(){
		return sicpowerComputed && slotsForSIC == 0;
	}
}

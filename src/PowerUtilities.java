import java.util.Random;

public class PowerUtilities {

	public static String TOKEN_DELIMITER = ",";
	public static int TOKEN_COUNT = 6;
	public static int UNDEFINED = -1;

	public static int NODE_DISTANCE = 11000000; // distance in km
	public static int CSMABACKOFF = 31;
	public static int SICBACKOFF = 7;

	public static int PACKET_SIZE = 10;
	public static int SLOT_COUNT = 100;
	
	public static int SLOT_POWER_COMPUTE = 4;
	public static int SIC_IDLE = 1;

	public static int NOISE = -90;
	public static int BETA = 20;

	public enum STATE {
		IDLE, BUSY, TRANSMIT, RECIEVE, BACKOFF, DEFER
	};

	Random generator = new Random();

	public PowerUtilities() {
		readvalues();
	}

	private void readvalues() {
		// TODO Auto-generated method stub
	}

	public int getRandomValue() {
		return generator.nextInt();
	}

	public int CSMAbackoff(int maxvalue) {
		return generator.nextInt(maxvalue);
	}

	public int SICbackOffvalue(int maxvalue) {
		return generator.nextInt(maxvalue);
	}

	// FSPL (dB) = 20 log10 (d) + 20 log10 (f) + 32.44 - Gtx - Grx
	public double getPathLoss(double distance, double frequency, double txGain,
			double rxGain) {

		double distanceLoss = 20 * Math.log10(distance); // convert to m
																// from km
		double freqLoss = 20 * Math.log10(frequency);
		return 32.44 + distanceLoss + freqLoss - txGain - rxGain;
	}

	// RSSI value
	public double getRecieverPower(double txPower, double pathloss) {
		double txpowerInDb = 20 * Math.log10(txPower / 1000);
		return txpowerInDb - pathloss;
	}

	public double getSNRPower(double rxPower) {
		return rxPower + NOISE;
	}

	public double getNoiseThreshold(double rxpower) {
		return rxpower - BETA;
	}

	public double getSICPowerMax(double rxPower, double noisePower) {
		return BETA + noisePower + rxPower;
	}

	public double getSICPowerMin(double rxPower, double noisePower) {
		return (rxPower / BETA) - noisePower;
	}

	public boolean isSicWithinRange(double sicpower, double maxPower,
			double minPower) {
		return ((maxPower > sicpower) && (sicpower > minPower));
	}
	
	public double getLogValueInDB(double linearVal){
		return 20 *  Math.log10(linearVal);
	}
	
	public double getLinearValueForDB(double logVal){
		return Math.pow(10, (logVal/20));
	}

	public double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344; // miles to km conversion
		return (dist * 1000);
	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public boolean isNodeTransmitting(Node node) {
		return node.getSicbackoffState() == PowerUtilities.STATE.TRANSMIT
				|| node.getCsmabackoffState() == PowerUtilities.STATE.TRANSMIT;
	}
}

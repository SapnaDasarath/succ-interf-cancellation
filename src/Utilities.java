import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Utilities {
	public static void printnodeProperties(Map<String, Node> nodeIdVsNodeMap) {
		System.out.println("************************************");
		System.out.println("Printing node values..");
		System.out.println("************************************");
		for (Node node : nodeIdVsNodeMap.values()) {
			printNodeProperties(nodeIdVsNodeMap, node);
		}
		System.out.println("Printing node values done..");
	}

	public static void printNodeProperties(Map<String, Node> nodeIdVsNodeMap,
			Node node) {
		StringBuffer sbnode = new StringBuffer();
		for (String nodeid : node.getNodeidVsDistanceMap().keySet()) {
			sbnode.append(nodeIdVsNodeMap.get(nodeid).getNodename())
					.append("-")
					.append(node.getNodeidVsDistanceMap().get(nodeid))
					.append(",").append(node.getNodeidVsPathLoss().get(nodeid))
					.append(",");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Name::").append(node.getNodename()).append(",")
				.append("Id::").append(node.getNodeId()).append(",")
				.append("latitude::").append(node.getLatitude()).append(",")
				.append("longitude::").append(node.getLongitude()).append(",")
				.append("transmissionPower::")
				.append(node.getTransmissionPower()).append(",")
				.append("centerFreqVal::").append(node.getCenterFreqVal())
				.append(",").append("busyToneFreqVal::")
				.append(node.getBusyToneFreqVal()).append(",")
				.append("csmaBackOff::").append(node.getBackoffvalue())
				.append(",").append("sicBackOff::")
				.append(node.getSicBackOff()).append(",")
				.append("csmabackoffState::")
				.append(node.getCsmabackoffState()).append(",")
				.append("sicbackoffState::").append(node.getSicbackoffState())
				.append(",").append("sicPowerMax::")
				.append(node.getSicPowerMax()).append("sicPowerMin::")
				.append(node.getSicPowerMin()).append(",")
				.append("List of neighbours::").append(sbnode.toString());

		System.out.println(sb.toString());
	}

	public static void printnodePropertiesInCSV(
			Map<String, Node> nodeIdVsNodeMap) {
		System.out.println("************************************");
		System.out.println("Printing node values to file..");
		System.out.println("************************************");

		try {
			String currentDir = new File(".").getCanonicalPath();
			String outfile = currentDir + File.separator +"output.txt";
			System.out.println("output file = " + outfile);
			
			FileWriter writer = new FileWriter(outfile);
			BufferedWriter out = new BufferedWriter(writer);
			
			StringBuffer header = new StringBuffer();
			out.write(header.toString());

			for (Node node : nodeIdVsNodeMap.values()) {
				String val = printNodePropertiesInCSV(nodeIdVsNodeMap, node);
				out.write(val);
				out.write("\n");
			}
			out.close();
			System.out.println("Printing node values to file done..");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String printNodePropertiesInCSV(
			Map<String, Node> nodeIdVsNodeMap, Node node) {
		StringBuffer sbnode = new StringBuffer();
		for (String nodeid : node.getNodeidVsDistanceMap().keySet()) {
			sbnode.append(nodeIdVsNodeMap.get(nodeid).getNodename())
					.append("-")
					.append(node.getNodeidVsDistanceMap().get(nodeid))
					.append(",").append(node.getNodeidVsPathLoss().get(nodeid))
					.append(",");
		}
		StringBuffer sb = new StringBuffer();
		sb.append(node.getNodename()).append(",").append(node.getNodeId())
				.append(",").append(node.getLatitude()).append(",")
				.append(node.getLongitude()).append(",")
				.append(node.getTransmissionPower()).append(",")
				.append(node.getCenterFreqVal()).append(",")
				.append(node.getBusyToneFreqVal()).append(",")
				.append(node.getBackoffvalue()).append(",")
				.append(node.getSicBackOff()).append(",")
				.append(node.getCsmabackoffState()).append(",")
				.append(node.getSicbackoffState()).append(",")
				.append(node.getSicPowerMax()).append(",")
				.append(node.getSicPowerMin()).append(",")
				.append(sbnode.toString());
		System.out.println(sb.toString());
		return sb.toString();
	}
	

	public static void computeThroughput(Map<String, Node> nodeIdVsNodeMap) {
		System.out.println("************************************");
		System.out.println("Computing Throughput..");
		System.out.println("************************************");
		Utilities.printnodeProperties(nodeIdVsNodeMap);
		Utilities.printnodePropertiesInCSV(nodeIdVsNodeMap);
		System.out.println("Computing Throughput done..");

	}

}

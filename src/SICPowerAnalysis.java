import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class SICPowerAnalysis {

	private Map<String, Node> nodeIdVsNodeMap = new LinkedHashMap<String, Node>();
	private PowerUtilities.STATE CHANNEL_STATE = PowerUtilities.STATE.IDLE;
	private int pktTransmittedinChannel;
	
	private PowerUtilities util = new PowerUtilities();

	public SICPowerAnalysis(String filename) {
		if (constructNodeList(filename)) {
			initializeNodeParameters();
			//Utilities.printnodeProperties(nodeIdVsNodeMap);

			// At this point for every node we know
			// It's name and ID - for unique identification
			startTransmission();
			//Utilities.computeThroughput(nodeIdVsNodeMap);
		}
	}

	/**
	 * This method reads the contents of a file with the following information
	 * nodename,latitude,longitude,txpower,centerfrequency,busytone val
	 */
	private boolean constructNodeList(String filename) {
		try {
			FileReader reader = new FileReader(filename);
			Scanner scanner = new Scanner(reader);

			System.out.println("************************************");
			System.out.println("Reading file..");
			System.out.println("************************************");
			while (scanner.hasNext()) {
				String nextline = scanner.next();
				StringTokenizer params = new StringTokenizer(nextline,
						PowerUtilities.TOKEN_DELIMITER);
				if (params.countTokens() != PowerUtilities.TOKEN_COUNT) {
					System.out.println("Invalid input of "
							+ params.countTokens() + "in file::" + nextline);
					return false;
				}

				String nodename = params.nextToken();
				String latString = params.nextToken();
				String longString = params.nextToken();
				String txpower = params.nextToken();
				String centerFreq = params.nextToken();
				String busyToneFreq = params.nextToken();

				double latVal = PowerUtilities.UNDEFINED;
				double longval = PowerUtilities.UNDEFINED;
				double txpowerVal = PowerUtilities.UNDEFINED;
				double centerFreqVal = PowerUtilities.UNDEFINED;
				double busyToneFreqVal = PowerUtilities.UNDEFINED;

				if (latString != null && !latString.equals("")) {
					latVal = Double.parseDouble(latString);
				}

				if (longString != null && !longString.equals("")) {
					longval = Double.parseDouble(longString);
				}

				if (txpower != null && !txpower.equals("")) {
					txpowerVal = Double.parseDouble(txpower);
				}

				if (centerFreq != null && !centerFreq.equals("")) {
					centerFreqVal = Double.parseDouble(centerFreq);
				}

				if (busyToneFreq != null && !busyToneFreq.equals("")) {
					busyToneFreqVal = Double.parseDouble(busyToneFreq);
				}

				long timestamp = System.currentTimeMillis();
				String nodeid = nodename + timestamp + util.getRandomValue();
				Node node = new Node();
				node.setNodeId(nodeid); // Unique node ID
				node.setNodename(nodename); // Node name

				node.setLatitude(latVal); // location
				node.setLongitude(longval); // location
				node.setTransmissionPower(txpowerVal); // 1000 mW or 30 dBm
				node.setCenterFreqVal(centerFreqVal); // 2.4GHz 802.11
				node.setBusyToneFreqVal(busyToneFreqVal); // 1.5GHz

				nodeIdVsNodeMap.put(nodeid, node);

				StringBuffer sb = new StringBuffer();
				sb.append(nodeid).append(" ").append(nodename).append(" ")
						.append(latVal).append(" ").append(longval).append(" ")
						.append(txpowerVal).append(" ").append(centerFreqVal)
						.append(" ").append(busyToneFreqVal).append(" ");

				System.out.println("values read::" + sb.toString());

			}
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist");
		} catch (NumberFormatException ne) {
			System.out.println("Number format exception ");
		}

		System.out.println("Closing file.. node values read:: "
				+ nodeIdVsNodeMap.size());

		if (nodeIdVsNodeMap.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * This method sets the node back off counters and distance between
	 * neighbors and the power loss between them
	 */
	private void initializeNodeParameters() {
		//System.out.println("************************************");
		//System.out.println("Initializing node parameters..");
		//System.out.println("************************************");
		for (Node node : nodeIdVsNodeMap.values()) {

			// set the max csma value
			node.setCsmaBackOffMax(PowerUtilities.CSMABACKOFF);
			int csmabackoff = util.CSMAbackoff(node.getCsmaBackOffMax());
			node.setBackoffvalue(csmabackoff);

			// set the sic back off value
			node.setSicBackOffMax(PowerUtilities.SICBACKOFF);
			int sicbackoff = util.SICbackOffvalue(node.getSicBackOffMax());
			node.setSicBackOff(sicbackoff);
			
			//Set max slot wait time for SIC
			node.setSlotsForSIC(PowerUtilities.SLOT_POWER_COMPUTE);

			for (Node neighbourNode : nodeIdVsNodeMap.values()) {
				if (!node.getNodeId().equals(neighbourNode.getNodeId())) {

//					System.out.println("Computing for node "
//							+ node.getNodename() + " and "
//							+ neighbourNode.getNodename());

					if (node.getLatitude() != PowerUtilities.UNDEFINED
							&& node.getLongitude() != PowerUtilities.UNDEFINED
							&& neighbourNode.getLatitude() != PowerUtilities.UNDEFINED
							&& neighbourNode.getLongitude() != PowerUtilities.UNDEFINED) {

						double distanceBetweenNodes = util.distance(
								node.getLatitude(), node.getLongitude(),
								neighbourNode.getLatitude(),
								neighbourNode.getLongitude());
//						System.out.println("distance computed = "
//								+ distanceBetweenNodes
//								+ " node distance required = "
//								+ PowerUtilities.NODE_DISTANCE);

						if (distanceBetweenNodes < PowerUtilities.NODE_DISTANCE) {
							node.setNodeidVsDistance(neighbourNode.getNodeId(),
									distanceBetweenNodes);
							neighbourNode.setNodeidVsDistance(node.getNodeId(),
									distanceBetweenNodes);

							double pathLossFromAtoB = util.getPathLoss(
									distanceBetweenNodes,
									node.getCenterFreqVal(), 0, 0);
							node.setNodeidVsPathLoss(neighbourNode.getNodeId(),
									pathLossFromAtoB);

							double pathLossFromBtoA = util.getPathLoss(
									distanceBetweenNodes,
									neighbourNode.getCenterFreqVal(), 0, 0);
							neighbourNode.setNodeidVsPathLoss(node.getNodeId(),
									pathLossFromBtoA);

//							System.out.println("path loss from A to B = "
//									+ pathLossFromAtoB
//									+ " path loss from B to A = "
//									+ pathLossFromBtoA);

						}
					}
				}
			}
		}
	//	System.out.println("Initializing node parameters done..");
	}

	private void startTransmission() {
		System.out.println("************************************");
		System.out.println("Starting transmission..");
		System.out.println("************************************");

		// Pick a random node from the nodelist as reciever
		Set<String> allNodes = new LinkedHashSet<String>();
		allNodes.addAll(nodeIdVsNodeMap.keySet());
		String rxnodeId = allNodes.iterator().next();
		Node rxNode = nodeIdVsNodeMap.get(rxnodeId);

		// get all its neighbors and start backoff to pick the first
		// transmitter
		Set<String> allTxNodes = new LinkedHashSet<String>();
		allTxNodes.addAll(rxNode.getNodeidVsPathLoss().keySet());

		for (int i = 1; i < PowerUtilities.SLOT_COUNT; i++) {
			setChannelState(rxNode, allTxNodes);
			if (CHANNEL_STATE == PowerUtilities.STATE.IDLE) {
				channelIdleProcessing(rxNode, allTxNodes, true);
			} else if (CHANNEL_STATE == PowerUtilities.STATE.BUSY) {
				channelBusyProcessing(rxNode, allTxNodes);
				pktTransmittedinChannel++;
			}
		}
		System.out.println("************************************");
		System.out.println("Transmission done..");
		System.out.println("************************************");
	}

	private void setChannelState(Node rxNode, Set<String> allTxNodes) {
		boolean channelfree = true;

		// For all the nodes check if the node is transmitting.
		// If it is has it reached the max packet limit
		// If yes reset the packet counter and set channel state to idle.
		for (String nodeid : allTxNodes) {
			Node node = nodeIdVsNodeMap.get(nodeid);

			if (util.isNodeTransmitting(node)) {
				if (node.getPktTransmittedInNode() == PowerUtilities.PACKET_SIZE) {
					node.resetPktTransmittedInNode();
					pktTransmittedinChannel++;

					if (node.getCsmabackoffState() == PowerUtilities.STATE.TRANSMIT) {
						node.setCsmabackoffState(PowerUtilities.STATE.IDLE);
						node.setCsmaBackOffMax(PowerUtilities.CSMABACKOFF);
						node.setBackoffvalue(util.CSMAbackoff(node
								.getCsmaBackOffMax()));
						rxNode.setSicPowerComputed(false);
					}

					if (node.getSicbackoffState() == PowerUtilities.STATE.TRANSMIT) {
						node.setSicbackoffState(PowerUtilities.STATE.IDLE);
						node.setSicBackOffMax(PowerUtilities.SICBACKOFF);
						node.setSicBackOff(util.SICbackOffvalue(node
								.getSicBackOffMax()));
					}
				}
				node.setPktTransmittedInNode();
			}

			// If there is any node transmitting then the channel is not free
			if (util.isNodeTransmitting(node)) {
				channelfree = false;
			}
		}

		// If the channel is free, reset all parameters
		if (channelfree) {
			rxNode.setSicPowerComputed(false);
			CHANNEL_STATE = PowerUtilities.STATE.IDLE;
		}
	}

	private void channelIdleProcessing(Node rxNode, Set<String> allTxNodes,
			boolean setSicParam) {
		Set<String> csmacounter = new LinkedHashSet<String>();
		// If channel is idle check the back off state for all
		// the nodes that can trasnmit
		// If any node is a state with backoff counter = 0
		// set the channel state to busy and exit.
		//System.out
		//		.println("Channel is Idle, Check if any node wants to transmit");
		for (String nodeid : allTxNodes) {

			Node txnodes = nodeIdVsNodeMap.get(nodeid);

			if (!util.isNodeTransmitting(txnodes)) {
				int csmabackoffval = txnodes.getBackoffvalue();
				if (csmabackoffval == 0) {
					csmacounter.add(nodeid);
				} else {
					txnodes.setBackoffvalue(csmabackoffval - 1);
					txnodes.setCsmabackoffState(PowerUtilities.STATE.BACKOFF);
				}
			}
		}

		if (csmacounter.size() == 1) {
			// This is the winner in this slot. let it trasnmit
			// change channel state and back off window size
			String nodeid = csmacounter.iterator().next();
			CHANNEL_STATE = PowerUtilities.STATE.BUSY;

			Node node = nodeIdVsNodeMap.get(nodeid);

			System.out.println("Idle channel, " + node.getNodename()
					+ " has won backoff. It is now the transmitter");

			// Set the required CSMA and SIC states.
			node.setCsmabackoffState(PowerUtilities.STATE.TRANSMIT);
			node.setCsmaBackOffMax(PowerUtilities.CSMABACKOFF);
			node.setBackoffvalue(util.CSMAbackoff(node.getCsmaBackOffMax()));

			if (setSicParam) {
				node.setSicbackoffState(PowerUtilities.STATE.IDLE);
				node.setSicBackOffMax(PowerUtilities.SICBACKOFF);
				node.setSicBackOff(util.SICbackOffvalue(node.getSicBackOffMax()));
			}

			Set<String> allTxNodesTodefer = new LinkedHashSet<String>();
			allTxNodesTodefer.addAll(allTxNodes);
			allTxNodesTodefer.remove(nodeid);

			// Set all other nodes to defer state
			for (String othernodeid : allTxNodesTodefer) {
				Node othernodes = nodeIdVsNodeMap.get(othernodeid);
				if (othernodes.getCsmabackoffState() == PowerUtilities.STATE.BACKOFF) {

					// leave the backoff counters alone since they should freeze
					// The other nodes will now go into backoff state for SIC
					othernodes.setCsmabackoffState(PowerUtilities.STATE.DEFER);

					if (setSicParam) {
						node.setSicbackoffState(PowerUtilities.STATE.IDLE);
					}
				}
			}

		} else if (csmacounter.size() > 1) {
			// If there are multiple nodes that want to transmit in this
			// slot, this implies collision.
			// double the backoff counter and set new backoff values

			System.out
					.println("Idle channel, Multiple nodes want to transmit.. Collision detected.");
			for (String nodeid : csmacounter) {

				CHANNEL_STATE = PowerUtilities.STATE.IDLE;

				Node node = nodeIdVsNodeMap.get(nodeid);

				// Set the CSMA state to IDLE and increase back off
				node.setCsmabackoffState(PowerUtilities.STATE.IDLE);
				node.setCsmaBackOffMax(node.getCsmaBackOffMax() * 2);
				node.setBackoffvalue(util.CSMAbackoff(node.getCsmaBackOffMax()));

				// Set the SIC state to IDLE
				if (setSicParam) {
					node.setSicbackoffState(PowerUtilities.STATE.IDLE);
				}
			}
		}
	}

	private void channelBusyProcessing(Node rxNode, Set<String> allTxNodes) {
		Set<String> siccounter = new LinkedHashSet<String>();
		// If channel is idle check the back off state for all
		// the nodes that can transmit
		// If any node is a state with backoff counter = 0
		// set the channel state to busy and exit.
		if (rxNode.sicPowerComputed() && rxNode.getSlotsForSIC() == 0 ) {
			rxNode.setSlotsForSIC(PowerUtilities.SLOT_POWER_COMPUTE);
			//keep this slot IDLE so return;
			return;
		}
		if (rxNode.sicPowerComputed() && rxNode.getSlotsForSIC() == PowerUtilities.SLOT_POWER_COMPUTE ) {
			//System.out
					//.println("SIC power is already computed, Check if there are any nodes that can trasnmit");
		
			double maxpower = rxNode.getSicPowerMax();
			double minpower = rxNode.getSicPowerMin();

			for (String nodeid : allTxNodes) {

				Node node = nodeIdVsNodeMap.get(nodeid);

				if (!util.isNodeTransmitting(node)) {

					if (node.getSicbackoffState() == PowerUtilities.STATE.IDLE) {
						// Check if the transmission power of the node lies
						// either
						// above the the max power or below the min power
						// If yes move it to back off
						if (node.getTransmissionPower() > maxpower
								|| node.getTransmissionPower() < minpower) {
							node.setSicbackoffState(PowerUtilities.STATE.BACKOFF);
						}
					} else if (node.getSicbackoffState() == PowerUtilities.STATE.BACKOFF) {
						// If there are any nodes that can go to backoff state
						// add them to backoff counter
						int sicbackoffval = node.getSicBackOff();
						if (sicbackoffval == 0) {
							siccounter.add(nodeid);
						} else {
							node.setSicBackOff(sicbackoffval - 1);
						}
					}
				}
			}

			if (siccounter.size() == 1) {
				// This is the winner in this slot. let it trasnmit
				// change channel state and back off window size
				CHANNEL_STATE = PowerUtilities.STATE.BUSY;

				String nodeid = siccounter.iterator().next();

				Node node = nodeIdVsNodeMap.get(nodeid);

				System.out.println("Busy channel, " + node.getNodename()
						+ " has won SIC backoff. It is now the trasnmitter");

				// This node is the winner of SIC back off
				// Set the CSMA state to IDLE and set back off values
				node.setCsmabackoffState(PowerUtilities.STATE.IDLE);
				node.setCsmaBackOffMax(PowerUtilities.CSMABACKOFF);
				node.setBackoffvalue(util.CSMAbackoff(node.getCsmaBackOffMax()));

				// Set the SIC values.
				node.setSicbackoffState(PowerUtilities.STATE.TRANSMIT);
				node.setSicBackOffMax(PowerUtilities.SICBACKOFF);
				node.setSicBackOff(util.SICbackOffvalue(node.getSicBackOffMax()));

				// Move all other nodes to defer state
				for (String othernodeid : allTxNodes) {
					Node othernode = nodeIdVsNodeMap.get(othernodeid);
					if (!util.isNodeTransmitting(othernode)) {
						othernode
								.setSicbackoffState(PowerUtilities.STATE.DEFER);
					}
				}
			} else if (siccounter.size() > 1) {
				// If there are multiple nodes that want to transmit in this
				// slot, this implies collision.
				// double the backoff counter and set new backoff values
				System.out
						.println("Busy channel, Multiple nodes want to transmit in SIC.. Collision detected.but do nothing");
				// for (String nodeid : siccounter) {
				// Node node = nodeIdVsNodeMap.get(nodeid);
				//
				// if (!util.isNodeTransmitting(node)) {
				//
				// node.setSicbackoffState(PowerUtilities.STATE.IDLE);
				// node.setSicBackOffMax(node.getSicBackOffMax() * 2);
				// node.setSicBackOff(util.SICbackOffvalue(node
				// .getSicBackOffMax()));
				// }
				// }
			}
		} else {

			if (!rxNode.sicPowerComputed() && rxNode.getSlotsForSIC() == PowerUtilities.SLOT_POWER_COMPUTE) {
				for (String nodeid : allTxNodes) {
					//System.out.println("Busy channel, Computing SIC power..");
					Node node = nodeIdVsNodeMap.get(nodeid);

					boolean anyNodeCSMATx = false;
					if (node.getCsmabackoffState() == PowerUtilities.STATE.TRANSMIT) {

						double rxpower = util.getRecieverPower(node
								.getTransmissionPower(), rxNode
								.getNodeidVsPathLoss().get(nodeid));
						double noisePower = util.getNoiseThreshold(rxpower);
						double sicpowermax = util.getSICPowerMax(rxpower,
								noisePower);
						double sicpowermin = util.getSICPowerMax(rxpower,
								noisePower);

						rxNode.setSicPowerMax(sicpowermax);
						rxNode.setSicPowerMin(sicpowermin);

						rxNode.setSicPowerComputed(true);
						rxNode.decSlotsForSIC();

//						System.out.println("Current reciever power = "
//								+ rxpower);
//						System.out.println("Current Noise power = "
//								+ noisePower);
//						System.out.println("Current Sic Max power = "
//								+ sicpowermax);
//						System.out.println("Current Sic Min power = "
//								+ sicpowermin);

						anyNodeCSMATx = true;
					}

					if (anyNodeCSMATx) {
						// do normal csma
						channelIdleProcessing(rxNode, allTxNodes, false);
					}
				}
			}
			else if (rxNode.sicPowerComputed() && rxNode.getSlotsForSIC() != PowerUtilities.SLOT_POWER_COMPUTE)
			{
				//BAck off and stay idle for the next 4 slots
				rxNode.decSlotsForSIC();
			}
		}
	}

	public static void main(String[] args) {
		String filename = null;
		if (args.length == 0) {
			try {
				String currentDir = new File(".").getCanonicalPath();
				filename = currentDir + File.separator + "input.txt";
				System.out.println("Using default file " + filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			filename = args[0];
			System.out.println("Using user file " + filename);
		}
		new SICPowerAnalysis(filename);
	}
}

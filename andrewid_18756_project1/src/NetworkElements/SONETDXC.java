package NetworkElements;

import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import DataTypes.*;

public class SONETDXC extends Switch {
	private String address = null;
	private ArrayList<OpticalNIC> NICs = new ArrayList<OpticalNIC>();
	private TreeMap<String, Integer> destinationFrequencies = new TreeMap<String, Integer>();
	private TreeMap<Integer, ArrayList<Integer>> destinationNextHop = new TreeMap<Integer, ArrayList<Integer>>();

	// All receiving packets will be put into packet buffer first
	// and while send the data, it will send at most 3 packets
	// If the STS3packet meeting this condition are fewer than 3, use null to hold
	// the place
	// We will simulate it in packets format, and the buffer will help us when
	// aggregate the data into STS3packet

	private Queue<STS1Packet> sendBuffer = new LinkedList<>();

	private Boolean trace = true;
	private ArrayList<Integer> dropFrequency = new ArrayList<Integer>();
	private Boolean cwHopCanSendCW = true;
	private Boolean acwHopCanSendACW = true;

	/**
	 * Construct a new SONET DXC with a given address
	 * 
	 * @param address the address of the new SONET router
	 */
	public SONETDXC(String address) {
		if (address == null || address.equals(""))
			System.err.println("Error (SONETDXC): The router must have an address");

		this.address = address;
	}

	/**
	 * This method processes a packet when it is received from any location
	 * (including being created on this router
	 * from the source method). It either drops the packet from the line, or
	 * forwards it around the ring
	 * 
	 * @param packet     the SONET packet to be processed
	 * @param wavelength the wavelength the packet was received on
	 * @param nic        the NIC the packet was received on
	 */
	public void receivePackets(Packet packet, int wavelength, OpticalNIC nic) {
		if (packet instanceof STS1Packet) {
			STS1Packet sts1 = (STS1Packet) packet;
			if (dropFrequency.contains(wavelength)) {
				// if both working and protection are working fine, then sink only from working
				// else sink from protection
				if (nic != null) {
					if (!nic.getHasError())
						this.sink(sts1, wavelength);
					else if (nic.getProtectionNIC() != null) {
						if (!nic.getProtectionNIC().getHasError())
							this.sink(sts1, wavelength);
					}
				}
			} else {
				this.sendBuffer.add(sts1);
			}
			return;
		}

		if (packet instanceof STS3Packet) {

			// Unpack STS3 into STS1 packets and buffer them for later
			STS3Packet sts3Packet = (STS3Packet) packet;
			List<STS1Packet> sts1Packets = sts3Packet.getPackets(); // Assume unpack returns an array

			for (STS1Packet sts1 : sts1Packets) {
				if (sts1 == null)
					break;

				if (dropFrequency.contains(wavelength) || !destinationFrequencies.containsValue(wavelength)) {
					if (nic != null) {
						if (!nic.getHasError())
							this.sink(sts1, wavelength);
						else if (!nic.getProtectionNIC().getHasError())
							this.sink(sts1, wavelength);
					}

				} else {
					sts1.setNic(nic);
					this.sendBuffer.add(sts1); // Buffer each unpacked STS1 packet

				}
			}
		}

		else {
			System.out.println("Packet type not recognized.");
		}
	}

	/**
	 * Sends a packet out onto the ring that this SONET DXC is joined to
	 * 
	 * @param packet     the packet to be sent
	 * @param wavelength the wavelength to send the packet on
	 * @param nic        the wavelength this packet originally came from (as we
	 *                   don't want to send it back to the sender)
	 */

	public void sendRingPacket(STS3Packet packet, int wavelength, OpticalNIC nic) {
		// Loop through the interfaces sending the packet on interfaces that are on the
		// ring
		// except the one it was received on. Basically what UPSR does
		ArrayList<OpticalNIC> eligibleNics = new ArrayList<>();

		// Collect eligible NICs
		for (OpticalNIC NIC : NICs) {
			// Check if the NIC is on the ring
			if (NIC.getIsOnRing() && NIC != null) {
				eligibleNics.add(NIC);
			}
		}

		// If nic is null, send packet to all eligible NICs
		if (nic == null) {
			for (OpticalNIC NIC : eligibleNics) {
				STS3Packet packetCopy = packet.copy(); // Create a new copy for each NIC
				NIC.sendPacket(packetCopy, wavelength); // Send the packet to the NIC
			}
		} else {
			// Send packets only on NICs where isClockWise is true
			for (OpticalNIC NIC : eligibleNics) {
				if (NIC.getIsClockwise()) {
					STS3Packet packetCopy = packet.copy(); // Create a new copy for each NIC
					NIC.sendPacket(packetCopy, wavelength); // Send the packet to the NIC
				}
			}
		}
	}

	/**
	 * Send out the packets from buffer, form STS3Packets and send to NICs
	 */

	public void sendPackets() {
		Map<Integer, List<STS1Packet>> destPackets = new HashMap<>();
		// Check the size of the sendBuffer
		if (this.sendBuffer.size() > 3) {
			// If buffer size is greater than 3, remove only the first 3 packets
			for (int i = 0; i < 3; i++) {
				STS1Packet pkt = this.sendBuffer.remove();
				destPackets.computeIfAbsent(pkt.getDest(), k -> new ArrayList<>()).add(pkt);
			}
		} else if (this.sendBuffer.size() > 0) {
			// If buffer size is between 1 and 3, process the existing packets
			while (!this.sendBuffer.isEmpty()) {
				STS1Packet pkt = this.sendBuffer.remove();
				destPackets.computeIfAbsent(pkt.getDest(), k -> new ArrayList<>()).add(pkt);
			}
		}

		for (Map.Entry<Integer, List<STS1Packet>> entry : destPackets.entrySet()) {
			List<STS1Packet> pkts = entry.getValue();
			for (int i = 0; i < pkts.size(); i += 3) {
				STS1Packet sts1 = null, sts2 = null, sts3 = null;
				sts1 = pkts.get(i);
				if (i + 1 < pkts.size()) {
					sts2 = pkts.get(i + 1);
				}
				if (i + 2 < pkts.size()) {
					sts3 = pkts.get(i + 2);
				}
				this.sendRingPacket(new STS3Packet(sts1, sts2, sts3), entry.getKey(), sts1.getNic());
			}
		}

	}

	/**
	 * create a STS1Packet from this DXC on a given wavelength. This is not a real
	 * SONET
	 * feature. We are using this to input to packets to simulate the data
	 * generation.
	 * 
	 * @param payload to put in the buffer
	 */
	public void create(STS1Packet payload) {
		int sourceWavelength = destinationFrequencies.get(address);
		payload.setSource(sourceWavelength);

		System.out
				.println("(SONET DXC) " + address + " has created a STS1Packet from wavelength " + payload.getSource() +
						" to " + payload.getDest() + "with payload" + payload.getPayload());
		this.checkSegmentation(payload);
	}

	/**
	 * STS1Packet has the length of 5 on its payload
	 * Check the payload length, process segmentation if necessary and create new
	 * STS1packets to buffer when necessary
	 * 
	 * @param payload to put in the buffer
	 */
	public void checkSegmentation(STS1Packet payload) {
		// TODO: Need to check segmentation for char_length>5 (3b)
		final int MAX_LEN = payload.getPayloadLengthLimit();
		String payloadString = payload.getPayload();
		int payloadLength = payloadString.length();

		/*
		 * Split the bigger payload into multiple STS1 frames and add to the DXC buffer
		 * for processing
		 */

		for (int i = 0; i < payloadLength; i += MAX_LEN) {
			String payloadSegment = payloadString.substring(i, Math.min(i + MAX_LEN, payloadLength));
			STS1Packet newSTS1Packet = new STS1Packet(payloadSegment, payload.getDest());
			this.sendBuffer.add(newSTS1Packet);

		}
	}

	/**
	 * Destroys a packet and prints an output with the packets details. SONET
	 * doesn't really do this, we use this
	 * feature to remove packets as we don't have anywhere to really send them
	 * 
	 * @param packet     the packet to be destroyed and printed
	 * @param wavelength the wavelength the packet was received on
	 */
	public void sink(STS1Packet packet, int wavelength) {
		System.out
				.println("(SONETDXC) " + address + " has received a STS1 packet at the sink on wavelength " + wavelength
						+ " " + packet.toString());
	}

	/**
	 * Adds a wavelength that will be dropped by this router instead of being
	 * forwarded on the ring
	 * 
	 * @param wavelength the wavelength to be dropped
	 */
	public void addDropWavelength(int wavelength) {
		this.dropFrequency.add(wavelength);
	}

	/**
	 * Adds a NIC to this router
	 * 
	 * @param nic the nic to be added to the router
	 */
	public void addNIC(OpticalNIC nic) {
		if (nic.getParent() != this)
			System.err.println("Error (SONETDXC): You cannot add an NIC to a router it was not made for");

		NICs.add(nic);
	}

	/**
	 * Gets the string address of this router
	 * 
	 * @return the address of this router
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Tells the router a wavelength that another router can be contacted on. Adding
	 * the routers own
	 * address and a wave length will let the router know that a wavelength is to be
	 * sent to the sink
	 * 
	 * @param destAddress the destination address that the wavelength will go to
	 * @param wavelength  the wavelength the destination can be contacted on
	 */
	public void addDestinationFrequency(String destAddress, int wavelength) {
		destinationFrequencies.put(destAddress, wavelength);
	}

	/**
	 * Tells the router the next interface it should use to get to the destination.
	 * There is a list as
	 * The most preferred interface may be down
	 * 
	 * @param destAddress the destination address that the wavelength will go to
	 * @param wavelength  the wavelength the destination can be contacted on
	 */
	public void addDestinationHopCount(Integer wavelength, ArrayList<Integer> nextHop) {
		destinationNextHop.put(wavelength, nextHop);
	}

	/**
	 * Sets all of the destination frequencies at once, so that they don't have to
	 * be repeatedly created for every
	 * router
	 * 
	 * @param destinationFrequencies the TreeMap of all of the destination
	 *                               frequencies
	 */
	public void setDestinationFrequencies(TreeMap<String, Integer> destinationFrequencies) {
		this.destinationFrequencies = destinationFrequencies;
	}
}

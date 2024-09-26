package NetworkElements;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;

import DataTypes.*;

public class HomeRouter extends Switch {

	private String address = null;
	private OpticalNIC nic = null;

	private Queue<STS1Packet> sendBuffer = new LinkedList<>();

	/**
	 * Construct a new HomeRouter with a given address
	 * 
	 * @param address the address of the new HomeRouter
	 */
	public HomeRouter(String address) {
		if (address == null || address.equals(""))
			System.err.println("Error (HomeRouter): The router must have an address");

		this.address = address;
	}

	public void addNIC(OpticalNIC nic) {
		this.nic = nic;
	}

	/**
	 * create a STS1 Packet from this router to the linked DXC
	 * 
	 * @param payload to put in the buffer
	 */
	public void create(STS1Packet payload) {

		System.out.println("(HomeRouter) " + address + " has created a STS1Packet to wavelength " + payload.getDest()
				+ "with payload" + payload.getPayload());
		checkSegmentation(payload);
	}

	/**
	 * STS1Packet has the length of 5 on its payload
	 * Check the payload length, process segmentation if necessary and create new
	 * STS1packets to buffer when necessary
	 * 
	 * @param payload to put in the buffer
	 */
	public void checkSegmentation(STS1Packet payload) {
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
	 * Send out the STS1Packets from buffer
	 */
	public void sendPackets() {
		OpticalNIC nic = this.nic;
		Map<Integer, List<STS1Packet>> destPackets = new HashMap<>();
		if (!this.sendBuffer.isEmpty()) {
			STS1Packet pkt = this.sendBuffer.remove();
			destPackets.computeIfAbsent(pkt.getDest(), k -> new ArrayList<>()).add(pkt);
			// TODO
		}

	}

	/**
	 * Gets the string address of this router
	 * 
	 * @return the address of this router
	 */
	public String getAddress() {
		return this.address;
	}

}

import java.util.ArrayList;
import java.util.List;

import DataTypes.*;
import NetworkElements.HomeRouter;
import NetworkElements.OpticalNIC;
import NetworkElements.OtoOLink;
import NetworkElements.SONETDXC;
import NetworkElements.Switch;

public class q3 {
	private int time = 0;
	private List<Switch> allSwitch = new ArrayList<Switch>();

	public void twoRings() {
		SONETDXC DXC1 = new SONETDXC("00:11:22");
		SONETDXC DXC2 = new SONETDXC("88:77:66");
		SONETDXC DXC3 = new SONETDXC("33:44:55");
		allSwitch.add(DXC1);
		allSwitch.add(DXC2);
		allSwitch.add(DXC3);

		// tell DXCs a wavelength to add/drop on (in this case their own frequencies)
		DXC1.addDropWavelength(1310);
		DXC2.addDropWavelength(1490);
		DXC3.addDropWavelength(1550);

		// tell DXC 1 the wavelength each DXC is add/dropping on
		DXC1.addDestinationFrequency("00:11:22", 1310);
		DXC1.addDestinationFrequency("88:77:66", 1490);
		DXC1.addDestinationFrequency("33:44:55", 1550);

		// tell DXC 2 the wavelength each DXC is add/dropping on
		DXC2.addDestinationFrequency("00:11:22", 1310);
		DXC2.addDestinationFrequency("88:77:66", 1490);
		DXC2.addDestinationFrequency("33:44:55", 1550);

		// tell DXC 3 the wavelength each DXC is add/dropping on
		DXC3.addDestinationFrequency("00:11:22", 1310);
		DXC3.addDestinationFrequency("88:77:66", 1490);
		DXC3.addDestinationFrequency("33:44:55", 1550);

		// Create an interface for each DXC -> each DXC has 2 NICs -> 1 for sending ; 1
		// for receiving
		OpticalNIC nicDXC11 = new OpticalNIC(DXC1);
		nicDXC11.setID(11);
		OpticalNIC nicDXC12 = new OpticalNIC(DXC1);
		nicDXC12.setID(12);

		OpticalNIC nicDXC21 = new OpticalNIC(DXC2);
		nicDXC21.setID(21);
		OpticalNIC nicDXC22 = new OpticalNIC(DXC2);
		nicDXC22.setID(22);

		OpticalNIC nicDXC31 = new OpticalNIC(DXC3);
		nicDXC31.setID(31);
		OpticalNIC nicDXC32 = new OpticalNIC(DXC3);
		nicDXC32.setID(32);

		// Set Protection NIC for Working NIC
		nicDXC11.setIsProtection(nicDXC12);
		nicDXC12.setIsProtection(nicDXC11);
		nicDXC22.setIsProtection(nicDXC21);
		nicDXC21.setIsProtection(nicDXC22);
		nicDXC32.setIsProtection(nicDXC31);
		nicDXC31.setIsProtection(nicDXC32);

		// Set Working NIC for Protection NIC
		nicDXC11.setIsWorking(nicDXC12);
		nicDXC12.setIsWorking(nicDXC11);
		nicDXC22.setIsWorking(nicDXC21);
		nicDXC21.setIsWorking(nicDXC22);
		nicDXC31.setIsWorking(nicDXC32);
		nicDXC32.setIsWorking(nicDXC31);
<<<<<<< Updated upstream
=======
		// Set clockwise rings
		nicDXC11.setClockwise(true);
		nicDXC22.setClockwise(true);
		nicDXC32.setClockwise(true);
>>>>>>> Stashed changes

		// Create three-uni directional links between the DXCs

		// Working path links
		OtoOLink One1ToTwo1 = new OtoOLink(nicDXC11, nicDXC21);
		OtoOLink Two2ToThree1 = new OtoOLink(nicDXC22, nicDXC31);
		OtoOLink Three2ToOne2 = new OtoOLink(nicDXC32, nicDXC12);

		// Protection path links
		OtoOLink Three1ToTwo2 = new OtoOLink(nicDXC31, nicDXC22);
		OtoOLink Two1ToOne1 = new OtoOLink(nicDXC21, nicDXC11);
		OtoOLink One2ToThree2 = new OtoOLink(nicDXC12, nicDXC32);

		// Set IN and OUT Link for each NIC
		nicDXC11.setInLink(Two1ToOne1);
		nicDXC11.setOutLink(One1ToTwo1);

		nicDXC12.setInLink(Three2ToOne2);
		nicDXC12.setOutLink(One2ToThree2);

		nicDXC21.setInLink(One1ToTwo1);
		nicDXC21.setOutLink(Two1ToOne1);

		nicDXC22.setInLink(Three1ToTwo2);
		nicDXC22.setOutLink(Two2ToThree1);

		nicDXC31.setInLink(Two2ToThree1);
		nicDXC31.setOutLink(Three1ToTwo2);

		nicDXC32.setInLink(One2ToThree2);
		nicDXC32.setOutLink(Three2ToOne2);

<<<<<<< Updated upstream
		// q3
		HomeRouter DXC1hr1 = new HomeRouter("DXC1hr1");
		HomeRouter DXC1hr2 = new HomeRouter("DXC1hr2");
		HomeRouter DXC2hr1 = new HomeRouter("DXC2hr1");
=======
		// Create packets to go to 1490 destination which is DXC2

		// DXC1.create(new STS1Packet("11", 1490));
		// DXC1.create(new STS1Packet("11", 1490));
		// DXC1.create(new STS1Packet("11", 1490));

		// Create packets to go to 1550 destination which is DXC3 -> This has to go via
		// ring 1->2->3
		// DXC1.create(new STS1Packet("11223344556", 1550)); // due to segmentation,this
		// would be split into 3 packets.

		// DXC3.create(new STS1Packet("11", 1310));
		// To test UPSR, cut working link between A->B
		// Two1ToOne1.cutLink();
		// Three2ToOne2.cutLink();

		HomeRouter DXC1hr1 = new HomeRouter("DXC1hr1");
		HomeRouter DXC1hr2 = new HomeRouter("DXC1hr2");
		HomeRouter DXC2hr1 = new HomeRouter("DXC1hr3");
>>>>>>> Stashed changes
		allSwitch.add(DXC1hr1);
		allSwitch.add(DXC1hr2);
		allSwitch.add(DXC2hr1);

		OpticalNIC nicDXC1hr1 = new OpticalNIC(DXC1hr1);
		nicDXC1hr1.setID(41);
		OpticalNIC nichr1DXC1 = new OpticalNIC(DXC1);
		nichr1DXC1.setID(13);
<<<<<<< Updated upstream
		nichr1DXC1.setIsOnRing(false);
		OpticalNIC nicDXC1hr2 = new OpticalNIC(DXC1hr2);
		nicDXC1hr1.setID(51);
		OpticalNIC nichr2DXC1 = new OpticalNIC(DXC1);
		nichr2DXC1.setID(14);
		nichr2DXC1.setIsOnRing(false);
=======
		OpticalNIC nicDXC1hr2 = new OpticalNIC(DXC1hr2);
		nicDXC1hr2.setID(51);
		OpticalNIC nichr2DXC1 = new OpticalNIC(DXC1);
		nichr2DXC1.setID(14);
>>>>>>> Stashed changes
		OpticalNIC nicDXC2hr1 = new OpticalNIC(DXC2hr1);
		nicDXC2hr1.setID(61);
		OpticalNIC nichr1DXC2 = new OpticalNIC(DXC2);
		nichr1DXC2.setID(23);
<<<<<<< Updated upstream
		nichr1DXC2.setIsOnRing(false);

		// Create bi-directional Links and set out links
		OtoOLink Hr1ToDXC1 = new OtoOLink(nicDXC1hr1, nichr1DXC1);
		// OtoOLink DXC1ToHr1 = new OtoOLink(nichr1DXC1, nicDXC1hr1);
		nicDXC1hr1.setOutLink(Hr1ToDXC1);

		OtoOLink Hr2ToDXC1 = new OtoOLink(nicDXC1hr2, nichr2DXC1);
		// OtoOLink DXC1ToHr2 = new OtoOLink(nichr2DXC1, nicDXC1hr2);
		nicDXC1hr2.setOutLink(Hr2ToDXC1);

		OtoOLink Hr3ToDXC2 = new OtoOLink(nicDXC2hr1, nichr1DXC2);
		// OtoOLink DXC2ToHr3 = new OtoOLink(nichr1DXC2, nicDXC2hr1);
		nicDXC2hr1.setOutLink(Hr3ToDXC2);

		// DXC1hr1.create(new STS1Packet("aa", 1490));
		DXC1hr1.create(new STS1Packet("aaaaab", 1490));
		// DXC1hr2.create(new STS1Packet("cccccd", 1490));
		// DXC2hr1.create(new STS1Packet("eeeeef", 1490));

		// // Create packets to go to 1490 destination which is DXC2
		// DXC1.create(new STS1Packet("11", 1490));

		// // Create packets to go to 1550 destination which is DXC3 -> This has to go
		// via
		// // ring 1->2->3
		// DXC1.create(new STS1Packet("11223344556", 1550)); // due to segmentation,this
		// would be split into 3 packets.

		// // Create packet to go from 33:44:55 to 00:11:22
		// DXC3.create(new STS1Packet("11", 1310));
		// // To test UPSR, cut working link between A->B
		// One1ToTwo1.cutLink();
		// // Three2ToOne2.cutLink();

=======

		// Set On Ring to false
		nichr1DXC1.setIsOnRing(false);
		nichr2DXC1.setIsOnRing(false);
		nichr1DXC2.setIsOnRing(false);

		OtoOLink Hr1ToDXC1 = new OtoOLink(nicDXC1hr1, nichr1DXC1);
		OtoOLink Hr2ToDXC1 = new OtoOLink(nicDXC1hr2, nichr2DXC1);
		OtoOLink Hr2ToDXC2 = new OtoOLink(nichr1DXC2, nicDXC2hr1);

		// Set outlinks for Home Router NIC
		nicDXC1hr1.setOutLink(Hr1ToDXC1);
		nicDXC1hr2.setOutLink(Hr2ToDXC1);
		nicDXC2hr1.setOutLink(Hr2ToDXC2);

		nichr1DXC1.setInLink(Hr1ToDXC1);

		DXC1hr1.create(new STS1Packet("aaaaab", 1490));
		// DXC1hr1.create(new STS1Packet("aaaaab", 1490));
		// DXC1hr2.create(new STS1Packet("cccccd", 1490));
		// DXC2hr1.create(new STS1Packet("eeeeef", 1490));

>>>>>>> Stashed changes
		for (int i = 0; i < 10; i++) {
			tock();
		}

	}

	public void tock() {
<<<<<<< Updated upstream
		System.out.println("** TIME = " + time + "");
=======
		System.out.println("** TIME = " + time + " **");
>>>>>>> Stashed changes
		time++;
		for (int i = 0; i < this.allSwitch.size(); i++) {
			allSwitch.get(i).sendPackets();
		}
	}

	public static void main(String args[]) {
		q3 go = new q3();
		go.twoRings();
	}
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes

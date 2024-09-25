import java.util.ArrayList;
import java.util.List;

import DataTypes.*;
import NetworkElements.OpticalNIC;
import NetworkElements.OtoOLink;
import NetworkElements.SONETDXC;
import NetworkElements.Switch;

public class q2a {
	private int time = 0;
	private List<Switch> allSwitch = new ArrayList<Switch>();

	public void ring() {
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

		// Create packets to go to 1490 destination which is DXC2

		// DXC1.create(new STS1Packet("11", 1490));
		// DXC1.create(new STS1Packet("11", 1490));
		// DXC1.create(new STS1Packet("11", 1490));

		// Create packets to go to 1550 destination which is DXC3 -> This has to go via
		// ring 1->2->3
		// DXC1.create(new STS1Packet("11223344556", 1550)); // due to segmentation,this
		// would be split into 3 packets.

		DXC3.create(new STS1Packet("11", 1310));
		// To test UPSR, cut working link between A->B
		// One1ToTwo1.cutLink();

		for (int i = 0; i < 10; i++) {
			tock();
		}

	}

	public void tock() {
		System.out.println("** TIME = " + time + " **");
		time++;
		for (int i = 0; i < this.allSwitch.size(); i++) {
			allSwitch.get(i).sendPackets();
		}
	}

	public static void main(String args[]) {
		q2a go = new q2a();
		go.ring();
	}
}

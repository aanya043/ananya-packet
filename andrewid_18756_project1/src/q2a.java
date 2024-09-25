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

		// Create an interface for each DXC
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
		OtoOLink OneToTwo1 = new OtoOLink(nicDXC11, nicDXC21);
		OtoOLink OneToTwo2 = new OtoOLink(nicDXC12, nicDXC22);
		OtoOLink OneToThree1 = new OtoOLink(nicDXC11, nicDXC31);
		OtoOLink OneToThree2 = new OtoOLink(nicDXC11, nicDXC32);

		OtoOLink TwoToOne1 = new OtoOLink(nicDXC21, nicDXC11);
		OtoOLink TwoToOne2 = new OtoOLink(nicDXC22, nicDXC12);
		OtoOLink TwoToThree1 = new OtoOLink(nicDXC21, nicDXC31);
		OtoOLink TwoToThree2 = new OtoOLink(nicDXC22, nicDXC32);

		OtoOLink ThreeToOne1 = new OtoOLink(nicDXC31, nicDXC11);
		OtoOLink ThreeToOne2 = new OtoOLink(nicDXC32, nicDXC12);
		OtoOLink ThreeToTwo1 = new OtoOLink(nicDXC31, nicDXC21);
		OtoOLink ThreeToTwo2 = new OtoOLink(nicDXC32, nicDXC22);

		// Create packets to go to 1490 destination which is DXC2

		DXC1.create(new STS1Packet("1122", 1550));

		// Create packets to go to 1550 destination which is DXC3 -> This has to go via
		// ring 1->2->3
		// DXC1.create(new STS1Packet("1122", 1550));
		// DXC1.create(new STS1Packet("12345", 1550));
		// DXC1.create(new STS1Packet("11", 1550));

		/*
		 * Test Question 2: Link broken with UPSR restoration
		 * 
		 * OneToTwo1.cutLink();
		 */
		// OneToTwo1.cutLink();

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

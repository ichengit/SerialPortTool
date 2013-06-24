import java.util.Enumeration;
import java.util.LinkedList;

import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

public class Comm {
	static SerialPort serialPort;
	static CommPort commPort;
	
	private LinkedList<CommPortIdentifier> ports;
	
	/*��ȡ���д���*/
	public LinkedList<CommPortIdentifier> getSerialPortsIdentifiers(){
		ports = new LinkedList<CommPortIdentifier>();	//�����˿�����
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();		//��ȡ�����ϵ����ж˿�
		while(portEnum.hasMoreElements()){	//�������ж˿�
			CommPortIdentifier portId = (CommPortIdentifier)portEnum.nextElement();
			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL)	//��Ϊ�������������
				ports.add(portId);
		}
		return ports;
	}

}



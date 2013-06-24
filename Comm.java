import java.util.Enumeration;
import java.util.LinkedList;

import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

public class Comm {
	static SerialPort serialPort;
	static CommPort commPort;
	
	private LinkedList<CommPortIdentifier> ports;
	
	/*获取所有串口*/
	public LinkedList<CommPortIdentifier> getSerialPortsIdentifiers(){
		ports = new LinkedList<CommPortIdentifier>();	//创建端口链表
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();		//获取机器上的所有端口
		while(portEnum.hasMoreElements()){	//遍历所有端口
			CommPortIdentifier portId = (CommPortIdentifier)portEnum.nextElement();
			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL)	//若为串口则加入链表
				ports.add(portId);
		}
		return ports;
	}

}



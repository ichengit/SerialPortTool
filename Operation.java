import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Operation {
	
	private final int MAX_MESSAGE_LEN = 50;
	private char[] message = new char[MAX_MESSAGE_LEN];		//存储从串口接收到的数据
	
	private Display display;
	private Shell shell;
	private Text textSend;
	private Text textReceive;
	private Combo commPort;
	private Combo comboDataBits;
	private Combo comboBaudRate;
	private Combo comboStopBits;
	private Combo comboParityBits;
	private Button buttonOpen;
	private Button buttonSend;
	private Button buttonClear;
	private MenuItem exitItem;
	private boolean active = false;
	private boolean isOpenSet = false;
	private boolean isChangeSet = false;
	private int currentIndex;
	private SerialPort currentSerialPort;
	private LinkedList<CommPortIdentifier> serialPortIdentifiers;
	private CommPortIdentifier[] portIdentifiers;
	private int[] baudRate = {1200,2400,4800,9600,19200,62500};
	private int[] dataBits = {5,6,7,8};
	private int[] stopBits = {SerialPort.STOPBITS_1,SerialPort.STOPBITS_1_5,SerialPort.STOPBITS_2};
	private int[] parityBits = {SerialPort.PARITY_NONE,SerialPort.PARITY_EVEN,SerialPort.PARITY_ODD,SerialPort.PARITY_MARK,SerialPort.PARITY_SPACE};
	
	private int currentBaudRate = 3;
	private int currentDataBits = 3;
	private int currentStopBits = 0;
	private int currentParityBits = 0;
	
	private BufferedReader in;
	public Operation(){
		
		Comm comm = new Comm();
		serialPortIdentifiers = comm.getSerialPortsIdentifiers();	//获取机器上的所有串口
		int portNum = serialPortIdentifiers.size();
		portIdentifiers = new CommPortIdentifier[portNum];
		for(int i = 0;i < portNum;i++)
			portIdentifiers[i] = serialPortIdentifiers.get(i);
		if(portNum == 0){		//若串口不存在给出提示后结束程序
			noPortOperation();
			return;
		}
		
		/*若串口存在，执行以下程序*/
		String[] portNames = new String[portNum];
		for(int i = 0;i < portNum;i++)
			portNames[i] = serialPortIdentifiers.get(i).getName();	//获取每个串口的名字
		Window window = new Window();
		window.createContents();
		textReceive = window.getTextReceive();
		buttonSend = window.getButtonSend();
		buttonOpen = window.getButtonOpen();
		buttonClear = window.getButtonClear();
		textSend = window.getTextSend();
		exitItem = window.getExitItem();
		buttonOpen.addSelectionListener(new OpenButtonListener());		//注册接收数据事件
		buttonSend.addSelectionListener(new SendListener());
		buttonClear.addSelectionListener(new ClearListener());
		textSend.addKeyListener(new EnterListener());
		exitItem.addSelectionListener(new ExitListener());
		commPort = window.getComboPort();
		commPort.addSelectionListener(new CommPortListener());
		
		comboBaudRate = window.getComboBaudRate();
		comboDataBits = window.getComboDataBits();
		comboStopBits = window.getComboStopBits();
		comboParityBits = window.getComboParityBits();
		ParameterListener parameterListener = new ParameterListener();
		
		/*注册参数监听器*/
		comboBaudRate.addSelectionListener(parameterListener);
		comboDataBits.addSelectionListener(parameterListener);
		comboStopBits.addSelectionListener(parameterListener);
		comboParityBits.addSelectionListener(parameterListener);
		
		shell = window.getShell();
		commPort.setItems(portNames);		//将串口显示在组合框
		commPort.select(0);				//默认显示第一个串口
		openWindow();
	}
	
	public void openWindow(){
		display = Display.getDefault();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		if(currentSerialPort != null && active)		//如果当前串口存在且打开，则关闭串口
			currentSerialPort.close();
		display.dispose();
	}
	
	/*没有串口时的操作*/
	public void noPortOperation(){
		Window window = new Window();
		Shell shell = window.getShell();
		MessageBox box = new MessageBox(shell,SWT.ICON_ERROR);
		box.setMessage("没有在此计算机中找到串口\n请在另一台计算机中运行");
		box.open();
	}
	
	/*打开串口失败时的操作*/
	public void failToOpen(String currentUser){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		box.setMessage("当前串口被" + currentUser + "占用\n请选择另外一个串口");
		box.open();
	}
	
	public void failToOpen(){
		MessageBox box = new MessageBox(shell,SWT.ICON_INFORMATION);
		box.setMessage("当前串口被占用\n请选择另外一个串口");
		box.open();
	}
	
	/*设置串口参数*/
	public void setPort(){
		try {
			currentSerialPort.setSerialPortParams(baudRate[currentBaudRate], dataBits[currentDataBits], 
					stopBits[currentStopBits], parityBits[currentParityBits]);
		} catch (UnsupportedCommOperationException e) {
			setFailed();
			return;
		}
	}
	
	/*串口参数设置失败时的操作*/
	public void setFailed(){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		if(isOpenSet)
			box.setMessage("串口已打开但参数设置错误\n请重新对参数设置");
		else if(isChangeSet)
			box.setMessage("串口以更换成功但参数设置错误\n请重新设置参数");
		else 
			box.setMessage("该参数超过本台计算机驱动支持范围\n请重新设置参数");			
		box.open();
	}
	
	/*发送数据时，没有串口打开时的操作*/
	public void noSerialPortOpened(){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		box.setMessage("请先打开串口");
		box.open();
	}
	
	/*发送文本为空时的提示操作*/
	public void emptyTextTip(){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		box.setMessage("发送文本不能为空\n请输入要发送字符串");
		box.open();
	}
	
	/*串口组合框监听器*/
	class CommPortListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {
			int selectionIndex = commPort.getSelectionIndex();
			if(active && (selectionIndex != currentIndex)){
				try {
					SerialPort selectionSerialPort = (SerialPort)portIdentifiers[selectionIndex].open("SerialPortTool", 100);	//尝试打开串口
					currentSerialPort.close();	//更换串口成功，关闭先前串口
					currentSerialPort = selectionSerialPort;	//成功打开，就把当前串口设为选中串口
					currentIndex = selectionIndex;		//更新当前串口下标
					currentSerialPort.notifyOnDataAvailable(true);
					in = new BufferedReader(new InputStreamReader(currentSerialPort.getInputStream()));	//更新当前串口输入流
					currentSerialPort.addEventListener(new ReceiveListener());	//注册数据到达事件
					isChangeSet = true;
					setPort();	//初始化选中串口	
				} catch (PortInUseException e) {
					failToOpen();
					commPort.select(currentIndex);
				} catch (TooManyListenersException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally{
					isChangeSet = false;
				}
			}
		}
	}
	
	/*打开按钮监听器*/
	class OpenButtonListener implements SelectionListener{
		
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {	//单击打开按钮触发的事件
			if(active){	//如果串口已经打开，则关闭当前串口
				active = false;		//活动标志设为false
				currentSerialPort.close();
				buttonOpen.setText("打开串口");	//显示打开串口
				return;
			}
			int selectionIndex = commPort.getSelectionIndex();	//当前选中串口
			
			if(portIdentifiers[selectionIndex].isCurrentlyOwned()){	//当前串口被占用，打开失败
				String currentUser = portIdentifiers[selectionIndex].getCurrentOwner();
				failToOpen(currentUser);
				return;
			}
			
			try {
				SerialPort selectionSerialPort = (SerialPort)portIdentifiers[selectionIndex].open("SerialPortTool", 100);		//尝试打开串口
				
				/*串口打开成功*/
				active = true;		//活动标志为true
				buttonOpen.setText("关闭串口");
				currentSerialPort = selectionSerialPort;		//将当前串口设为选中串口	
				currentIndex = selectionIndex;					//记录当前串口标号
				currentSerialPort.notifyOnDataAvailable(true);
				in = new BufferedReader(new InputStreamReader(currentSerialPort.getInputStream()));	//获取当前串口输入流
				currentSerialPort.addEventListener(new ReceiveListener());	//注册数据到达事件
				isOpenSet = true;
				setPort();	//初始化选中串口
			} catch (PortInUseException e) {
				failToOpen();
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				isOpenSet = false; 
			}
		}
	}
	
	/*接收数据监听器*/
	class ReceiveListener implements SerialPortEventListener{
		public void serialEvent(SerialPortEvent event){
			if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE){			
					
					display.syncExec(new Runnable(){	//不能在其他线程对SWT界面线程进行操作，否则抛出Invalid thread access异常
						public void run(){
							try {
								int num = in.read(message, 0, MAX_MESSAGE_LEN);		//读取从串口接收到的数据
								textReceive.append(new String(message,0,num));		//将接收到的数据显示在textReceive上
							} catch (IOException e) {
								e.printStackTrace();
							}		
						}
					});
			}	
		}
	}
	
	/*参数组合框监听器*/
	class ParameterListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}

		public void widgetSelected(SelectionEvent event) {
			Combo combo = (Combo)event.getSource();		//获取事件源
			int selectionIndex = combo.getSelectionIndex();
			int last = 0;
			/*更新对应的参数当前值*/
			if(combo == comboDataBits){
				last = currentDataBits;
				currentDataBits = selectionIndex;
			}
			else if(combo == comboBaudRate){
				last = currentBaudRate;
				currentBaudRate = selectionIndex;
			}
			else if(combo == comboStopBits){
				last = currentStopBits;
				currentStopBits = selectionIndex;
			}
			else if(combo == comboParityBits){
				last = currentParityBits;
				currentParityBits = selectionIndex;
			}

			if(active && last != selectionIndex)
				try {
					currentSerialPort.setSerialPortParams(baudRate[currentBaudRate], dataBits[currentDataBits], 
							stopBits[currentStopBits], parityBits[currentParityBits]);
				} catch (UnsupportedCommOperationException e) {
					setFailed();
					if(combo == comboDataBits){
						currentDataBits = last;
						combo.select(currentDataBits);
					}
					else if(combo == comboBaudRate){
						currentBaudRate = last;
						combo.select(currentBaudRate);
					}
					else if(combo == comboStopBits){
						currentStopBits = last;
						combo.select(currentStopBits);
					}
					else if(combo == comboParityBits){
						currentParityBits = last;
						combo.select(currentParityBits);
					}
				}
		}		
	}
	
	class SendListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {
			if(!active){	//如果没有打开串口，给出提示
				noSerialPortOpened();
				return;
			}
			
			String message = textSend.getText();
			if(message.equals("")){		//如果发送字符串为空，给出提示
				emptyTextTip();
				return;
			}
			try {
				OutputStream out = currentSerialPort.getOutputStream();
				for(int i = 0;i < message.length();i++)		//逐个发送字符
					out.write((int)message.charAt(i));
				textSend.setText("");	//发送完毕，清空发送文本框
			} catch (IOException e) {
				
			}	
		}
	}
	
	/*按下Enter即可发送*/
	class EnterListener implements KeyListener{
		public void keyPressed(KeyEvent arg0) {			
		}
		public void keyReleased(KeyEvent e) {
			if(e.keyCode == SWT.CR){
				if(!active){	//如果没有打开串口，给出提示
					noSerialPortOpened();
					return;
				}
				String message = textSend.getText();
				if(message.equals("")){		//如果发送字符串为空，给出提示
					emptyTextTip();
					return;
				}
				try {
					OutputStream out = currentSerialPort.getOutputStream();
					for(int i = 0;i < message.length();i++)	  //逐个发送字符
						out.write((int)message.charAt(i));
					textSend.setText("");	//发送完毕，清空发送文本框
				} catch (IOException e1) {
					e1.printStackTrace();
				}		
			}
		}
		
	}
	/*清空接收区文本*/
	class ClearListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {
			textReceive.setText("");
		}
	}
	
	/*退出菜单*/
	class ExitListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg){			
		}		
		public void widgetSelected(SelectionEvent e){			
			if(active)
				currentSerialPort.close();		//关闭串口并退出
			shell.dispose();
		}
	}
}

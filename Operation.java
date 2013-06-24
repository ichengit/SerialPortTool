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
	private char[] message = new char[MAX_MESSAGE_LEN];		//�洢�Ӵ��ڽ��յ�������
	
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
		serialPortIdentifiers = comm.getSerialPortsIdentifiers();	//��ȡ�����ϵ����д���
		int portNum = serialPortIdentifiers.size();
		portIdentifiers = new CommPortIdentifier[portNum];
		for(int i = 0;i < portNum;i++)
			portIdentifiers[i] = serialPortIdentifiers.get(i);
		if(portNum == 0){		//�����ڲ����ڸ�����ʾ���������
			noPortOperation();
			return;
		}
		
		/*�����ڴ��ڣ�ִ�����³���*/
		String[] portNames = new String[portNum];
		for(int i = 0;i < portNum;i++)
			portNames[i] = serialPortIdentifiers.get(i).getName();	//��ȡÿ�����ڵ�����
		Window window = new Window();
		window.createContents();
		textReceive = window.getTextReceive();
		buttonSend = window.getButtonSend();
		buttonOpen = window.getButtonOpen();
		buttonClear = window.getButtonClear();
		textSend = window.getTextSend();
		exitItem = window.getExitItem();
		buttonOpen.addSelectionListener(new OpenButtonListener());		//ע����������¼�
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
		
		/*ע�����������*/
		comboBaudRate.addSelectionListener(parameterListener);
		comboDataBits.addSelectionListener(parameterListener);
		comboStopBits.addSelectionListener(parameterListener);
		comboParityBits.addSelectionListener(parameterListener);
		
		shell = window.getShell();
		commPort.setItems(portNames);		//��������ʾ����Ͽ�
		commPort.select(0);				//Ĭ����ʾ��һ������
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
		if(currentSerialPort != null && active)		//�����ǰ���ڴ����Ҵ򿪣���رմ���
			currentSerialPort.close();
		display.dispose();
	}
	
	/*û�д���ʱ�Ĳ���*/
	public void noPortOperation(){
		Window window = new Window();
		Shell shell = window.getShell();
		MessageBox box = new MessageBox(shell,SWT.ICON_ERROR);
		box.setMessage("û���ڴ˼�������ҵ�����\n������һ̨�����������");
		box.open();
	}
	
	/*�򿪴���ʧ��ʱ�Ĳ���*/
	public void failToOpen(String currentUser){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		box.setMessage("��ǰ���ڱ�" + currentUser + "ռ��\n��ѡ������һ������");
		box.open();
	}
	
	public void failToOpen(){
		MessageBox box = new MessageBox(shell,SWT.ICON_INFORMATION);
		box.setMessage("��ǰ���ڱ�ռ��\n��ѡ������һ������");
		box.open();
	}
	
	/*���ô��ڲ���*/
	public void setPort(){
		try {
			currentSerialPort.setSerialPortParams(baudRate[currentBaudRate], dataBits[currentDataBits], 
					stopBits[currentStopBits], parityBits[currentParityBits]);
		} catch (UnsupportedCommOperationException e) {
			setFailed();
			return;
		}
	}
	
	/*���ڲ�������ʧ��ʱ�Ĳ���*/
	public void setFailed(){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		if(isOpenSet)
			box.setMessage("�����Ѵ򿪵��������ô���\n�����¶Բ�������");
		else if(isChangeSet)
			box.setMessage("�����Ը����ɹ����������ô���\n���������ò���");
		else 
			box.setMessage("�ò���������̨���������֧�ַ�Χ\n���������ò���");			
		box.open();
	}
	
	/*��������ʱ��û�д��ڴ�ʱ�Ĳ���*/
	public void noSerialPortOpened(){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		box.setMessage("���ȴ򿪴���");
		box.open();
	}
	
	/*�����ı�Ϊ��ʱ����ʾ����*/
	public void emptyTextTip(){
		MessageBox box = new MessageBox(shell,SWT.ICON_WARNING);
		box.setMessage("�����ı�����Ϊ��\n������Ҫ�����ַ���");
		box.open();
	}
	
	/*������Ͽ������*/
	class CommPortListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {
			int selectionIndex = commPort.getSelectionIndex();
			if(active && (selectionIndex != currentIndex)){
				try {
					SerialPort selectionSerialPort = (SerialPort)portIdentifiers[selectionIndex].open("SerialPortTool", 100);	//���Դ򿪴���
					currentSerialPort.close();	//�������ڳɹ����ر���ǰ����
					currentSerialPort = selectionSerialPort;	//�ɹ��򿪣��Ͱѵ�ǰ������Ϊѡ�д���
					currentIndex = selectionIndex;		//���µ�ǰ�����±�
					currentSerialPort.notifyOnDataAvailable(true);
					in = new BufferedReader(new InputStreamReader(currentSerialPort.getInputStream()));	//���µ�ǰ����������
					currentSerialPort.addEventListener(new ReceiveListener());	//ע�����ݵ����¼�
					isChangeSet = true;
					setPort();	//��ʼ��ѡ�д���	
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
	
	/*�򿪰�ť������*/
	class OpenButtonListener implements SelectionListener{
		
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {	//�����򿪰�ť�������¼�
			if(active){	//��������Ѿ��򿪣���رյ�ǰ����
				active = false;		//���־��Ϊfalse
				currentSerialPort.close();
				buttonOpen.setText("�򿪴���");	//��ʾ�򿪴���
				return;
			}
			int selectionIndex = commPort.getSelectionIndex();	//��ǰѡ�д���
			
			if(portIdentifiers[selectionIndex].isCurrentlyOwned()){	//��ǰ���ڱ�ռ�ã���ʧ��
				String currentUser = portIdentifiers[selectionIndex].getCurrentOwner();
				failToOpen(currentUser);
				return;
			}
			
			try {
				SerialPort selectionSerialPort = (SerialPort)portIdentifiers[selectionIndex].open("SerialPortTool", 100);		//���Դ򿪴���
				
				/*���ڴ򿪳ɹ�*/
				active = true;		//���־Ϊtrue
				buttonOpen.setText("�رմ���");
				currentSerialPort = selectionSerialPort;		//����ǰ������Ϊѡ�д���	
				currentIndex = selectionIndex;					//��¼��ǰ���ڱ��
				currentSerialPort.notifyOnDataAvailable(true);
				in = new BufferedReader(new InputStreamReader(currentSerialPort.getInputStream()));	//��ȡ��ǰ����������
				currentSerialPort.addEventListener(new ReceiveListener());	//ע�����ݵ����¼�
				isOpenSet = true;
				setPort();	//��ʼ��ѡ�д���
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
	
	/*�������ݼ�����*/
	class ReceiveListener implements SerialPortEventListener{
		public void serialEvent(SerialPortEvent event){
			if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE){			
					
					display.syncExec(new Runnable(){	//�����������̶߳�SWT�����߳̽��в����������׳�Invalid thread access�쳣
						public void run(){
							try {
								int num = in.read(message, 0, MAX_MESSAGE_LEN);		//��ȡ�Ӵ��ڽ��յ�������
								textReceive.append(new String(message,0,num));		//�����յ���������ʾ��textReceive��
							} catch (IOException e) {
								e.printStackTrace();
							}		
						}
					});
			}	
		}
	}
	
	/*������Ͽ������*/
	class ParameterListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}

		public void widgetSelected(SelectionEvent event) {
			Combo combo = (Combo)event.getSource();		//��ȡ�¼�Դ
			int selectionIndex = combo.getSelectionIndex();
			int last = 0;
			/*���¶�Ӧ�Ĳ�����ǰֵ*/
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
			if(!active){	//���û�д򿪴��ڣ�������ʾ
				noSerialPortOpened();
				return;
			}
			
			String message = textSend.getText();
			if(message.equals("")){		//��������ַ���Ϊ�գ�������ʾ
				emptyTextTip();
				return;
			}
			try {
				OutputStream out = currentSerialPort.getOutputStream();
				for(int i = 0;i < message.length();i++)		//��������ַ�
					out.write((int)message.charAt(i));
				textSend.setText("");	//������ϣ���շ����ı���
			} catch (IOException e) {
				
			}	
		}
	}
	
	/*����Enter���ɷ���*/
	class EnterListener implements KeyListener{
		public void keyPressed(KeyEvent arg0) {			
		}
		public void keyReleased(KeyEvent e) {
			if(e.keyCode == SWT.CR){
				if(!active){	//���û�д򿪴��ڣ�������ʾ
					noSerialPortOpened();
					return;
				}
				String message = textSend.getText();
				if(message.equals("")){		//��������ַ���Ϊ�գ�������ʾ
					emptyTextTip();
					return;
				}
				try {
					OutputStream out = currentSerialPort.getOutputStream();
					for(int i = 0;i < message.length();i++)	  //��������ַ�
						out.write((int)message.charAt(i));
					textSend.setText("");	//������ϣ���շ����ı���
				} catch (IOException e1) {
					e1.printStackTrace();
				}		
			}
		}
		
	}
	/*��ս������ı�*/
	class ClearListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		public void widgetSelected(SelectionEvent arg0) {
			textReceive.setText("");
		}
	}
	
	/*�˳��˵�*/
	class ExitListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent arg){			
		}		
		public void widgetSelected(SelectionEvent e){			
			if(active)
				currentSerialPort.close();		//�رմ��ڲ��˳�
			shell.dispose();
		}
	}
}

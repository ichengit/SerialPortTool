import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

public class Window {
	
	private static int NONE;
	protected Shell shell;
	private Text textReceive;
	private Text textSend;
	private Combo comboPort;
	private Combo comboDataBits;
	private Combo comboBaudRate;
	private Combo comboStopBits;
	private Combo comboParityBits;
	private Button buttonOpen;
	private Button buttonSend;
	private Button buttonClear;
	private MenuItem exitItem;
	
	public Window(){
		shell = new Shell();		
	}

	protected void createContents() {
		shell.setImage(SWTResourceManager.getImage("image\\鸣人.jpg"));
		shell.setBackground(SWTResourceManager.getColor(211, 211, 211));
		shell.setSize(586, 400);
		shell.setText("串口调试——单片机课程设计");
		shell.setLayout(new GridLayout(1, false));
		
		Menu menu = new Menu(shell, org.eclipse.swt.SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem fileItem = new MenuItem(menu, org.eclipse.swt.SWT.CASCADE);
		fileItem.setText("\u6587\u4EF6(&F)");
		
		Menu menu_1 = new Menu(fileItem);
		fileItem.setMenu(menu_1);
		
		MenuItem saveItem = new MenuItem(menu_1, org.eclipse.swt.SWT.NONE);
		saveItem.setText("\u4FDD\u5B58(&S)");
		
		exitItem = new MenuItem(menu_1, org.eclipse.swt.SWT.NONE);
		exitItem.setText("\u9000\u51FA(&E)");
		
		CLabel label = new CLabel(shell, org.eclipse.swt.SWT.NONE);
		label.setBackground(SWTResourceManager.getColor(211, 211, 211));
		label.setText("\u63A5\u6536\u5230\u7684\u6570\u636E");
		
		SashForm sashForm = new SashForm(shell, org.eclipse.swt.SWT.VERTICAL);
		sashForm.setBackground(SWTResourceManager.getColor(255, 250, 205));
		sashForm.setLayoutData(new GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true, 1, 1));
		sashForm.setLayout(new FillLayout());
		
		Composite composite1 = new Composite(sashForm, org.eclipse.swt.SWT.NONE);
		composite1.setLayout(new FillLayout(org.eclipse.swt.SWT.HORIZONTAL));
		
		textReceive = new Text(composite1, org.eclipse.swt.SWT.READ_ONLY | org.eclipse.swt.SWT.WRAP | org.eclipse.swt.SWT.V_SCROLL | org.eclipse.swt.SWT.MULTI);
		textReceive.setBackground(SWTResourceManager.getColor(org.eclipse.swt.SWT.COLOR_WHITE));
		Composite composite2 = new Composite(sashForm,Window.NONE);
		composite2.setBackground(SWTResourceManager.getColor(211, 211, 211));
		composite2.setLayout(new FormLayout());
		
		comboBaudRate = new Combo(composite2, org.eclipse.swt.SWT.READ_ONLY);
		comboBaudRate.setItems(new String[] {"1200", "2400", "4800", "9600", "19200", "62500"});
		FormData fd_comboBaudRate = new FormData();
		comboBaudRate.setLayoutData(fd_comboBaudRate);
		
		buttonOpen = new Button(composite2, org.eclipse.swt.SWT.NONE);
		fd_comboBaudRate.top = new FormAttachment(buttonOpen, 2, org.eclipse.swt.SWT.TOP);
		FormData fd_buttonOpen = new FormData();
		fd_buttonOpen.top = new FormAttachment(0);
		buttonOpen.setLayoutData(fd_buttonOpen);
		buttonOpen.setText("\u6253\u5F00\u4E32\u53E3");
		
		CLabel lblNewLabel_1 = new CLabel(composite2, org.eclipse.swt.SWT.NONE);
		lblNewLabel_1.setBackground(SWTResourceManager.getColor(211, 211, 211));
		fd_comboBaudRate.right = new FormAttachment(100, -250);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.left = new FormAttachment(comboBaudRate, 6);
		fd_lblNewLabel_1.bottom = new FormAttachment(comboBaudRate, 0, org.eclipse.swt.SWT.BOTTOM);
		fd_lblNewLabel_1.top = new FormAttachment(0, 4);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("\u6570\u636E\u4F4D");
		
		comboDataBits = new Combo(composite2, org.eclipse.swt.SWT.READ_ONLY);
		fd_lblNewLabel_1.right = new FormAttachment(100, -205);
		comboDataBits.setItems(new String[] {"5", "6", "7", "8"});
		FormData fd_comboDataBits = new FormData();
		fd_comboDataBits.top = new FormAttachment(comboBaudRate, 0, org.eclipse.swt.SWT.TOP);
		fd_comboDataBits.left = new FormAttachment(lblNewLabel_1, 10);
		fd_comboDataBits.right = new FormAttachment(100, -163);
		comboBaudRate.select(3);
		comboDataBits.setLayoutData(fd_comboDataBits);
		
		CLabel label_1 = new CLabel(composite2, org.eclipse.swt.SWT.NONE);
		label_1.setBackground(SWTResourceManager.getColor(211, 211, 211));
		FormData fd_label_1 = new FormData();
		fd_label_1.top = new FormAttachment(0, 4);
		label_1.setLayoutData(fd_label_1);
		label_1.setText("\u4E32\u53E3");
		
		CLabel label_2 = new CLabel(composite2, org.eclipse.swt.SWT.NONE);
		label_2.setBackground(SWTResourceManager.getColor(211, 211, 211));
		FormData fd_label_2 = new FormData();
		fd_label_2.top = new FormAttachment(label_1, 8);
		fd_label_2.left = new FormAttachment(0, 10);
		label_2.setLayoutData(fd_label_2);
		label_2.setText("\u505C\u6B62\u4F4D");
		
		comboStopBits = new Combo(composite2, org.eclipse.swt.SWT.READ_ONLY);
		comboStopBits.setItems(new String[] {"1", "1.5","2"});
		FormData fd_comboStopBits = new FormData();
		fd_comboStopBits.left = new FormAttachment(label_2, 9);
		comboStopBits.setLayoutData(fd_comboStopBits);
		
		CLabel label_3 = new CLabel(composite2, org.eclipse.swt.SWT.NONE);
		label_3.setBackground(SWTResourceManager.getColor(211, 211, 211));
		fd_comboStopBits.right = new FormAttachment(100, -456);
		FormData fd_label_3 = new FormData();
		fd_label_3.bottom = new FormAttachment(label_2, 0, org.eclipse.swt.SWT.BOTTOM);
		fd_label_3.left = new FormAttachment(buttonOpen, 10, org.eclipse.swt.SWT.LEFT);
		label_3.setLayoutData(fd_label_3);
		label_3.setText("\u6821\u9A8C\u4F4D");
		
		comboParityBits = new Combo(composite2, org.eclipse.swt.SWT.READ_ONLY);
		comboParityBits.setItems(new String[] {"NONE", "EVEN", "ODD","MARK","SPACE"});
		FormData fd_comboParityBits = new FormData();
		fd_comboParityBits.top = new FormAttachment(buttonOpen, 6);
		fd_comboParityBits.left = new FormAttachment(label_3, 6);
		comboParityBits.setLayoutData(fd_comboParityBits);
		comboParityBits.select(0);
		
		buttonClear = new Button(composite2,SWT.PUSH);
		buttonClear.setText("清除接收区");
		FormData fd_buttonClear = new FormData();
		fd_buttonClear.top = new FormAttachment(comboDataBits,6,SWT.BOTTOM);
		fd_buttonClear.left = new FormAttachment(comboParityBits,20,SWT.RIGHT);
		buttonClear.setLayoutData(fd_buttonClear);
		
		textSend = new Text(composite2, org.eclipse.swt.SWT.BORDER);
		FormData fd_textSend = new FormData();
		fd_textSend.left = new FormAttachment(0, 11);
		fd_textSend.top = new FormAttachment(label_2, 16);
		fd_textSend.bottom = new FormAttachment(100, -19);
		comboStopBits.select(0);
		textSend.setLayoutData(fd_textSend);
		
		buttonSend = new Button(composite2, org.eclipse.swt.SWT.NONE);
		fd_textSend.right = new FormAttachment(100, -220);
		FormData fd_buttonSend = new FormData();
		fd_buttonSend.top = new FormAttachment(textSend, -2, org.eclipse.swt.SWT.TOP);
		fd_buttonSend.left = new FormAttachment(textSend, 6);
		comboDataBits.select(3);
		buttonSend.setLayoutData(fd_buttonSend);
		buttonSend.setText("\u53D1\u9001\u6587\u672C");
		
		comboPort = new Combo(composite2, org.eclipse.swt.SWT.READ_ONLY);
		fd_comboStopBits.top = new FormAttachment(comboPort, 8);
		fd_label_1.right = new FormAttachment(comboPort, -5);
		fd_buttonOpen.left = new FormAttachment(comboPort, 7);
		FormData fd_comboPort = new FormData();
		fd_comboPort.right = new FormAttachment(100, -452);
		fd_comboPort.left = new FormAttachment(0, 46);
		fd_comboPort.top = new FormAttachment(0);
		comboPort.setLayoutData(fd_comboPort);
		
		CLabel label_4 = new CLabel(composite2, org.eclipse.swt.SWT.NONE);
		label_4.setBackground(SWTResourceManager.getColor(211, 211, 211));
		fd_comboParityBits.right = new FormAttachment(label_4, 0, org.eclipse.swt.SWT.RIGHT);
		fd_comboBaudRate.left = new FormAttachment(label_4, 6);
		FormData fd_label_4 = new FormData();
		fd_label_4.top = new FormAttachment(0, 2);
		fd_label_4.left = new FormAttachment(buttonOpen, 25);
		label_4.setLayoutData(fd_label_4);
		label_4.setText("\u6CE2\u7279\u7387");
		sashForm.setWeights(new int[] {190, 112});
		
		saveItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				FileDialog dialog = new FileDialog(shell,SWT.SAVE);
				dialog.setFilterExtensions(new String[]{"*.txt","*.*"});
				dialog.setFilterNames(new String[]{"Text Files(*.txt)","All Files(*.*)"});
				String fileName = dialog.open();
				if(fileName != null){
					File file = new File(fileName);
					if(file.exists()){
						MessageBox box = new MessageBox(shell,SWT.ICON_WORKING|SWT.YES|SWT.NO);
						box.setMessage("文件\"" + fileName + "\"已存在\n要覆盖此文件吗");	
						int choice = box.open();
						if(choice == SWT.YES){
							try {
								PrintWriter output = new PrintWriter(file);
								String message = textReceive.getText();
								output.println(message);
								output.close();
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
						}
					}
					else{
						try {
							PrintWriter output = new PrintWriter(file);
							String message = textReceive.getText();
							output.println(message);
							output.close();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
	}

	public Shell getShell() {
		return shell;
	}

	public Text getTextSend() {
		return textSend;
	}
	
	public Text getTextReceive(){
		return textReceive;
	}

	public Combo getComboPort() {
		return comboPort;
	}

	public Combo getComboDataBits() {
		return comboDataBits;
	}

	public Combo getComboBaudRate() {
		return comboBaudRate;
	}

	public Combo getComboStopBits() {
		return comboStopBits;
	}

	public Combo getComboParityBits() {
		return comboParityBits;
	}

	public Button getButtonOpen() {
		return buttonOpen;
	}

	public Button getButtonSend() {
		return buttonSend;
	}
	
	public Button getButtonClear(){
		return buttonClear;
	}
	
	public MenuItem getExitItem(){
		return exitItem;
	}
}

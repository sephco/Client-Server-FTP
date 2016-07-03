import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FTP_Client extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private File file; //Hold file chosen
	private JTextArea display; //Hold activity and event messgaes
	private JPanel buttonPanel; //Hold actionable buttons
	private JButton btnServerConnect = new JButton("Server Connect");
	private JButton btnSelectFile = new JButton("Select File");
	private JButton btnTransferFile = new JButton("Transfer File");
	private JButton btnServerDisconnect = new JButton("Server Disconnect");
	private JButton btnHelp = new JButton("Help");
	private JButton btnExit = new JButton("Exit");
	
	//streams for network IO
	OutputStream osClient;
	PrintWriter clientOutput;//used for character based output
	InputStreamReader ipStreamReader;
	BufferedReader br;  //used for character based input
	
	public FTP_Client(){
		//********** INITIALIZE GUI **********
		super("FTP Client");
		setSize(725, 300);
		Container cont = getContentPane();
		// Initialize display panel and add log area
		display = new JTextArea();
		display.setEditable(false);
		cont.add(new JScrollPane(display), BorderLayout.CENTER);
		// Initialize button panel
		buttonPanel = new JPanel();
		cont.add(buttonPanel, BorderLayout.SOUTH);
		// Add buttons to buttonPanel
		buttonPanel.add(btnServerConnect);
		buttonPanel.add(btnSelectFile);
		buttonPanel.add(btnTransferFile);
		buttonPanel.add(btnServerDisconnect);
		buttonPanel.add(btnHelp);
		buttonPanel.add(btnExit);
		// Add action listeners
		btnServerConnect.addActionListener(this);
		btnSelectFile.addActionListener(this);
		btnTransferFile.addActionListener(this);
		btnServerDisconnect.addActionListener(this);
		btnHelp.addActionListener(this);
		btnExit.addActionListener(this);
		// Set window as visible
		setVisible(true); 
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		final JFileChooser fc = new JFileChooser();
		// filter files for txt, xml, htm, & html only
		fc.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt", "text", "xml", "htm", "html"));

		//***** if btnServerConnect is pressed *****
		if (source == btnServerConnect){
			try {
				// connect to localHost via IP address through port 21
				socket = new Socket("127.0.0.1", 21);
				display.append("Connected to server\n");
			} catch (IOException ioException) {
				// display append message to log
				ioException.printStackTrace();
				display.append("Unable to connect to server - did you start it?\n");
			}
		}
		//***** if btnSelectFile is pressed *****
		else if (source == btnSelectFile){
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// This is where a real application would open the file.
	            file = fc.getSelectedFile();
	            // Dissplay message with file path
	            display.append("File to transfer is: " + file.getPath() + "\n");
	        } else if (returnVal == JFileChooser.CANCEL_OPTION) {
				display.append("Save command cancelled by user - no file selected\n");
	        }
		}
		//***** if btnTransferFile is pressed *****
		else if (source == btnTransferFile){
			if(socket == null ){ // If socket hasn't been connected to or closed
				display.append("No connection to server. Connect to server before trying to transfer a file\n");
			}
			else if (socket.isClosed()){ // If socket was connected then closed 
				display.append("You disconnected from server - try reconnecting with the Server Connect button below\n");
			}
			else if (file == null){ // if no file has been selected to transfer
				display.append("No file selected to transfer - select a file first\n");
			}
			else{ // Transfer 
				display.append("File being transfered " + file + "...\n");
				try{
					// BufferedReader to read from the file selected
					br = new BufferedReader(new FileReader(file));
			    	// Output stream and writer to write to the socket
					osClient = socket.getOutputStream();
					clientOutput = new PrintWriter(osClient);
					
					clientOutput.println(file.getName()); // Send file name for Path assignment
					clientOutput.println(file.length()); // Send file size

					String line;
					while ((line = br.readLine()) != null) { // Loop until EOF
						clientOutput.println(line); // Write to socket
						clientOutput.flush(); // Ensure it was written and clear Writer
				    }
				} catch (IOException ioException) {
					ioException.printStackTrace();
				} finally {
					clientOutput.flush(); // Ensure all was written to socket
					display.append("Transfer complete: " + file + "\n"); // Log status
				}
			}
		}
		//***** if btnServerDisconnect button is pressed *****
		else if(source == btnServerDisconnect){
			try {
				socket.close(); // Attempt to close socket
				display.append("Disconnected from server\n"); // If socket close is successful - display this msg
			} catch (NullPointerException nullPointerException){ // Caused if no server was previously connected to
				display.append("You weren't previously connected to the server\n");
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		//***** if btnHelp button is pressed *****
		else if (source == btnHelp) {
			//Display help message
			String msg = "Press Server Connect to connect to server"
						+ "\nPress Select File to choose which file you'd like to transfer"
						+ "\nPress Transfer File to initiate the transfer"
						+ "\nPress Server Disconnect to close connection"
						+ "\nPress Exit to exit";
			JOptionPane.showConfirmDialog(this, msg, "Help", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
		}
		//***** if btnExit button is pressed *****
		else if (source == btnExit) {
			try {	
				if (socket.isConnected()) // Will either return true/false or NullPointerException if never connected
					socket.close();	
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (NullPointerException e2){
				// Ignore - This just means the socket was never opened
			}
			
			try {
				clientOutput.close();
			} catch (NullPointerException e1){
				// Ignore - this means it was already closed
			}
			System.exit(0);
		}
	}
	
	public static void main(String[] args){
		FTP_Client clientApp = new FTP_Client(); //Initailize FTP_Client object
		clientApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Close when user closes window
		//clientApp.run();
	}
}

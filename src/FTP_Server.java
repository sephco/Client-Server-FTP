import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FTP_Server extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private ServerSocket ss;
	private File folderDestination = new File("/Users/Seph/Documents/COP2805/Assignments/NetworkingApp/"); // ****CHANGE TO C:\Temp BEFORE SHIP
	private JTextArea display; // Hold activity and event messgaes
	private JPanel buttonPanel; // Hold actionable buttons
	private JButton btnDestination = new JButton("Destination");
	private JButton btnHelp = new JButton("Help");
	private JButton btnExit = new JButton("Exit");
	
	int fileLength;
	String fileName;
	InputStream isServer;
	InputStreamReader ipStreamReader;
	BufferedReader serverInput;  //used for character based input
	PrintWriter writer;
	
	public FTP_Server(){
		//********** INITIALIZE GUI **********
		super("FTP Server");
		setSize(600, 300);
		Container cont = getContentPane();
		// Initialize display panel and add log area
		display = new JTextArea();
		display.setEditable(false);
		cont.add(new JScrollPane(display), BorderLayout.CENTER);
		// Initialize button panel
		buttonPanel = new JPanel();
		cont.add(buttonPanel, BorderLayout.SOUTH);
		// Add buttons to buttonPanel
		buttonPanel.add(btnDestination);
		buttonPanel.add(btnHelp);
		buttonPanel.add(btnExit);
		// Add action listeners
		btnDestination.addActionListener(this);
		btnHelp.addActionListener(this);
		btnExit.addActionListener(this);
		// Set window as visible
		setVisible(true); 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == btnDestination){
			JFileChooser fc = new JFileChooser(); // Init file chooser for btnDestination
			fc.setCurrentDirectory(folderDestination);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Limit to only directories
			int returnVal = fc.showOpenDialog(this);
			// Determine whether user selected a folder or canceled the operation
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// This is where the file will be saved to
	            folderDestination = fc.getSelectedFile();
	            // Dissplay message with file path
	            display.append("Folder to transfer to is: " + folderDestination + "\n");
	        } else if (returnVal == JFileChooser.CANCEL_OPTION) {
				display.append("Save command cancelled by user - Transferred files will be in " + folderDestination + "\n");
	        }
		}
		else if (source == btnHelp) {
			//Display help message
			String msg = "Press Destination to change the directory to save file to"
						+ "\nPress Exit to exit";
			JOptionPane.showConfirmDialog(this, msg, "Help", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
		}
		else if (source == btnExit){
			
		}
	}

	private void run(){
		// Display initial messages
		display.append("Server waiting for connections.\n");
		display.append("Transferred files will be in " + folderDestination + " unless you change this destination\n");
		while (true){
			try {
				ss = new ServerSocket(21); // Set server to use port 21
				socket = ss.accept(); // Blocks until connected to client
				display.append("Connected to client: " + socket.getRemoteSocketAddress() + "\n");
				
				// Create input stream connected to socket
				isServer = socket.getInputStream();
				ipStreamReader = new InputStreamReader(isServer);
				serverInput = new BufferedReader(ipStreamReader);
				while (true){
					fileName = serverInput.readLine(); // Get name of file
					fileLength = Integer.parseInt(serverInput.readLine()); // Get size of file
		
					// Create BufferedWriter with file path
					writer = new PrintWriter(new OutputStreamWriter(
				              new FileOutputStream(folderDestination + File.separator + fileName), "utf-8"));
				
				
					try{
						String line;
						while(true){ // Loop until break
							line = serverInput.readLine(); // Read line from socket via serverInput
							writer.println(line); // Write line to file
							writer.flush();	
							fileLength -= (line.length() + 1); // Subtract lenght of line from fileLength
							if(fileLength <= 0){break;} // Break when entire file has been read and written
						}
						
					} catch (NumberFormatException e){
						//ignore
					} catch (Exception e){ // Catch all
						e.printStackTrace();
					} finally {
						display.append("File transferred to " + folderDestination + "\n"); // Write to server log
						writer.close();
					}//end try
				}//end while
				
			} catch (IOException e) {
			e.printStackTrace();
			} catch (NumberFormatException e){
				//ignore
			} finally {
				try {
					serverInput.close();
					ss.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				display.append("Client disconnected\n");
			}
		}
	}
	
	public static void main(String[] args){
		FTP_Server serverApp = new FTP_Server(); //Initailize FTP_Server object
		serverApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Close when user closes window
		serverApp.run();
	}
}

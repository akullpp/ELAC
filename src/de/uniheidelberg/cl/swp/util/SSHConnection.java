/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.util
 * class: SSHConnection
 * 
 * Authors: E-Mail
 * Thomas Boegel: boegel@cl.uni-heidelberg.de
 * Lukas Funk: funk@cl.uni-heidelberg.de
 * Andreas Kull: kull@cl.uni-heidelberg.de
 * 
 * Please find a detailed explanation of this particular class/package and its role and usage at
 * the first JavaDoc following this comment.
 * 
 * Copyright 2010 Thomas Boegel & Lukas Funk & Andreas Kull
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uniheidelberg.cl.swp.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;


/**
 * Connects to an SSH server to execute JavaRap externally.
 * <br>
 * Used by {@link de.uniheidelberg.cl.swp.preproc.JavaRapPreProcessing} to preprocess the training
 * and test corpus in order to reduce loading time.
 * <br>
 * To use this class, <a href="http://www.cleondris.ch/ssh2">Ganymed's SSH2</a> is required and
 * should be placed in the resource folder.
 */
public class SSHConnection {
	private static String userName = null;
	private static String password = null;	
	private static Connection con = null;
	
	/**
	 * 	Name of the host SSH server.
	 */
	private String hostName;
	
	/**
	 * Path to a folder where the temporary files can be stored.
	 */
	private String serverPath;
	
	/**
	 * Establishes an SSH connection.
	 * <br>
	 * The hostname and path for the temporary files need to be specified in advance.
	 * 
	 * @param hostName Name of the host SSH server.
	 * @param serverPath Path to a folder where the temporary files can be stored.
	 * @throws IOException If the connection couldn't be established. 
	 */
	public SSHConnection(String hostName, String serverPath) throws IOException {
		this.hostName = hostName;
		this.serverPath = serverPath;
		
		if (con == null) {
			con = new Connection(this.hostName);
			con.connect();
			this.authenticate();
					
		}
	}
	
	/**
	 * Establishes an SSH connection.
	 * <br>
	 * The user will be prompted for hostname and path for the temporary files.
	 * 
	 * @throws IOException If the connection couldn't be established.
	 */
	public SSHConnection() throws IOException {
		if (hostName == null) {
			hostName = getCredential("Enter hostname");
		}
		
		if (serverPath == null) {
			serverPath = getCredential("Enter path for temporary files");
		}
		
		if (con == null) {
			con = new Connection(this.hostName);
			con.connect();
			this.authenticate();
		}
	}
	
	/**
	 * Asks the user for his credentials and tries to authenticate.
	 * 
	 * @return True if the authentication was successful, false otherwise.
	 */
	private boolean authenticate() throws IOException {		
		if (userName == null)
			userName = getCredential("Enter username");
		
		if (password == null) {
			password = getCredential("Enter password");
		}
		return con.authenticateWithPassword(userName, password);
	}
	
	/**
	 * Gets a specific credential.
	 * 
	 * @param credential Credential needed.
	 * @return User input.
	 */
	private String getCredential(String credential) {
		JFrame frame = new JFrame("Credential");
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screen = tk.getScreenSize();
		
		frame.setLocation(screen.width / 2, screen.height / 2);
		
		String tmp = null;
		
		if (credential.equals("Enter password")) {
			JPasswordField jpf = new JPasswordField();
			JOptionPane.showConfirmDialog(frame, jpf, "Enter password",
					JOptionPane.OK_CANCEL_OPTION);
			tmp = String.valueOf(jpf.getPassword());
		}
		else {
			tmp = JOptionPane.showInputDialog(frame, credential);
		}
		
		frame.dispose();
		
		return tmp;
	}
	
	/**
	 * Copies a file to the SSH server.
	 * 
	 * @param fileName The file to be copied.
	 * @throws IOException If the file wasn't copied successfully.
	 */
	public void copyFileToServer(String fileName) throws IOException {
		try {
			SCPClient scp = new SCPClient(con);
			scp.put(fileName, this.serverPath);
		} catch (Exception e) {
			throw new IOException();
		}
	}
	
	/**
	 * Closes the connection and removes the temporary files from the SSH server.
	 */
	public void close() {
		try {
			Session ses = con.openSession();
			ses.execCommand("rm " + serverPath + "*");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't remove files from server, remove manually!");
		}
		con.close();
	}
	
	/**
	 * Returns the current connection to the SSH Server.
	 * 
	 * @return Current Connection to the SSH Server.
	 */
	public Connection getCon() {
		return con;
	}
	
	/**
	 * Returns the path to the temporary files on the SSH Server.
	 * 
	 * @return Path to the temporary files on the SSH Server.
	 */
	public String getServerPath() {
		return this.serverPath;
	}
}

/* 
 *              weupnp - Trivial upnp java library 
 *
 * Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, FΩifth Floor, Boston, MA  02110-1301  USA
 * 
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 * 
 */

/*
 * refer to miniupnpc-1.0-RC8
 */
package org.bitlet.weupnp;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class contains a trivial main method that can be used to test whether
 * weupnp is able to manipulate port mappings on a IGD (Internet Gateway
 * Device) on the same network.
 *
 * @author Alessandro Bahgat Shehata
 */
public class Main {

	private static int SAMPLE_PORT = 6991;
	private static short WAIT_TIME = 10;
	private static boolean LISTALLMAPPINGS = false;

	private static JTextArea log = null;
	private static JFrame frame = null;

	public static void main(String[] args) throws Exception{

		boolean usegui=true;
		if (args.length==1 && args[0].equalsIgnoreCase("nogui"))
			usegui = false;
		
		// Creates the GUI for the log output
		if (usegui)
			initGUI();

		AddLogline("Starting weupnp");

		GatewayDiscover gatewayDiscover = new GatewayDiscover();
		AddLogline("Looking for Gateway Devices...");

		Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();

		if (gateways.isEmpty()) {
			AddLogline("No gateways found");
			AddLogline("Stopping weupnp");
			return;
		}
		AddLogline(gateways.size()+" gateway(s) found\n");

		int counter=0;
		for (GatewayDevice gw: gateways.values()) {
			counter++;
			AddLogline("Listing gateway details of device #" + counter+
					"\n\tFriendly name: "+gw.getFriendlyName()+
					"\n\tPresentation URL: "+gw.getPresentationURL()+
					"\n\tModel name: "+gw.getModelName()+
					"\n\tModel number: "+gw.getModelNumber()+
					"\n\tLocal interface address: "+gw.getLocalAddress().getHostAddress()+"\n");
		}

		// choose the first active gateway for the tests
		GatewayDevice activeGW = gatewayDiscover.getValidGateway();

		if (null != activeGW) {
			AddLogline("Using gateway:"+activeGW.getFriendlyName());
		} else {
			AddLogline("No active gateway device found");
			AddLogline("Stopping weupnp");
			return;
		}


		// testing PortMappingNumberOfEntries
		Integer portMapCount = activeGW.getPortMappingNumberOfEntries();
		AddLogline("GetPortMappingNumberOfEntries="+(portMapCount!=null?portMapCount.toString():"(unsupported)"));

		// testing getGenericPortMappingEntry
		PortMappingEntry portMapping0=new PortMappingEntry();
		if (LISTALLMAPPINGS) {
			int pmCount=0;
			do {
				if (activeGW.getGenericPortMappingEntry(pmCount,portMapping0))
					AddLogline("Portmapping #"+pmCount+" successfully retrieved ("+portMapping0.getPortMappingDescription()+":"+portMapping0.getExternalPort()+")");
				else{
					AddLogline("Portmapping #"+pmCount+" retrival failed"); 
					break;
				}
				pmCount++;
			} while (portMapping0!=null);
		} else {
			if (activeGW.getGenericPortMappingEntry(0,portMapping0))
				AddLogline("Portmapping #0 successfully retrieved ("+portMapping0.getPortMappingDescription()+":"+portMapping0.getExternalPort()+")");
			else
				AddLogline("Portmapping #0 retrival failed");        	
		}

		InetAddress localAddress = activeGW.getLocalAddress();
		AddLogline("Using local address: "+ localAddress.getHostAddress());
		String externalIPAddress = activeGW.getExternalIPAddress();
		AddLogline("External address: "+ externalIPAddress);

		AddLogline("Querying device to see if a port mapping already exists for port "+ SAMPLE_PORT);
		PortMappingEntry portMapping = new PortMappingEntry();

		if (activeGW.getSpecificPortMappingEntry(SAMPLE_PORT,"TCP",portMapping)) {
			int ok = JOptionPane.showConfirmDialog(frame,"Port mapping for port "+SAMPLE_PORT+
					" already exists. Do you want to remove this portmapping to continue the test?", 
					"Port mapping exists", 
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (ok == JOptionPane.CANCEL_OPTION) {
				AddLogline("Aborting test");
				return;
			}
			if (ok==JOptionPane.YES_OPTION) {
				if (activeGW.deletePortMapping(SAMPLE_PORT,"TCP")==true)
					AddLogline("Mapping removed");
				else
					AddLogline("Mapping removal failed");
			}
		}
		
		if (!activeGW.getSpecificPortMappingEntry(SAMPLE_PORT,"TCP",portMapping)) {
			AddLogline("Mapping free. Sending port mapping request for port "+SAMPLE_PORT);

			// test static lease duration mapping
			if (activeGW.addPortMapping(SAMPLE_PORT,SAMPLE_PORT,localAddress.getHostAddress(),"TCP","test")) {
				AddLogline("Mapping SUCCESSFUL. Waiting "+WAIT_TIME+" seconds before removing mapping...");
				Thread.sleep(1000*WAIT_TIME);

				if (activeGW.deletePortMapping(SAMPLE_PORT,"TCP")==true)
					AddLogline("Port mapping removed, test SUCCESSFUL");
				else
					AddLogline("Port mapping removal FAILED");

			}

		} else {
			AddLogline("Port is already mapped. Aborting test.");
		}

		AddLogline("Stopping weupnp");
	}

	/**
	* Creates a SWT GUI for logging test output and a copy-to-clipboard button
	*/
	private static void initGUI() {

		frame = new JFrame("weupnp Test Application");
		frame.setLayout(new BorderLayout(10,10));

		JTextArea info = new JTextArea("This test will attempt to discover gateway devices in "+
				"your LAN and test creating and removing a port mapping.");
		info.setEditable(false);
		info.setLineWrap(true);
		info.setOpaque(false);
		info.setBorder(null);
		info.setFocusable(false);
		info.setWrapStyleWord(true);
		info.setMargin(new Insets(10, 10, 10, 10));

		log = new JTextArea();
		log.setEditable(false);
		log.setLineWrap(true);
		log.setMargin(new Insets(0, 5, 0, 5));

		JScrollPane scrollPane = new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JButton btnCopyLog2CB= new JButton("Copy log to clipboard");
		btnCopyLog2CB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				StringSelection stringSelection = new StringSelection( log.getText() );
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents( stringSelection, null );
			}
		});

		frame.add(info,BorderLayout.NORTH);
		frame.add(scrollPane,BorderLayout.CENTER);
		frame.add(btnCopyLog2CB,BorderLayout.SOUTH);

		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	static void AddLogline(String line) {

		String timeStamp = DateFormat.getTimeInstance().format(new Date());
		String logline = timeStamp+": "+line+"\n";

		if (log!=null) {
			log.setCaretPosition(log.getDocument().getLength());
			log.append(logline);
		} else
			System.out.print(logline);
	}

}

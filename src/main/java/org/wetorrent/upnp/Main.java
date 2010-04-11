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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 * 
 */

/*
 * refer to miniupnpc-1.0-RC8
 */
package org.wetorrent.upnp;

import java.net.InetAddress;
import java.util.logging.Logger;

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
    
    public static void main(String[] args) throws Exception{
        Logger logger = LogUtils.getLogger();
        logger.info("Starting weupnp");

        GatewayDiscover discover = new GatewayDiscover();
        logger.info("Looking for Gateway Devices");
        discover.discover();
        GatewayDevice d = discover.getValidGateway();

        if (null != d) {
            logger.info("Gateway device found.");
        } else {
            logger.info("No valid gateway device found.");
            return;
        }
        
        InetAddress localAddress = d.getLocalAddress();
        logger.info("Using local address: " + localAddress);
        String externalIPAddress = d.getExternalIPAddress();
        logger.info("External address: " + externalIPAddress);
        PortMappingEntry portMapping = new PortMappingEntry();

        logger.info("Attempting to map port " + SAMPLE_PORT);
        logger.info("Querying device to see if mapping for port " + SAMPLE_PORT + " already exists");

        if (!d.getSpecificPortMappingEntry(SAMPLE_PORT,"TCP",portMapping)) {
            logger.info("Sending port mapping request");

            if (d.addPortMapping(SAMPLE_PORT,SAMPLE_PORT,localAddress.getHostAddress(),"TCP","test")) {
                logger.info("Mapping succesful: waiting " + WAIT_TIME + " seconds before removing mapping.");
                
                Thread.sleep(1000*WAIT_TIME);
                d.deletePortMapping(SAMPLE_PORT,"TCP");

                logger.info("Port mapping removed");
                logger.info("Test SUCCESSFUL");
            } else {
                logger.info("Port mapping removal failed");
                logger.info("Test FAILED");
            }
            
        } else {
            logger.info("Port was already mapped. Aborting test.");
        }

        logger.info("Stopping weupnp");
    }
    
}

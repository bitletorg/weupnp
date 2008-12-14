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

package org.wetorrent.upnp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author casta
 */
public class GatewayDeviceHandler extends DefaultHandler{
    
    GatewayDevice device;
    /** Creates a new instance of GatewayDeviceHandler */
    public GatewayDeviceHandler(GatewayDevice device) {
        this.device = device;
    }
    
    String currentElement;
    int level = 0;
    short state = 0;
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = localName;
        level++;
        if (state < 1 && "serviceList".compareTo(currentElement) == 0) {
            state = 1;
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentElement = "";
        level--;
        if (localName.compareTo("service")==0){
            if (device.getServiceTypeCIF() != null &&
                    device.getServiceTypeCIF().compareTo("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1") == 0)
                state = 2;
            if (device.getServiceType() != null &&
                    device.getServiceType().compareTo("urn:schemas-upnp-org:service:WANIPConnection:1") == 0)
                state = 3;
        }
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentElement.compareTo("URLBase") == 0)
            device.setURLBase(new String(ch,start,length));
        else if (state<=1) {
            if (state == 0) {
                if ("friendlyName".compareTo(currentElement) == 0)
                    device.setFriendlyName(new String(ch,start,length));
                else if ("manufacturer".compareTo(currentElement) == 0)
                    device.setManufacturer(new String(ch,start,length));
                else if ("modelDescription".compareTo(currentElement) == 0)
                    device.setModelDescription(new String(ch,start,length));
                else if ("presentationURL".compareTo(currentElement) == 0)
                    device.setPresentationURL(new String(ch,start,length));
            }
            if( currentElement.compareTo("serviceType") == 0 )
                device.setServiceTypeCIF(new String(ch,start,length));
            else if( currentElement.compareTo( "controlURL") == 0)
                device.setControlURLCIF(new String(ch,start,length));
            else if( currentElement.compareTo( "eventSubURL") == 0 )
                device.setEventSubURLCIF(new String(ch,start,length));
            else if( currentElement.compareTo( "SCPDURL") == 0  )
                device.setSCPDURLCIF(new String(ch,start,length));
            else if( currentElement.compareTo( "deviceType") == 0 )
                device.setDeviceTypeCIF(new String(ch,start,length));
        }else if (state==2){
            if( currentElement.compareTo("serviceType") == 0 )
                device.setServiceType(new String(ch,start,length));
            else if( currentElement.compareTo( "controlURL") == 0)
                device.setControlURL(new String(ch,start,length));
            else if( currentElement.compareTo( "eventSubURL") == 0 )
                device.setEventSubURL(new String(ch,start,length));
            else if( currentElement.compareTo( "SCPDURL") == 0  )
                device.setSCPDURL(new String(ch,start,length));
            else if( currentElement.compareTo( "deviceType") == 0 )
                device.setDeviceType(new String(ch,start,length));
            
        }
    }
    
}

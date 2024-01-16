package org.cloudsimplus;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.FileInputStream;
import java.io.IOException;

public class PlatformParser {
    public static Platform loadPlatform(String filepath) {
        XmlMapper xmlMapper = new XmlMapper();
        try (FileInputStream fileInputStream = new FileInputStream(filepath)) {
            return xmlMapper.readValue(fileInputStream, Platform.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void testifworks(Platform platform) {
        for(DatacenterPOJO dc : platform.getDatacenters()){
            System.out.println("Datacenter: " + dc.getId());
            for(HostPOJO host : dc.getHosts()){
                System.out.println("Host: " + host.getId());
                for(PePOJO pe : host.getCores()){
                    System.out.println("Pe: " + pe.getId() + " " + pe.getMips());
                }
            }
        }
    }
}

package com.openenergi.flex;

import com.openenergi.flex.device.BasicClient;
import com.openenergi.flex.device.Client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

    static Client newClient() throws IOException{
        String resourceName = "iothub.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        InputStream resourceStream = loader.getResourceAsStream(resourceName);
        props.load(resourceStream);
        return new BasicClient(props.getProperty("hub"), props.getProperty("device"), props.getProperty("key"));
    }

    public static void main(String [] args){
        DeviceMock device;
        try {
            device = new DeviceMock(newClient());
        } catch (IOException e){
            e.printStackTrace();
            return;
        }

        LoadMock L1234 = new LoadMock(device, "L1234");
        LoadMock L2345 = new LoadMock(device, "L2345");
        LoadMock L3456 = new LoadMock(device, "L3456");
        System.out.println("...Starting simulation...");
        L1234.startSimulation();
        L2345.startSimulation();
        L3456.startSimulation();
    }
}

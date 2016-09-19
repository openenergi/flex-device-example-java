package com.openenergi.flex;

import com.openenergi.flex.device.Client;
import com.openenergi.flex.message.*;
/**
 * Mocks a device for sending data to the IoT Hub.
 */
public class DeviceMock {
    Client client;


    public DeviceMock(Client client){
        this.client = client;
    }

    public void reportTemperature(String entity, Double temperature){
        System.out.println("New temperature for " + entity + " : " + temperature.toString() + " degC");
        this.client.publish(new Reading.Builder()
                                .withCustomType("temperature")
                                .withEntity(entity)
                                .withValue(temperature)
                                .build());
    }

    public void reportAvailabilityHigh(String entity, Double availability){
        System.out.println("New high availability for " + entity + " : " + availability.toString() + " kW");
        this.client.publish(new Reading.Builder()
                                .withType(Reading.Type.AVAILABILITY_FFR_HIGH)
                                .withEntity(entity)
                                .withValue(availability)
                                .build());
    }

    public void reportAvailabilityLow(String entity, Double availability){
        System.out.println("New low availability for " + entity + " : " + availability.toString() + " kW");
        this.client.publish(new Reading.Builder()
                .withType(Reading.Type.AVAILABILITY_FFR_LOW)
                .withEntity(entity)
                .withValue(availability)
                .build());
    }

    public void reportPowerConsumption(String entity, Double power){
        System.out.println("New power consumption for " + entity + " : " + power.toString() + " kW");
        this.client.publish(new Reading.Builder()
                .withType(Reading.Type.POWER)
                .withEntity(entity)
                .withValue(power)
                .build());
    }

    public void reportEvent(Event event){
        System.out.println("Received event from entity " + event.getEntity() + " : " + event.toString());
        this.client.publish(event);
    }
}

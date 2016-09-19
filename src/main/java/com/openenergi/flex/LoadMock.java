package com.openenergi.flex;

import com.openenergi.flex.message.Event;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * LoadMock simulates a bitumen tank.
 */
public class LoadMock {
    static final Double CLAMP = 35.0; //maximum power consumption of the load
    static final Double LOW_TEMP = 130.; //lowest possible setpointLow
    static final Double TEMP_DELTA_OFF = -0.1; //degrees lost per iteration when tank off
    static final Double TEMP_DELTA_ON = 0.25; //degrees gained per iteration when tank on
    static final Double REPORTING_THRESHOLD = 1.; //percentage that a quantity must have changed before we report it
    static final Long SIMULATION_INTERVAL = 5L; //number of seconds between simulation runs

    public final static ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2, new ThreadPoolExecutor.CallerRunsPolicy());

    private Double temperature = 0.;
    private Double reportedTemperature = 0.;
    private Double setpointHigh = 0.;
    private Double setpointLow = 0.;
    private Double power = 0.;
    private Double reportedPower = 0.;
    private Double availabilityHigh = 0.;
    private Double reportedAvailabilityHigh = 0.;
    private Double availabilityLow = 0.;
    private Double reportedAvailabilityLow = 0.;

    private Boolean interrupt;
    private String entity;
    private DeviceMock device;


    public LoadMock(DeviceMock device, String entity){
        this.entity = entity;
        this.device = device;
        Random rand = new Random();

        this.setpointLow = LOW_TEMP + rand.nextInt(20);
        this.setpointHigh = this.setpointLow + 1 + rand.nextInt(20);
        this.temperature = this.setpointLow - 5.0 + rand.nextDouble()*10; //50-50 chance of tank needing to turn on now
        this.availabilityHigh = CLAMP;
    }

    /**
     * Starts the simulation - will perform one iteration every 5 seconds.
     */
    public void startSimulation(){
        this.interrupt = false;
        scheduler.schedule(()->this.next(), SIMULATION_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Interrupt the simulation.
     */
    public void stopSimulation(){
        this.interrupt = true;
    }

    private void next(){
        try {
            if (this.power > 0){
                this.temperature += TEMP_DELTA_ON;
            } else {
                this.temperature += TEMP_DELTA_OFF;
            }

            recomputeAvailability();

            if (this.temperature <= this.setpointLow){
                this.power = CLAMP;
            }

            if (this.temperature >= this.setpointHigh){
                this.power = 0.;
            }

            report();

            if (!this.interrupt){
                this.startSimulation(); //schedule next run unless the user has interrupted us
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private Double percentDifference(Double x1, Double x2){
        if (x1 == 0. && x2 == 0.){
            return 0.;
        } else if (x1 == 0){
            return 100.;
        }
        return 100*(x2-x1)/x1;
    }

    private void report(){
        if (this.temperature < this.setpointLow && this.reportedTemperature >= this.setpointLow){
            device.reportEvent(new Event.Builder()
                                .withCustomType("temperature-low")
                                .withEntity(this.entity)
                                .withLevel(Event.Level.INFO)
                                .build());
        }

        if (percentDifference(this.reportedTemperature, this.temperature) > REPORTING_THRESHOLD){
            device.reportTemperature(this.entity, this.temperature);
            this.reportedTemperature = this.temperature;
        }

        if (percentDifference(this.reportedPower, this.power) > REPORTING_THRESHOLD){
            device.reportPowerConsumption(this.entity, this.power);
            this.reportedPower = this.power;
        }

        if (percentDifference(this.reportedAvailabilityHigh, this.availabilityHigh) > REPORTING_THRESHOLD){
            device.reportAvailabilityHigh(this.entity, this.availabilityHigh);
            this.reportedAvailabilityHigh = this.availabilityHigh;
        }

        if (percentDifference(this.reportedAvailabilityLow, this.availabilityLow) > REPORTING_THRESHOLD){
            device.reportAvailabilityLow(this.entity, this.availabilityLow);
            this.reportedAvailabilityLow = this.availabilityLow;
        }

        System.out.println(this.entity + ": Temperature=(" + this.setpointLow.toString() + ", [" + this.temperature.toString() + "], " + this.setpointHigh.toString() + "); Power=" + this.power.toString());

    }



    private void recomputeAvailability(){
        if (this.temperature > this.setpointLow && this.temperature < this.setpointHigh){
            //controllable
            if (this.power > 0) {
                this.availabilityLow = CLAMP;
                this.availabilityHigh = 0.;
            } else {
                this.availabilityHigh = CLAMP;
                this.availabilityLow = 0.;
            }
        }
        //uncontrollable
        this.availabilityHigh = 0.;
        this.availabilityLow = 0.;
    }

}

package com.kce.warehouse.model;
public class Shipment {
    private String shipmentId;
    private Pack pack;
    private String carrierName;
    private String status; 
    public Shipment(String shipmentId, Pack pack, String carrierName) {
        this.shipmentId = shipmentId;
        this.pack = pack;
        this.carrierName = carrierName;
        this.status = "OPEN";
    }
    public String getShipmentId() {
        return shipmentId;
    }
    public Pack getPack() {
        return pack;
    }
    public String getCarrierName() {
        return carrierName;
    }
    public String getStatus() {
        return status;
    }
    public void close() {
        this.status = "CLOSED";
    }
}


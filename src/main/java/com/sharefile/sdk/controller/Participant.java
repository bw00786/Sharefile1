package com.sharefile.sdk.controller;

import javax.persistence.*;
@Entity
@Table(name ="participant",schema="sharedReference")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="participantID")
    long participantID;

    @Column(name = "participantName")
    private String participantName;

    @Column(name="entityType")
    private String entityType;

    @Column(name="address1")
    private String address;

    @Column(name="city")
    private String city;

    @Column(name="state")
    private String state;

    @Column(name="zip")
    private String zip;

    @Column(name="contactFirstName")
    private String contactFirstName;

    @Column(name="contactLastName")
    private String contactLastName;

    @Column(name="contactEmail")
    private String contactEmail;

    @Column(name="contactPhone")
    private String contactPhone;

    @Column(name="industryType")
    private String industryType;

    @Column(name="intakeSubmitDate")
    private String intakeSubmitDate;

    @Column(name="shortName")
    private String shortName;

    @Column(name="shortNameStage")
    private String shortNameStage;


    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public long getParticipantID() {
        return participantID;
    }

    public void setParticipantID(long participantID) {
        this.participantID = participantID;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getIntakeSubmitDate() {
        return intakeSubmitDate;
    }

    public void setIntakeSubmitDate(String intakeSubmitDate) {
        this.intakeSubmitDate = intakeSubmitDate;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortNameStage() {
        return shortNameStage;
    }

    public void setShortNameStage(String shortNameStage) {
        this.shortNameStage = shortNameStage;
    }

}

package org.dhis2.messaging.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by iNick on 25.09.14.
 */
public class ProfileModel {

    @SerializedName("id")
    private String id;
    @SerializedName("firstName")
    private String firstName;
    @SerializedName("surname")
    private String surname;
    @SerializedName("email")
    private String email;
    @SerializedName("phoneNumber")
    private String phoneNumber;
    @SerializedName("jobTitle")
    private String jobTitle;
    @SerializedName("employer")
    private String employer;
    @SerializedName("education")
    private String education;
    @SerializedName("gender")
    private String gender;
    @SerializedName("interests")
    private String interests;
    @SerializedName("nationality")
    private String nationality;
    @SerializedName("languages")
    private String languages;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("gcmid")
    private String gcmid;

    public String getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getSurname() {
        return this.surname;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getJobTitle() {
        return this.jobTitle;
    }

    public String getEmployer() {
        return this.employer;
    }

    public String getEducation() {
        return this.education;
    }

    public String getGender() {
        return this.gender;
    }

    public String getInterests() {
        return this.interests;
    }

    public String getNationality() {
        return this.nationality;
    }

    public String getLanguages() {
        return this.languages;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public String getGCMIds() {
        return this.gcmid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setGcmid(String gcmid) {
        this.gcmid = gcmid;
    }
}

package com.example.womenprotection;

public class UserProfile {
    public String fullName;
    public String email;
    public String phoneNumber;
    public String birthdate;
    public String gender;
    public String emergencyContact;

    public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
    }

    public UserProfile(String fullName, String email, String phoneNumber, String birthdate, String gender, String emergencyContact) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
        this.gender = gender;
        this.emergencyContact = emergencyContact;
    }
}

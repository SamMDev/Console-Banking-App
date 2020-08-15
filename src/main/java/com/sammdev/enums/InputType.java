package com.sammdev.enums;

public enum InputType {
    NAME("(?i)(^[a-z])((?![ .,'-]$)[a-z .,'-]){0,24}$"),
    EMAIL("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$"),
    PASSWORD("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    String validationRegex;

    InputType(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public boolean isValid(String str) {
        return str.matches(validationRegex);
    }
}

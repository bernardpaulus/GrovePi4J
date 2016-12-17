package com.digitalpetri.grovepi;

public enum GroveAnalogPin {
    A0,
    A1,
    A2;

    public int number() {
        return ordinal();
    }
}

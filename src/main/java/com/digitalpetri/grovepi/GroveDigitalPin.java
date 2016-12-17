package com.digitalpetri.grovepi;

public enum GroveDigitalPin {
    D0,
    D1,
    D2,
    D3,
    D4,
    D5,
    D6,
    D7,
    D8;

    public int number() {
        return ordinal();
    }
}

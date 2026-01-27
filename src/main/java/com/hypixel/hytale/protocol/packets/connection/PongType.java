package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PongType {
    Raw(0),
    Direct(1),
    Tick(2);

    public static final PongType[] VALUES;

    static {
        VALUES = PongType.values();
    }

    private final int value;

    PongType(int value) {
        this.value = value;
    }

    public static PongType fromValue(int value) {
        if (value >= 0 && value < VALUES.length) {
            return VALUES[value];
        }
        throw ProtocolException.invalidEnumValue("PongType", value);
    }

    public int getValue() {
        return this.value;
    }
}

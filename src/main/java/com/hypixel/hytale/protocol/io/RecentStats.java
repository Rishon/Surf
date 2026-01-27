package com.hypixel.hytale.protocol.io;

public record RecentStats(int count, long uncompressedTotal, long compressedTotal, int uncompressedMin,
                          int uncompressedMax, int compressedMin, int compressedMax) {
    public static final PacketStatsRecorder.RecentStats EMPTY = new PacketStatsRecorder.RecentStats(0, 0L, 0L, 0, 0, 0, 0);
}

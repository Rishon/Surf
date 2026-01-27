package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.packets.auth.*;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.connection.Ping;
import com.hypixel.hytale.protocol.packets.connection.Pong;
import com.hypixel.hytale.protocol.packets.player.ClientReady;
import com.hypixel.hytale.protocol.packets.player.JoinWorld;
import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.protocol.packets.setup.*;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class PacketRegistry {
    private static final Map<Integer, PacketInfo> BY_ID = new HashMap<Integer, PacketInfo>();
    private static final Map<Integer, PacketInfo> BY_ID_UNMODIFIABLE = Collections.unmodifiableMap(BY_ID);
    private static final Map<Class<? extends Packet>, Integer> BY_TYPE = new HashMap<Class<? extends Packet>, Integer>();

    static {
        PacketRegistry.register(0, "Connect", Connect.class, 46, 38013, false, Connect::validateStructure, Connect::deserialize);
        PacketRegistry.register(1, "Disconnect", Disconnect.class, 2, 16384007, false, Disconnect::validateStructure, Disconnect::deserialize);
        PacketRegistry.register(2, "Ping", Ping.class, 29, 29, false, Ping::validateStructure, Ping::deserialize);
        PacketRegistry.register(3, "Pong", Pong.class, 20, 20, false, Pong::validateStructure, Pong::deserialize);
        PacketRegistry.register(10, "Status", Status.class, 9, 2587, false, Status::validateStructure, Status::deserialize);
        PacketRegistry.register(11, "AuthGrant", AuthGrant.class, 1, 49171, false, AuthGrant::validateStructure, AuthGrant::deserialize);
        PacketRegistry.register(12, "AuthToken", AuthToken.class, 1, 49171, false, AuthToken::validateStructure, AuthToken::deserialize);
        PacketRegistry.register(13, "ServerAuthToken", ServerAuthToken.class, 1, 32851, false, ServerAuthToken::validateStructure, ServerAuthToken::deserialize);
        PacketRegistry.register(14, "ConnectAccept", ConnectAccept.class, 1, 70, false, ConnectAccept::validateStructure, ConnectAccept::deserialize);
        PacketRegistry.register(15, "PasswordResponse", PasswordResponse.class, 1, 70, false, PasswordResponse::validateStructure, PasswordResponse::deserialize);
        PacketRegistry.register(16, "PasswordAccepted", PasswordAccepted.class, 0, 0, false, PasswordAccepted::validateStructure, PasswordAccepted::deserialize);
        PacketRegistry.register(17, "PasswordRejected", PasswordRejected.class, 5, 74, false, PasswordRejected::validateStructure, PasswordRejected::deserialize);
        PacketRegistry.register(18, "ClientReferral", ClientReferral.class, 1, 5141, false, ClientReferral::validateStructure, ClientReferral::deserialize);
        PacketRegistry.register(23, "RequestAssets", RequestAssets.class, 1, 0x64000000, true, RequestAssets::validateStructure, RequestAssets::deserialize);
        PacketRegistry.register(24, "AssetInitialize", AssetInitialize.class, 4, 2121, false, AssetInitialize::validateStructure, AssetInitialize::deserialize);
        PacketRegistry.register(25, "AssetPart", AssetPart.class, 1, 4096006, true, AssetPart::validateStructure, AssetPart::deserialize);
        PacketRegistry.register(26, "AssetFinalize", AssetFinalize.class, 0, 0, false, AssetFinalize::validateStructure, AssetFinalize::deserialize);
        PacketRegistry.register(27, "RemoveAssets", RemoveAssets.class, 1, 0x64000000, false, RemoveAssets::validateStructure, RemoveAssets::deserialize);
        PacketRegistry.register(28, "RequestCommonAssetsRebuild", RequestCommonAssetsRebuild.class, 0, 0, false, RequestCommonAssetsRebuild::validateStructure, RequestCommonAssetsRebuild::deserialize);
        PacketRegistry.register(29, "SetUpdateRate", SetUpdateRate.class, 4, 4, false, SetUpdateRate::validateStructure, SetUpdateRate::deserialize);
        PacketRegistry.register(30, "SetTimeDilation", SetTimeDilation.class, 4, 4, false, SetTimeDilation::validateStructure, SetTimeDilation::deserialize);
        PacketRegistry.register(31, "UpdateFeatures", UpdateFeatures.class, 1, 8192006, false, UpdateFeatures::validateStructure, UpdateFeatures::deserialize);
        PacketRegistry.register(33, "PlayerOptions", PlayerOptions.class, 1, 327680184, false, PlayerOptions::validateStructure, PlayerOptions::deserialize);
        PacketRegistry.register(34, "ServerTags", ServerTags.class, 1, 0x64000000, false, ServerTags::validateStructure, ServerTags::deserialize);
        PacketRegistry.register(100, "SetClientId", SetClientId.class, 4, 4, false, SetClientId::validateStructure, SetClientId::deserialize);
        PacketRegistry.register(104, "JoinWorld", JoinWorld.class, 18, 18, false, JoinWorld::validateStructure, JoinWorld::deserialize);
        PacketRegistry.register(105, "ClientReady", ClientReady.class, 2, 2, false, ClientReady::validateStructure, ClientReady::deserialize);
    }

    private PacketRegistry() {
    }

    private static void register(int id, String name, Class<? extends Packet> type, int fixedBlockSize, int maxSize, boolean compressed, BiFunction<ByteBuf, Integer, ValidationResult> validate, BiFunction<ByteBuf, Integer, Packet> deserialize) {
        PacketInfo existing = BY_ID.get(id);
        if (existing != null) {
            throw new IllegalStateException("Duplicate packet ID " + id + ": '" + name + "' conflicts with '" + existing.name() + "'");
        }
        PacketInfo info = new PacketInfo(id, name, type, fixedBlockSize, maxSize, compressed, validate, deserialize);
        BY_ID.put(id, info);
        BY_TYPE.put(type, id);
    }

    @Nullable
    public static PacketInfo getById(int id) {
        return BY_ID.get(id);
    }

    @Nullable
    public static Integer getId(Class<? extends Packet> type) {
        return BY_TYPE.get(type);
    }

    @Nonnull
    public static Map<Integer, PacketInfo> all() {
        return BY_ID_UNMODIFIABLE;
    }

    public record PacketInfo(int id, @Nonnull String name, @Nonnull Class<? extends Packet> type, int fixedBlockSize,
                             int maxSize, boolean compressed,
                             @Nonnull BiFunction<ByteBuf, Integer, ValidationResult> validate,
                             @Nonnull BiFunction<ByteBuf, Integer, Packet> deserialize) {
    }
}

package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.packets.auth.*;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.connection.Ping;
import com.hypixel.hytale.protocol.packets.connection.Pong;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.protocol.packets.player.*;
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
        PacketRegistry.register(20, "WorldSettings", WorldSettings.class, 5, 0x64000000, true, WorldSettings::validateStructure, WorldSettings::deserialize);
        PacketRegistry.register(21, "WorldLoadProgress", WorldLoadProgress.class, 9, 16384014, false, WorldLoadProgress::validateStructure, WorldLoadProgress::deserialize);
        PacketRegistry.register(22, "WorldLoadFinished", WorldLoadFinished.class, 0, 0, false, WorldLoadFinished::validateStructure, WorldLoadFinished::deserialize);
        PacketRegistry.register(23, "RequestAssets", RequestAssets.class, 1, 0x64000000, true, RequestAssets::validateStructure, RequestAssets::deserialize);
        PacketRegistry.register(24, "AssetInitialize", AssetInitialize.class, 4, 2121, false, AssetInitialize::validateStructure, AssetInitialize::deserialize);
        PacketRegistry.register(25, "AssetPart", AssetPart.class, 1, 4096006, true, AssetPart::validateStructure, AssetPart::deserialize);
        PacketRegistry.register(26, "AssetFinalize", AssetFinalize.class, 0, 0, false, AssetFinalize::validateStructure, AssetFinalize::deserialize);
        PacketRegistry.register(27, "RemoveAssets", RemoveAssets.class, 1, 0x64000000, false, RemoveAssets::validateStructure, RemoveAssets::deserialize);
        PacketRegistry.register(28, "RequestCommonAssetsRebuild", RequestCommonAssetsRebuild.class, 0, 0, false, RequestCommonAssetsRebuild::validateStructure, RequestCommonAssetsRebuild::deserialize);
        PacketRegistry.register(29, "SetUpdateRate", SetUpdateRate.class, 4, 4, false, SetUpdateRate::validateStructure, SetUpdateRate::deserialize);
        PacketRegistry.register(30, "SetTimeDilation", SetTimeDilation.class, 4, 4, false, SetTimeDilation::validateStructure, SetTimeDilation::deserialize);
        PacketRegistry.register(31, "UpdateFeatures", UpdateFeatures.class, 1, 8192006, false, UpdateFeatures::validateStructure, UpdateFeatures::deserialize);
        PacketRegistry.register(32, "ViewRadius", ViewRadius.class, 4, 4, false, ViewRadius::validateStructure, ViewRadius::deserialize);
        PacketRegistry.register(33, "PlayerOptions", PlayerOptions.class, 1, 327680184, false, PlayerOptions::validateStructure, PlayerOptions::deserialize);
        PacketRegistry.register(34, "ServerTags", ServerTags.class, 1, 0x64000000, false, ServerTags::validateStructure, ServerTags::deserialize);
        PacketRegistry.register(100, "SetClientId", SetClientId.class, 4, 4, false, SetClientId::validateStructure, SetClientId::deserialize);
        PacketRegistry.register(101, "SetGameMode", SetGameMode.class, 1, 1, false, SetGameMode::validateStructure, SetGameMode::deserialize);
        PacketRegistry.register(102, "SetMovementStates", SetMovementStates.class, 2, 2, false, SetMovementStates::validateStructure, SetMovementStates::deserialize);
        PacketRegistry.register(103, "SetBlockPlacementOverride", SetBlockPlacementOverride.class, 1, 1, false, SetBlockPlacementOverride::validateStructure, SetBlockPlacementOverride::deserialize);
        PacketRegistry.register(104, "JoinWorld", JoinWorld.class, 18, 18, false, JoinWorld::validateStructure, JoinWorld::deserialize);
        PacketRegistry.register(105, "ClientReady", ClientReady.class, 2, 2, false, ClientReady::validateStructure, ClientReady::deserialize);
        PacketRegistry.register(106, "LoadHotbar", LoadHotbar.class, 1, 1, false, LoadHotbar::validateStructure, LoadHotbar::deserialize);
        PacketRegistry.register(107, "SaveHotbar", SaveHotbar.class, 1, 1, false, SaveHotbar::validateStructure, SaveHotbar::deserialize);
        PacketRegistry.register(108, "ClientMovement", ClientMovement.class, 153, 153, false, ClientMovement::validateStructure, ClientMovement::deserialize);
        PacketRegistry.register(109, "ClientTeleport", ClientTeleport.class, 52, 52, false, ClientTeleport::validateStructure, ClientTeleport::deserialize);
        PacketRegistry.register(110, "UpdateMovementSettings", UpdateMovementSettings.class, 252, 252, false, UpdateMovementSettings::validateStructure, UpdateMovementSettings::deserialize);
        PacketRegistry.register(111, "MouseInteraction", MouseInteraction.class, 44, 20480071, false, MouseInteraction::validateStructure, MouseInteraction::deserialize);
        PacketRegistry.register(112, "DamageInfo", DamageInfo.class, 29, 32768048, false, DamageInfo::validateStructure, DamageInfo::deserialize);
        PacketRegistry.register(113, "ReticleEvent", ReticleEvent.class, 4, 4, false, ReticleEvent::validateStructure, ReticleEvent::deserialize);
        PacketRegistry.register(114, "DisplayDebug", DisplayDebug.class, 19, 32768037, false, DisplayDebug::validateStructure, DisplayDebug::deserialize);
        PacketRegistry.register(115, "ClearDebugShapes", ClearDebugShapes.class, 0, 0, false, ClearDebugShapes::validateStructure, ClearDebugShapes::deserialize);
        PacketRegistry.register(116, "SyncPlayerPreferences", SyncPlayerPreferences.class, 12, 12, false, SyncPlayerPreferences::validateStructure, SyncPlayerPreferences::deserialize);
        PacketRegistry.register(117, "ClientPlaceBlock", ClientPlaceBlock.class, 20, 20, false, ClientPlaceBlock::validateStructure, ClientPlaceBlock::deserialize);
        PacketRegistry.register(118, "UpdateMemoriesFeatureStatus", UpdateMemoriesFeatureStatus.class, 1, 1, false, UpdateMemoriesFeatureStatus::validateStructure, UpdateMemoriesFeatureStatus::deserialize);
        PacketRegistry.register(119, "RemoveMapMarker", RemoveMapMarker.class, 1, 16384006, false, RemoveMapMarker::validateStructure, RemoveMapMarker::deserialize);
        PacketRegistry.register(210, "ServerMessage", ServerMessage.class, 2, 0x64000000, false, ServerMessage::validateStructure, ServerMessage::deserialize);
        PacketRegistry.register(211, "ChatMessage", ChatMessage.class, 1, 16384006, false, ChatMessage::validateStructure, ChatMessage::deserialize);
        PacketRegistry.register(212, "Notification", Notification.class, 2, 0x64000000, false, Notification::validateStructure, Notification::deserialize);
        PacketRegistry.register(213, "KillFeedMessage", KillFeedMessage.class, 1, 0x64000000, false, KillFeedMessage::validateStructure, KillFeedMessage::deserialize);
        PacketRegistry.register(214, "ShowEventTitle", ShowEventTitle.class, 14, 0x64000000, false, ShowEventTitle::validateStructure, ShowEventTitle::deserialize);
        PacketRegistry.register(215, "HideEventTitle", HideEventTitle.class, 4, 4, false, HideEventTitle::validateStructure, HideEventTitle::deserialize);
        PacketRegistry.register(216, "SetPage", SetPage.class, 2, 2, false, SetPage::validateStructure, SetPage::deserialize);
        PacketRegistry.register(217, "CustomHud", CustomHud.class, 2, 0x64000000, true, CustomHud::validateStructure, CustomHud::deserialize);
        PacketRegistry.register(218, "CustomPage", CustomPage.class, 4, 0x64000000, true, CustomPage::validateStructure, CustomPage::deserialize);
        PacketRegistry.register(219, "CustomPageEvent", CustomPageEvent.class, 2, 16384007, false, CustomPageEvent::validateStructure, CustomPageEvent::deserialize);
        PacketRegistry.register(222, "EditorBlocksChange", EditorBlocksChange.class, 30, 139264048, true, EditorBlocksChange::validateStructure, EditorBlocksChange::deserialize);
        PacketRegistry.register(223, "ServerInfo", ServerInfo.class, 5, 32768023, false, ServerInfo::validateStructure, ServerInfo::deserialize);
        PacketRegistry.register(224, "AddToServerPlayerList", AddToServerPlayerList.class, 1, 0x64000000, false, AddToServerPlayerList::validateStructure, AddToServerPlayerList::deserialize);
        PacketRegistry.register(225, "RemoveFromServerPlayerList", RemoveFromServerPlayerList.class, 1, 65536006, false, RemoveFromServerPlayerList::validateStructure, RemoveFromServerPlayerList::deserialize);
        PacketRegistry.register(226, "UpdateServerPlayerList", UpdateServerPlayerList.class, 1, 131072006, false, UpdateServerPlayerList::validateStructure, UpdateServerPlayerList::deserialize);
        PacketRegistry.register(227, "UpdateServerPlayerListPing", UpdateServerPlayerListPing.class, 1, 81920006, false, UpdateServerPlayerListPing::validateStructure, UpdateServerPlayerListPing::deserialize);
        PacketRegistry.register(228, "UpdateKnownRecipes", UpdateKnownRecipes.class, 1, 0x64000000, false, UpdateKnownRecipes::validateStructure, UpdateKnownRecipes::deserialize);
        PacketRegistry.register(229, "UpdatePortal", UpdatePortal.class, 6, 16384020, false, UpdatePortal::validateStructure, UpdatePortal::deserialize);
        PacketRegistry.register(230, "UpdateVisibleHudComponents", UpdateVisibleHudComponents.class, 1, 4096006, false, UpdateVisibleHudComponents::validateStructure, UpdateVisibleHudComponents::deserialize);
        PacketRegistry.register(231, "ResetUserInterfaceState", ResetUserInterfaceState.class, 0, 0, false, ResetUserInterfaceState::validateStructure, ResetUserInterfaceState::deserialize);
        PacketRegistry.register(232, "UpdateLanguage", UpdateLanguage.class, 1, 16384006, false, UpdateLanguage::validateStructure, UpdateLanguage::deserialize);
        PacketRegistry.register(233, "WorldSavingStatus", WorldSavingStatus.class, 1, 1, false, WorldSavingStatus::validateStructure, WorldSavingStatus::deserialize);
        PacketRegistry.register(234, "OpenChatWithCommand", OpenChatWithCommand.class, 1, 16384006, false, OpenChatWithCommand::validateStructure, OpenChatWithCommand::deserialize);
    }

    public record PacketInfo(int id, @Nonnull String name, @Nonnull Class<? extends Packet> type, int fixedBlockSize,
                             int maxSize, boolean compressed,
                             @Nonnull BiFunction<ByteBuf, Integer, ValidationResult> validate,
                             @Nonnull BiFunction<ByteBuf, Integer, Packet> deserialize) {
    }
}

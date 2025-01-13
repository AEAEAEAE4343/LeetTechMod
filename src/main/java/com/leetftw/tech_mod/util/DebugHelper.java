package com.leetftw.tech_mod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class DebugHelper
{
    public static void chatOutput(String message)
    {
        Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(message)));
    }

    public static void chatOutput(Component message)
    {
        Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(message));
    }
}

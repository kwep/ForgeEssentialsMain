package com.forgeessentials.core.commands.selections;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.util.PlayerInfo;
import com.forgeessentials.util.UserIdent;
import com.forgeessentials.util.events.ServerEventHandler;
import com.forgeessentials.commons.selections.WorldPoint;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class SelectionEventHandler extends ServerEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent event)
    {
        // Only handle server events
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        // get info now rather than later
        EntityPlayer player = event.entityPlayer;
        PlayerInfo info = PlayerInfo.getPlayerInfo(player);

        if (!info.isWandEnabled()) return;

        // Check if wand should activate
        if (player.getCurrentEquippedItem() == null)
        {
            if (info.getWandID() != "hands") return;
        }
        else
        {
            if (!(player.getCurrentEquippedItem().getItem().getUnlocalizedName().equals(info.getWandID()))) return;
            if (player.getCurrentEquippedItem().getItemDamage() != info.getWandDmg()) return;
        }

        WorldPoint point = new WorldPoint(player.dimension, event.x, event.y, event.z);
        if (!APIRegistry.perms.checkUserPermission(new UserIdent(player), point, "fe.core.pos"))
        {
            OutputHandler.chatError(player,
                    "You have insufficient permissions to do that. If you believe you received this message in error, please talk to a server admin.");
            return;
        }

        // left Click
        if (event.action.equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
        {
            PlayerInfo.selectionProvider.setPoint1((EntityPlayerMP) event.entityPlayer, point);
            IChatComponent format = OutputHandler.createFromText("Pos1 set to " + event.x + ", " + event.y + ", " + event.z);
            player.addChatMessage(OutputHandler.colourize(format, EnumChatFormatting.DARK_PURPLE));
            event.setCanceled(true);
        }
        // right Click
        else if (event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK))
        {
            PlayerInfo.selectionProvider.setPoint2((EntityPlayerMP) event.entityPlayer, point);
            IChatComponent format = OutputHandler.createFromText("Pos2 set to " + event.x + ", " + event.y + ", " + event.z);
            player.addChatMessage(OutputHandler.colourize(format, EnumChatFormatting.DARK_PURPLE));
            event.setCanceled(true);
        }
    }
    
}

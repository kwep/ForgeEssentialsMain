package com.forgeessentials.teleport;

import com.forgeessentials.commons.selections.Point;
import com.forgeessentials.core.commands.ForgeEssentialsCommandBase;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.util.PlayerInfo;
import com.forgeessentials.commons.selections.WarpPoint;
import cpw.mods.fml.common.FMLCommonHandler;
import java.util.HashMap;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.permissions.PermissionsManager.RegisteredPermValue;
public class CommandTppos extends ForgeEssentialsCommandBase {

    /**
     * Spawn point for each dimension
     */
    public static HashMap<Integer, Point> spawnPoints = new HashMap<Integer, Point>();

    @Override
    public String getCommandName()
    {
        return "tppos";
    }

    @Override
    public void processCommandPlayer(EntityPlayerMP sender, String[] args)
    {
        if (args.length == 3)
        {
            double x = parseDouble(sender, args[0], sender.posX);
            double y = parseDouble(sender, args[1], sender.posY);
            double z = parseDouble(sender, args[2], sender.posZ);
            EntityPlayerMP player = sender;
            PlayerInfo playerInfo = PlayerInfo.getPlayerInfo(player.getPersistentID());
            playerInfo.setLastTeleportOrigin(new WarpPoint(player));
            CommandBack.justDied.remove(player.getPersistentID());
            TeleportHelper.teleport(player, new WarpPoint(player.dimension, x, y, z, player.cameraPitch, player.cameraYaw));
        }
        else
        {
            this.error(sender);
        }
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public String getPermissionNode()
    {
        return TeleportModule.PERM_TPPOS;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1 || args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, FMLCommonHandler.instance().getMinecraftServerInstance().getAllUsernames());
        }
        else
        {
            return null;
        }
    }

    @Override
    public RegisteredPermValue getDefaultPermission()
    {
        return RegisteredPermValue.TRUE;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/tppos <x y z> Teleport to a position.";
    }
}

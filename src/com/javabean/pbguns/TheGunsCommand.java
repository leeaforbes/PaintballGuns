package com.javabean.pbguns;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class TheGunsCommand implements CommandExecutor{
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender instanceof Player && ((Player)sender).isOp()){
			Player player = (Player)sender;
			String commandSoFar = "/guns";
			
			//no args
			if(args.length == 0){
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <give>.");
			}
			else if(args[0].equalsIgnoreCase("give") && args.length == 2){
				commandSoFar += " " + args[0];
				Integer gunID = PaintballGuns.nameToGunID.get(args[1].toLowerCase());
				if(args.length == 2 && gunID != null){
					PaintballGuns.giveGun(player, gunID);
				}
			}
			else{
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <give>.");
			}
		}
		return true;
	}
}

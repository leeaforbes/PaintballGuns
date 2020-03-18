package com.javabean.pbguns;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class PaintballGunsListener implements Listener{
	
	Plugin plugin;
	
	public PaintballGunsListener(Plugin p){
		plugin = p;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getHand() == EquipmentSlot.HAND){
			Player player = event.getPlayer();
			
			if(event.getItem() != null && event.getItem().getItemMeta() != null && event.getItem().getItemMeta().hasAttributeModifiers()){
				Collection<AttributeModifier> luckModifiers = event.getItem().getItemMeta().getAttributeModifiers(Attribute.GENERIC_LUCK);
//				player.sendMessage("" + luckModifiers);
				double isAGun = 0;
				double gunUUID = Double.MAX_VALUE;
				for(AttributeModifier attMod : luckModifiers){
					if(attMod.getName().equals("isAGun")){
						isAGun = attMod.getAmount();
					}
					if(attMod.getName().equals("UUID")){
						gunUUID = attMod.getAmount();
					}
				}
				if(isAGun == 1){
					Gun gun = PaintballGuns.guns.get(gunUUID);
					if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
						gunAction(player, gun, false);
					}
					else if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR){
						gunAction(player, gun, true);
					}
					event.setCancelled(true);
				}
			}
			
		}
	}
	
	private void gunAction(Player player, Gun gun, boolean isLeftClick){
		//if not left click, is right click
		if(isLeftClick){
			gun.reload();
		}
		else{
			gun.fire();
		}
	}
	
	@EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Snowball){
			if(event.getEntity() instanceof LivingEntity){
				LivingEntity victim = (LivingEntity)event.getEntity();
				Snowball bullet = (Snowball)event.getDamager();
				Player attacker = (Player)bullet.getShooter();
				if(attacker != null && bullet.getMetadata("gun").size() > 0){
					Gun attackerGun = (Gun)bullet.getMetadata("gun").get(0).value();
					//
					@SuppressWarnings("unchecked")
					ArrayList<Snowball> grouping = (ArrayList<Snowball>)bullet.getMetadata("grouping").get(0).value();
					
					if(grouping.size() == 1){
						if(isAHeadshot(victim, bullet)){
							victim.damage(attackerGun.getDamage() * 3);
							attacker.sendMessage(ChatColor.GREEN  + "" + ChatColor.BOLD + "HEADSHOT " + ChatColor.DARK_GREEN + victim.getName() + ChatColor.RED + " (-" + attackerGun.getDamage() * 3 + ").");
							victim.sendMessage(ChatColor.RED  + "" + ChatColor.BOLD + "HEADSHOT " + ChatColor.DARK_RED + "by " + attacker.getName() + ChatColor.RED + " (-" + attackerGun.getDamage() * 3 + ").");
							if(victim instanceof Player){
								((Player)victim).playSound(victim.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 2.0f);
							}
							if(attacker instanceof Player){
								((Player)attacker).playSound(attacker.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 2.0f);
							}
						}
						else{
							victim.damage(attackerGun.getDamage());
							attacker.sendMessage(ChatColor.GREEN  + "" + ChatColor.BOLD + "SHOT " + ChatColor.DARK_GREEN + victim.getName() + ChatColor.RED + " (-" + attackerGun.getDamage() + ").");
							victim.sendMessage(ChatColor.RED  + "" + ChatColor.BOLD + "SHOT " + ChatColor.DARK_RED + "by " + attacker.getName() + ChatColor.RED + " (-" + attackerGun.getDamage() + ").");
							if(victim instanceof Player){
								((Player)victim).playSound(victim.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
							}
							if(attacker instanceof Player){
								((Player)attacker).playSound(attacker.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
							}
						}
					}
					else{
						double groupingDamage = 0;
						boolean hasAHeadshot = false;
						for(Snowball groupBullet : grouping){
							if(isSnowballInHitbox(victim, groupBullet)){
								if(isAHeadshot(victim, groupBullet)){
									groupingDamage += attackerGun.getDamage() * 3;
									hasAHeadshot = true;
								}
								groupingDamage += attackerGun.getDamage();
							}
						}
						victim.damage(groupingDamage);
						
						attacker.sendMessage(ChatColor.GREEN  + "" + ChatColor.BOLD + (hasAHeadshot ? "HEADSHOT " : "SHOT ") + ChatColor.DARK_GREEN + victim.getName() + ChatColor.RED + " (-" + groupingDamage + ").");
						victim.sendMessage(ChatColor.RED  + "" + ChatColor.BOLD + (hasAHeadshot ? "HEADSHOT " : "SHOT ") + ChatColor.DARK_RED + "by " + attacker.getName() + ChatColor.RED + " (-" + groupingDamage + ").");
						
						if(hasAHeadshot){
							if(victim instanceof Player){
								((Player)victim).playSound(victim.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 2.0f);
							}
							if(attacker instanceof Player){
								((Player)attacker).playSound(attacker.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 2.0f);
							}
						}
						else{
							if(victim instanceof Player){
								((Player)victim).playSound(victim.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
							}
							if(attacker instanceof Player){
								((Player)attacker).playSound(attacker.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
							}
						}
					}
					
				}
	        }
		}
		else if(event.getDamager() instanceof Player){
			Player player = (Player)event.getDamager();
			ItemMeta metaData = player.getInventory().getItemInMainHand().getItemMeta();
			if(metaData.hasAttributeModifiers()){
				Collection<AttributeModifier> attMods = metaData.getAttributeModifiers(Attribute.GENERIC_LUCK);
				if(attMods.size() > 0){
					for(AttributeModifier attMod : attMods){
						if(attMod.getName().equals("UUID") && PaintballGuns.guns.get(attMod.getAmount()) != null){
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
    }
	
	private boolean isAHeadshot(LivingEntity victim, Snowball bullet){
		Location loc = bullet.getLocation();
		RayTraceResult result = victim.getBoundingBox().expand(0.5).rayTrace(new Vector(loc.getX(), loc.getY(), loc.getZ()), bullet.getVelocity(), 10);
		if(result != null){
			double distance = result.getHitPosition().distance(victim.getEyeLocation().toVector());
			if(distance <= 0.9){
				return true;
			}
		}
		result = victim.getBoundingBox().expand(0.5).rayTrace(new Vector(loc.getX(), loc.getY(), loc.getZ()), bullet.getVelocity(), -10);
		if(result != null){
			double distance = result.getHitPosition().distance(victim.getEyeLocation().toVector());
			if(distance <= 0.9){
				return true;
			}
		}
		return false;
	}
	
	private boolean isSnowballInHitbox(LivingEntity victim, Snowball bullet){
		Location loc = bullet.getLocation();
		return victim.getBoundingBox().expand(0.5).rayTrace(new Vector(loc.getX(), loc.getY(), loc.getZ()), bullet.getVelocity(), 5) != null
				|| victim.getBoundingBox().expand(0.5).rayTrace(new Vector(loc.getX(), loc.getY(), loc.getZ()), bullet.getVelocity(), -5) != null;
	}
	
	@EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Snowball && event.getDamager().getMetadata("gun").size() > 0){
            LivingEntity entity = (LivingEntity) event.getEntity();
            new BukkitRunnable(){
                public void run(){
                    if(!event.isCancelled()){
                        entity.setNoDamageTicks(0);
                        entity.setMaximumNoDamageTicks(0);
                    }
                }
            }.runTaskLater(plugin, 1);
        }
    }
}

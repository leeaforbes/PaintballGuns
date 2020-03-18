package com.javabean.pbguns;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Gun{
	
	Plugin plugin;
	
	//gun properties
	private int gunID;
	private String displayName;
	private double damage;
	private double bulletSpeed;
	private double innacuracy;
	
	private long timeLastFired = 0;
	private double fireRate;
	
	private long timeStartedReloading = 0;
	private double reloadSpeed;
	private final int maxAmmo;
	private int ammo;
	private final int shotsFired;
	
	//keep track of the actual item as it moves around
	private ItemStack itemStack;
	private Material itemMaterial;
	private Player holder;
	
	public Gun(Plugin plugin, int id, String name, double d, double s, double a, double rate, double reload, int maxAmmo, int shotsFired, Player p, ItemStack item, Material mat){
		this.plugin = plugin;
		gunID = id;
		displayName = name;
		damage = d;
		bulletSpeed = s;
		setInaccuracy(a);
		setFireRate(rate);
		setReloadSpeed(reload);
		holder = p;
		setItemStack(item);
		itemMaterial = mat;
		this.maxAmmo = maxAmmo;
		ammo = maxAmmo;
		this.shotsFired = shotsFired;
	}
	
	public void fire(){
		String message = "";
		if(canFireAgain() && ammo > 0){
			timeLastFired = System.currentTimeMillis();
			ammo--;
//			holder.sendMessage(ChatColor.RED + "FIRE! ammo left:" + ammo);
			ArrayList<Snowball> grouping = new ArrayList<Snowball>(shotsFired);
			for(int shot = 0; shot < shotsFired; shot++){
				Location loc = holder.getLocation();
				Entity snowballEntity = holder.getWorld().spawnEntity(new Location(loc.getWorld(), loc.getX(), loc.getY() + 1.5, loc.getZ()), EntityType.SNOWBALL);
				Snowball snowball = (Snowball)snowballEntity;
				snowball.setShooter(holder);
				//
				grouping.add(snowball);
				
				FixedMetadataValue gunMeta = new FixedMetadataValue(plugin, this);
				snowball.setMetadata("gun", gunMeta);
				//
				FixedMetadataValue shotGrouping = new FixedMetadataValue(plugin, grouping);
				snowball.setMetadata("grouping", shotGrouping);
				
				adjustDirectionForInnacuracy(loc);
				Vector direction = getDirection(loc);
				direction.multiply(bulletSpeed);
				snowballEntity.setVelocity(direction);
//				snowball.setGravity(false); snipers?
			}
			if(ammo == 0){
				message = "§a§l" + displayName + " §c§lEMPTY";
				
			}
			else{
				message = "§a§l" + displayName + " §b§l(" + ammo + "/" + maxAmmo + ")";
			}
			holder.playSound(holder.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 0.7f, 2.0f);
		}
		else if(ammo == 0){
			message = "§a§l" + displayName + " §c§lEMPTY";
			holder.playSound(holder.getLocation(), Sound.BLOCK_LANTERN_BREAK, 1.0f, 2.0f);
		}
		else if(!canReloadAgain()){
			message = "§e§lReloading...";
		}
		else if(!canFireAgain()){
			message = "§a§l" + displayName + " §b§l(" + ammo + "/" + maxAmmo + ")";
		}
		
		holder.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
	
	public Vector getDirection(Location loc)
	{
		Vector vector = new Vector();
		
		double rotX = loc.getYaw();
		double rotY = loc.getPitch();
		
		vector.setY(-Math.sin(Math.toRadians(rotY)));
		
		double h = Math.cos(Math.toRadians(rotY));
		
		vector.setX(-h * Math.sin(Math.toRadians(rotX)));
		vector.setZ(h * Math.cos(Math.toRadians(rotX)));
		
		return vector;
	}
	
	public void adjustDirectionForInnacuracy(Location location){
		location.setPitch((float)(location.getPitch() + (Math.random() * innacuracy * (Math.random() > 0.5 ? 1 : -1))));
		location.setYaw((float)(location.getYaw() + (Math.random() * innacuracy * (Math.random() > 0.5 ? 1 : -1))));
	}
	
	public void reload(){
		String message = "";
		if(canReloadAgain() && ammo != maxAmmo){
			timeStartedReloading = System.currentTimeMillis();
			ammo = maxAmmo;
			message = "§e§lReloading...";
			holder.playSound(holder.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 2.0f);
		}
		holder.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
	
	public boolean canFireAgain(){
		//not firing too fast AND finished reloading
		return (System.currentTimeMillis() - timeLastFired >= (fireRate * 1000)) && canReloadAgain();
	}
	
	public boolean canReloadAgain(){
		return System.currentTimeMillis() - timeStartedReloading >= (reloadSpeed * 1000);
	}
	
	public int getGunID() {
		return gunID;
	}
	
	public void setGunID(int gunID) {
		this.gunID = gunID;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public double getDamage() {
		return damage;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}
	
	public double getBulletSpeed() {
		return bulletSpeed;
	}
	
	public void setBulletSpeed(double bulletSpeed) {
		this.bulletSpeed = bulletSpeed;
	}
	
	public double getInaccuracy() {
		return innacuracy;
	}
	
	public void setInaccuracy(double accuracy) {
		this.innacuracy = accuracy;
	}
	
	public double getFireRate() {
		return fireRate;
	}
	
	public void setFireRate(double fireRate) {
		this.fireRate = fireRate;
	}
	
	public double getReloadSpeed() {
		return reloadSpeed;
	}
	
	public void setReloadSpeed(double reloadSpeed) {
		this.reloadSpeed = reloadSpeed;
	}
	
	public Player getHolder() {
		return holder;
	}
	
	public void setHolder(Player holder) {
		this.holder = holder;
	}
	
	public Material getItemMaterial() {
		return itemMaterial;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	public void setItemMaterial(Material itemMaterial) {
		this.itemMaterial = itemMaterial;
	}
	
	public int getMaxAmmo() {
		return maxAmmo;
	}
	
	public int getAmmo() {
		return ammo;
	}
	
	public void setAmmo(int ammo) {
		this.ammo = ammo;
	}
	
	public int getShotsFired() {
		return shotsFired;
	}
	
	public Gun getCopy(Player playerToHold, ItemStack item){
		return new Gun(plugin, gunID, displayName, damage, bulletSpeed, innacuracy, fireRate, reloadSpeed, maxAmmo, shotsFired, playerToHold, item, itemMaterial);
	}
}
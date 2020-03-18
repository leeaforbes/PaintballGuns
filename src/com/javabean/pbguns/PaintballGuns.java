package com.javabean.pbguns;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.md_5.bungee.api.ChatColor;

public class PaintballGuns extends JavaPlugin{
	
	//gunID, Gun model to copy
	public static HashMap<Integer, Gun> models = new HashMap<Integer, Gun>();
	
	//gun names(lowercase) to IDs
	public static HashMap<String, Integer> nameToGunID = new HashMap<String, Integer>();
	
	public static double nextGunUUID = 0;
	public static HashMap<Double, Gun> guns = new HashMap<Double, Gun>();
	
	//TODO javadoc everywhere
	
	// Fired when plugin is first enabled
	@Override
	public void onEnable(){
		//read gun data from XML file
		parseXMLGunData();
		
		//creates commands
		getCommand("guns").setExecutor(new TheGunsCommand());
//		getCommand("guns").setTabCompleter(new GunsCommandTabCompleter());
		
		//event listener
		getServer().getPluginManager().registerEvents(new PaintballGunsListener(this), this);
		
		//plugin enabled successfully
		getLogger().info("-----------------------");
		getLogger().info(getClass().getSimpleName() + " enabled!");
		getLogger().info("-----------------------");
	}
	
	// Fired when plugin is disabled
	@Override
	public void onDisable(){
		//plugin disabled successfully
		getLogger().info("------------------------");
		getLogger().info(getClass().getSimpleName() + " disabled!");
		getLogger().info("------------------------");
	}
	
	private void parseXMLGunData(){
		//set up file location
		String gunsFileName = "guns.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\PaintballGuns directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + gunsFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + gunsFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(arenaInfoFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		//get the list of gun models
		NodeList gunList = doc.getElementsByTagName("gun");
		for (int gunIndex = 0; gunIndex < gunList.getLength(); gunIndex++) {
			Node gunNode = gunList.item(gunIndex);
			if (gunNode.getNodeType() == Node.ELEMENT_NODE) {
				Element gunElement = (Element)gunNode;
				int id = Integer.parseInt(gunElement.getAttribute("gunID"));
				String name = gunElement.getAttribute("name");
				Gun gun = new Gun(
						this,
						id,
						name,
						Double.parseDouble(gunElement.getAttribute("damage")),
						Double.parseDouble(gunElement.getAttribute("speed")),
						Double.parseDouble(gunElement.getAttribute("inaccuracy")),
						Double.parseDouble(gunElement.getAttribute("firerate")),
						Double.parseDouble(gunElement.getAttribute("reload")),
						Integer.parseInt(gunElement.getAttribute("maxAmmo")),
						Integer.parseInt(gunElement.getAttribute("shotsFired")),
						null, //no player holding model
						null, //no item stack exists yet
						Material.getMaterial(gunElement.getAttribute("material")));
				nameToGunID.put(name.toLowerCase(), id);
				models.put(id, gun);
			}
		}
	}
	
	public static void giveGun(Player player, int gunID){
		Gun gunModel = PaintballGuns.models.get(gunID);
		ItemStack item = new ItemStack(gunModel.getItemMaterial(), 1);
		Gun gun = gunModel.getCopy(player, item);
		
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setUnbreakable(true);
		itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		itemMeta.setDisplayName(ChatColor.GREEN + gunModel.getDisplayName());
		LinkedList<String> lore = new LinkedList<String>();
		lore.add(ChatColor.DARK_RED + "Damage: " + ChatColor.WHITE + gun.getDamage() + " HP");
		lore.add(ChatColor.AQUA + "Ammo: " + ChatColor.WHITE + gun.getMaxAmmo());
		lore.add(ChatColor.DARK_GREEN + "Reload: " + ChatColor.WHITE + gun.getReloadSpeed() + " seconds");
		int rpm = (int)((1 / gun.getFireRate()) * 60);
		lore.add(ChatColor.GOLD + "Fire rate: " + ChatColor.WHITE + rpm + " RPM");
		
		//pistols, shotguns
		String speed = ChatColor.RED + "slow";
		//ARs, SMGs
		if(gun.getBulletSpeed() > 2){
			speed = ChatColor.YELLOW + "medium";
		}
		//snipers
		if(gun.getBulletSpeed() > 3){
			speed = ChatColor.GREEN + "fast";
		}
		
		lore.add(ChatColor.DARK_BLUE + "Bullet speed: " + speed);
		
		//shotguns
		String accuracy = ChatColor.RED + "bad";
		//pistols
		if(gun.getInaccuracy() <= 3.0){
			accuracy = ChatColor.YELLOW + "okay";
		}
		//ARs, SMGs
		if(gun.getInaccuracy() <= 2.0){
			accuracy = ChatColor.GREEN + "good";
		}
		//snipers
		if(gun.getInaccuracy() <= 1.0){
			accuracy = ChatColor.LIGHT_PURPLE + "epic";
		}
		
		lore.add(ChatColor.DARK_PURPLE + "Accuracy: " + accuracy);
		itemMeta.setLore(lore);
		
		AttributeModifier attributeModIsAGun = new AttributeModifier("isAGun", 1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
		AttributeModifier attributeModUUID = new AttributeModifier("UUID", nextGunUUID, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
		itemMeta.addAttributeModifier(Attribute.GENERIC_LUCK, attributeModIsAGun);
		itemMeta.addAttributeModifier(Attribute.GENERIC_LUCK, attributeModUUID);
		
		item.setItemMeta(itemMeta);
		player.getInventory().addItem(gun.getItemStack());
		guns.put(nextGunUUID, gun);
		nextGunUUID++;
	}
}
package com.example;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class TestCommandExecutor implements TabExecutor, Listener {
	
	private final ExamplePlugin plugin;
	
	public TestCommandExecutor(ExamplePlugin plugin) {
		this.plugin = plugin;
		plugin.getCommand("testinventoryholder").setExecutor(this);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (args.length == 0) {
				Inventory testInventory = new TestInventoryHolder(player.getServer()).getInventory();
				testInventory.addItem(new ItemStack(Material.COBBLESTONE), new ItemStack(Material.DIAMOND), new ItemStack(Material.OAK_LOG));
				player.openInventory(testInventory);
			} else {
				String firstArg = args[0];
				Inventory testInventory = new TestInventoryHolder(player.getServer(), InventoryType.valueOf(firstArg.toUpperCase())).getInventory();
				testInventory.addItem(new ItemStack(Material.COBBLESTONE), new ItemStack(Material.DIAMOND), new ItemStack(Material.OAK_LOG));
				player.openInventory(testInventory);
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return Stream.of(InventoryType.values()).map(InventoryType::name).collect(Collectors.toList());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory topInventory = event.getView().getTopInventory();
		if (topInventory.getHolder() instanceof TestInventoryHolder) {
			plugin.getLogger().info("DEBUG clicked TestInventoryHolder - plugin is " + (plugin.isEnabled() ? "enabled" : "disabled") + ".");
		} else {
			//I reported this as a bug on spigotmc - md_5 doesn't acknowledge it as a bug, but it's a behavioural change compared to spigot 1.12.2 and earlier.
			//Paper keeps the old behaviour when the provided InventoryHolder is not null, so this branch is never taken by Paper :) Thanks Paper!
			plugin.getLogger().info("DEBUG clicked an inventory not held by TestInventoryHolder.");
			plugin.getLogger().info("DEBUG topinventory holder = " + topInventory.getHolder());
		}
	}
	
	private static class TestInventoryHolder implements InventoryHolder {
		
		private final Inventory inventory;
		
		public TestInventoryHolder(Server server) {
			this.inventory = server.createInventory(this, 3 * 9, "Test Inventory");
		}
		
		public TestInventoryHolder(Server server, InventoryType type) {
			this.inventory = server.createInventory(this, type);
		}
		
		public Inventory getInventory() {
			return inventory;
		}
		
	}

}

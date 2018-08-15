# GuiLib

[![Build Status](https://travis-ci.org/Jannyboy11/GuiLib.svg?branch=master)](https://travis-ci.org/Jannyboy11/GuiLib)

Easily create inventory GUIs! Have a look at the [JavaDocs](https://jitpack.io/com/github/Jannyboy11/GuiLib/v1.5.1/javadoc)!

### Compiling

Prerequisites: Apache Maven 3.5+, JDK10+.
Then run `mvn javadoc:jar install`.

### Pre-built jars

Available on [GitHub Releases](https://github.com/Jannyboy11/GuiLib/releases).

### Example Usage

```
package com.example;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.api.menu.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExamplePlugin extends JavaPlugin {

    private MenuHolder<ExamplePlugin> menu1, menu2;

    @Override
    public void onEnable() {
        //basic usage
        menu1 = new MenuHolder<>(this, 9, "Example Gui");
        menu2 = new MenuHolder<>(this, InventoryType.HOPPER, "Example Gui");

        menu1.setButton(0, new RedirectItemButton(new ItemStack(Material.PURPLE_GLAZED_TERRACOTTA), menu2::getInventory));
        menu1.setButton(8, new CloseButton());
        String permission = "foo.bar";
        menu1.setButton(4, new PermissionButton<>(
                permission,
                new ItemButton<>(new ItemStack(Material.GREEN_GLAZED_TERRACOTTA)) {
                    @Override
                    public void onClick(MenuHolder holder, InventoryClickEvent event) {
                        event.getWhoClicked().sendMessage("You have permission " + permission + ".");
                    }
                },
                humanEntity -> humanEntity.sendMessage("You don't have permission " + permission + ".")));

        ItemStack onStack = new ItemBuilder(Material.STRUCTURE_VOID).name("Enabled").build();
        ItemStack offStack = new ItemBuilder(Material.BARRIER).name("Disabled").build();
        menu2.setButton(0, new ToggleButton(new ItemStack(Material.BARRIER)) {
            @Override
            public void afterToggle(MenuHolder holder, InventoryClickEvent event) {
                event.getWhoClicked().sendMessage("Is the button enabled? " + (isEnabled() ? "yes" : "no"));
            }

            @Override
            public ItemStack updateIcon(MenuHolder menuHolder, InventoryClickEvent event) {
                return isEnabled() ? onStack : offStack;
            }
        });
        menu2.setButton(2, new BackButton(menu1::getInventory));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You can only use this command as a player.");
            return true;
        }

        Player player = (Player) sender;

        switch(command.getName().toLowerCase()) {
            case "gui":
                player.openInventory(menu1.getInventory());
                break;
            case "pages":
                PageMenu<ExamplePlugin> pageMenu = PageMenu.create(this, Stream.generate(() -> menu1).limit(5).iterator());
                player.openInventory(pageMenu.getInventory());
                break;
            case "freediamonds":
                MenuHolder<ExamplePlugin> menu = new MenuHolder<>(this, 45);
                for (int slot = 0; slot < menu.getInventory().getSize(); slot++) {
                    menu.setButton(slot, new ClaimButton(new ItemStack(Material.DIAMOND, 64)));
                }
                player.openInventory(menu.getInventory());
                break;
            case "claimallitems":
                ArrayList<ItemStack> mutableRewardsList = Arrays.stream(Material.values())
                        .map(ItemStack::new)
                        .collect(Collectors.toCollection(ArrayList::new));
                ClaimItemsMenu claimItemsMenu = new ClaimItemsMenu(this, 45, mutableRewardsList);
                player.openInventory(claimItemsMenu.getInventory());
        }

        return true;
    }
}
```

```
package com.example;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.api.menu.ClaimButton;
import xyz.janboerman.guilib.api.menu.MenuHolder;
import xyz.janboerman.guilib.api.menu.PageMenu;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

//more advanced usage - implements a custom page menu
public class ClaimItemsMenu extends PageMenu<ExamplePlugin> {

    /** rewards list */
    private List<ItemStack> rewards;
    /** list indices */
    private int rewardStartIndex /*inclusive*/, rewardEndIndex /*exclusive*/;

    /**
     * Creates the ClaimItemsMenu
     * @param plugin the plugin
     * @param pageSize the size of the embedded page (9 - 45)
     * @param rewards a mutable list of reward items
     */
    public ClaimItemsMenu(ExamplePlugin plugin, int pageSize, List<ItemStack> rewards) {
        this(plugin, pageSize, rewards, 0, Math.min(rewards.size(), pageSize));
    }

    /**
     * Creates the ClaimItemsMenu
     * @param plugin the plugin
     * @param pageSize the size of the embedded page (9 - 45)
     * @param rewards a mutable list of reward items
     * @param rewardStartIndex the lowerbound of the sublist we are displaying (inclusive)
     * @param rewardEndIndex the upperbound of the sublist we are displaying (exclusive)
     */
    private ClaimItemsMenu(ExamplePlugin plugin, int pageSize, List<ItemStack> rewards, int rewardStartIndex, int rewardEndIndex) {
        super(plugin, new MenuHolder<>(plugin, pageSize), "Claim your items", null, null);
        this.rewards = rewards;
        this.rewardStartIndex = rewardStartIndex;
        this.rewardEndIndex = rewardEndIndex;
    }

    @Override
    public MenuHolder<ExamplePlugin> getPage() {
        //we know the GuiInventoryHolder of the page is always a MenuHolder since we always create it ourselves
        return (MenuHolder<ExamplePlugin>) super.getPage();
    }

    //shifts all buttons in the page after the buttons that was transferred
    //actually creates new buttons
    private void shiftButtons(int slotIndex) {
        var page = getPage();

        int listIndex = rewardStartIndex + slotIndex;
        rewards.remove(listIndex);

        while (slotIndex < page.getInventory().getSize()) {
            if (listIndex < rewards.size()) {
                ItemStack reward = rewards.get(listIndex);
                page.setButton(slotIndex, new ShiftingClaimButton(reward));
            } else {
                page.unsetButton(slotIndex);
            }

            slotIndex++;
            listIndex++;
        }

        resetButtons(); //removes the next-page button if there are no items after the current page
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        //setup rewards
        for (int slot = 0; slot < getPageSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
            getPage().setButton(slot, new ShiftingClaimButton(rewards.get(rewardStartIndex + slot)));
        }

        //required for the page to even work
        super.onOpen(event);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        getPage().clearButtons(); //help gc

        //required
        super.onClose(event);
    }

    @Override
    public Optional<Supplier<ClaimItemsMenu>> getNextPageMenu() {
        //there is a next page if the current range upper bound is smaller than the end of the list
        if (rewardEndIndex < rewards.size()) {
            return Optional.of(() -> new ClaimItemsMenu(getPlugin(), getPageSize(), rewards, rewardEndIndex, Math.min(rewards.size(), rewardEndIndex + getPageSize())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Supplier<ClaimItemsMenu>> getPreviousPageMenu() {
        //there is a previous page if we didn't start 0
        if (rewardStartIndex > 0) {
            return Optional.of(() -> new ClaimItemsMenu(getPlugin(), getPageSize(), rewards, Math.max(0, rewardStartIndex - getPageSize()), Math.min(rewardStartIndex, rewards.size())));
        } else {
            return Optional.empty();
        }
    }

    //a special claim button that shift itemsin the page after the itemstack is claimed
    public class ShiftingClaimButton extends ClaimButton<ClaimItemsMenu> {
        public ShiftingClaimButton(ItemStack reward) {
            super(reward, (menu, event, itemStack) -> ClaimItemsMenu.this.shiftButtons(event.getSlot()));
        }
    }
}
```

This example uses GuiLib as a runtime dependency, so `depend: ["GuiLib"]` is in the plugin.yml and the dependency scope
is set to `provided`.

### Dependency

[![](https://jitpack.io/v/Jannyboy11/GuiLib.svg)](https://jitpack.io/#Jannyboy11/GuiLib)

##### Maven

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
	
	<dependency>
	    <groupId>com.github.Jannyboy11</groupId>
	    <artifactId>GuiLib</artifactId>
	    <version>v1.5.1</version>
	    <scope>provided</scope>
	</dependency>	

##### Gradle

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
	dependencies {
    	compileOnly 'com.github.Jannyboy11:GuiLib:v1.5.1'
    }

##### Sbt

    resolvers += "jitpack" at "https://jitpack.io"
    libraryDependencies += "com.github.Jannyboy11" % "GuiLib" % "v1.5.1" % "provided"	

### Licensing

The default license is LGPLv3 because I want this thing to be free software but still usable by closed source plugins.
If you however want to *include* this codebase in either source or binary form in your own open source project but not
adopt the (L)GPL license, please [create an issue](https://github.com/Jannyboy11/GuiLib/issues/new) that includes the
preferred license for GuiLib and includes a link to your project and I'll likely give you permission to use this under
the conditions of the alternative license.
# GuiLib

[![Build Status](https://travis-ci.org/Jannyboy11/GuiLib.svg?branch=master)](https://travis-ci.org/Jannyboy11/GuiLib)

Easily create inventory GUIs

### Example Usage
```
public class ExamplePlugin extends JavaPlugin {

    private MenuHolder<ExamplePlugin> menu1, menu2;

    @Override
    public void onEnable() {
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

        menu2.setButton(0, new ToggleButton(new ItemStack(Material.BARRIER)) {
            @Override
            public void afterToggle(MenuHolder holder, InventoryClickEvent event) {
                event.getWhoClicked().sendMessage("Is the button enabled? " + (isEnabled() ? "yes" : "no");
            }

            @Override
            public ItemStack updateIcon(MenuHolder menuHolder, InventoryClickEvent event) {
                return new ItemStack(isEnabled() ? Material.STRUCTURE_VOID : Material.BARRIER);
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
        player.openInventory(menu1.getInventory());

        return true;
    }

}
```
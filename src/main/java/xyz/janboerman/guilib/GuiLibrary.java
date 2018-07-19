package xyz.janboerman.guilib;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * GuiLib's main class.
 * <p>
 * GuiLib can either be used as a runtime dependency or a compile time dependency.
 * When used as a runtime dependency just drop GuiLib's jar in the plugins folder and be sure to depend on GuiLib in your plugin.yml.
 * <pre><code>depend: ["GuiLib"]</code></pre>
 * <p>
 * When used as a compile time dependency, be sure to shade the classes into your jar and relocate them.
 * Then you need to register the {@link GuiListener} in your onEnable.
 *
 * @see xyz.janboerman.guilib.api.GuiInventoryHolder
 */
public class GuiLibrary extends JavaPlugin {

    /**
     * Registers the {@link GuiListener}. This method is only called when GuiLib is used as a runtime dependency.
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

}

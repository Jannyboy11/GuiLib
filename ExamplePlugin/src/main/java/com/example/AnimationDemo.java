package com.example;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.api.GuiInventoryHolder;
import xyz.janboerman.guilib.api.animate.Animation;
import xyz.janboerman.guilib.api.animate.AnimationRunner;
import xyz.janboerman.guilib.api.animate.Frame;
import xyz.janboerman.guilib.api.animate.Schedule;
import xyz.janboerman.guilib.api.util.IntGenerator;
import xyz.janboerman.guilib.api.util.Option;

public class AnimationDemo extends GuiInventoryHolder<ExamplePlugin> {

    private static final int SIZE = 54;
    private static final ItemStack PUFFERFISH = new ItemStack(Material.PUFFERFISH);

    private final AnimationRunner<ItemStack> animation;

    public AnimationDemo(ExamplePlugin plugin) {
        super(plugin, SIZE, "Animation Demo");

        Animation animation = Animation.infinite(new CustomFrame(0), CustomFrame::next);
        this.animation = new AnimationRunner<>(plugin, animation, getInventory()::setItem);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        animation.play(Schedule.now().append(Schedule.fixedRate(5L)));
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        animation.stop();
    }


    private static final class CustomFrame extends Frame<Boolean, ItemStack> {

        private final int active;

        private CustomFrame(int active) {
            super(index -> index == active,
                    b -> Option.some(b ? PUFFERFISH : null),
                    IntGenerator.of(normalize(active - 1), active, normalize(active + 1))
            );

            this.active = active;
        }

        public CustomFrame next() {
            return new CustomFrame((active + 1) % SIZE);
        }

        private static int normalize(int index) {
            if (index < 0) index += SIZE;
            else if (index >= SIZE) index -= SIZE;
            return index;
        }
    }

}

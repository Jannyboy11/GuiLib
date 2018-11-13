package xyz.janboerman.guilib.api.menu;

import org.bukkit.command.Command;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * A button that executes a command when clicked.
 * @param <MH> the menu holder type
 */
public class CommandButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    private Command command;
    private String[] arguments;
    private CommandResultHandler<MH> resultHandler;

    /**
     * Protected constructor for command buttons that wish to use non-constant commands and arguments.
     * Subclasses that use this super constructor must override {@link #getArguments()} or {@link #getCommand} or their overloads.
     * @param icon the icon
     */
    protected CommandButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Creates the CommandButton.
     * @param icon the icon of the button
     * @param command the command to be executed
     * @param arguments the arguments used to execute the command
     */
    public CommandButton(ItemStack icon, Command command, String... arguments) {
        super(icon);
        setCommand(command);
        setArguments(arguments);
    }

    /**
     * Creates the CommandButton.
     * @param icon the icon of the button
     * @param command the command to be executed
     * @param arguments the arguments used to execute the command
     * @param resultHandler the handler that is executed after the command has run
     */
    public CommandButton(ItemStack icon, Command command, CommandResultHandler<MH> resultHandler, String... arguments) {
        this(icon, command, arguments);
        setResultHandler(resultHandler);
    }

    /**
     * Executes the command obtained by {@link #getCommand()} using the arguments provided by {@link #getArguments()}.
     * If a {@link CommandResultHandler} is present, then its {@link CommandResultHandler#afterCommand(HumanEntity, Command, String[], boolean, MenuHolder, InventoryClickEvent)} executed too.
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        Command command = getCommand(menuHolder, event);
        String[] arguments = getArguments(menuHolder, event);

        HumanEntity player = event.getWhoClicked();
        boolean success = command.execute(player, command.getLabel(), arguments);

        getResultHandler().ifPresent(resultHandler -> resultHandler.afterCommand(player, command, arguments, success, menuHolder, event));
    }

    /**
     * Set the command.
     * @param command the command
     */
    public void setCommand(Command command) {
        this.command = Objects.requireNonNull(command, "Command cannot be null");
    }

    /**
     * Set the arguments.
     * @param arguments the arguments
     */
    public void setArguments(String... arguments) {
        this.arguments = Objects.requireNonNull(arguments, "Arguments cannot be null");
    }

    /**
     * Set the result handler.
     * @param resultHandler the result handler
     */
    public void setResultHandler(CommandResultHandler<MH> resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * Computes the command to be used from the MenuHolder and InventoryClickEvent.
     * This method is called by {@link #onClick(MenuHolder, InventoryClickEvent)}.
     * The default implementation delegates to {@link #getCommand()}.
     *
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the command to be executed
     */
    protected Command getCommand(MH menuHolder, InventoryClickEvent event) {
        return getCommand();
    }

    /**
     * Computes the arguments to be used from the MenuHolder and InventoryClickEvent.
     * This method is called by {@link #onClick(MenuHolder, InventoryClickEvent)}.
     * The default implementation delegates to {@link #getArguments()}.
     *
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the arguments with which to execute command
     */
    protected String[] getArguments(MH menuHolder, InventoryClickEvent event) {
        return getArguments();
    }

    /**
     * Get the command.
     * @return the command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Get the arguments.
     * @return the arguments
     */
    public String[] getArguments() {
        return arguments;
    }

    /**
     * Get the result handler.
     * @return an Optional containing the {@link CommandResultHandler} if one is present, otherwise the empty Optional.
     */
    public Optional<? extends CommandResultHandler<MH>> getResultHandler() {
        return Optional.ofNullable(resultHandler);
    }

    /**
     * A callback that is executed after a command is run from the {@link #onClick(MenuHolder, InventoryClickEvent)} method.
     * @param <MH> the menu holder type
     */
    @FunctionalInterface
    public static interface CommandResultHandler<MH extends MenuHolder<?>> {

        /**
         * The callback method.
         * @param player the player that executed the command
         * @param command the command that was executed
         * @param arguments the arguments the command was executed with
         * @param wasExecutedSuccessFully whether the command was executed successfully
         * @param menuHolder the menu holder
         * @param event the event that caused the command to execute
         */
        public void afterCommand(HumanEntity player, Command command, String[] arguments, boolean wasExecutedSuccessFully, MH menuHolder, InventoryClickEvent event);

    }
}

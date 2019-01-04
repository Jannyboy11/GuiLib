module xyz.janboerman.guilib.api {

    requires org.bukkit;
    requires com.google.common;

    exports xyz.janboerman.guilib.api;
    exports xyz.janboerman.guilib.api.menu;

    opens xyz.janboerman.guilib.api;
}
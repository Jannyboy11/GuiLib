module xyz.janboerman.guilib.api {

    requires org.bukkit;
    requires com.google.common;

    exports xyz.janboerman.guilib.api;
    exports xyz.janboerman.guilib.api.menu;
    exports xyz.janboerman.guilib.api.mask;
    exports xyz.janboerman.guilib.api.mask.patterns;
    exports xyz.janboerman.guilib.api.animate;
    exports xyz.janboerman.guilib.api.util;

    opens xyz.janboerman.guilib.api;
}
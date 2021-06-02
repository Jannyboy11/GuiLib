module xyz.janboerman.guilib {

    requires org.bukkit;
    requires transitive xyz.janboerman.guilib.api;

    exports xyz.janboerman.guilib;
    opens xyz.janboerman.guilib to org.bukkit;
}
package xyz.janboerman.guilib.util;

public class FoliaSupport {

    private static final boolean FOLIA;
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            folia = true;
        } catch (ClassNotFoundException notFolia) {
            folia = false;
        }
        FOLIA = folia;
    }

    private FoliaSupport() {}

    public static boolean isFolia() {
        return FOLIA;
    }

}

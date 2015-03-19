package ayshi.ivpsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for user interface in IVP Suite app
 */
public class MenuContent {
    public static List<MenuItem> ITEMS = new ArrayList<MenuItem>();

    /**
     * A map of items, by ID.
     */
    public static Map<String, MenuItem> ITEM_MAP = new HashMap<String, MenuItem>();

    static {
        // Add 3 sample items.
        addItem(new MenuItem("new_source", "New Source"));
        addItem(new MenuItem("threshold", "Threshold"));
        addItem(new MenuItem("gamma_correction", "Gamma Correction"));
        addItem(new MenuItem("histogram_transform", "Histogram Transform"));
    }

    private static void addItem(MenuItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class MenuItem {
        public String id;
        public String content;

        public MenuItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

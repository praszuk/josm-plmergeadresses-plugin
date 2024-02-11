package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class MergeAddressesPlugin extends Plugin {
    public final static String name = "plmergeaddresses";
    public MergeAddressesPlugin(PluginInformation info){
        super(info);
        MainMenu.add(MainApplication.getMenu().selectionMenu, new MergeAddressesAction());
    }
}

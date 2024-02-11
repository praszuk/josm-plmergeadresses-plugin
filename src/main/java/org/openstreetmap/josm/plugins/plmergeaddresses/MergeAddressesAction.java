package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static org.openstreetmap.josm.tools.I18n.tr;

public class MergeAddressesAction extends JosmAction {

    public MergeAddressesAction(){
        super(
            tr("Merge addresses"),
            (ImageProvider) null,
            tr("Merge addresses with keeping part of history in old_addr tags"),
            Shortcut.registerShortcut(
                    tr("Merge addresses"),
                    String.format("%s:merge_addresses", MergeAddressesPlugin.name),
                    KeyEvent.VK_G,
                    Shortcut.CTRL_SHIFT
            ),
            true,
            String.format("%s:merge_addresses", MergeAddressesPlugin.name),
            false
        );
    }
    Command performMerge(DataSet dataSet) {
        OsmPrimitive[] selected = dataSet.getSelectedNodesAndWays().toArray(OsmPrimitive[]::new);
        if (selected.length != 2){
            return null;
        }
        return new MergeAddressesCommand(dataSet, selected[0], selected[1]);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DataSet dataSet = getLayerManager().getEditDataSet();
        Command mergeCommand = performMerge(dataSet);
        if (mergeCommand != null && mergeCommand.executeCommand()){
            UndoRedoHandler.getInstance().add(mergeCommand, false);
        }
    }
}

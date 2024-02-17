package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryCommand;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;
import org.openstreetmap.josm.tools.UserCancelException;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.openstreetmap.josm.tools.I18n.tr;

public class MergeAddressesCommand extends Command {
    private final OsmPrimitive newAddress;
    private final OsmPrimitive currentAddress;
    private final TagMap newAddressTags;
    private final TagMap currentAddressTags;

    private ReplaceGeometryCommand replaceCommand;
    private ReplaceGeometryCommand utilsPluginFallbackCommand;
    static final String COMMAND_DESCRIPTION = MergeAddressesPlugin.name + ": " + tr("Auto merge addresses");
    static final String COMMAND_DESCRIPTION_UTILS_PLUGIN_FALLBACK = MergeAddressesPlugin.name + ": "  + tr(
            "Auto merge addresses (with UtilsPlugin2)"
    );
    static final MergeAddressesCase[] MERGE_ADDRESSES_CASES = {
            new PlaceToSamePlaceNewHouseNumber(),
            new PlaceToStreetNewHouseNumber(),
            new PlaceToStreetSameHouseNumber(),
            new StreetToNewStreetNewHouseNumber(),
            new StreetToNewStreetSameHouseNumber(),
            new StreetToSameStreetNewHouseNumber(),
    };

    protected MergeAddressesCommand(DataSet data, OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        super(data);
        this.newAddress = newAddress;
        this.currentAddress = currentAddress;
        this.newAddressTags = new TagMap(this.newAddress.getKeys());
        this.currentAddressTags = new TagMap(this.currentAddress.getKeys());
        this.replaceCommand = null;
        this.utilsPluginFallbackCommand = null;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        modified.add(newAddress);
        modified.add(currentAddress);
    }

    @Override
    public String getDescriptionText() {
        return utilsPluginFallbackCommand != null ? COMMAND_DESCRIPTION_UTILS_PLUGIN_FALLBACK:COMMAND_DESCRIPTION;
    }

    @Override
    public void undoCommand() {
        if (utilsPluginFallbackCommand != null){
            utilsPluginFallbackCommand.undoCommand();
        }
        if (replaceCommand != null){
            replaceCommand.undoCommand();
        }

        newAddress.setKeys(newAddressTags);
        currentAddress.setKeys(currentAddressTags);
    }

    boolean mergeTagsAndResolveConflicts(OsmPrimitive newAddress, OsmPrimitive currentAddress){
        Collection<OsmPrimitive> primitives = Arrays.asList(currentAddress, newAddress);
        TagCollection tagsOfPrimitives = TagCollection.unionOfAllPrimitives(primitives);
        try {
            List<Command> resolvedTagConflictCommands = CombinePrimitiveResolverDialog.launchIfNecessary(tagsOfPrimitives, primitives, Collections.singleton(currentAddress));
            resolvedTagConflictCommands.forEach(Command::executeCommand);
        } catch (UserCancelException e) {
            return false;
        }
        return true;
    }

    private MergeAddressesCase getMergeAddressCase(){
        return Arrays.stream(MERGE_ADDRESSES_CASES)
                .filter(mergeAddressesCase -> mergeAddressesCase.isMatch(newAddress, currentAddress))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean executeCommand() {
        MergeAddressesCase mergeAddressesCase = getMergeAddressCase();
        if (mergeAddressesCase != null) {
            mergeAddressesCase.proceed(newAddress, currentAddress);
            new SourceAddrReplace().isMatchThenProceed(newAddress, currentAddress);

            if (!mergeTagsAndResolveConflicts(newAddress, currentAddress)) {
                undoCommand();
                return false;
            }

            // below UtilsPlugin only replace geometry, because tags are already resolved above
            try {
                replaceCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(newAddress, currentAddress);
            } catch (ReplaceGeometryException exc){
                new Notification(exc.getMessage()).setIcon(UIManager.getIcon("OptionPane.warningIcon")).show();
                undoCommand();
                return false;
            }
            if (!replaceCommand.executeCommand()) {
                undoCommand();
                return false;
            }

        } else { // No change by plugin login detected
            return fallbackToUtilsPluginResolver(newAddress, currentAddress);
        }
        return true;
    }

    boolean fallbackToUtilsPluginResolver(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        try {
            utilsPluginFallbackCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(newAddress, currentAddress);
        } catch (ReplaceGeometryException exc){
            new Notification(exc.getMessage()).setIcon(UIManager.getIcon("OptionPane.warningIcon")).show();
            return false;
        }
        return utilsPluginFallbackCommand != null && utilsPluginFallbackCommand.executeCommand();
    }
}
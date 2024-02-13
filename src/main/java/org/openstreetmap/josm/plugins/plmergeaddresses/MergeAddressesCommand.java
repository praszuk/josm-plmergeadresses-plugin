package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryCommand;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;
import org.openstreetmap.josm.tools.UserCancelException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.openstreetmap.josm.plugins.plmergeaddresses.Tags.SOURCE_ADDR;
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
    static final MergeAddressCase[] mergeAddressCases = {
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

    boolean mergeTagsAndResolveConflicts(OsmPrimitive currentAddress, OsmPrimitive newAddress){
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

    private MergeAddressCase getMergeAddressCase(){
        return Arrays.stream(mergeAddressCases)
                .filter(mergeAddressCase -> mergeAddressCase.isMatch(newAddress, currentAddress))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean executeCommand() {
        MergeAddressCase mergeAddressCase = getMergeAddressCase();
        if (mergeAddressCase != null) {
            mergeAddressCase.proceed(newAddress, currentAddress);
            updateSourceAddr(newAddress, currentAddress);
            if (!mergeTagsAndResolveConflicts(currentAddress, newAddress)) {
                undoCommand();
                return false;
            }

            // below UtilsPlugin only replace geometry, because tags are already resolved above
            replaceCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(newAddress, currentAddress);
            if (!replaceCommand.executeCommand()) {
                undoCommand();
                return false;
            }
        } else { // No change by plugin login detected
            return fallbackToUtilsPluginResolver(currentAddress, newAddress);
        }
        return true;
    }

    boolean fallbackToUtilsPluginResolver(OsmPrimitive currentAddress, OsmPrimitive newAddress) {
        utilsPluginFallbackCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(newAddress, currentAddress);
        return utilsPluginFallbackCommand != null && utilsPluginFallbackCommand.executeCommand();
    }

    void updateSourceAddr(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        if (newAddress.hasTag(SOURCE_ADDR) && newAddress.get(SOURCE_ADDR).equals(currentAddress.get(SOURCE_ADDR)))
            return;

        currentAddress.put(SOURCE_ADDR, newAddress.get(SOURCE_ADDR));
    }

}
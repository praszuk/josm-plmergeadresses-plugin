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

public class MergeAddressesCommand extends Command {
    private final OsmPrimitive newAddress;
    private final OsmPrimitive currentAddress;
    private final TagMap newAddressTags;
    private final TagMap currentAddressTags;

    private ReplaceGeometryCommand replaceCommand;
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
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        modified.add(newAddress);
        modified.add(currentAddress);
    }

    @Override
    public String getDescriptionText() {
        return "Auto merge addresses";
    }

    @Override
    public void undoCommand() {
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
            if (!fallbackToUtilsPluginResolver()) {
                undoCommand();
                return false;
            }
        }
        return true;
    }

    boolean fallbackToUtilsPluginResolver() {
        replaceCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(newAddress, currentAddress);
        return replaceCommand.executeCommand();
    }

    void updateSourceAddr(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        if (newAddress.get("source:addr").equals(currentAddress.get("source:addr")))
            return;

        currentAddress.put("source:addr", newAddress.get("source:addr"));
    }

}
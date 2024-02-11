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
    @Override
    public boolean executeCommand() {
        if (isPlaceToStreet(newAddress, currentAddress)){
            currentAddress.put("old_addr:place", currentAddress.get("addr:place"));
            currentAddress.remove("addr:place");
            updateAddrHousenumber(newAddress, currentAddress);
            updateSourceAddr(newAddress, currentAddress);
        }
        else if (isStreetToStreet(newAddress, currentAddress)){
            currentAddress.put("old_addr:street", currentAddress.get("addr:street"));
            currentAddress.remove("addr:street");
            updateAddrHousenumber(newAddress, currentAddress);
            updateSourceAddr(newAddress, currentAddress);
        }
        else{
            return false;
        }

        if (!mergeTagsAndResolveConflicts(currentAddress, newAddress)) {
            undoCommand();
            return false;
        }
        replaceCommand = ReplaceGeometryUtils.buildReplaceWithNewCommand(newAddress, currentAddress);
        return replaceCommand.executeCommand();
    }

    void updateAddrHousenumber(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        if (newAddress.get("addr:housenumber").equals(currentAddress.get("addr:housenumber")))
            return;

        currentAddress.put("old_addr:housenumber", currentAddress.get("addr:housenumber"));
        currentAddress.remove("addr:housenumber");
    }

    void updateSourceAddr(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        if (newAddress.get("source:addr").equals(currentAddress.get("source:addr")))
            return;

        currentAddress.put("source:addr", newAddress.get("source:addr"));
    }

    private boolean isPlaceToStreet(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return newAddress.hasTag("addr:street") && currentAddress.hasTag("addr:place") && !currentAddress.hasTag("addr:street");
    }

    private boolean isStreetToStreet(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return newAddress.hasTag("addr:street") && currentAddress.hasTag("addr:street") && !currentAddress.get("addr:street").equals(newAddress.get("addr:street"));
    }

}

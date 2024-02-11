package org.openstreetmap.josm.plugins.plmergeaddresses;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.openstreetmap.josm.plugins.plmergeaddresses.TestUtils.assertTagListEquals;
import static org.openstreetmap.josm.plugins.plmergeaddresses.TestUtils.toTagList;

public class MergeAddressesActionTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    private DataSet dataSet;
    private OsmPrimitive newAddress;
    private OsmPrimitive currentAddress;


    @Before
    public void init(){
        dataSet = new DataSet();
        newAddress = new Node(new LatLon(52.23, 21.01124));
        currentAddress = new Node(new LatLon(52.23, 21.01123));
        currentAddress.setOsmId(1, 1);

        dataSet.addPrimitive(newAddress);
        dataSet.addPrimitive(currentAddress);

        dataSet.setSelected(newAddress, currentAddress);
    }

    @Test
    public void testUpdateCanceledByUser() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive currentAddress, OsmPrimitive newAddress){
                return false;
            }
        };
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        ).forEach(newAddress::put);
        Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
                "addr:housenumber", "43A",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ).forEach(currentAddress::put);

        TagMap expectedTagMap = new TagMap(currentAddress.getKeys());

        UndoRedoHandler.getInstance().add(new MergeAddressesAction().performMerge(dataSet));

        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdateAddressWithPlaceToStreetIncorrectSelectionOrderDoNothing() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive currentAddress, OsmPrimitive newAddress){
                new ChangePropertyCommand(
                        newAddress.getDataSet(),
                        Collections.singletonList(currentAddress),
                        newAddress.getKeys()
                ).executeCommand();
                return true;
            }
        };
        ExpertToggleAction.getInstance().setExpert(true);
        Map<String, String> newAddressTags = Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        );
        Map<String, String> currentAddressTags = Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
                "addr:housenumber", "43A",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        );
        newAddressTags.forEach(newAddress::put);
        currentAddressTags.forEach(currentAddress::put);

        dataSet.clearSelection();
        dataSet.setSelected(currentAddress, newAddress); // Reversed order – should do nothing
        Command command = new MergeAddressesAction().performMerge(dataSet);
        command.executeCommand();

        assertNull(UndoRedoHandler.getInstance().getLastCommand());
        assertTagListEquals(toTagList(currentAddressTags), currentAddress.getKeys().getTags());
        assertTagListEquals(toTagList(newAddressTags), newAddress.getKeys().getTags());
    }

    @Test
    public void testUpdateAddressWithPlaceToStreet() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive currentAddress, OsmPrimitive newAddress){
                new ChangePropertyCommand(
                        newAddress.getDataSet(),
                        Collections.singletonList(currentAddress),
                        newAddress.getKeys()
                ).executeCommand();
                return true;
            }
        };
        ExpertToggleAction.getInstance().setExpert(true);
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        ).forEach(newAddress::put);
        Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
                "addr:housenumber", "43A",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ).forEach(currentAddress::put);

        Command command = new MergeAddressesAction().performMerge(dataSet);
        dataSet.setSelected(currentAddress, newAddress); // Reversed order – should do nothing
        command.executeCommand();

        assertNull(UndoRedoHandler.getInstance().getLastCommand());

        dataSet.setSelected(currentAddress, newAddress); // Reversed order – should do nothing
        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
            Map.of(
                    "addr:city:simc", "12345",
                    "addr:city", "Place",
                    "addr:street", "Street",
                    "addr:housenumber", "1",
                    "addr:postcode", "00-000",
                    "old_addr:place", "Place",
                    "old_addr:housenumber", "43A",
                    "source:addr", "gugik.gov.pl"
            )
        );
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdateAddressWithStreetToStreet() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive currentAddress, OsmPrimitive newAddress){
                new ChangePropertyCommand(
                        newAddress.getDataSet(),
                        Collections.singletonList(currentAddress),
                        newAddress.getKeys()
                ).executeCommand();
                return true;
            }
        };
        ExpertToggleAction.getInstance().setExpert(true);
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street2",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        ).forEach(newAddress::put);
        Map.of(
                "addr:city:simc", "12345",
                "addr:street", "Street1",
                "addr:housenumber", "43A",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ).forEach(currentAddress::put);
        new MergeAddressesAction().performMerge(dataSet).executeCommand();

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:city", "Place",
                        "addr:street", "Street2",
                        "addr:housenumber", "1",
                        "addr:postcode", "00-000",
                        "old_addr:street", "Street1",
                        "old_addr:housenumber", "43A",
                        "source:addr", "gugik.gov.pl"
                )
        );
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }
}

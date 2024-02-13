package org.openstreetmap.josm.plugins.plmergeaddresses;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openstreetmap.josm.plugins.plmergeaddresses.TestUtils.assertTagListEquals;

public class MergeAddressesActionTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main().projection().timeout(5000);

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
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(dataSet, "Test", null));
    }

    @Test
    public void testUpdateCanceledByUser() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive newAddress, OsmPrimitive currentAddress){
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

        new MergeAddressesAction().actionPerformed(null);

        assertNull(UndoRedoHandler.getInstance().getLastCommand());
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdatePlaceToSamePlaceNewHouseNumber() {
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags
        Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
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

        dataSet.setSelected(newAddress, currentAddress);
        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:place", "Place",
                        "addr:housenumber", "1",
                        "addr:postcode", "00-000",
                        "old_addr:housenumber", "43A",
                        "source:addr", "gugik.gov.pl"
                )
        );
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdatePlaceToStreetNewHouseNumber() {
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags
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

        dataSet.setSelected(newAddress, currentAddress);
        new MergeAddressesAction().actionPerformed(null);

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
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdatePlaceToStreetSameHouseNumber() {
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags
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
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ).forEach(currentAddress::put);

        dataSet.setSelected(newAddress, currentAddress);
        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:city", "Place",
                        "addr:street", "Street",
                        "addr:housenumber", "1",
                        "addr:postcode", "00-000",
                        "old_addr:place", "Place",
                        "source:addr", "gugik.gov.pl"
                )
        );
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdateStreetToNewStreetNewHouseNumber() {
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags
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

        new MergeAddressesAction().actionPerformed(null);

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
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdateStreetToNewStreetSameHouseNumber() {
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags
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
                "addr:city", "Place",
                "addr:street", "Street1",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ).forEach(currentAddress::put);

        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:city", "Place",
                        "addr:street", "Street2",
                        "addr:housenumber", "1",
                        "addr:postcode", "00-000",
                        "old_addr:street", "Street1",
                        "source:addr", "gugik.gov.pl"
                )
        );
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testUpdateStreetToSameStreetNewHouseNumber() {
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "5",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        ).forEach(newAddress::put);
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ).forEach(currentAddress::put);

        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:city", "Place",
                        "addr:street", "Street",
                        "addr:housenumber", "5",
                        "old_addr:housenumber", "1",
                        "addr:postcode", "00-000",
                        "source:addr", "gugik.gov.pl"
                )
        );
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }
    @Test
    public void testUpdateStreetToSameStreetSameHouseNumberNoChangeFallbackToUtilsPluginReplaceGeometry() {
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000"
        ).forEach(newAddress::put);
        Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000"
        ).forEach(currentAddress::put);

        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:city", "Place",
                        "addr:street", "Street",
                        "addr:housenumber", "1",
                        "addr:postcode", "00-000"
                )
        );
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION_UTILS_PLUGIN_FALLBACK);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }
    @Test
    public void testUpdatePlaceToSamePlaceSameHouseNumberNoChangeFallbackToUtilsPluginReplaceGeometry() {
        Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
                "addr:housenumber", "1",
                "addr:postcode", "00-000"
        ).forEach(newAddress::put);
        Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
                "addr:housenumber", "1",
                "addr:postcode", "00-000"
        ).forEach(currentAddress::put);

        dataSet.setSelected(newAddress, currentAddress);
        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(
                Map.of(
                        "addr:city:simc", "12345",
                        "addr:place", "Place",
                        "addr:housenumber", "1",
                        "addr:postcode", "00-000"
                )
        );
        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION_UTILS_PLUGIN_FALLBACK);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testNoChangeFallbackToUtilsPluginReplaceGeometry(){
        ExpertToggleAction.getInstance().setExpert(true);  // avoid asking about merging obvious tags

        Map.of("test2", "test2").forEach(newAddress::put);
        Map.of("test1", "test1").forEach(currentAddress::put);

        new MergeAddressesAction().actionPerformed(null);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(Map.of("test2", "test2", "test1", "test1"));

        assertEquals(UndoRedoHandler.getInstance().getLastCommand().getDescriptionText(), MergeAddressesCommand.COMMAND_DESCRIPTION_UTILS_PLUGIN_FALLBACK);
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }

    @Test
    public void testNoChangeFallbackToUtilsPluginReplaceGeometryCanceledByUser(){
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean fallbackToUtilsPluginResolver(OsmPrimitive newAddress, OsmPrimitive currentAddress){
                return false;
            }
        };

        Map.of("test2", "test2").forEach(newAddress::put);
        Map.of("test2", "test1").forEach(currentAddress::put);

        TagMap expectedTagMap = new TagMap();
        expectedTagMap.putAll(currentAddress.getKeys());

        new MergeAddressesAction().actionPerformed(null);

        assertNull(UndoRedoHandler.getInstance().getLastCommand());
        assertTagListEquals(expectedTagMap.getTags(), currentAddress.getKeys().getTags());
    }
}

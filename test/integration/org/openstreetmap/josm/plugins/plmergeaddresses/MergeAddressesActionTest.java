package org.openstreetmap.josm.plugins.plmergeaddresses;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MergeAddressesActionTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();


    @Test
    public void testUpdateAddressWithPlaceToStreet() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive dist, OsmPrimitive src){
                new ChangePropertyCommand(
                        src.getDataSet(),
                        Collections.singletonList(dist),
                        src.getKeys()
                ).executeCommand();
                return true;
            }
        };
        ExpertToggleAction.getInstance().setExpert(true);

        DataSet dataSet = new DataSet();
        OsmPrimitive src = new Node(new LatLon(52.23, 21.01124));
        OsmPrimitive dist = new Node(new LatLon(52.23, 21.01123));
        dist.setOsmId(1, 1);

        src.putAll(Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        ));
        dist.putAll(Map.of(
                "addr:city:simc", "12345",
                "addr:place", "Place",
                "addr:housenumber", "43A",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ));
        dataSet.addPrimitive(src);
        dataSet.addPrimitive(dist);

        dataSet.setSelected(src, dist);

        new MergeAddressesAction().performMerge(dataSet).executeCommand();

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
        assertTrue(expectedTagMap.getTags().containsAll(dist.getKeys().getTags()));
    }

    @Test
    public void testUpdateAddressWithStreetToStreet() {
        new MockUp<MergeAddressesCommand>(){
            @Mock
            boolean mergeTagsAndResolveConflicts(OsmPrimitive dist, OsmPrimitive src){
                new ChangePropertyCommand(
                        src.getDataSet(),
                        Collections.singletonList(dist),
                        src.getKeys()
                ).executeCommand();
                return true;
            }
        };
        ExpertToggleAction.getInstance().setExpert(true);

        DataSet dataSet = new DataSet();
        OsmPrimitive src = new Node(new LatLon(52.23, 21.01124));
        OsmPrimitive dist = new Node(new LatLon(52.23, 21.01123));
        dist.setOsmId(1, 1);

        src.putAll(Map.of(
                "addr:city:simc", "12345",
                "addr:city", "Place",
                "addr:street", "Street2",
                "addr:housenumber", "1",
                "addr:postcode", "00-000",
                "source:addr", "gugik.gov.pl"
        ));
        dist.putAll(Map.of(
                "addr:city:simc", "12345",
                "addr:street", "Street1",
                "addr:housenumber", "43A",
                "addr:postcode", "00-000",
                "source:addr", "gmina.e-mapa.net"
        ));
        dataSet.addPrimitive(src);
        dataSet.addPrimitive(dist);

        dataSet.setSelected(src, dist);

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
        assertTrue(expectedTagMap.getTags().containsAll(dist.getKeys().getTags()));
    }
}

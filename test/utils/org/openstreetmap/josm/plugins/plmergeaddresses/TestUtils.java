package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.data.osm.Tag;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TestUtils {

    static void assertTagListEquals(List<Tag> expected, List<Tag> actual){
        assertEquals(
                expected.stream().map(tag -> tag.getKey() + '=' + tag.getValue()).sorted().collect(Collectors.toList()),
                actual.stream().map(tag -> tag.getKey() + '=' + tag.getValue()).sorted().collect(Collectors.toList())
        );
    }
}

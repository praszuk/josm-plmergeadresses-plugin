package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.data.osm.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TestUtils {
    static List<Tag> toTagList(Map<String, String> tags){
        ArrayList<Tag> tagList = new ArrayList<>();
        tags.forEach((key, value) -> tagList.add(new Tag(key, value)));
        return tagList;
    }

    static void assertTagListEquals(List<Tag> expected, List<Tag> actual){
        assertEquals(
                expected.stream().map(tag -> tag.getKey() + '=' + tag.getValue()).sorted().collect(Collectors.toList()),
                actual.stream().map(tag -> tag.getKey() + '=' + tag.getValue()).sorted().collect(Collectors.toList())
        );
    }
}

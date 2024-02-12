package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

abstract public class MergeAddressCase {
    static final String ADDR_PLACE = "addr:place";
    static final String ADDR_STREET = "addr:street";
    static final String ADDR_HOUSENUMBER = "addr:housenumber";
    static final String OLD_ADDR_PLACE = "old_addr:place";
    static final String OLD_ADDR_STREET = "old_addr:street";
    static final String OLD_ADDR_HOUSENUMBER = "old_addr:housenumber";

    abstract boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress);
    abstract void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress);
}



class PlaceToStreetNewHouseNumber extends MergeAddressCase {

    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return currentAddress.hasTag(ADDR_PLACE) &&
                newAddress.hasTag(ADDR_STREET) &&
                !currentAddress.get(ADDR_HOUSENUMBER).equals(newAddress.get(ADDR_HOUSENUMBER)) && currentAddress.get(ADDR_HOUSENUMBER) != null &&
                !newAddress.hasTag(ADDR_PLACE); // Skip some neighborhoods/quarters strange tagging
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_PLACE, currentAddress.get(ADDR_PLACE));
        currentAddress.remove(ADDR_PLACE);
        currentAddress.put(OLD_ADDR_HOUSENUMBER, currentAddress.get(ADDR_HOUSENUMBER));
        currentAddress.remove(ADDR_HOUSENUMBER);
    }
}

class PlaceToStreetSameHouseNumber extends MergeAddressCase {
    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return currentAddress.hasTag(ADDR_PLACE) &&
                newAddress.hasTag(ADDR_STREET) &&
                currentAddress.get(ADDR_HOUSENUMBER).equals(newAddress.get(ADDR_HOUSENUMBER)) && newAddress.get(ADDR_HOUSENUMBER) != null &&
                !newAddress.hasTag(ADDR_PLACE); // Skip some neighborhoods/quarters strange tagging
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_PLACE, currentAddress.get(ADDR_PLACE));
        currentAddress.remove(ADDR_PLACE);
    }
}

class StreetToNewStreetNewHouseNumber extends MergeAddressCase {
    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return currentAddress.hasTag(ADDR_STREET) &&
                newAddress.hasTag(ADDR_STREET) &&
                !currentAddress.get(ADDR_STREET).equals(newAddress.get(ADDR_STREET)) && newAddress.get(ADDR_STREET) != null &&
                !currentAddress.get(ADDR_HOUSENUMBER).equals(newAddress.get(ADDR_HOUSENUMBER)) && newAddress.get(ADDR_HOUSENUMBER) != null;
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_STREET, currentAddress.get(ADDR_STREET));
        currentAddress.remove(ADDR_STREET);

        currentAddress.put(OLD_ADDR_HOUSENUMBER, currentAddress.get(ADDR_HOUSENUMBER));
        currentAddress.remove(ADDR_HOUSENUMBER);
    }
}

package org.openstreetmap.josm.plugins.plmergeaddresses;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

import static org.openstreetmap.josm.plugins.plmergeaddresses.Tags.*;

abstract public class MergeAddressCase {
    abstract boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress);
    abstract void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress);

    static boolean haveTagKey(OsmPrimitive first, OsmPrimitive second, String key){
        return first.hasTag(key) && second.hasTag(key);
    }
    static boolean equalsTagValue(OsmPrimitive first, OsmPrimitive second, String key){
        return haveTagKey(first, second, key) && first.get(key).equals(second.get(key));
    }
}



class PlaceToStreetNewHouseNumber extends MergeAddressCase {

    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return currentAddress.hasTag(ADDR_PLACE) &&
                newAddress.hasTag(ADDR_STREET) &&
                !equalsTagValue(currentAddress, newAddress, ADDR_HOUSENUMBER) &&
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
                equalsTagValue(currentAddress, newAddress, ADDR_HOUSENUMBER) &&
                !newAddress.hasTag(ADDR_PLACE); // Skip some neighborhoods/quarters strange tagging
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_PLACE, currentAddress.get(ADDR_PLACE));
        currentAddress.remove(ADDR_PLACE);
    }
}

class PlaceToSamePlaceNewHouseNumber extends MergeAddressCase {

    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return haveTagKey(currentAddress, newAddress, ADDR_PLACE) &&
                equalsTagValue(currentAddress, newAddress, ADDR_PLACE) &&
                !equalsTagValue(currentAddress, newAddress, ADDR_HOUSENUMBER);
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_HOUSENUMBER, currentAddress.get(ADDR_HOUSENUMBER));
        currentAddress.remove(ADDR_HOUSENUMBER);
    }
}

class StreetToNewStreetNewHouseNumber extends MergeAddressCase {
    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return haveTagKey(currentAddress, newAddress, ADDR_STREET) &&
                !equalsTagValue(currentAddress, newAddress, ADDR_STREET) &&
                !equalsTagValue(currentAddress, newAddress, ADDR_HOUSENUMBER);
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_STREET, currentAddress.get(ADDR_STREET));
        currentAddress.remove(ADDR_STREET);

        currentAddress.put(OLD_ADDR_HOUSENUMBER, currentAddress.get(ADDR_HOUSENUMBER));
        currentAddress.remove(ADDR_HOUSENUMBER);
    }
}

class StreetToNewStreetSameHouseNumber extends MergeAddressCase {
    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return haveTagKey(currentAddress, newAddress, ADDR_STREET) &&
                !equalsTagValue(currentAddress, newAddress, ADDR_STREET) &&
                equalsTagValue(currentAddress, newAddress, ADDR_HOUSENUMBER);
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_STREET, currentAddress.get(ADDR_STREET));
        currentAddress.remove(ADDR_STREET);
    }
}
class StreetToSameStreetNewHouseNumber extends MergeAddressCase {
    @Override
    boolean isMatch(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        return haveTagKey(currentAddress, newAddress, ADDR_STREET) &&
                equalsTagValue(currentAddress, newAddress, ADDR_STREET) &&
                !equalsTagValue(currentAddress, newAddress, ADDR_HOUSENUMBER);
    }

    @Override
    void proceed(OsmPrimitive newAddress, OsmPrimitive currentAddress) {
        currentAddress.put(OLD_ADDR_HOUSENUMBER, currentAddress.get(ADDR_HOUSENUMBER));
        currentAddress.remove(ADDR_HOUSENUMBER);
    }
}
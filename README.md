# PLMergeAddresses plugin

## Description
PLMergeAddresses is a [JOSM](https://josm.openstreetmap.de/) plugin which allows to easily merge 2 conflicting addresses to automatically resolve conflict with [keeping part of address history](https://wiki.openstreetmap.org/wiki/Key:old_addr:housenumber).


## Example
Existing address:
```
addr:city:simc=12345
addr:housenumber=45A
addr:place=Place
addr:postcode=00-000
source:addr=gmina.e-mapa.net
```

The new address:

```
addr:city:simc=12345
addr:city=Place
addr:housenumber=1
addr:postcode=00-000
addr:street=Street
source:addr=gugik.gov.pl
```

Would be merged to:

```
addr:city:simc=12345
addr:city=Place
addr:housenumber=1
addr:postcode=00-000
addr:street=Street
old_addr:place=Place
old_addr:housenumber=45A
source:addr=gugik.gov.pl
```

It's actually wrapper for [utilsplugin2](https://wiki.openstreetmap.org/wiki/JOSM/Plugins/utilsplugin2).


## How to use it
Check the binding in the JOSM settings `plmergeaddresses:merge_addresses`, you can replace default utilsplugin shortcut `CTRL+SHIFT+G` – it should work well.
Select 2 objects with addresses and press the shortcut.


## License
[GPLv3](LICENSE)

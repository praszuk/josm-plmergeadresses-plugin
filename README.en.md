# PLMergeAddresses plugin
[Read in Polish](README.md).

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

<pre>
addr:city:simc=12345
addr:city=Place
addr:housenumber=1
addr:postcode=00-000
addr:street=Street
<b>old_addr:place=Place</b>
<b>old_addr:housenumber=45A</b>
source:addr=gugik.gov.pl
</pre>

It's actually wrapper for [utilsplugin2](https://wiki.openstreetmap.org/wiki/JOSM/Plugins/utilsplugin2).


## How to use it
Check the binding in the JOSM settings `plmergeaddresses:merge_addresses`, you can replace default UtilsPlugin2 shortcut `CTRL+SHIFT+G` – it should work well.
Select 2 objects with addresses and press the shortcut.

## Supported cases
- `addr:place` to the same `addr:place` with new `addr:housenumber`
- `addr:place` to `addr:street` with new `addr:housenumber`
- `addr:place` to `addr:street` with same `addr:housenumber`
- `addr:street` to new `addr:street` with new `addr:housenumber`
- `addr:street` to new `addr:street` with same `addr:housenumber`
- `addr:street` to same `addr:street` with new `addr:housenumber`

In addition, it automatically replaces `source:addr`, without creating unnecessary conflict.

## License
[GPLv3](LICENSE)

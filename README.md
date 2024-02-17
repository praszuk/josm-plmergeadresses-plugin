# PLMergeAddresses plugin
[Read in English](README.en.md).

PLMergeAddresses to wtyczka do [JOSMa](https://josm.openstreetmap.de/), która pozwala na łatwe scalenie 2 adresów z częściowo automatycznym rozwiązywaniem konfliktu z [zachowaniem części historii adresu](https://wiki.openstreetmap.org/wiki/Pl:Key:old_addr:housenumber).


## Przykład
Istniejący adres:
```
addr:city:simc=12345
addr:housenumber=45A
addr:place=Place
addr:postcode=00-000
source:addr=gmina.e-mapa.net
```

Nowy adres:
```
addr:city:simc=12345
addr:city=Place
addr:housenumber=1
addr:postcode=00-000
addr:street=Street
source:addr=gugik.gov.pl
```

Zostanie scalony do:
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

Jest to w zasadzie nakładka na wtyczkę [utilsplugin2](https://wiki.openstreetmap.org/wiki/JOSM/Plugins/utilsplugin2).


## Jak tego używać
Sprawdź skrót w ustawieniach JOSMa pod nazwą `plmergeaddresses:merge_addresses`. Możesz zastąpić go domyślnym skrótem z UtilsPlugin2 `CTRL+SHIFT+G` – powinno to działać poprawnie.  
Zaznacz 2 obiekty z adresami i użyj skrótu klawiszowego.

**Uwaga: kolejność zaznaczania ma znaczenie!**<br>
Pierwszy obiekt będzie scalany do drugiego, czyli pierwszym obiektem powinien być "nowy adres", a drugim "stary adres".

## Obsługiwane przypadki
- `addr:place` do tego samego `addr:place` z nowym `addr:housenumber`
- `addr:place` do `addr:street` z nowym `addr:housenumber`
- `addr:place` do `addr:street` z tym samym `addr:housenumber`
- `addr:street` do nowego `addr:street` z nowym `addr:housenumber`
- `addr:street` do nowego `addr:street` z tym samym `addr:housenumber`
- `addr:street` do tego samego `addr:street` z nowym `addr:housenumber`

W dodatku automatycznie zostaje zastąpiony `source:addr`, bez tworzenia zbędnego konfliktu.


## Licencja
[GPLv3](LICENSE)

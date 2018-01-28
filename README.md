## StanfordParser

### MyDemon.java
note for stanford parser

### Text.java
implement word split and weight calculate in paper6, and thanks to DALAO Shen's [node distance'cal algorithm](https://github.com/shenzhiqiang1997/QueryWordWeightCal)

run this Text.java you will get such output
 - amod(trouser-2, blue-1)
 - root(ROOT-0, trouser-2)
 - case(pocket-4, with-3)
 - nmod:with(trouser-2, pocket-4)
 - with0.10312567614835726
 - trouser3.7412074217383937
 - blue0.19188323961442497
 - pocket0.17003501479553917

### Word.java
helpe to cal weight

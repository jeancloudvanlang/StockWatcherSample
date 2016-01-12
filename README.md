# StockWatcher Sample

This is the [official GWT](http://www.gwtproject.org/) StockWatcher Sample created with Maven ([gwt-maven-plugin](https://gwt-maven-plugin.github.io))

```maven
 mvn archetype:generate \
   -DarchetypeGroupId=org.codehaus.mojo \
   -DarchetypeArtifactId=gwt-maven-plugin \
   -DarchetypeVersion=2.7
```

or within Intellij (like [that](https://www.youtube.com/watch?v=XD9anp_p4mc))

## How to run it

### via Maven
* `mvn clean install`
* `mvn gwt:run`

### via Intellij
* create a new gwt configuration (with `Edit Configuration`)
* adjust the fields (select your module and the `super debug modus`)
* debug/run it

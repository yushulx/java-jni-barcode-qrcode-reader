FROM openjdk:11-stretch
COPY images/AllSupportedBarcodeTypes.png AllSupportedBarcodeTypes.png
COPY target/test-1.0-SNAPSHOT-jar-with-dependencies.jar test-1.0-SNAPSHOT-jar-with-dependencies.jar
CMD java -cp test-1.0-SNAPSHOT-jar-with-dependencies.jar com.dynamsoft.App AllSupportedBarcodeTypes.png
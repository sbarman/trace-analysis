run:
	java -cp kodkod.jar:angels.jar:commons-lang-2.6.jar:bin -Djava.library.path=darwin_x86_64 apps.CsvAnalysis

compile:
	javac -d bin -sourcepath src/java -cp angels.jar:kodkod.jar:commons-lang-2.6.jar:. src/java/apps/CsvAnalysis.java

clean:
	rm -rf bin/*


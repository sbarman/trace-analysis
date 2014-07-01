run:
	java -cp lib/kodkod.jar:lib/angels.jar:lib/commons-lang-2.6.jar:bin -Djava.library.path=darwin_x86_64 apps.CsvViewerGui 'SODA.csv'

compile:
	javac -d bin -sourcepath src/main/java -cp lib/angels.jar:lib/kodkod.jar:lib/commons-lang-2.6.jar:. src/main/java/apps/CsvViewerGui.java

clean:
	rm -rf bin/*


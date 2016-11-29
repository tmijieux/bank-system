CC=javac
IDLJ=idlj
JAR="lib/*"

all:
	$(CC) -cp $(JAR) *.java BankSystem/*.java

idl: Banque.idl
	$(IDLJ) -fall $^

clean:
	$(RM) BankSystem/* *.class

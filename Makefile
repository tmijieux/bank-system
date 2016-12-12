CC=javac
JAR=".:lib/*"
# IDLJ=java -cp $(JAR) -jar lib/openorb_orb_tools-1.4.0.jar "lib/*" org.openorb.compiler.IdlCompiler
IDLJ=idlj

all:
	$(CC) -cp $(JAR) *.java BankSystem/*.java #-Xlint:unchecked

.PHONY: idl

idl: Banque.idl
	$(IDLJ) -fall -Iidl $^

clean:
	$(RM) BankSystem/* *.class

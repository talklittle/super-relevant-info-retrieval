
JAVAC  = javac
OUTDIR = $(PWD)/bin
LIBDIR = $(PWD)/src/lib

SRCDIR1 = $(PWD)/src/coms6111/proj1
SRCFILES = $(SRCDIR1)/Query.java $(SRCDIR1)/QueryExpander.java \
	   $(SRCDIR1)/Result.java $(SRCDIR1)/Resultset.java \
	   $(SRCDIR1)/RunnerCLI.java $(SRCDIR1)/RunnerGUI.java \
	   $(SRCDIR1)/TermFreqQueryExpander.java

build: $(SRCFILES)
	./build.sh $(OUTDIR)

all: build

exec:
	./run.sh $(LIBDIR)

clean:
	-rm -rf $(OUTDIR)

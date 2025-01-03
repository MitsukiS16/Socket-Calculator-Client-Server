# Variables
JAVAC = javac
JAVA = java

PEER_CLASS = Peer

CHECK_PORTS_SCRIPT = ./cleanup.sh
RUN_PEERS_SCRIPT = ./run_peers.sh

# Default target
all: compile

# Compile target
compile:
	@echo "Compiling files..."
	$(JAVAC) *.java

# Run target
run: compile
	@echo "Checking ports before running..."
	$(CHECK_PORTS_SCRIPT)
	@echo "Starting application..."
	$(RUN_PEERS_SCRIPT)

# Clean target
clean:
	rm -f *.class
	rm -f *.log
	@echo "Cleaning up..."
	$(CHECK_PORTS_SCRIPT)

.PHONY: all compile run clean

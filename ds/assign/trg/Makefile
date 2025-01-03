# Variables
JAVAC = javac
JAVA = java

MAIN_CLASS = CalculatorMultiServer
PEER_CLASS = Peer
TOKEN_CLASS = Token

ARGS_SERVER = localhost 3000
ARGS_TOKEN = localhost 20000 token

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
	@echo "Cleaning up..."
	$(CHECK_PORTS_SCRIPT)

.PHONY: all compile run clean

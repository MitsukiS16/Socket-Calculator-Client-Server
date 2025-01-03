# Socket Calculator Client Server

## Description

Implementing three distributed systems scenarios using Java, focusing on distributed algorithms and networked applications. These include:

1. Mutual Exclusion with Token Passing

2. P2P Network

3. Chat Application

## Mutual Exclusion with Token Passing

### Features

- **Mutual Exclusion**: The system ensures mutual exclusion for calculator operations across distributed peers using a token ring.
- **Poisson Process for Request Generation**: Requests are generated at random intervals according to a Poisson distribution, simulating realistic request traffic.
- **Token Passing**: Peers communicate by passing a token around the network. Only the peer holding the token can send requests to the server.
- **Distributed Calculator Server**: A multi-threaded server processes mathematical operations and returns results to the requesting peers.
- **Fault Tolerance**: The system has mechanisms for handling connection failures and retrying token sending.

### Architecture

- **Calculator Multi-Server**: Listens for incoming client requests (arithmetic operations) and returns results.
- **Peer**: Each peer in the network generates requests according to a Poisson process, handles token passing, and communicates with the server.
- **Server**: Handles incoming connections from peers and processes their requests in a queue. Servers forward tokens to the next peer after processing.
- **Token**: A special message passed between peers to enforce mutual exclusion. Only the peer holding the token can submit requests to the server.

### Components

1. Calculator Server

    **Function**: Listens for client connections, processes arithmetic operations (add, sub, mul, div), and sends back results.
    **Files**: CalculatorMultiServer.java, ClientHandler.java

2. Peer

    **Function**: Represents each peer in the distributed network. Each peer communicates with the next peer in a token ring, generates random arithmetic requests using a Poisson process, and sends these requests to the server.
    **Files**: Peer.java

3. Server

    **Function**: Manages incoming requests from peers, processes requests, and forwards the token to the next peer in the ring after completing the operation.
    **Files**: Server.java

4. Token

    **Function**: Handles the sending of tokens between peers. A token is required for a peer to make a request to the server, and only one peer can hold the token at any time.
    **Files**: Token.java

5. Poisson Process

    **Function**: Generates random inter-arrival times for generating requests to the server. Requests are generated according to a Poisson distribution to simulate realistic request traffic.
    **Files**: PoissonProcess.java

6. Request Generator

    **Function**: Simulates the generation of random requests (add, sub, mul, div) to the server, with inter-arrival times determined by the Poisson process.
    **Files**: RequestGenerator.java

### Usage

```bash
$ make clean
$ make compile
$ make run
```

## P2P Network

### Features

- **Dynamic Peer Discovery**: Peers dynamically discover and register with each other upon connection, building a robust and decentralized network.
- **Peer Map Management**: Each peer maintains a local map of other peers it knows, including their addresses and last communication timestamps.
- **Anti-Entropy Algorithm**: Peers periodically exchange their maps with other peers, ensuring that all peers converge to a consistent view of the network.
- **Stale Peer Removal**: Peers use timestamps to detect and remove stale entries from their maps, maintaining an up-to-date and efficient network.
- **Poisson-Based Update Intervals**: Peer map updates and communication follow a Poisson distribution, simulating realistic network conditions.

### Architecture

- **Peer Class**: Represents a node in the network. Handles incoming connections, peer map management, and periodic communication.
- **Peer Map**: A data structure to store peer information, including addresses and timestamps. Provides serialization and deserialization for communication.
- **Anti-Entropy Mechanism**: Ensures eventual consistency in the network by sharing and merging peer maps among nodes.
- **Poisson Process**: Simulates realistic update intervals for communication and map updates.

### Components

1. Peer
 
 - Listens for incoming connections and merges received peer maps.
 - Initiates periodic anti-entropy broadcasts to randomly selected peers.
 - Cleans up stale entries in its peer map based on a configurable threshold.
 - Handles dynamic peer connections, ensuring robust fault tolerance.

2. PeerMap

 - Stores information about known peers, including their addresses and timestamps.
 - Supports merging maps received from other peers and removing outdated entries.
 - Provides serialization for transmission over the network.
 
3. PoissonProcess

 - Generates random inter-arrival times for events based on a Poisson distribution.
 - Simulates realistic peer map update intervals to mimic network behavior.

4. App

 - Entry point for the application.
 - Initializes the peer with specified connections and starts the network simulation.
 - Demonstrates the anti-entropy mechanism and Poisson process in action.

### Usage

```bash
$ make clean
$ make compile
$ make run
```


## Chat Application

### Features

- **Real-Time Communication**: Enables seamless and instant messaging among distributed peers.
- **User Authentication**: Ensures secure access by authenticating peers before they join the chat network.
- **Peer-to-Peer Architecture**: Decentralized communication model allows direct message exchanges without a central server.
- **Message Broadcasting**: Facilitates message delivery to all connected peers, ensuring everyone stays updated.
- **Fault Tolerance**: Handles peer disconnections gracefully, ensuring minimal disruption to the chat network.

### Architecture

1. **Peer**: 
   - Acts as an individual user in the chat system.
   - Manages the sending and receiving of messages.
   - Maintains connections to other peers in the network.
   
2. **Server**: 
   - Serves as an initial point for peer registration and discovery.
   - Facilitates message broadcasting and ensures connected peers are updated.

3. **Messaging**:
   - Implements protocols for direct communication between peers.
   - Ensures reliable delivery of messages and resolves potential conflicts.

4. **Poisson Process**:
    - Simulates random intervals between messages, making message sending more realistic and varied

5. **Lamport Clock**:
    - Ensures correct message order by timestamping messages, so events are logically ordered across peers.

### Components

1. **ChatPeer**: Represents a chat participant. Handles:
   - Establishing peer-to-peer connections.
   - Sending and receiving chat messages.
   - Authenticating with the server upon joining.

2. **ChatServer**: Acts as a mediator for:
   - Authenticating users.
   - Facilitating initial peer discovery.
   - Broadcasting messages to active peers.

3. **MessageHandler**: Manages the communication protocols for:
   - Encoding/decoding messages.
   - Ensuring reliable delivery across peers.

4. **Authentication**:
   - Verifies user credentials before allowing them to join the network.
   - Prevents unauthorized access.

5. **Fault Management**:
   - Detects and handles peer disconnections.
   - Ensures continuity in message broadcasting.

### Usage

```bash
$ make clean
$ make compile
$ make run
```

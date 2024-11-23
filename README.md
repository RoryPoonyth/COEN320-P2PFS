# Peer-to-Peer File Transfer System - COEN 366

## Overview

This project was developed for the **COEN 366** course and implements a **Peer-to-Peer (P2P) File Transfer System** in **Java**. The system uses both **UDP** and **TCP** protocols to enable efficient and reliable file sharing between peers in a decentralized network. Users can discover available files, request downloads, and share files seamlessly without relying on a central server.

---

## Features

- **Hybrid Protocol Usage**:
  - **TCP**: Ensures reliable file transfers with acknowledgment and retransmission.
  - **UDP**: Enables lightweight and faster file discovery through broadcast messages.
- **Decentralized Architecture**: Fully distributed, with each peer acting as both a client and a server.
- **File Discovery**: Peers use UDP broadcasts to announce and query available files.
- **File Transfer**: Files are transferred over TCP for reliability and error-free transmission.
- **Multithreaded Design**: Supports multiple simultaneous connections for scalability.

---

## How It Works

### Peer-to-Peer Communication

1. **Discovery Phase (UDP)**:
   - A peer sends a broadcast request over the network using UDP to find files.
   - Other peers respond with their file inventories.

2. **Connection Establishment (TCP)**:
   - Once the requesting peer identifies a file, it establishes a TCP connection with the peer hosting the file.
   - The file is transferred securely over this connection.

3. **File Transfer**:
   - Files are split into chunks, sent sequentially, and reassembled at the receiving end.
   - TCP ensures no data loss or corruption.

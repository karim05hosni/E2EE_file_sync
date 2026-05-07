## Index

- [Problem](#problem)
- [What the system does](#what-the-system-does)
    - [Overview (what the system does)](#overview-what-the-system-does)
    - [How users interact with it (setup)](#how-users-interact-with-it)
- [Technical challenges](#technical-challenges)
    - [Encryption model](#encryption-model)
    - [Key management](#key-management)
    - [File streaming](#file-streaming)
    - [Client-side concurrency model](#client-side-concurrency-model)
- [Architecture decisions](#architecture-decisions)
- [What I’d improve (trade-offs)](#what-id-improve-trade-offs)

## Problem

Most file-sharing systems require trusting the server with your data. I wanted to design a system where even the server cannot read user files.

## What the system does

### Overview (what the system does)

Users play with files → encrypted locally → sent to server → downloaded on space’s users’ devices → decrypted by space’s users’ devices

#### client-side upload pipeline sequence diagram

```mermaid
sequenceDiagram 
    participant OS as OS/FileSystem
    participant FW as FileWatcher
    participant EQ as encryptQueue
    participant FE as FileEncryptor
    participant DebMap as debounceMap
    participant CS as CryptoService
    participant PUQ as pendingUploadsQueue
    participant WS as WebSocketClient
    participant UQ as uploadQueue
    participant FU as FileUploader
    participant Srv as Server

    OS->>FW: emit ENTRY_MODIFY (file path)
    FW->>EQ: put EncryptJob(path, MODIFY)
    FE->>EQ: take()
    EQ-->>FE: return EncryptJob
    
    FE->>DebMap: check & cancel old ScheduledFuture for path
    FE->>DebMap: put new ScheduledFuture (700ms delay)
    Note over FE, DebMap: Wait 700ms (Debounce constraint)
    
    FE->>CS: encryptFile(path)
    CS-->>FE: return EncryptedFileResult (CipherInputStream, Metadata)
    
    FE->>WS: send("MODIFY|{metadata}")
    WS->>Srv: WebSocket message [MODIFY metadata]
    
    FE->>PUQ: add(pendingUpload(metadata, cipherStream))
    
    Srv->>WS: WebSocket event [UPLOAD_REQUIRED]
    WS->>PUQ: take()
    PUQ-->>WS: return pendingUpload
    
    WS->>UQ: add(UploadFileJob(cipherStream, metadata))
    
    FU->>UQ: take()
    UQ-->>FU: return UploadFileJob
    
    FU->>Srv: HTTP POST /api/file/upload (Multipart)
    Note over FU, Srv: Streams cipherStream chunk by chunk
    FU->>OS: close() cipherStream
    Srv-->>FU: HTTP 200 OK
```

#### client-side download pipeline sequence diagram

```mermaid
sequenceDiagram 
    participant Srv as Server
    participant WS as WebSocketClient
    participant IM as IndexManager
    participant DQ as downloadQueue
    participant DL as Downloader
    participant HTTP as HttpClient
    participant FS as FileStorage
    participant DecQ as decryptQueue
    participant Dec as Decryptor
    participant CS as CryptoService
    participant IQ as installQueue
    participant ES as EventsSuppressor (Map)
    participant Inst as Installer

    Srv->>WS: WebSocket event [DOWNLOAD_REQUIRED|metadata]
    WS->>IM: addFile(workspacePath, fileId)
    WS->>DQ: put DownloadJob(metadata)
    
    DL->>DQ: take()
    DL->>HTTP: GET /api/file/download/{fileId}
    HTTP-->>DL: HTTP Response (InputStream of encrypted bytes)
    
    DL->>FS: saveDownloadTmpFile(InputStream, fileId)
    Note over DL, FS: Read HTTP InputStream chunk by chunk & write to tmp/*.bin
    FS->>FS: auto-close HTTP InputStream
    
    DL->>FS: openDownloadTmpFile(fileId)
    FS-->>DL: return FileInputStream (encrypted data)
    
    DL->>DecQ: put DecryptJob(metadata, FileInputStream)
    
    Dec->>DecQ: take()
    Dec->>CS: decryptFile(FileInputStream, encryptedDEK, IV)
    Note over CS: RSA decrypt DEK, init AES-GCM Cipher
    CS-->>Dec: return CipherInputStream (plaintext data stream)
    
    Dec->>IQ: put InstallJob(metadata, CipherInputStream)
    
    Inst->>IQ: take()
    Inst->>ES: suppress(workspacePath)
    Note over Inst, ES: Adds path to suppressedEvents map (prevents upload loop)
    
    Inst->>FS: saveFile(CipherInputStream, workspacePath)
    Note over Inst, FS: Read chunk by chunk & write to final workspace OutputStream
    FS->>FS: auto-close BOTH InputStream and OutputStream
```

#### server-side file sync flow with message protocol sequence diagram

```mermaid
sequenceDiagram 
    participant Client as Client-side
    participant EH as EventsHandler
    participant WSM as WebSocketSessionManager
    participant SSM as SpaceSessionManager
    participant CS as ClientSession
    participant FS as fileService
    participant FC as FileController
    participant DB as Database
    participant Disk as FileSystem

    Note over SSM: Shared State: workspaceSessions<br/>Map<String(spaceId), Map<String(clientId), ClientSession>>
    Note over WSM: Shared State: sessions<br/>Map<String(userId), WebSocketSession>

    Client->>EH: Send WS Text: "MODIFY|{FileMetadataDTO_JSON}"
    Note over EH: Parse JSON and extract fileId & checksum
    
    EH->>DB: findLastVersionByFileId(dto.fileId)
    DB-->>EH: Return server's latest FileVersion

    alt Checksum differs AND Client Version > Server Version
        EH->>Client: Send WS Text: "UPLOAD_REQUIRED|{dtoJson}"
        
        Client->>FC: POST /api/file/upload (Multipart HTTP: file + metadata)
        FC->>FS: receiveFile(MultipartFile, FileMetadataDTO)
        
        FS->>DB: saveFileMetadataAndVersion() (inserts Metadata, Version, DEKs)
        DB-->>FS: Acknowledge DB insertions completed
        
        FS->>Disk: saveFile(file.getBytes(), fileId, version, spaceId)
        Note over Disk: Write raw bytes to /server_storage/.../v[X].bin
        Disk-->>FS: Acknowledge disk write
        
        FS->>WSM: getSession(uploaderId)
        WSM-->>FS: returns WebSocketSession
        FS->>Client: Send WS Text: "META|{confirmedDtoJson}"
        
        FS->>SSM: broadcastExcept(spaceId, uploaderId, "DOWNLOAD_REQUIRED|...")
        Note over SSM: Looks up space's ClientSession map
        loop For each active ClientSession in Space (except uploader)
            SSM->>CS: sendMessage("DOWNLOAD_REQUIRED|...")
            CS->>Client: (Other clients receive download notification)
        end 
        
        FS-->>FC: Return Metadata DTO
        FC-->>Client: HTTP 200 OK
        
    else Checksum matches (Already in sync)
        EH->>Client: Send WS Text: "NOTHING_REQUIRED|{serverFileVersionJson}"
    end 

    opt Broadcast Event (e.g., DELETE)
        Client->>EH: Send WS Text: "DELETE|{payload}"
        EH->>SSM: broadcastExcept(spaceId, clientId, stringMessage)
        Note over SSM: Looks up space's ClientSession map
        loop For each active ClientSession in Space (except sender)
            SSM->>CS: sendMessage(stringMessage)
            CS->>Client: (Other clients receive WS Update)
        end 
    end 

```

### How users interact with it ( setup )

1. prerequirements
- maven

### 1. Run the client

```
git clone https://github.com/karim05hosni/E2EE_file_sync.git
cd E2EE_file_sync/client-app/client-app
mvn clean compile
mvn exec:java -Dexec.mainClass="com.karimhosny.Main"
```

---

### Run the server

Check E2EE_file_sync/filesyncserver/README.md

### 2. First-Time Setup (Onboarding)

On the first run, the client will guide you through initial setup:

- You will be prompted to enter a **base directory** for your workspace.
- This directory will contain:
    - your files (`workspace/`)
    - encrypted versions (`ciphertext/`)
    - metadata (`metadata/`)
    - keys (`keys/`)
- you will be asked for config file id, if you want to test on two clients, you can run the same instance but input two different config file id, this is where base directory get stored

---

### 3. Configuration File

After setup, a `config.json` file will be created in the application directory:

```
{
  "baseDirectory":"/your/path/here"
}
```

This file is used to persist your configuration for future runs.

---

### 4. Key Generation

During setup:

- A **public/private key pair** is generated on your device.
- Your **private key is encrypted** using a User Master Key (UMK).
- The **public key is sent to the server**.
- Keys are stored in:

```
<baseDirectory>/keys/
```

---

### 5. Directory Structure

After setup, your base directory will look like:

```
client_storage/
├── workspace/      # Your editable files
├── ciphertext/     # Encrypted versions (synced with server)
├── metadata/       # File index and checksums
├── keys/           # Encrypted private key + public key
└── temp/           # Temporary files
```

---

### 6. Running After Setup

On subsequent runs:

- The client loads `config.json`
- Decrypts your private key using UMK
- Starts monitoring your workspace for changes
- Automatically syncs files with the server

### ⚠️ Notes

- Do not manually modify files inside `ciphertext/` or `metadata/`.
- Deleting the `keys/` directory will result in loss of access to encrypted files.
- Ensure your base directory has sufficient storage for file versions.

## Technical challenges

### Key management

user keys: 

- UMK: its parameters are stored locally, on app startup, the parameters get loaded and user enters his password to derive UMK. used for encrypting and decrypting user’s private key
- RSA Private Key: stored encrypted locally, on app startup, it gets loaded and derived from UMK
- RSA Public key: stored on server

each file has versions, each version has a Data Encryption Key “DEK” which is a AES symmetric key, that is stored on postgres encrypted by users’ public keys

### Encryption model

Each file version is encrypted by “DEK”, it’s stored on server encrypted with all space users’ public keys so that it can be decrypted only by space users’ private keys

### File streaming

upload pipeline:

FileEncryptor worker opens file stream by triggering CryptoService.encryptFile(filePath) by Files.newInputStream(filePath).

FileUploader worker closes it after upload the encrypted file to server over HTTP multipart request.

download pipeline:

FileDownloader opens it after downloading the encrypted file from server “executing DownloadJob” by fileStorage.saveDownloadTmpFile().

FileInstaller closes it after writing to user’s final workspace by fileStorage.saveFile().

the standard streaming interface used is InputStream, since it doesn’t load all the file once in memory but only chunk-by-chunk

### Client-side concurrency model

it’s mainly consists of Upload pipeline & Download pipeline, each has workers running concurrently with relying on producer-consumer pattern with mechanisms that decrease the common backpressure problem in that design

```mermaid
flowchart TD

    subgraph Upload Pipeline
        FW[FileWatcher]
        EQ[("encryptQueue<br/>(LinkedBlockingQueue)")]
        FE[FileEncryptor]
        DebMap[("debounceMap<br/>(ConcurrentHashMap)")]
        PUQ[("pendingUploadsQueue<br/>(LinkedBlockingQueue)")]
        UQ[("uploadQueue<br/>(LinkedBlockingQueue)")]
        FU[FileUploader]
        WS[WebSocketClient]
        Srv((Server))

        FW -- "put EncryptJob(path, action)<br/>raw file change event" --> EQ
        EQ -- "take EncryptJob()" --> FE
        FE <--> |"check/update ScheduledFuture<br/>coalesces rapid file edits"| DebMap
        
        FE -- "put pendingUpload(meta, cipherStream)<br/>holds stream until server approval" --> PUQ
        FE -- "put UploadMetadataJob / UploadFileJob" --> UQ
        
        WS -- "takes pendingUpload upon<br/>UPLOAD_REQUIRED msg" --> PUQ
        WS -- "transforms pending to UploadFileJob" --> UQ
        
        UQ -- "take UploadJob()" --> FU
        FU -- "streams chunked HTTP POST" --> Srv
    end

    subgraph Download Pipeline
        Srv2((Server))
        WS2[WebSocketClient]
        DQ[("downloadQueue<br/>(LinkedBlockingQueue)")]
        DL[Downloader]
        DecQ[("decryptQueue<br/>(LinkedBlockingQueue)")]
        Dec[Decryptor]
        IQ[("installQueue<br/>(LinkedBlockingQueue)")]
        Inst[Installer]
        ESMap[("suppressedEvents<br/>(ConcurrentHashMap)")]

        Srv2 -- "[DOWNLOAD_REQUIRED|metadata]" --> WS2
        WS2 -- "put DownloadJob(metadata)" --> DQ
        DQ -- "take DownloadJob()" --> DL
        
        DL -- "downloads HTTP to tmp,<br/>put DecryptJob(metadata, stream)" --> DecQ
        DecQ -- "take DecryptJob()" --> Dec
        
        Dec -- "decrypts chunk stream,<br/>put InstallJob(metadata, plainStream)" --> IQ
        IQ -- "take InstallJob()" --> Inst
        
        Inst -- "put(path, timestamp)<br/>skips FileWatcher loop" --> ESMap
        Inst -- "writes plaintext to workspace" --> Workspace[(Workspace FS)]
    end
```

## Architecture decisions

### Tech Stack:

- Java: due to the strengths of it’s libraries in cryptographic operations and concurrency models
- postgresql:  for storing files metadata and users’ public keys in a structured relational schema
- spring-boot: the main Java framework for server-side applications and it’s libraries that handles websocket protocol perfectly, making it ideal for event-drive systems

## What I’d improve (trade-offs)

- Error handling on threads interruptions, cryptographic operations failures and corrupted websocket messages
- Focus was on architecture and learning not full security hardening, it would be mandatory to perform security audit before going on production
- key management is simplified and not production grade, e.g user’s keys needs recovery mechanism
- some things were not taken in considerations since I already learnt what I wanted e.g renaming files, encrypting user’s file path, scheduling tasks that handles thread interruptions, file sync failures on different levels in the pipeline, connection recovery with the server and closing the idle opened file streams
- cleaning the messy functions
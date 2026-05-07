# distributed_file_sync client-app — working memory (for diagrams/docs later)

This is a client for an E2EE workspace “sync/share” app:
- Local file changes in `workspace/` are detected, encrypted client-side, then uploaded.
- Remote events arrive via WebSocket; files are downloaded, decrypted client-side, then installed into `workspace/`.
- Crypto model: per-user asymmetric keys + per-file symmetric DEK; DEK is encrypted for each recipient user.

Entry point + wiring:
- Main boot: [src/main/java/com/karimhosny/Main.java](src/main/java/com/karimhosny/Main.java)
- Dependency wiring + shared queues/maps: [src/main/java/com/karimhosny/DIcontainer/AppFactory.java](src/main/java/com/karimhosny/DIcontainer/AppFactory.java)

---

## 1) Upload Pipeline

Core files:
- Pipeline manager: [src/main/java/com/karimhosny/file/uploadPipeline/UploadPipelineManager.java](src/main/java/com/karimhosny/file/uploadPipeline/UploadPipelineManager.java)
- Watcher: [src/main/java/com/karimhosny/file/uploadPipeline/watcher/FileWatcher.java](src/main/java/com/karimhosny/file/uploadPipeline/watcher/FileWatcher.java)
- Encryptor: [src/main/java/com/karimhosny/file/uploadPipeline/encryptor/FileEncryptor.java](src/main/java/com/karimhosny/file/uploadPipeline/encryptor/FileEncryptor.java)
- Uploader: [src/main/java/com/karimhosny/file/uploadPipeline/uploader/FileUploader.java](src/main/java/com/karimhosny/file/uploadPipeline/uploader/FileUploader.java)
- Jobs: [src/main/java/com/karimhosny/file/uploadPipeline/jobs/EncryptJob.java](src/main/java/com/karimhosny/file/uploadPipeline/jobs/EncryptJob.java),
  [src/main/java/com/karimhosny/file/uploadPipeline/jobs/UploadJob.java](src/main/java/com/karimhosny/file/uploadPipeline/jobs/UploadJob.java),
  [src/main/java/com/karimhosny/file/uploadPipeline/jobs/UploadMetadataJob.java](src/main/java/com/karimhosny/file/uploadPipeline/jobs/UploadMetadataJob.java),
  [src/main/java/com/karimhosny/file/uploadPipeline/jobs/UploadFileJob.java](src/main/java/com/karimhosny/file/uploadPipeline/jobs/UploadFileJob.java)
- Pending uploads handshake object: [src/main/java/com/karimhosny/file/uploadPipeline/pendingUpload/pendingUpload.java](src/main/java/com/karimhosny/file/uploadPipeline/pendingUpload/pendingUpload.java)

Shared queues (created in AppFactory, bounded):
- `encryptQueue` (EncryptJob): FileWatcher → FileEncryptor
- `uploadQueue` (UploadJob): FileEncryptor → FileUploader
- `pendingUploadsQueue` (pendingUpload): FileEncryptor → WsClient-triggered UploadFileJob

Thread model (UploadPipelineManager):
- ExecutorService fixed pool(5)
- Runs: 1 watcher, 2 encryptors, 2 uploaders (same instances can be submitted multiple times)

### Upload flow (CREATE)
1. FileWatcher receives ENTRY_CREATE in workspace dir.
2. It enqueues `EncryptJob(path, CREATE)` to `encryptQueue` (blocking `put`).
3. FileEncryptor takes job, calls `cryptoService.encryptFile(path)` → returns:
   - CipherInputStream of encrypted file bytes
   - FileMetadata (checksum, iv, encryptedDEK map, localPath, size, etc.)
4. FileEncryptor sets metadata fields:
   - `version=0`, `owner`, `spaceId`, `action="UPLOAD"`
5. It enqueues to `uploadQueue`:
   - UploadMetadataJob(metadata) → sends JSON over WebSocket
   - UploadFileJob(cipherStream, metadata) → uploads bytes via HTTP multipart

### Upload flow (MODIFY) + debounce + server-coordination
- FileWatcher enqueues `EncryptJob(path, MODIFY)`.
- FileEncryptor debounces per-path using `debounceMap<Path,ScheduledFuture>`:
  - Cancels prior scheduled task for the same path
  - Schedules encryption after ~700ms (to coalesce rapid edits)
- The scheduled task:
  1. Re-encrypts file (new DEK + new IV).
  2. Looks up `fileId` using IndexManager (path→fileId mapping).
  3. Loads existing metadata `filesMetadata/file_{fileId}.json`.
  4. Updates: `version++`, `checksum`, `iv`, and `encryptedDEKs`.
  5. Saves updated metadata to disk.
  6. Sends `MODIFY|{metadataJson}` over WebSocket.
  7. Enqueues a `pendingUpload(metadata, encryptedStream)` into `pendingUploadsQueue`.
- The file bytes are NOT immediately HTTP-uploaded; instead, server later requests them:
  - WebSocket event `UPLOAD_REQUIRED` triggers WsClient to `take()` one pending upload and enqueue UploadFileJob into `uploadQueue`.

### Upload flow (DELETE)
- FileWatcher enqueues `EncryptJob(path, DELETE)`.
- FileEncryptor:
  - Resolves `fileId` from IndexManager
  - Loads metadata from disk
  - Sets `action="DELETE"`
  - Sends `DELETE|{metadataJson}` over WebSocket
  - (No local delete here; remote clients will install/delete via download pipeline)

---

## 2) Download Pipeline

Core files:
- Pipeline manager: [src/main/java/com/karimhosny/file/downloadPipeline/DownloadPipelineManager.java](src/main/java/com/karimhosny/file/downloadPipeline/DownloadPipelineManager.java)
- Downloader worker: [src/main/java/com/karimhosny/file/downloadPipeline/downloader/Downloader.java](src/main/java/com/karimhosny/file/downloadPipeline/downloader/Downloader.java)
- Decryptor worker: [src/main/java/com/karimhosny/file/downloadPipeline/decryptor/Decryptor.java](src/main/java/com/karimhosny/file/downloadPipeline/decryptor/Decryptor.java)
- Installer worker: [src/main/java/com/karimhosny/file/downloadPipeline/installer/Installer.java](src/main/java/com/karimhosny/file/downloadPipeline/installer/Installer.java)
- Jobs: [src/main/java/com/karimhosny/file/downloadPipeline/jobs/DownloadJob.java](src/main/java/com/karimhosny/file/downloadPipeline/jobs/DownloadJob.java),
  [src/main/java/com/karimhosny/file/downloadPipeline/jobs/DecryptJob.java](src/main/java/com/karimhosny/file/downloadPipeline/jobs/DecryptJob.java),
  [src/main/java/com/karimhosny/file/downloadPipeline/jobs/InstallJob.java](src/main/java/com/karimhosny/file/downloadPipeline/jobs/InstallJob.java)

Shared queues (created in AppFactory, bounded):
- `downloadQueue` (DownloadJob): WsClient → Downloader
- `decryptQueue` (DecryptJob): Downloader → Decryptor
- `installQueue` (InstallJob): Decryptor/WsClient → Installer

Thread model (DownloadPipelineManager):
- ExecutorService fixed pool(4)
- Runs: downloader, decryptor, installer

### Download flow (remote change → local install)
1. WsClient receives `DOWNLOAD_REQUIRED|{metadataJson}` over WebSocket.
2. It parses metadata, stores it to local filesMetadata, updates IndexManager mapping, and enqueues `DownloadJob(metadata)` into `downloadQueue`.
3. Downloader takes DownloadJob:
   - DownloadJob downloads via HTTP `/api/file/download/{fileId}` and saves to `tmp/downloads/file_{fileId}.bin`.
   - Downloader opens the tmp file stream and enqueues DecryptJob(metadata, encryptedStream, cryptoService) into `decryptQueue`.
4. Decryptor takes DecryptJob:
   - DecryptJob selects the encrypted DEK for current user (`metadata.encryptedDEK[currentUserId]`).
   - Calls `cryptoService.decryptFile(encryptedStream, encryptedDEK, iv)` → CipherInputStream (plaintext)
   - Wraps into InstallJob(metadata, plaintextStream)
5. Installer takes InstallJob:
   - Suppresses file watcher events for that path
   - Writes plaintext stream to workspace path (buffered write)
   - For delete: deletes workspace file instead of writing

---

## 3) Concurrency Model (shared data structures)

Wiring + shared primitives:
- [src/main/java/com/karimhosny/DIcontainer/AppFactory.java](src/main/java/com/karimhosny/DIcontainer/AppFactory.java)

Concurrency primitives used:
- Bounded `LinkedBlockingQueue<T>` for pipeline stages (backpressure via capacity).
- Thread pools:
  - Upload pipeline: fixed pool(5) running 5 tasks (watch + multiple encrypt/upload workers).
  - Download pipeline: fixed pool(4) running 3 tasks.
- Debounce map in FileEncryptor:
  - `ConcurrentHashMap<Path, ScheduledFuture<?>> debounceMap`
  - Scheduler: `ScheduledExecutorService` pool(1)
- Event suppression:
  - `suppressedEvents` is a `ConcurrentHashMap<Path,Long>`
  - [src/main/java/com/karimhosny/file/EventsSuppressor.java](src/main/java/com/karimhosny/file/EventsSuppressor.java)
  - Suppression window is ~2 seconds per path.

Notes for later diagrams:
- This is a classic producer/consumer pipeline design with multiple workers per stage.
- Cross-pipeline coupling points:
  - WsClient produces DownloadJobs/InstallJobs (download pipeline).
  - FileEncryptor produces pending uploads; WsClient consumes them on `UPLOAD_REQUIRED` (upload pipeline).

---

## 4) Encryption Algorithms / Key Model

Key management + unlock:
- Unlock private key (requires password): [src/main/java/com/karimhosny/crypto/KeysManagement.java](src/main/java/com/karimhosny/crypto/KeysManagement.java)
- UMK derivation + private-key wrap/unwrap:
  - [src/main/java/com/karimhosny/crypto/services/impl/UMKutils.java](src/main/java/com/karimhosny/crypto/services/impl/UMKutils.java)
- User key initialization + fetching recipient pubkeys:
  - [src/main/java/com/karimhosny/crypto/services/impl/UserKeysUtils.java](src/main/java/com/karimhosny/crypto/services/impl/UserKeysUtils.java)
- File crypto service:
  - [src/main/java/com/karimhosny/crypto/services/impl/CrytoService.java](src/main/java/com/karimhosny/crypto/services/impl/CrytoService.java)
- Metadata model:
  - [src/main/java/com/karimhosny/crypto/dto/FileMetadata.java](src/main/java/com/karimhosny/crypto/dto/FileMetadata.java)

Algorithms used (current implementation):
- UMK (unlock key):
  - Argon2id KDF (BouncyCastle) derives 32-byte UMK from user password + salt.
  - UMK metadata stored on disk (kdfMetadata).
- Private key at rest:
  - RSA private key bytes are encrypted with UMK using AES-GCM (12-byte nonce, 128-bit tag).
  - Stored as JSON `wrapped_privk.json` (base64 fields).
- Per-file encryption:
  - Generate fresh random DEK (AES-256).
  - Encrypt file stream with AES/GCM/NoPadding using random 12-byte IV.
  - DEK is encrypted for each space user using RSA-OAEP (SHA-256).
  - Metadata contains:
    - `iv` (AES-GCM IV)
    - `encryptedDEK: Map<userId, rsaCiphertextDEK>`
    - checksum (SHA-256), localPath, size, timestamp, ext, version, etc.
- Per-file decryption:
  - Choose encrypted DEK for current userId from metadata.
  - Decrypt DEK with RSA private key (must be unlocked).
  - Decrypt file stream with AES-GCM using `iv`.

Lifecycle (high level):
1. First run: generate RSA pair; store public key on server; store wrapped private key locally.
2. Startup: user logs in; enter password; derive UMK; decrypt private key; keep private key in memory for decrypting DEKs.
3. Each file encryption uses a new DEK (not reused).

---

## 5) Storage Model (client-side folders + services)

Storage configuration + directory layout:
- [src/main/java/com/karimhosny/storage/config/StorageConfig.java](src/main/java/com/karimhosny/storage/config/StorageConfig.java)

On-disk layout under `baseDirectory`:
- `workspace/` — user-visible files (watched & installed)
- `keys/`
  - `umk_metadata.json`
  - `wrapped_privk.json`
- `filesMetadata/`
  - `fileIndex.json` (path→fileId)
  - `file_{id}.json` (FileMetadata per file)
- `tmp/downloads/` — downloaded encrypted blobs `file_{id}.bin`
- `tmp/decrypted/` — present in config, not heavily used in current flow

File I/O services:
- File metadata persistence: [src/main/java/com/karimhosny/file/FileMetadataService.java](src/main/java/com/karimhosny/file/FileMetadataService.java)
- Index manager (path→fileId): [src/main/java/com/karimhosny/file/IndexManager.java](src/main/java/com/karimhosny/file/IndexManager.java)
- File storage + tmp handling: [src/main/java/com/karimhosny/storage/services/impl/FileStorage.java](src/main/java/com/karimhosny/storage/services/impl/FileStorage.java)
- Key storage: [src/main/java/com/karimhosny/storage/services/impl/FileKeyStorage.java](src/main/java/com/karimhosny/storage/services/impl/FileKeyStorage.java)

---

## 6) Workspace file chunking / streaming / writing

Where “streaming” happens today:
- Encrypt: `CipherInputStream` produced in CrytoService (stream encryption).
- Upload: OkHttp custom RequestBody streams InputStream in 8192-byte chunks:
  - [src/main/java/com/karimhosny/connection/http/requests/FileRequest.java](src/main/java/com/karimhosny/connection/http/requests/FileRequest.java)
- Install (write plaintext): FileStorage.saveFile writes in 4096-byte chunks:
  - [src/main/java/com/karimhosny/storage/services/impl/FileStorage.java](src/main/java/com/karimhosny/storage/services/impl/FileStorage.java)
- Download tmp: currently reads *entire* response into memory (`readAllBytes()`) before writing tmp file (not streaming).

So “chunking” is mostly via buffered streaming; there is no explicit fixed-size chunk protocol at the app level yet (no chunk hashes, resumes, etc.).

---

## 7) WebSocket client for events (protocol + side effects)

Core file:
- [src/main/java/com/karimhosny/connection/websockets/WsClient.java](src/main/java/com/karimhosny/connection/websockets/WsClient.java)

Connect:
- Uses Java HttpClient WebSocket.
- Sets header `Authorization` to the JWT token (note: not prefixed with `Bearer` here).

Inbound event handling (onText):
- `META|{json}`:
  - Parse FileMetadata, save to disk, update IndexManager with path→fileId.
- `UPLOAD_REQUIRED`:
  - `take()` one pendingUpload from pendingUploadsQueue, enqueue UploadFileJob into uploadQueue.
- `DOWNLOAD_REQUIRED|{json}`:
  - Save metadata, update index, enqueue DownloadJob.
  - There is also local checksum comparison logic, but current control flow still downloads anyway.
- `DELETE|{json}`:
  - Parse metadata and enqueue InstallJob(action=DELETE) into installQueue (local delete).

Outbound messages currently sent by client:
- UploadMetadataJob: sends raw metadata JSON (no prefix).
- FileEncryptor:
  - Sends `MODIFY|{metadataJson}` on debounced modify
  - Sends `DELETE|{metadataJson}` on delete

Binary messages:
- onBinary currently just logs (no chunk protocol implemented over WS).

---

## 8) Onboarding / session

- Onboarding: [src/main/java/com/karimhosny/setup/OnboardingManager.java](src/main/java/com/karimhosny/setup/OnboardingManager.java)
- Session singleton: [src/main/java/com/karimhosny/auth/api/UserSession.java](src/main/java/com/karimhosny/auth/api/UserSession.java)
- Auth client/service: [src/main/java/com/karimhosny/auth/services/impl/AuthService.java](src/main/java/com/karimhosny/auth/services/impl/AuthService.java)

High-level:
- User logs in → JWT stored in UserSession.
- KeysManagement.unlock(password) decrypts RSA private key into memory (required for decrypting DEKs).

---

## Diagram seeds (what to draw later)

1) Component diagram:
- Workspace watcher → Encryptor → Upload queue → HTTP uploader
- WebSocket client ↔ server events
- Download queue → Downloader → Decryptor → Installer → workspace
- Storage (keys/filesMetadata/tmp)

2) Sequence diagram: CREATE upload
- Watcher → Encryptor → (encryptFile) → upload metadata (WS) → upload file (HTTP)

3) Sequence diagram: MODIFY with debounce + UPLOAD_REQUIRED handshake
- Watcher → Encryptor (debounce) → ws MODIFY → pendingUploadsQueue → ws UPLOAD_REQUIRED → HTTP upload

4) Sequence diagram: DOWNLOAD_REQUIRED
- ws event → downloadQueue → HTTP download → tmp file → decrypt → install (write + suppression)

5) Key lifecycle diagram:
- Password → Argon2id UMK → unwrap RSA private key → RSA decrypt DEK → AES-GCM decrypt file

---

## Known quirks / assumptions (useful when refining later)

- Many paths are derived by finding substring `"workspace"` inside metadata.localPath and taking `substring(index+10)`; this assumes localPath always contains `"workspace"`.
- IndexManager uses an in-memory HashMap (not synchronized) but is accessed from multiple threads (WsClient + FileEncryptor + others).
- Download tmp file is written using `readAllBytes()` (memory-heavy for large files).
- WsClient `DOWNLOAD_REQUIRED|` handler currently downloads even if checksum matches (control flow duplicates download path).
## Filesyncserver: sync/websocket + file module memory (code-verified)

Scope of these notes
- Focused on server-side event-driven sync: `src/main/java/com/kariimhosny/filesyncserver/sync/websocket/**` and file storage: `src/main/java/com/kariimhosny/filesyncserver/file/**`.
- Verified against actual code (not inferred). Date: 2026-05-05.

---

## High-level architecture
- Transport: Spring WebSocket endpoint at `/ws` (raw WebSocket, not STOMP).
- Auth on WS handshake: `Authorization` header is validated; on success the server stores attributes on the WS session: `id` (userId) and `spaceId`.
- Event model: prefix-based string protocol over WebSocket text messages; binary frames carry file bytes.
- Storage model: server persists encrypted file bytes to local disk under `/server_storage/spaces/{spaceId}/files/{fileId}/v[{version}].bin`.
- Metadata model: versions + DEKs are persisted in DB tables (`files_metadata`, `file_versions`, `deks`).

---

## Key modules and responsibilities

### WebSocket module (`sync/websocket`)
- `WebSocketConfig`
  - Registers the WS handler at `/ws`.
  - Adds `AuthHandshakeInterceptor`.
  - `setAllowedOrigins("*")` currently allows all origins.

- `AuthHandshakeInterceptor`
  - Reads `Authorization` header (does not strip a `Bearer ` prefix).
  - Validates token via `IJWTServices.isValidToken(token)`.
  - On success: sets WS session attributes:
    - `id` = `jwtService.extractUserId(token)`
    - `spaceId` = `jwtService.extractClaims(token).get("spaceId")`
  - Important: `afterHandshake(...)` currently throws `UnsupportedOperationException("Not supported yet.")`.

- `EventsHandler` (main WS handler)
  - On connect:
    - Gets `clientId` from session attribute `id`.
    - Gets `spaceId` from session attribute `spaceId`.
    - Registers:
      - `WebSocketSessionManager.addSession(clientId, session)`
      - `SpaceSessionManager.addClient(spaceId, new ClientSession(clientId, session))`
    - Sends `"Welcome"` to the client.
  - On text message:
    - If prefix `MODIFY|...`:
      - Parses JSON into `FileMetadataDTO`.
      - Loads server latest version: `fileVersionRepository.findLastVersionByFileId(dto.fileId)`.
      - If checksum differs AND client version is higher:
        - Sends `UPLOAD_REQUIRED|{dtoJson}` to that client.
        - Calls `fileService.receiveMetadata(session.getId(), dto)` (note: key is WS *sessionId*).
      - Else (checksum matches): sends `NOTHING_REQUIRED|{serverFileVersionJson}`.
    - If prefix `DELETE...`:
      - Calls `spaceSessionManager.broadcastExcept(spaceId, clientId, originalMessage)`.
  - On binary message:
    - Calls `fileService.receiveFile(session.getId(), binMsg)`.
    - Sends back `META|{confirmedMetadataJson}` only to the same client.

- `SpaceSessionManager`
  - Maintains: `workspaceSessions: Map<workspaceId, Map<clientId, ClientSession>>`.
  - Used with `workspaceId == spaceId` (naming mismatch).
  - Broadcast method used by delete + HTTP upload flow:
    - `broadcastExcept(workspaceId, excludeClientId, message)` sends to all open sessions except the excluded id.
  - `broadcastToAll(workspaceId, message)` delegates to `broadcastExcept(workspaceId, null, message)`.

- `WebSocketSessionManager`
  - Stores `Map<String userId, WebSocketSession>`.
  - `addSession(userId, session)` and `getSession(id)`.
  - `removeSession(WebSocketSession session)` currently does `sessions.remove(session.getId())` (removes by WS session id, not user id).

- `ClientSession`
  - Wraps a `WebSocketSession` with a `clientId`.
  - Provides `sendMessage(String)` sending a text frame.

- `sync/websocket/events/*`
  - `FilesEventsHandler` and `MetadataEventsHandler` currently empty stubs.


### File module (`file`)
- `FileController` (`/api/file`)
  - `POST /upload` with multipart parts: `file` (MultipartFile) + `metadata` (FileMetadataDTO).
  - `GET /download/{id}` streams the latest version as `application/octet-stream`.

- `FileService` (implements `IFileService`)
  - Owns a thread-safe `pendingUploads: Map<wsSessionId, FileMetadataDTO>` to correlate metadata and the next binary frame.
  - WS path:
    - `receiveMetadata(wsSessionId, dto)` stores pending upload iff `dto.action == "UPLOAD"` and writes DB metadata.
    - `receiveFile(wsSessionId, BinaryMessage)`:
      - Looks up pending metadata by wsSessionId.
      - Calls `serverStorage.saveFile(bytes, dto.fileId, dto.version, dto.spaceId)`.
      - Removes pending entry.
      - Returns the DTO (used to send `META|...` back).
  - HTTP multipart upload path:
    - Saves DB metadata + versions.
    - Sets `metadata.action = "DOWNLOAD"`.
    - Saves file bytes to storage.
    - Sends `META|{json}` to uploader’s WS session.
    - Broadcasts `DOWNLOAD_REQUIRED|{json}` to other space members via `SpaceSessionManager.broadcastExcept(spaceId, uploaderId, ...)`.
  - DB write helper `saveFileMetadataAndVersion(dto)`:
    - Ensures a `files_metadata` record exists (creates if absent).
    - Inserts a new `file_versions` row with checksum, versionNo, `by`, and `iv`.
    - For each entry in `dto.encryptedDEK: Map<Long userId, byte[] encryptedDek>` inserts into `deks` via JDBC.

- `LocalDiskStorage` (implements `ServerStorage`)
  - Root folder is hard-coded: `Paths.get("/server_storage")`.
  - `saveFile(data, fileId, versionNo, spaceId)` writes:
    - `spaces/{spaceId}/files/{fileId}/v[{versionNo}].bin`
  - `getFilePath(fileId, spaceId, versionNo)` uses the same layout.
  - `saveFileVersionDEKs(fileId, versionId, dek)` writes to `/server_storage/DEKs/{fileId}/{versionId}.dek`.
    - Note: in current code path, DEKs are persisted to DB via `DekJdbcRepository`; this disk DEK method is not invoked from `FileService`.
  - `readFile(...)` and `exists(...)` are unimplemented.

- DTO: `FileMetadataDTO`
  - Fields used by sync/storage:
    - `checksum: String`
    - `version: int`
    - `iv: byte[]`
    - `encryptedDEK: Map<Long, byte[]>`
    - `action: String` expected values: `UPLOAD` or `DOWNLOAD`
    - `by: Long` (user id)
    - `ext: String`, `localPath: String`, `spaceId: Long`, `owner: Long`, `fileId: Integer`

- Entities
  - `FileMetadata` → table `files_metadata`: `id`, `ext`, `space_id`, `owner`, `path`.
  - `FileVersion` → table `file_versions`: `id`, `file_id`, `version_no`, `checksum`, `by`, `iv`, `created_at`.
  - `Dek` → table `deks`: `id`, `file_version_id`, `user_id`, `encrypted_dek`, `created_at`.

---

## Observed WebSocket message protocol (as implemented)
- Incoming (client → server)
  - `MODIFY|{FileMetadataDTO as JSON}`
  - `DELETE...` (string starting with `DELETE` is broadcast as-is)
  - Binary frame: file bytes (the server expects a prior `receiveMetadata` call to have stored metadata for this wsSessionId)

- Outgoing (server → client)
  - `Welcome` (plain text)
  - `UPLOAD_REQUIRED|{dtoJson}` (client should upload file bytes)
  - `NOTHING_REQUIRED|{FileVersionJson}` (client already synced)
  - `META|{dtoJson}` (ack/echo metadata)
  - `DOWNLOAD_REQUIRED|{dtoJson}` (broadcasted to other space members during HTTP upload flow)

---

## Canonical flows (as implemented)

### WS connect
1. Client opens WS `/ws` with `Authorization` header.
2. `AuthHandshakeInterceptor.beforeHandshake()` validates token and sets session attributes `id` + `spaceId`.
3. `EventsHandler.afterConnectionEstablished()` registers session and sends `Welcome`.

### File update via WS (client-driven)
1. Client sends `MODIFY|{dtoJson}`.
2. Server compares dto checksum/version vs latest `FileVersion` for `dto.fileId`.
3. If upload needed, server sends `UPLOAD_REQUIRED|{dtoJson}` and stores metadata in `pendingUploads` (only if `dto.action == "UPLOAD"`).
4. Client sends binary frame with encrypted file bytes.
5. Server writes bytes to disk and replies `META|{dtoJson}` to the same client.
6. Note: This WS upload path does not currently broadcast a `DOWNLOAD_REQUIRED` to other clients.

### File update via HTTP multipart
1. Client POSTs `/api/file/upload` with `file` + `metadata`.
2. Server persists metadata/version + DEKs to DB, writes bytes to disk.
3. Server sends `META|{dtoJson}` to uploader’s WS session.
4. Server broadcasts `DOWNLOAD_REQUIRED|{dtoJson}` to other space members in the same `spaceId`.

### Delete event
1. Client sends a WS text message starting with `DELETE`.
2. Server broadcasts that message to all other clients in that `spaceId`.

---

## Notable inconsistencies / diagram-relevant caveats
- `AuthHandshakeInterceptor.afterHandshake()` throws `UnsupportedOperationException` (may break WS connections depending on Spring’s call path).
- `WebSocketSessionManager.removeSession(session)` removes by `session.getId()` but sessions are keyed by userId (likely leaving stale sessions).
- `SpaceSessionManager.broadcastToAll()` calls `broadcastExcept(..., null, ...)`, but `broadcastExcept` does `session.getClientId().equals(excludeClientId)` → potential `NullPointerException` when exclude is null.
- `EventsHandler` compares checksums but does not handle `serverFileVersion == null` (first version edge case) and does not handle the case where checksum differs but version is not greater.
- `FileService.receiveMetadata(sessionId, dto)` requires `dto.action == "UPLOAD"`; `EventsHandler` doesn’t set this field (it relies on whatever the client sent).
- Storage root uses absolute Unix-like path `/server_storage`, which is fine in Linux/Docker but surprising on Windows.

---

## How to use these notes for future diagrams
- Component diagram: Client(s) ↔ WebSocket `/ws` (EventsHandler) ↔ FileService ↔ (DB repos + LocalDiskStorage) and SpaceSessionManager for broadcasts.
- Sequence diagram candidates:
  - `MODIFY → UPLOAD_REQUIRED → binary upload → META`
  - HTTP `upload → META to uploader → DOWNLOAD_REQUIRED broadcast → download/{id}`
  - `DELETE` broadcast fan-out

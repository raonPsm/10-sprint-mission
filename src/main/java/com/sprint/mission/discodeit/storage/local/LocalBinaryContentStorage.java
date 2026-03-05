//package com.sprint.mission.discodeit.storage.local;
//
//import com.sprint.mission.discodeit.storage.BinaryContentStorage;
//import org.springframework.http.ResponseEntity;
//
//import java.io.InputStream;
//import java.nio.file.Path;
//import java.util.UUID;
//
//public class LocalBinaryContentStorage implements BinaryContentStorage {
//    private final Path root;
//
//    public UUID put(UUID binaryContentId, byte[] bytes) {
//        return binaryContentId;
//    }
//
//    public InputStream get(UUID binaryContentId) {
//        return null;
//    }
//
//    public ResponseEntity<?> download(UUID binaryContentId) {
//        return null;
//    }
//}

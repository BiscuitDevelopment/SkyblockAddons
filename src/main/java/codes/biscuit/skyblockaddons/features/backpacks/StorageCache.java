package codes.biscuit.skyblockaddons.features.backpacks;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageCache {
    @Getter
    private final Map<String, CompressedStorage> backpacks = new HashMap<>();

    public static class CompressedStorage {
        @Getter
        @Setter
        List<Byte> storage = new ArrayList<>();

        public CompressedStorage() {
        }

        public CompressedStorage(List<Byte> compressedStorage) {
            storage = compressedStorage;
        }
    }
}

package nu.mine.mosher.gedcom;

import com.google.common.io.BaseEncoding;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GedcomUidGenerator {
    private final int length;
    private final Set<String> generated = new HashSet<>(1024);

    public GedcomUidGenerator(final int length) {
        this.length = length;
    }

    public String generateIdWithAts() {
        return "@" + generateId() + "@";
    }

    public String generateId() {
        int sanity = 100000;
        String s = generateCandidateIdOfLength();
        while (sanity > 0 && (s.contains("_") || s.contains("-") || startsWithDigit(s) || this.generated.contains(s))) {
            --sanity;
            s = generateCandidateIdOfLength();
        }
        if (sanity <= 0) {
            throw new IllegalStateException("Cannot generate universally unique IDs as requested.");
        }
        this.generated.add(s);
        return s;
    }

    private static boolean startsWithDigit(String s) {
        final char x = s.charAt(0);
        return '0' <= x && x <= '9';
    }

    private String generateCandidateIdOfLength() {
        return generateCandidateId().substring(0, this.length);
    }

    private static String generateCandidateId() {
        return base64encode(rbFromUuid(UUID.randomUUID()));
    }

    private static byte[] rbFromUuid(final UUID uuid) {
        return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
    }

    private static String base64encode(final byte[] rb) {
        return BaseEncoding.base64Url().encode(rb);
    }
}

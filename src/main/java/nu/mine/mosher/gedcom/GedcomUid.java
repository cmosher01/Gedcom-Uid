package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by user on 12/6/16.
 */
public class GedcomUid {
    private final File file;
    private Charset charset;
    private GedcomTree gt;
    private final Map<String, String> mapRemapIds = new HashMap<>(4096);



    public static void main(final String... args) throws InvalidLevel, IOException {
        if (args.length < 1) {
            printOneId();
        } else {
            new GedcomUid(args[0]).main();
        }
    }



    GedcomUid(final String filename) {
        this.file = new File(filename);
    }

    public void main() throws IOException, InvalidLevel {
        loadGedcom();
        updateGedcom();
        saveGedcom();
    }

    private void loadGedcom() throws IOException, InvalidLevel {
        this.charset = Gedcom.getCharset(this.file);
        this.gt = Gedcom.parseFile(file, this.charset);
    }

    private void updateGedcom() {
        buildIdMap(this.gt.getRoot());
        remapIds(this.gt.getRoot());
    }

    private void saveGedcom() throws IOException {
        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), this.charset));

        Gedcom.writeFile(this.gt, out, 120);

        out.flush();
        out.close();
    }



    private void buildIdMap(final TreeNode<GedcomLine> node) {
        node.forEach(c -> buildIdMap(c));

        final GedcomLine gedcomLine = node.getObject();
        if (gedcomLine != null) {
            if (gedcomLine.hasID()) {
                final String idOld = gedcomLine.getID();
                this.mapRemapIds.put(idOld, generateIdWithAts());
            }
        }
    }

    private void remapIds(final TreeNode<GedcomLine> node) {
        node.forEach(c -> remapIds(c));

        final GedcomLine gedcomLine = node.getObject();
        if (gedcomLine != null) {
            if (gedcomLine.hasID()) {
                final String newId = this.mapRemapIds.get(gedcomLine.getID());
                node.setObject(new GedcomLine(gedcomLine.getLevel(), newId, gedcomLine.getTagString(), gedcomLine.getValue()));
            }
            if (gedcomLine.isPointer()) {
                final String newId = this.mapRemapIds.get(gedcomLine.getPointer());
                if (newId == null) {
                    System.err.println("Invalid pointer: "+gedcomLine.getPointer());
                } else {
                    node.setObject(new GedcomLine(gedcomLine.getLevel(), "", gedcomLine.getTagString(), newId));
                }
            }
        }
    }


    private static void printOneId() {
        final String s = generateId();
        System.out.println(s);
        System.out.flush();
    }

    private static String generateIdWithAts() {
        return "@"+generateId()+"@";
    }

    private static String generateId() {
        String s = generateCandidateId();
        while (s.contains("_") || s.contains("-")) {
            s = generateCandidateId();
        }
        return s.substring(0, 20);
    }

    private static String generateCandidateId() {
        return base64encode(rbFromUuid(UUID.randomUUID()));
    }

    private static byte[] rbFromUuid(final UUID uuid) {
        return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
    }

    private static String base64encode(final byte[] rb) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rb);
    }
}

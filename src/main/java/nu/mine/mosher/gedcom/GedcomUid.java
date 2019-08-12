package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static nu.mine.mosher.logging.Jul.log;

/**
 * Created by user on 12/6/16.
 */
public class GedcomUid implements Gedcom.Processor {
    private GedcomUidGenerator uid;
    private final Map<String, String> mapRemapIds = new HashMap<>(4096);



    public static void main(final String... args) throws InvalidLevel, IOException {
        log();
        final GedcomUidOptions options = new ArgParser<>(new GedcomUidOptions()).parse(args).verify();
        new Gedcom(options, new GedcomUid(options)).main();
        System.out.flush();
        System.err.flush();
    }



    private GedcomUid(final GedcomUidOptions options) {
        this.uid = new GedcomUidGenerator(options.length);
    }



    @Override
    public boolean process(final GedcomTree tree) {
        tree.getRoot().forAll(this::buildIdMap);
        tree.getRoot().forAll(this::remapIds);
        return true;
    }



    private void buildIdMap(final TreeNode<GedcomLine> node) {
        final GedcomLine gedcomLine = node.getObject();
        if (gedcomLine != null) {
            if (gedcomLine.hasID()) {
                final String idOld = gedcomLine.getID();
                this.mapRemapIds.put(idOld, uid.generateIdWithAts());
            }
        }
    }

    private void remapIds(final TreeNode<GedcomLine> node) {
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
}

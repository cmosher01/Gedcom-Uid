package nu.mine.mosher.gedcom;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomUidOptions extends GedcomOptions {
    public int length = 20;

    public void help() {
        this.help = true;
        System.err.println("Usage: gedcom-uid [OPTIONS] <original.ged >out.ged");
        System.err.println("Convert all IDs in a GEDCOM file to universally unique ones.");
        System.err.println("Options:");
        System.err.println("-l, --length         Length of IDs (default 20).");
        options();
    }

    public void l(final String length) {
        length(length);
    }

    public void length(final String length) {
        this.length = Integer.parseInt(length);
    }

    public GedcomUidOptions verify() {
        if (this.help) {
            return this;
        }
        if (this.length < 2 || 256 < this.length) {
            throw new IllegalArgumentException("Invalid length; must be between 2 and 256.");
        }
        return this;
    }
}

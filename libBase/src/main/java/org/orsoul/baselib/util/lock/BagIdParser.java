package org.orsoul.baselib.util.lock;

public class BagIdParser {
    public static final int BAG_ID_LEN = 24;
    public static final String BAG_BUNDLES = "20";
    public static final String BAG_TIE = "0";

    String bagId;
    String version;
    String org;
    String wanz;
    String bagType;

    /**
     * 05 027 1 03 0480613ED24B89BE
     *
     * @param str
     * @return
     */
    public BagIdParser(String str) {
        this.bagId = str;
        // 05 027 1 03 0480613ED24B89BE
        this.version = str.substring(0, 2);
        this.org = str.substring(2, 5);
        this.wanz = str.substring(5, 6);
        this.bagType = str.substring(6, 8);
    }

    public static boolean isBagId(String bagId) {
        return (bagId != null) && (bagId.length() == BAG_ID_LEN);
    }

    public static String getBagType(String bagId) {
        if (!isBagId(bagId)) {
            return null;
        }

        return bagId.substring(6, 8);
    }

}

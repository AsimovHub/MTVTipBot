package ac.asimov.mtvtipbot.helper;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class MessageFormatHelper {

    public final static String DISCLAIMER = "\n\nThis bot is maintained by https://asimov.ac, and the MultiVAC team is not responsible.\n"
            + "Please keep in mind your tipbot wallet is not safe enough to hold large amounts of funds.\nDo not deposit money you cannot afford to lose on it.";

    public static String escapeString(String s) {
        if (StringUtils.isBlank(s)) {
            return s;
        }

        return s.replaceAll("\\.", "\\" + "\\.")
                .replaceAll("/", "\\" + "\\/")
                .replaceAll("\\*", "\\" + "\\*")
                .replaceAll("\\+", "\\" + "\\+")
                .replaceAll("-", "\\" + "\\-")
                .replaceAll("_", "\\" + "\\_")
                .replaceAll("\\[", "\\" + "\\[")
                .replaceAll("]", "\\" + "\\]")
                .replaceAll("\\(", "\\" + "\\(")
                .replaceAll("\\)", "\\" + "\\)")
                .replaceAll("\\{", "\\" + "\\{")
                .replaceAll("}", "\\" + "\\}")
                .replaceAll("`", "\\" + "\\`")
                .replaceAll("!", "\\" + "\\!")
                .replaceAll("#", "\\" + "\\#");
    }

    public static String appendDisclaimer(String s) {
        return s + DISCLAIMER;
    }

    public static String appendDisclaimer(String s, boolean escapeString) {
        s = appendDisclaimer(s);
        if (escapeString) {
            return escapeString(s);
        } else {
            return s;
        }
    }


}

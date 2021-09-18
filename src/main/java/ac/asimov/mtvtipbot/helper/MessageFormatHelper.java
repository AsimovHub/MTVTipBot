package ac.asimov.mtvtipbot.helper;

import org.apache.commons.lang3.StringUtils;

public class MessageFormatHelper {

    public final static String DISCLAIMER = "\n\nThis bot is developed and maintained by https://asimov.ac and not associated with the official MultiVAC\n"
            + "Please notice your tipbot wallet is not as secure as your private wallet and you should not hold large amounts of funds in it.";

    public static String escapeStringMarkdownV1(String s) {
        if (StringUtils.isBlank(s)) {
            return s;
        }

        return s
                .replaceAll("\\*", "\\" + "\\*")
                .replaceAll("\\+", "\\" + "\\+")
                .replaceAll("_", "\\" + "\\_")
                .replaceAll("`", "\\" + "\\`");
    }

    public static String escapeStringMarkdownV2(String s) {
        if (StringUtils.isBlank(s)) {
            return s;
        }

        return s.replaceAll("\\.", "\\" + "\\.")
                .replaceAll("/", "\\" + "\\/")
                .replaceAll("\\*", "\\" + "\\*")
                .replaceAll("\\+", "\\" + "\\+")
                .replaceAll("=", "\\" + "\\=")
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
                .replaceAll("\\|", "\\" + "\\|")
                .replaceAll("#", "\\" + "\\#")
                .replaceAll("<", "\\" + "\\<")
                .replaceAll(">", "\\" + "\\>");
    }

    public static String appendDisclaimer(String s) {
        return s + DISCLAIMER;
    }

    public static String appendDisclaimerAndEscapeMarkdownV2(String s, boolean escapeString) {
        s = appendDisclaimer(s);
        if (escapeString) {
            return escapeStringMarkdownV2(s);
        } else {
            return s;
        }
    }

    public static String appendDisclaimerAndEscapeMarkdownV1(String s, boolean escapeString) {
        s = appendDisclaimer(s);
        if (escapeString) {
            return escapeStringMarkdownV1(s);
        } else {
            return s;
        }
    }


}

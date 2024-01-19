package utils;

public class Utils {
    public static String removeUnwantedSymbols(String text) {
        text = text.replaceAll("&lt;b&gt;", "");
        text = text.replaceAll("&lt;/b&gt;", "");
        text = text.replaceAll("&#39;", "'");
        text = text.replaceAll("&lt;b&gt;", "");
        text = text.replaceAll("&lt;/b&gt;", "");
        text = text.replaceAll("&amp;#39;", "");
        text = text.replaceAll("&amp;amp;", "");
        text = text.replaceAll("&amp;nbsp;", "");
        text = text.replaceAll("&amp;middot;", "");
        text = text.replaceAll("------", "");
        text = text.replaceAll("&amp;middot;", "");
        text = text.replaceAll("<b>", "");
        text = text.replaceAll("</b>", "");
        return text;
    }
}
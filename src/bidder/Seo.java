/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author dmitry
 */
public class Seo {

    static String[] disAllowedKeywords = new String[]{"SIGN UP FOR"};
    static String remove = "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js\"></script>";
    static String rev = "wp-content/plugins/revslider/rs-plugin/js/jquery";
    static String meta = "<meta name=\"keywords\" content=\"";
    static String script = "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js\"></script>\n"
            + "        <script type=\"text/javascript\">\n"
            + "            jQuery(function($) {\n"
            + "                $(\".r123\").each(function() {\n"
            + "                    var inc = $(this);\n"
            + "                    inc.hide();\n"
            + "                })\n"
            + "            });\n"
            + "			</script>";
    static final String addedDiv = "<div class=\"r123\">";
    static final String add = "<div class=\"r123\">\n"
            + "<h1><keyword></h1>\n"
            + "<h2><keyword></h2>\n"
            + "<h3><keyword></h2>\n"
            + "<h4><keyword></h4>\n"
            + "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"VIRTUAL COLLEAGUE &raquo; <keyword> Comments Feed\" href=\"feed/index.html\" />\n"
            + "<title>Inbound Call Center - <keyword> - VIRTUAL COLLEAGUE</title>\n"
            + "<meta property=\"og:title\" content=\"Inbound Call Center - unique Support - VIRTUAL COLLEAGUE\" />\n"
            + "<a href=\"index.html\"><keyword></a>\n"
            + "<span class=\"current\"><keyword></span>\n"
            + "<p>Many companies augment voice support with online <keyword> services such as live online chat and email support. VIRTUAL COLLEAGUE can find employees with upbeat personalities and excellent English skills to work in these roles.</p>\n"
            + "</div>";

    public static void main(String[] args) throws IOException {
        String folder = "C:\\My Web Sites\\vc";
        new Seo().searchIndex(folder);

    }
    private boolean addFix = true;
    private boolean removeJq = false;
    private boolean removeAddedDiv = false;

    void searchIndex(String folder) throws IOException {
        for (File file : new File(folder).listFiles()) {
            if (file.isDirectory()) {
                searchIndex(file.getAbsolutePath());
            } else if (file.getName().equalsIgnoreCase("index.html")) {
                String text = readFile(file.getAbsolutePath());
                if (removeAddedDiv && text.contains(addedDiv)) {
                    text = removeTag(addedDiv, text);
                    saveToFile(file, text);
                }
                if (addFix && !text.contains(addedDiv) && text.contains(meta)) {
                    String keyword = getFirstKeyword(text);
                    if (keyword.length() == 0 || Arrays.asList(disAllowedKeywords).contains(keyword)) {
                        keyword = getKeyWord(text);
                    }
                    if (keyword.length() > 0) {
                        keyword = removeAllEndingWords(keyword,"and,or,with,for,by,to,in,on,a,the,an");
                        addHtml(file, text, keyword);
                    }
                }
                if (removeJq && text.contains(rev) && text.contains(remove)) {
                    removeHtml(file, text);
                }
            }

        }
    }
    
    String removeAllEndingWords(String s,String s1)
    {
        for(String s2 : s1.toLowerCase().split(","))
        {
            if(s.toLowerCase().endsWith(" " + s2))
            {
                s = s.substring(0,s.length()-(s2.length() + 1)).trim();
            }
        }
        return s;
    }

    String removeTag(String remove, String text) {

        String tagend = remove.substring(1);
        tagend = tagend.substring(0, tagend.indexOf(" "));
        tagend = "</" + tagend + ">";
        int i = text.indexOf(remove);
        return text.substring(0, i) + text.substring(text.indexOf(tagend, i) + tagend.length());
    }

    void removeHtml(File file, String text) throws IOException {

        text = text.substring(0, text.indexOf(remove)) + text.substring(text.indexOf(remove) + remove.length());

        saveToFile(file, text);
        return;
    }

    String getFirstKeyword(String text) {
        for (String s : Helper.split(text, meta, "/>")) {
            for (String s1 : s.replace(meta, "").split(",")) {
                if (s1.length() > 0) {
                    return s1;
                }
            }
        }
        return "";
    }

    void addHtml(File file, String text, String keyword) throws IOException {
        try {
            boolean f = false;
            for (String s : Helper.split(text, meta, "/>")) {
                for (String s1 : s.replace(meta, "").split(",")) {
                    if (s1.equalsIgnoreCase(keyword)) {
                        f = true;
                        break;
                    }
                }
                if (f) {
                    break;
                }
            }
            String newText = "";
            if (!f) {
                text = text.substring(0, text.indexOf(meta) + meta.length()) + keyword + ", " + text.substring(text.indexOf(meta) + meta.length());
            }

            if (!text.contains(rev)) {
                newText = text.substring(0, text.indexOf("</head>")) + script + text.substring(text.indexOf("</head>"), text.indexOf("</body>"));
            } else {
                newText = text.substring(0, text.indexOf("</body>"));
            }

            newText += add.replace("<keyword>", keyword) + text.substring(text.indexOf("</body>"));
            saveToFile(file, newText);
        } catch (Exception ex) {

        }
    }

    void saveToFile(File file, String text) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(text);
        out.close();
    }

    String getKeyWord(String text) {
        int y = 0;
        while (++y < 10) {
            String keyword = "";
            List<String> keywords = new ArrayList<String>();
            for (String ii : "1234".split("|")) {
                if (ii.length() > 0) {
                    for (String s : Helper.split(text, "<h" + ii + ">", "</h" + ii + ">")) {
                        String[] a = replaceAll(s.replace("<h" + ii + ">", "").replace("</h" + ii + ">", ""), "<>\".,!?*&)(/'-+=").split(" ");
                        keyword = "";
                        for (int j : new int[]{0, 3, 6, 9}) {
                            keyword = "";
                            for (int i = 0; i < 3 && (i + j) < a.length; i++) {
                                keyword += " " + a[i + j];
                            }
                            if (keyword.length() > 0) {
                                keyword = keyword.substring(1).toLowerCase();
                                if (!keywords.contains(keyword)) {
                                    keywords.add(keyword);
                                }
                            }
                        }
                    }
                }
            }
            int j = 0;
            for (String s : keywords) {
                System.out.println(j + ")" + s);
                j++;
            }
            Scanner inputReader = new Scanner(System.in);
            String i = inputReader.nextLine();
            try {
                int ii = Integer.valueOf(i);
                if (ii > -1 && ii < keywords.size()) {
                    return keywords.get(ii);
                }
            } catch (NumberFormatException ex) {
                if (i.length() > 0) {
                    return i;
                }
            } catch (Exception ex) {
            }
        }
        return "";
    }

    String replaceAll(String text, String s) {
        for (String s1 : s.split("|")) {
            text = text.replace(s1, "");
        }
        return text;
    }

    public String readFile(String filename) throws IOException {
        String content = null;
        File file = new File(filename); //for ex foo.txt
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return content;
    }
}

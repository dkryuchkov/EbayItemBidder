/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *
 * @author dmitry
 */
public class NewClass {

    static String dir = "C:\\My Web Sites\\virtualemployee.com.au\\www.staffvirtual.com";

    public static void main(String[] args) throws IOException {
        change(dir);
    }

    public static void change(String d) throws IOException {
        File f = new File(d);
        File i = new File(f.getAbsoluteFile() + "\\index.html");

        if (i.exists() && i.length() == 0) {
            Files.copy(new File("C:\\My Web Sites\\staffvitual.com.au\\www.staffvirtual.com\\"
                    + i.getAbsolutePath().substring(dir.length())).toPath(),
                    i.toPath(), REPLACE_EXISTING);
        }
        for (String ff : f.list()) {
            File g =new File(f.getAbsolutePath() + "\\" + ff);
            if (g.isDirectory()) {
                change(g.getAbsolutePath());
            }
        }
    }
}

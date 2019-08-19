package it.xzq;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GenUtil {

    public String underlineToHump(String underline)
    {
        return underlineToHump(underline, true);
    }

    public String underlineToHump(String underline, boolean toUpperCase)
    {
        String hump = "";
        String[] names = underline.split("_");
        for (String n : names) {
            hump = hump + initialCapital(n);
        }
        if (!toUpperCase) {
            hump = hump.substring(0, 1).toLowerCase() + hump.substring(1);
        }
        return hump;
    }

    public String initialCapital(String str)
    {
        str = str.toLowerCase();
        char[] ch = str.toCharArray();
        if ((ch[0] >= 'a') && (ch[0] <= 'z')) {
            ch[0] = (char)(ch[0] - ' ');
        }
        return new String(ch);
    }

    public void OutFile(String content, String fileName, String path) throws Exception
    {
        PrintWriter pw = null;
        String outputPath = null;
        try
        {
            File directory = new File("");
            if (path.indexOf("mybatis") != -1)
                outputPath = directory.getAbsolutePath() + "/src/main/resources/" + path.replace(".", "/");
            else {
                outputPath = directory.getAbsolutePath() + "/src/main/java/" + path.replace(".", "/");
            }
            if (StringUtils.isEmpty(outputPath)) {
                throw new Exception("Not a valid path!");
            }
            File op = new File(outputPath);
            if (!op.exists()) {
                op.mkdirs();
            }
            pw = new PrintWriter(new FileWriter(outputPath + "/" + fileName));
            pw.println(content);
        }
        catch (IOException e)
        {
            throw new Exception(e.getMessage().length() == 999 ? e.getMessage().substring(0, 999) : e.getMessage());
        }
        finally
        {
            if (pw != null)
            {
                pw.flush();
                pw.close();
            }
        }
    }
}

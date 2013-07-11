package windmon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ReportGenerator {
	private static final Logger logger = Logger.getLogger(ReportGenerator.class.getName());

    /* Custom Tags */
    public static final String TAG_PREFIX    = "<windmon.";
    public static final String TAG_SUFFIX    = "/>";
    public static final String REPORT_DTM    = "report_dtm"; 
    public static final String INTERVAL_SEC  = "interval_sec"; 
    public static final String INTERVAL_MIN  = "interval_min";
    public static final String DIR_DEG       = "dir_deg";
    public static final String DIR_COMP      = "dir_comp";
    public static final String AVE_SPEED_KTS = "ave_speed_kts";
    public static final String AVE_SPEED_BFT = "ave_speed_bft";
    public static final String MIN_SPEED_KTS = "min_speed_kts";
    public static final String MIN_SPEED_BFT = "min_speed_bft";
    public static final String MAX_SPEED_KTS = "max_speed_kts";
    public static final String MAX_SPEED_BFT = "max_speed_bft";
    public static final String DAY_PEAK_KTS  = "day_peak_kts";
    public static final String DAY_PEAK_BFT  = "day_peak_bft";
    public static final String DAY_PEAK_TM  = "day_peak_tm";
    
    /* Hold data values */
    private static Map<String,String> map = new HashMap<String,String>();
    
    /**
     * 
     */
    public ReportGenerator() {
        super();
    }
    
    public void setValue ( String key, String value )
    {
        map.put(key, value);
    }

    public String getvalue ( String key )
    {
        return (String) map.get(key);
    }
    
    public void genReport ( String templatePathname, String reportPathname)
    {
        /* Open template file */
        BufferedReader br;
        try
        {
            br = new BufferedReader(new FileReader(templatePathname));
        }
        catch (Exception e)
        {
            logger.severe("Unable to open template file '" + templatePathname + "'");
            e.printStackTrace();
            return;
        }

        /* Open report file */
        PrintWriter pw;
        try
        {
            pw = new PrintWriter(
                    new FileWriter(reportPathname, false));
        }
        catch (Exception e)
        {
            logger.severe("Unable to open output report file '" + reportPathname + "'");
            e.printStackTrace();
            /* Close template file */
            try
            {
                br.close();
            }
            catch (Exception e2)
            {
                logger.severe("Error closing template '" + templatePathname + "'");
                e2.printStackTrace();
            }
            return;
        }

        /* Read each line. Substitute values and output to report */
        try
        {
            String line;
            int i, j;
            int lineNum = 1;
            while ( (line = br.readLine()) != null)
            {
                StringBuffer buff = new StringBuffer(line);
                i = 0; j = 0;
                while ( i>= 0 )
                {
                    i = buff.indexOf(TAG_PREFIX);
                    if ( i >= 0 )
                    {
                        j = buff.indexOf(TAG_SUFFIX, i);
                        if ( j < i )
                        {
                            /* Problem - could not find closing tag */
                            logger.warning( "Incomlpete tag ignored in template file '" + templatePathname + "' at line " + lineNum + " character " + i);
                            /* Force move to next line */
                            i = -1;
                        }
                        else
                        {
                            /* Extract tag */
                            int x = i + TAG_PREFIX.length();
                            int y = j;
                            String tag = buff.substring(x, y);
                            String value = (String) map.get(tag);
                            if ( value == null )
                            {
                                /* Problem - no data specified for tag */
                                logger.warning( "No data for tag " + tag + " in template file '" + templatePathname + "' at line " + lineNum + " character " + i);
                                /* Just skip - leave tag in file */
                            }
                            else
                            {
                                /* Substitute tag with value */
                                buff.replace(i, j + TAG_SUFFIX.length(), value);
                            }
                        }
                    }
                }
                pw.println(buff);
                lineNum++;
            }
        }
        catch (Exception e)
        {
            logger.severe("Error writing report '" + reportPathname + "' from template '" + templatePathname + "'");
            e.printStackTrace();
            /* Don't return from method. Must close files below */
        }

        try
        {
            br.close();
            pw.close();
        }
        catch (Exception e)
        {
            logger.severe("Error closing report '" + reportPathname + "' and template '" + templatePathname + "'");
            e.printStackTrace();
            return;
        }
    }
}

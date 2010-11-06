/*
 * Created on 08-Feb-2005
 *
 */
package windmon;

import java.util.Vector;

/**
 * @author BallD
 *
 * Encapsulates an NMEA message. Currently supports only message types required
 * for the Autonnic Masthead unit.
 */
public class NMEAMessage
{
    public static char PROPRIETARY_CHAR = 'P';
    public static char FIELD_SEP_CHAR   = ',';
    
    String msgStr = null;
    
    String fields[] = null;
    private boolean proprietary = false;
    private String talkerIDString = null;
    private String sentenceIDString = null;
    private String manufacturerIDString = null;
    
    // When true, message content is believed valid and correct.
    private boolean valid = false;
    // When true, message string requires update to match parameters.
    private boolean reform = true;
    
    // Constructor for standard message from parts
    public NMEAMessage(String talkerID, String sentenceID, String fields[])
    {
        this.proprietary = false;
        this.talkerIDString = talkerID;
        this.sentenceIDString = sentenceID;
        this.manufacturerIDString = null;
        this.fields = fields;
        this.valid = true;
        
        StringBuffer buff = new StringBuffer();
        buff.append("$" + talkerID + sentenceID);
        
        for ( int i=0; i<fields.length; i++)
        {
            buff.append("" + FIELD_SEP_CHAR + fields[i]);
        }
        this.msgStr = buff.toString();
    }

    // Constructor for standard message from parts
    public NMEAMessage(String manufacturerID, String fields[])
    {
        this.proprietary = true;
        this.talkerIDString = null;
        this.sentenceIDString = null;
        this.manufacturerIDString = manufacturerID;
        this.fields = fields;
        this.valid = true;
        
        StringBuffer buff = new StringBuffer();
        buff.append("$" + PROPRIETARY_CHAR + manufacturerID);
        
        for ( int i=0; i<fields.length; i++)
        {
            buff.append("" + FIELD_SEP_CHAR + fields[i]);
        }
        this.msgStr = buff.toString();
    }

    
    public NMEAMessage(String messageString)
    {
        this.msgStr = messageString;
       
        if ( messageString !=  null)
        {
            this.valid = parseString(messageString);
        }
        else
        {
            valid = false;
        }
    }
    
    private boolean parseString(String str)
    {
        int i=0;
        Vector vflds = new Vector();
        
        // Check for "$" in first character
        if ( str.length() <= 0 || str.charAt(0) != '$')
        {
            System.err.println("Invalid NMEA string: '" + str + "'");
            return false;
        }
        
        // Check there is no "$" anywhere else as that suggests garbled message
        // which can happen if link is interrupted and restored mid sentence
        if ( str.length() > 1 && str.indexOf('$', 1) > 0 )
        {
            System.err.println("Garbled NMEA string: '" + str + "'");
            return false;
        }
        
        // Parse the message string
        if ( str.charAt(1) == PROPRIETARY_CHAR )
        {
            // Proprietary message. Need atleast 5 characters
            if ( str.length() < 5 )
            {
                System.err.println("Proprietary NMEA string too short: "
                        + str);
                return false;
            }
            this.proprietary = true;
            // Next 3 characters are manfr ID
            this.manufacturerIDString = str.substring(2,5);
            i = 5;
        }
        else
        {
            // Standard message. Need atleast 6 characters
            if ( str.length() < 6 )
            {
                System.err.println("Standard NMEA string too short: "
                        + str);
                return false;
            }
            this.proprietary = false;
            // First two characters are Talker ID
            this.talkerIDString = str.substring(1,3);
            // Next 3 characters are the Sentence ID
            this.sentenceIDString = str.substring(3,6);
            i = 6;
        }
        
        // Assuming there is more data to come, start processing the 
        // comma separated fields.
        if ( i <= str.length() )
        {
            // Check for comma immediately following message header
            if ( str.charAt(i) != FIELD_SEP_CHAR )
            {
                System.err.println("Invalid NMEA string: " + str);
                return false;
            }
            
            // Advance past comma
            i++;
            // Find next comma
            int j = str.indexOf((int) FIELD_SEP_CHAR, i);
            do
            {
                if ( j == -1 )
                {
                    // No more commas. Field extends to end of string
                    vflds.add(str.substring(i));
                    i=-1;
                }
                else
                {
                    vflds.add(str.substring(i, j));
                    i=j+1;
                    j = str.indexOf((int) FIELD_SEP_CHAR, i);
                }
            } while ( i >= 0 );
        }
        fields = new String[vflds.size()];
        vflds.copyInto(fields);
        return true;
    }

    private void reset()
    {
        fields = null;
        proprietary = false;
        talkerIDString = null;
        sentenceIDString = null;
        manufacturerIDString = null;
    }
    
    /**
     * @return Returns the proprietary.
     */
    public boolean isProprietary() {
        return proprietary;
    }

    /**
     * @return Returns the manufacturerIDString.
     */
    public String getManufacturerIDString() {
        return manufacturerIDString;
    }

    /**
     * @return Returns the sentenceIDString.
     */
    public String getSentenceIDString() {
        return sentenceIDString;
    }

    /**
     * @return Returns the talkerIDString.
     */
    public String getTalkerIDString() {
        return talkerIDString;
    }

    public String getField(int index)
    {
        return (String) fields[index];
    }
    
    public String getMessageString()
    {
        return this.msgStr;
    }
    
    public boolean isValid()
    {
        return valid;
    }
}

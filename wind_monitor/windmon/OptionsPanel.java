/*
 * Created on 10-Feb-2005
 */
package windmon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author BallD
 *
 * UI panel that enables the user to input and send NMEA commands that configure
 * the Autonnic masthead unit.
 */
public class OptionsPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private NMEAController nmea = null;
    private static final int fieldSize = 5;
    private static final String defaultFieldText = "Not Set"; 
    
    private JButton autoCalib, restoreDefault, refWindDirection, refWindSpeed;
    private JLabel windSpeedDampLabel, windDirDampLabel, windIntLabel, periodLabel;
    private JButton windSpeedDampUpdate, windDirDampUpdate, windIntUpdate, periodUpdate;
    private JTextField windSpeedDampCurr, windDirDampCurr, windIntCurr, periodCurr;
    private JTextField windSpeedDampNew, windDirDampNew, windIntNew, periodNew;
    
    private JTextField infoField = null;

    public OptionsPanel(NMEAController nmea)
    {
        super();
        setBackground(Color.GRAY);
        // Make something!
        this.nmea = nmea;
        
        // The usual buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        autoCalib = new JButton("Auto Calibrate");
        restoreDefault = new JButton("Restore Defaults");
        refWindDirection = new JButton("Ref Wind Direction");
        refWindSpeed = new JButton("Ref Wind Speed");
        buttonPanel.add(autoCalib);
        buttonPanel.add(restoreDefault);
        buttonPanel.add(refWindDirection);
        buttonPanel.add(refWindSpeed);

        // The editable fields
        JPanel fieldPanel = new JPanel();
        windDirDampLabel = new JLabel("Wind Direction Damping (0.0 - 100.0%)");
        windSpeedDampLabel = new JLabel("Wind Speed Damping (0.0 - 100.0%)");
        windIntLabel = new JLabel("Wind Speed Integration (milliseconds)");
        periodLabel = new JLabel("Sensor Update Interval (milliseconds)");
        windDirDampUpdate = new JButton("Update");
        windSpeedDampUpdate = new JButton("Update");
        windIntUpdate = new JButton("Update");
        periodUpdate = new JButton("Update");
        windDirDampCurr = new JTextField(defaultFieldText, fieldSize);
        windSpeedDampCurr = new JTextField(defaultFieldText, fieldSize);
        windIntCurr = new JTextField(defaultFieldText, fieldSize);
        periodCurr = new JTextField(defaultFieldText, fieldSize);
        windDirDampCurr.setEditable(false);
        windSpeedDampCurr.setEditable(false);
        windIntCurr.setEditable(false);
        periodCurr.setEditable(false);
        windDirDampNew = new JTextField("", fieldSize);
        windSpeedDampNew = new JTextField("", fieldSize);
        windIntNew = new JTextField("", fieldSize);
        periodNew = new JTextField("", fieldSize);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        fieldPanel.setLayout(gbl);
        
        Utils.addComponentToGridBag(gbl, gbc, windDirDampLabel, fieldPanel,
                0, 0, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windSpeedDampLabel, fieldPanel,
                0, 1, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windIntLabel, fieldPanel,
                0, 2, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, periodLabel, fieldPanel,
                0, 3, 1, 1, 0, 0);

        Utils.addComponentToGridBag(gbl, gbc, windDirDampCurr, fieldPanel,
                1, 0, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windSpeedDampCurr, fieldPanel,
                1, 1, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windIntCurr, fieldPanel,
                1, 2, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, periodCurr, fieldPanel,
                1, 3, 1, 1, 0, 0);

        Utils.addComponentToGridBag(gbl, gbc, windDirDampNew, fieldPanel,
                2, 0, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windSpeedDampNew, fieldPanel,
                2, 1, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windIntNew, fieldPanel,
                2, 2, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, periodNew, fieldPanel,
                2, 3, 1, 1, 0, 0);

        Utils.addComponentToGridBag(gbl, gbc, windDirDampUpdate, fieldPanel,
                3, 0, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windSpeedDampUpdate, fieldPanel,
                3, 1, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, windIntUpdate, fieldPanel,
                3, 2, 1, 1, 0, 0);
        Utils.addComponentToGridBag(gbl, gbc, periodUpdate, fieldPanel,
                3, 3, 1, 1, 0, 0);

        infoField = new JTextField();
        infoField.setEditable(false);
        infoField.setHorizontalAlignment(JTextField.CENTER);
        
        setLayout(new BorderLayout(5,5));
        
        buttonPanel.setBackground(Color.GRAY);
        fieldPanel.setBackground(Color.GRAY);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(fieldPanel, BorderLayout.CENTER);
        add(infoField, BorderLayout.SOUTH);
        
        autoCalib.addActionListener(this);
        restoreDefault.addActionListener(this);
        refWindDirection.addActionListener(this);
        refWindSpeed.addActionListener(this);
        windDirDampUpdate.addActionListener(this);
        windSpeedDampUpdate.addActionListener(this);
        windIntUpdate.addActionListener(this);
        periodUpdate.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if ( e.getSource() == autoCalib)
        {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to start auto calibration?",
                    "Auto Calibration",
                    JOptionPane.YES_NO_OPTION);
              if ( result == JOptionPane.YES_OPTION )
              {
                  nmea.autoCalibrate();
                  goodResult("Auto calibration started.");
              }
              else
              {
                  badResult("Auto calibration NOT restored.");
              }
        }
        else if ( e.getSource() == restoreDefault)
        {
            int result = JOptionPane.showConfirmDialog(this,
                  "Are you sure you want to restore factory default settings?",
                  "Restore Defaults",
                  JOptionPane.YES_NO_OPTION);
            if ( result == JOptionPane.YES_OPTION )
            {
                nmea.restoreDefaults();
                goodResult("Default settings restored.");
            }
            else
            {
                badResult("Default settings NOT restored.");
            }
        }
        else if ( e.getSource() == refWindDirection)
        {
            String result = JOptionPane.showInputDialog(this,
                    "Enter reference wind angle and hit ok?",
                    "Set reference wind angle",
                    JOptionPane.QUESTION_MESSAGE);
              if ( result != null && result.length() > 0 )
              {
                  try
                  {
                      nmea.setRefWindDir(Double.parseDouble(result));
                  }
                  catch (Exception ex)
                  {
                      badResult("Reference wind direction update failed :" 
                              + ex.getMessage());
                      return;
                  }
                  
                  goodResult("Reference wind direction set.");
              }
              else
              {
                  badResult("Reference wind direction aborted.");
                  return;
              }
        }
        else if ( e.getSource() == refWindSpeed)
        {
            String result = JOptionPane.showInputDialog(this,
                    "Enter reference wind speed and hit ok",
                    "Set reference wind speed",
                    JOptionPane.WARNING_MESSAGE);
              if ( result != null && result.length() > 0 )
              {
                  try
                  {
                      nmea.setRefWindSpeed(Double.parseDouble(result));
                  }
                  catch (Exception ex)
                  {
                      badResult("Reference wind speed update failed :" 
                              + ex.getMessage());
                      return;
                  }
                  
                  goodResult("Reference wind speed direction set.");
              }
              else
              {
                  badResult("Reference wind speed aborted.");
                  return;
              }
        }
        else if ( e.getSource() == windDirDampUpdate)
        {
            try
            {
                String str = windDirDampNew.getText();
                nmea.setWindDirDamping(Double.parseDouble(str));
            }
            catch (Exception ex)
            {
                badResult("Wind direction damping update failed :" 
                                  + ex.getMessage());
                return;
            }
            // All OK. Update the current value field
            windDirDampCurr.setText(windDirDampNew.getText());
            windDirDampNew.setText("");
        }
        else if ( e.getSource() == windSpeedDampUpdate)
        {
            try
            {
                String str = windSpeedDampNew.getText();
                nmea.setWindDirDamping(Double.parseDouble(str));
            }
            catch (Exception ex)
            {
                badResult("Wind speed damping update failed :" 
                                  + ex.getMessage());
                return;
            }
            // All OK. Update the current value field
            windSpeedDampCurr.setText(windSpeedDampNew.getText());
            windSpeedDampNew.setText("");
        }
        else if ( e.getSource() == windIntUpdate)
        {
            try
            {
                String str = windIntNew.getText();
                nmea.setWindSpeedInt(Double.parseDouble(str));
            }
            catch (Exception ex)
            {
                badResult("Wind speed integration period update " +
                                  "failed :" + ex.getMessage());
                return;
            }
            // All OK. Update the current value field
            windIntCurr.setText(windIntNew.getText());
            windIntNew.setText("");
        }
        else if ( e.getSource() == periodUpdate)
        {
            try
            {
                String str = periodNew.getText();
                nmea.setOutputPeriod(Double.parseDouble(str));
            }
            catch (Exception ex)
            {
                badResult("Period update failed :" + ex.getMessage());
                return;
            }
            // All OK. Update the current value field
            periodCurr.setText(periodNew.getText());
            periodNew.setText("");
        }
        else
        {
            badResult("Unknown even source in " + this.getName());
        }
    }
    
    private void goodResult(String str)
    {
        Font cf = infoField.getFont();
        Font nf = cf.deriveFont(Font.PLAIN, cf.getSize());
        infoField.setFont(nf);
        infoField.setForeground(Color.BLACK);
        infoField.setText(str);
    }

    private void badResult(String str)
    {
        Font cf = infoField.getFont();
        Font nf = cf.deriveFont(Font.BOLD, cf.getSize());
        infoField.setFont(nf);
        infoField.setForeground(Color.RED);
        infoField.setText(str);
    }

}

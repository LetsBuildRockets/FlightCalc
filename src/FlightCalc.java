import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

import java.io.FileWriter;
import java.io.IOException;


@SuppressWarnings("serial")
public class FlightCalc extends JFrame {

	private static final double DEFAULT_Cd = 0.50;			// Unitless
	private static final double DEFAULT_A = 0.008107;		// m^2
	private static final double DEFAULT_M0 = 60;			// kg
	private static final double DEFAULT_M_DOT = 0.1;		// kg/sec
	private static final double DEFAULT_F_Thrust = 1800;	// N
	private static final double DEFAULT_T_BURN = 10;		// sec
	

	private static final double T_INC = 0.1;				// sec
	private static final double T_MAX = 300;				// sec

	private static final double SEA_PRESSURE = 101.325; 	// kPa
	private static final double SEA_TEMPERATURE = 288.15;	// K
	private static final double GRAVITY = 9.80665;			// m/s^2
	private static final double TEMP_LAPSE = 0.0065;		// K/m
	private static final double GAS_CONSTANT = 8.31447; 	// J/(mol K)
	private static final double MOLAR_MASS_AIR = 0.0289644;	// kg/mol

    private JTextField textm0 = new JTextField(10);
    private JTextField textmdot = new JTextField(10);
    private JTextField textFt = new JTextField(10);
    private JTextField textCd = new JTextField(10);
    private JTextField textTburn = new JTextField(10);
    private JTextField textArea = new JTextField(10);
    private JButton calculate = new JButton("Calculate");
     
    public FlightCalc() {
        super("Flight Caclucator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
        // create a new panel with GridBagLayout manager
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new GridLayout(7, 2, 50, 10));

        newPanel.add(new JLabel("M0 (no fuel): "));
        newPanel.add(textm0);         
      
        newPanel.add(new JLabel("M dot: "));
        newPanel.add(textmdot);
        
        newPanel.add(new JLabel("F Thrust: "));
        newPanel.add(textFt);

        newPanel.add(new JLabel("T burn: "));
        newPanel.add(textTburn);

        newPanel.add(new JLabel("Cd: "));
        newPanel.add(textCd);

        newPanel.add(new JLabel("Area: "));
        newPanel.add(textArea);
         
       calculate.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			calculate();
		}
	});
             
        newPanel.add(calculate);
         
         
        // add the panel to this frame
        this.add(newPanel);
         
        this.pack();
        setLocationRelativeTo(null);
        
        fillDefaults();
    }
    
    /** throws an error if inputs look wrong in a non-obvious way
     * ex: if something is negative
     */
    private void checkInputs() {
	if(
	    Double.valueOf(textmdot.getText()) < 0 ||
	    Double.valueOf(textm0.getText()) < 0 ||
	    Double.valueOf(textFt.getText()) < 0 ||
	    Double.valueOf(textCd.getText()) < 0 ||
	    Double.valueOf(textTburn.getText()) < 0 ||
	    Double.valueOf(textArea.getText()) < 0
	) throw new IllegalArgumentException("an input's negative and that would be silly");
	
    }
    
    private void calculate() {
    	try {
		checkInputs();
        	FileWriter writer = new FileWriter("test.csv");
   		 
        	writer.append("Time");
    	    writer.append(',');
    	    writer.append("position");
    	    writer.append(',');
    	    writer.append("velocity");
    	    writer.append(',');
    	    writer.append("acceleration");
    	    writer.append(',');
    	    writer.append("Force");
    	    writer.append(',');
    	    writer.append("Mass");
    	    writer.append('\n');

    	    double lastpos = 0;
    	    double lastvel = 0;
    	    
    	    double maxAlt = 0;

    	    System.out.println(getThrust(0)/getWeight(getMass(0)));
	    	calculate:
    	    for(double t = 0; t < T_MAX; t+=T_INC){ 
    	    	double mass = getMass(t);
    	    	double force = getThrust(t) - getWeight(mass) - getFd(lastpos, lastvel);
    	    	double accel = force / mass;    	    	
    	    	double deltavel = accel * T_INC;
    	    	double vel = deltavel + lastvel;
    	    	double deltapos = vel * T_INC;
    	    	double pos = deltapos + lastpos;
    	    	
    	    	lastvel = vel;
    	    	lastpos = pos;
    	    	    	    	
    	    	if (pos > maxAlt) maxAlt = pos;
    	    	if (pos < 0) break calculate;
    	  

    	    	writer.append(String.valueOf(t));		// Time
    	    	writer.append(",");
    	    	writer.append(String.valueOf(pos));		// Position
    	    	writer.append(",");
    	    	writer.append(String.valueOf(vel));		// Velocity
    	    	writer.append(",");
    	    	writer.append(String.valueOf(accel));	// Acceleration
    	    	writer.append(",");
    	    	writer.append(String.valueOf(force));	// Force
    	    	writer.append(",");
    	    	writer.append(String.valueOf(mass));	//Mass
    	    	writer.append("\n");

    	    }
    			
    	    writer.flush();
    	    writer.close();
        	System.out.println(maxAlt);
    	}
    	catch(Exception e)
    	{
    	     e.printStackTrace();
		JOptionPane.showMessageDialog(null, e.getMessage(), "oops", JOptionPane.ERROR_MESSAGE);
    	}
    	
    }
    
    private double getMass(double time) {
    	if(time < Double.valueOf(textTburn.getText())) {
    		return (Double.valueOf(textm0.getText())+ Double.valueOf(textmdot.getText())*Double.valueOf(textTburn.getText())) - time * Double.valueOf(textmdot.getText());
    	} else {
    		return Double.valueOf(textm0.getText());
    	}
    }
    
    private double getThrust(double time) {
    	if(time < Double.valueOf(textTburn.getText())) {
    		return Double.valueOf(textFt.getText());
    	} else {
    		return 0;
    	}
    }
    
    private double getWeight(double mass) {
    	return mass * GRAVITY;
    }

    private double getFd(double altitude, double velocity) {
    	double Fd = .5 * getrho(altitude) * Double.valueOf(textArea.getText()) * Double.valueOf(textCd.getText()) * Math.pow(velocity,2);
    	return Fd;
    }
    
    private double getrho(double altitude) {
    	double temperature = gettemp(altitude);
    	double pressure = getpressure(altitude)*1000;
    	double density = (pressure * MOLAR_MASS_AIR) / (GAS_CONSTANT * temperature);
    	return density;
    }
    
    private double gettemp(double altitude) {
    	double temperature = SEA_TEMPERATURE - TEMP_LAPSE * altitude;
    	return temperature;
    }
    
    private double getpressure(double altitude) {
    	double pressure = SEA_PRESSURE * Math.pow(( 1 - (TEMP_LAPSE * altitude) / (SEA_TEMPERATURE)), (GRAVITY * MOLAR_MASS_AIR) / (GAS_CONSTANT * TEMP_LAPSE));
    	return pressure;
    }
    
    
    
    
    
    
    private void fillDefaults() {
    	textCd.setText(String.valueOf(DEFAULT_Cd));
    	textArea.setText(String.valueOf(DEFAULT_A));
    	textm0.setText(String.valueOf(DEFAULT_M0));
    	textmdot.setText(String.valueOf(DEFAULT_M_DOT));
    	textTburn.setText(String.valueOf(DEFAULT_T_BURN));
    	textFt.setText(String.valueOf(DEFAULT_F_Thrust));
    }
     
    public static void main(String[] args) {
        // set look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FlightCalc().setVisible(true);
            }
        });
    }
}
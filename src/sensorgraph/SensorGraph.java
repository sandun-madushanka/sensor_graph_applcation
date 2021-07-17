package sensorgraph;
//Import all Java libraries
import com.fazecast.jSerialComm.SerialPort;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
public class SensorGraph {
//Define all variables
static SerialPort chosenPort;
static int x = 1;
static int number[] = new int[769];
public static void main(String[] args) {
//Create and configure the window
JFrame window = new JFrame();
window.setTitle("HRS_Graph v1.0");
window.setSize(1300, 700);
window.setLayout(new BorderLayout());
window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//Create a drop-down box and connect button
//Then place them at the top of the window
JComboBox<String> portList = new JComboBox<>();
JButton connectButton = new JButton("Connect");
JButton aButton = new JButton("Start");
JButton eButton = new JButton("Save Data");
JPanel topPanel = new JPanel(new GridLayout(2, 20, 10, 8));
topPanel.add(portList);
topPanel.add(connectButton);
topPanel.add(aButton);
topPanel.add(eButton);
window.add(topPanel, BorderLayout.NORTH);
//Populate the drop-down box
SerialPort[] portNames = SerialPort.getCommPorts();
    for (SerialPort portName : portNames) {
        portList.addItem(portName.getSystemPortName());
    }
//Create the line graph
XYSeries series = new XYSeries("CCD Readings");
XYSeriesCollection dataset = new XYSeriesCollection(series);
JFreeChart chart = ChartFactory.createXYLineChart("", "Pixel Number",
"Intensity (A.U)", dataset);
window.add(new ChartPanel(chart), BorderLayout.CENTER);

//Configuration of Save data button
eButton.addActionListener((ActionEvent e) -> {
    if (connectButton.getText().equals("Connect")) {
        sensorgraph.MessageBx.infoBox("Please Connect to Device First", "ERROR");
    }
    else {
        if (eButton.getText().equals("Save Data")) {
            eButton.setText("Saving...");
            aButton.setEnabled(false);
            eButton.setEnabled(false);
            
            try {
                saveRecord();
                aButton.setEnabled(true);
                eButton.setEnabled(true);
//fButton.setEnabled(true);
eButton.setText("Save Data");
            } catch (Exception ex) {
            }}}
});

//Configuration of Start button
aButton.addActionListener((ActionEvent e) -> {
    if (connectButton.getText().equals("Connect")) {
        sensorgraph.MessageBx.infoBox("Please Connect to Device First", "ERROR");
    }
    else {
        if (aButton.getText().equals("Start")) {
            series.clear();
            x = 1;
            aButton.setText("Reading...");
            aButton.setEnabled(false);
            try {
                new Thread(() -> {
                    PrintWriter pw = new PrintWriter(chosenPort.getOutputStream());
                    String s = "A";
                    pw.print(s);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    pw.flush();
                    
                    Thread thread;
                    thread = new Thread() {
                        @Override
                        public void run() {
                            try (Scanner scanner = new Scanner(chosenPort.getInputStream())) {
                                while (scanner.hasNextLine() && x < 768) {
                                    try {
                                        String line = scanner.nextLine();
                                        number[x] = Integer.parseInt(line);
                                        //System.out.println(number[x]);
                                        series.add(x, number[x]);
                                        x++;
                                        window.repaint();
                                    } catch (NumberFormatException e) {
                                    }}
                                aButton.setEnabled(true);
                                aButton.setText("Start");
                            }
                        }};
                    thread.start();
                }).start();
            } catch (Exception ex) {
            }}}
});
//Configure the connect button and use another thread to listen for data
connectButton.addActionListener((ActionEvent arg0) -> {
    if (connectButton.getText().equals("Connect")) {
//Attempt to connect to the serial port
chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
chosenPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
if (chosenPort.openPort()) {
    connectButton.setText("Disconnect");
    portList.setEnabled(false);
}}
    else {
//Disconnect from the serial port
chosenPort.closePort();
portList.setEnabled(true);
connectButton.setText("Connect");
aButton.setText("Start");
aButton.setEnabled(true);
eButton.setEnabled(true);
series.clear();
x = 1;
    }
});
//Show the window
window.setVisible(true);
}
//Configuration of data record and save as "csv" file type
public static void saveRecord() {
try {
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
Date date = new Date();
String data = "Sensor Data " + sdf.format(date);
FileWriter fw = new FileWriter(data + ".csv", true);
BufferedWriter bw = new BufferedWriter(fw);
    try (PrintWriter pw = new PrintWriter(bw)) {
        pw.println("Wavelength (nm)" + "," + "Intnsity (A.U)");
        for (int i = 1; i < 768; i++) {
            pw.println(i + "," + number[i]);
            pw.flush();
        }   }
JOptionPane.showMessageDialog(null, "Data Saved");
} catch (HeadlessException | IOException E) {
JOptionPane.showMessageDialog(null, "Data not Saved");
}}}
class MessageBx {
public static void infoBox(String infoMessage, String titleBar) {
JOptionPane.showMessageDialog(null, infoMessage, titleBar,
JOptionPane.INFORMATION_MESSAGE);
}}
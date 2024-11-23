import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class MyFrame extends JFrame implements ActionListener{
    public boolean buttonclicked = false;
    GridBagConstraints cons = new GridBagConstraints();
    JTextField textField = new JTextField();
    JTextField client_port = new JTextField();
    JTextField server_port = new JTextField();
    JTextField server_ip = new JTextField();
    JButton clear = new JButton("Clear");
    JButton button = new JButton("Send");
    String text = "";
    JPanel leftpanel= new JPanel();
    JPanel toppanel = new JPanel();
    JPanel centerpanel = new JPanel();
    JPanel bottompanel = new JPanel();
    JPanel bottomprompt = new JPanel();
    JPanel bottominfo = new JPanel();
    JPanel bottommiddle = new JPanel();
    JPanel topleft = new JPanel();
    JPanel topright = new JPanel();
    JPanel rightpanel = new JPanel();
    JLabel MReceived = new JLabel("Message Log");
    JLabel SMessage = new JLabel("Selected Message");
    JLabel RightLabel = new JLabel("<html> <br/> Welcome to our P2PFS </html>");
    JLabel LeftLabel = new JLabel("");
    Hover hov = new Hover(RightLabel, textField);
    ArrayList<JLabel> alabel = new ArrayList<>();
    Font f35 = new Font("Consolas", Font.PLAIN, 35);
    Font f25 = new Font("Consolas", Font.PLAIN, 25);
    JScrollPane jsp = new JScrollPane(leftpanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    MyFrame(){
        MReceived.setFont(f35);
        SMessage.setFont(f35);
        RightLabel.setFont(f25);
        textField.setFont(f35);
        client_port.setFont(f35);
        server_ip.setFont(f35);
        server_port.setFont(f35);
        clear.setFont(f35);
        button.setFont(f35);

        toppanel.setPreferredSize(new Dimension(100, 40));
        bottompanel.setPreferredSize(new Dimension(100, 80));
        bottominfo.setPreferredSize(new Dimension(800, 40));
        bottomprompt.setPreferredSize(new Dimension(100, 40));
        bottommiddle.setPreferredSize(new Dimension(800, 40));
        centerpanel.setPreferredSize(new Dimension(1200, 600));
        textField.setPreferredSize(new Dimension(1000,40));
        client_port.setPreferredSize(new Dimension(200, 40));
        server_ip.setPreferredSize(new Dimension(100, 40));
        server_port.setPreferredSize(new Dimension(250, 40));
        clear.setPreferredSize(new Dimension(200, 40));

        toppanel.setLayout(new GridLayout(0,2));
        centerpanel.setLayout(new GridLayout(0,2));
        bottompanel.setLayout(new BorderLayout());
        bottominfo.setLayout(new BorderLayout());
        bottommiddle.setLayout(new BorderLayout());
        bottomprompt.setLayout(new BorderLayout());
        leftpanel.setLayout(new GridBagLayout());
        rightpanel.setLayout(new BorderLayout());

        toppanel.add(topleft);
        toppanel.add(topright);

        
        jsp.getVerticalScrollBar().setUnitIncrement(16);
        centerpanel.add(jsp);
        centerpanel.add(rightpanel);
        topleft.add(MReceived);
        topright.add(SMessage);
        rightpanel.add(RightLabel, BorderLayout.NORTH);
        bottompanel.add(bottominfo, BorderLayout.NORTH);
        bottompanel.add(bottomprompt, BorderLayout.SOUTH);
        bottommiddle.add(clear, BorderLayout.WEST);
        bottommiddle.add(server_ip, BorderLayout.CENTER);
        bottominfo.add(client_port, BorderLayout.WEST);
        bottominfo.add(bottommiddle, BorderLayout.CENTER);
        bottominfo.add(server_port, BorderLayout.EAST);
        bottomprompt.add(textField, BorderLayout.CENTER);
        bottomprompt.add(button, BorderLayout.EAST);

        button.addActionListener(this);
        button.setFocusable(false);
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == clear){
                    clearLeftLabel();
                }
                
            }
        });
        clear.setFocusable(false);

        textField.setForeground(Color.GRAY);
        client_port.setForeground(Color.GRAY);
        server_ip.setForeground(Color.GRAY);
        server_port.setForeground(Color.GRAY);
        clear.setForeground(Color.GRAY);

        textField.setText("Message to be sent");
        client_port.setText("UDP port");
        server_ip.setText("Server/Peer IP Address");
        server_port.setText("Server Port");
        

        textField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if(textField.getForeground() == Color.GRAY){
                    textField.setForeground(Color.BLACK);
                    textField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(textField.getText().isEmpty()){
                    textField.setForeground(Color.GRAY);
                    textField.setText("Message to be sent");
                }
            }
            
        });

        client_port.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if(client_port.getForeground() == Color.GRAY){
                    client_port.setForeground(Color.BLACK);
                    client_port.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(client_port.getText().isEmpty()){
                    client_port.setForeground(Color.GRAY);
                    client_port.setText("UDP Port");
                }else{
                    synchronized(Client.class){
                        Client.class.notify();
                    }
                    
                }
            }
            
        });

        server_ip.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if(server_ip.getForeground() == Color.GRAY){
                    server_ip.setForeground(Color.BLACK);
                    server_ip.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(server_ip.getText().isEmpty()){
                    server_ip.setForeground(Color.GRAY);
                    server_ip.setText("Server/Peer IP Address");
                }
            }
            
        });

        server_port.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if(server_port.getForeground() == Color.GRAY){
                    server_port.setForeground(Color.BLACK);
                    server_port.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(server_port.getText().isEmpty()){
                    server_port.setForeground(Color.GRAY);
                    server_port.setText("Server Port");
                }
            }
            
        });

        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.weighty = 1;
        cons.gridx = 0;
        cons.anchor = GridBagConstraints.NORTH;
        
        
        LeftLabel.setFont(f25);
        LeftLabel.addMouseListener(hov);
        leftpanel.add(LeftLabel, cons, 0);
        cons.weighty = 0;
        

        this.add(toppanel, BorderLayout.NORTH);
        this.add(centerpanel, BorderLayout.CENTER);
        this.add(bottompanel, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("P2PFS");
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        synchronized(Client.class){
            if(e.getSource()==button){
                buttonclicked = true;
                Client.class.notifyAll();
                text = textField.getText();
                addLeftLabel(textField.getText());
                textField.setText("");
                jsp.getVerticalScrollBar().setValue(0);
            }
        }
        
    }

    public void addLeftLabel(String text){
        JLabel t = new JLabel(text);
        t.setFont(f25);
        t.addMouseListener(hov);
        leftpanel.add(t,cons, 0);
        leftpanel.revalidate();
        leftpanel.repaint();
    }

    public void clearLeftLabel(){
        leftpanel.removeAll();
        leftpanel.revalidate();
        leftpanel.repaint();
    }
}
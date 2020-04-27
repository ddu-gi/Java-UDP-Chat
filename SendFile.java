import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class SendFile extends Frame implements ActionListener
{
  private TextField textfield_filename;
  private Button button_dialog, button_send, button_close;
  private Label loading_status;

  private String address;

  public SendFile(String address){
    super("��������");
    this.address = address;

    setLayout(null);

    Label lbl = new Label("�����̸�");
    lbl.setBounds(10, 30, 60, 20);
    add(lbl);

    textfield_filename = new TextField();
    textfield_filename.setBounds(80, 30, 160, 20);
    add(textfield_filename);

    button_dialog = new Button("ã�ƺ���");
    button_dialog.setBounds(45, 60, 60, 20);
    button_dialog.addActionListener(this);
    add(button_dialog);

    button_send = new Button("����");
    button_send.setBounds(115, 60, 40, 20);
    button_send.addActionListener(this);
    add(button_send);

    button_close = new Button("����");
    button_close.setBounds(165, 60, 40, 20);
    button_close.addActionListener(this);
    add(button_close);

    loading_status = new Label("�������� �����....");
    loading_status.setBounds(10, 90, 230, 20);
    loading_status.setBackground(Color.gray);
    loading_status.setForeground(Color.white);

    add(loading_status);

    addWindowListener(new WinListener());

    setSize(250, 130);
    show();
  }

  public void actionPerformed(ActionEvent e){
    if (e.getSource() ==button_dialog){
      FileDialog fd = new FileDialog(this, "���� ����", FileDialog.LOAD);
      fd.show();
      textfield_filename.setText(fd.getDirectory() + fd.getFile());
      if (textfield_filename.getText().startsWith("null"))
        textfield_filename.setText("");
    } else if(e.getSource() == button_send){
      String filename = textfield_filename.getText();

      if(filename.equals("")){
        loading_status.setText("�����̸��� �Է��ϼ���.");
        return;
      }
      
      loading_status.setText("���ϰ˻���..");

      File file = new File(filename);

      if (!file.exists()) {
        loading_status.setText("�ش������� ã�� �� �����ϴ�.");
        return;
      }

      StringBuffer buffer = new StringBuffer();
      int fileLength = (int) file.length();

      buffer.append(file.getName());
      buffer.append(":");
      buffer.append(fileLength);

      loading_status.setText("���ἳ����......");

      try{
        Socket sock = new Socket(address, 3777);
        FileInputStream fin = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(fin, fileLength);
        byte data[] = new byte[fileLength];
        try{
          loading_status.setText("������ ���� �ε���......");
          bin.read(data, 0, fileLength);
          bin.close();
        }catch(IOException err){
          loading_status.setText("�����б� ����.");
          return;
        }

        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        out.writeUTF(buffer.toString());

        textfield_filename.setText("");
        loading_status.setText("����������......( 0 Byte)");

        BufferedOutputStream bout = new BufferedOutputStream(out, 2048);
        DataInputStream din = new DataInputStream(sock.getInputStream());
        sendFile(bout, din, data, fileLength);
        bout.close();
        din.close();

        loading_status.setText(file.getName() + " ���������� �Ϸ�Ǿ����ϴ�.");
        sock.close();
      }catch(IOException e1){
        System.out.println(e1);
        loading_status.setText(address + "���� ���ῡ �����Ͽ����ϴ�.");
      }
    } else if(e.getSource() == button_close){
      dispose();
    }
  }

  private void sendFile(BufferedOutputStream bout, DataInputStream din, byte[] data, int fileLength)
    throws IOException{
    int size = 2048;
    int count = fileLength/size;
    int rest = fileLength%size;
    int flag = 1;

    if(count == 0) flag = 0;

    for(int i=0; i<=count; i++){
      if(i == count && flag == 0){
        bout.write(data, 0, rest);
        bout.flush();
        return;
      } else if(i == count){
        bout.write(data, i*size, rest);
        bout.flush();
        return;
      } else {
        bout.write(data, i*size, size);
        bout.flush();
        loading_status.setText("����������......(" + ((i+1)*size) + "/" + fileLength + " Byte)");
        din.readUTF();
      }
    }
  }

  class WinListener extends WindowAdapter
  {
    public void windowClosing(WindowEvent we){
      System.exit(0);
    }
  }
}

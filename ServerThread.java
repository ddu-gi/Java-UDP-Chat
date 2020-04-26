import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread
{
  private Socket severthread_sock;
  private DataInputStream severthread_in;
  private DataOutputStream severthread_out;
  private StringBuffer severthread_buffer;
  private WaitRoom severthread_waitRoom;
  public String severthread_ID;
  public int severthread_room_Number;
  private static final int WAITROOM = 0;

  private static final int �α��ο�û = 1001;
  private static final int �������û = 1011;
  private static final int �������û = 1021;
  private static final int �������û = 1031;
  private static final int �α׾ƿ���û = 1041;
  private static final int �۽��ڿ�û = 1051;
  private static final int �����ڿ�û = 1052;
  private static final int �ڽ���û = 1053;
  private static final int �������ۿ�û = 1061;

  private static final int �α��μ��� = 2001;
  private static final int �α��ΰ��� = 2002;
  private static final int ��������� = 2011;
  private static final int ��������� = 2012;
  private static final int ��������� = 2021;
  private static final int ��������� = 2022;
  private static final int ��������� = 2031;
  private static final int �α׾ƿ����� = 2041;
  private static final int �۽��ڼ��� = 2051;
  private static final int �����ڼ��� = 2052;
  private static final int �����ڰ��� = 2053;
  private static final int �ڽ���û���� = 2054;
  private static final int �������ۼ��� = 2061;
  private static final int �������۰��� = 2062;
  private static final int ����ڼ��� = 2003;
  private static final int ������������� = 2013;
  private static final int �����ڼ��� = 2023;

  private static final int �������_���� = 3001;
  private static final int ERR_SERVERFULL = 3002;
  private static final int ����ȭ = 3011;
  private static final int ����ڼ���ȭ = 3021;
  private static final int Ʋ����й�ȣ = 3022;
  private static final int �źε� = 3031;
  private static final int ����ھ��� = 3032;

  public ServerThread(Socket sock){
    try{
      severthread_sock = sock;
      severthread_in = new DataInputStream(sock.getInputStream());
      severthread_out = new DataOutputStream(sock.getOutputStream());
      severthread_buffer = new StringBuffer(2048);
      severthread_waitRoom = new WaitRoom();
    }catch(IOException e){
      System.out.println(e);
    }
  }

  private void sendErrCode(int message, int errCode) throws IOException{
    severthread_buffer.setLength(0);
    severthread_buffer.append(message);
    severthread_buffer.append(":");
    severthread_buffer.append(errCode);
    send(severthread_buffer.toString());
  }

  private void modifyWaitRoom() throws IOException{
    severthread_buffer.setLength(0);
    severthread_buffer.append(�������������);
    severthread_buffer.append(":");
    severthread_buffer.append(severthread_waitRoom.getWaitRoomInfo());
    broadcast(severthread_buffer.toString(), WAITROOM);
  }  
    
  private void modifyWaitUser() throws IOException{
    String ids = severthread_waitRoom.getUsers();
    severthread_buffer.setLength(0);
    severthread_buffer.append(����ڼ���);
    severthread_buffer.append(":");
    severthread_buffer.append(ids);
    broadcast(severthread_buffer.toString(), WAITROOM);
  }

  private void modifyRoomUser(int room_Number, String id, int code) throws IOException{
    String ids = severthread_waitRoom.getRoomInfo(room_Number);
    severthread_buffer.setLength(0);
    severthread_buffer.append(�����ڼ���);
    severthread_buffer.append(":");
    severthread_buffer.append(id);
    severthread_buffer.append(":");
    severthread_buffer.append(code);
    severthread_buffer.append(":");
    severthread_buffer.append(ids);
    broadcast(severthread_buffer.toString(), room_Number);
  }

  private void send(String sendData) throws IOException{
    synchronized(severthread_out){

      System.out.println(sendData);

      severthread_out.writeUTF(sendData);
      severthread_out.flush();
    }
  }

  private synchronized void broadcast(String sendData, int room_Number) throws IOException{
    ServerThread client;
    Hashtable clients = severthread_waitRoom.getClients(room_Number);
    Enumeration enu = clients.keys();
    while(enu.hasMoreElements()){
      client = (ServerThread) clients.get(enu.nextElement());
      client.send(sendData);
    }
  }
    
  public void run(){
    try{
      while(true){
        String recvData = severthread_in.readUTF();

        System.out.println(recvData);

        StringTokenizer st = new StringTokenizer(recvData, ":");
        int command = Integer.parseInt(st.nextToken());
        switch(command){
          case �α��ο�û : {
            severthread_room_Number = WAITROOM;
            int result;
            severthread_ID = st.nextToken();
            result = severthread_waitRoom.addUser(severthread_ID, this);
            severthread_buffer.setLength(0);
            if(result == 0){
              severthread_buffer.append(�α��μ���);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_waitRoom.getRooms());
              send(severthread_buffer.toString());
              modifyWaitUser();
              System.out.println(severthread_ID + "�� �����û ����");
            } else {
              sendErrCode(�α��ΰ���, result);
            }
            break;
          }
          case �������û : {
            String id, roomName, password;
            int roomMaxUser, result;
            boolean lock;

            id = st.nextToken();
            String roomInfo = st.nextToken();
            StringTokenizer room = new StringTokenizer(roomInfo, "'");
            roomName = room.nextToken();
            roomMaxUser = Integer.parseInt(room.nextToken());
            lock = (Integer.parseInt(room.nextToken()) == 0) ? false : true;
            password = room.nextToken();

            ChatRoom chatRoom = new ChatRoom(roomName, roomMaxUser,
            		lock, password, id);
            result = severthread_waitRoom.addRoom(chatRoom);
            if (result == 0) {
              severthread_room_Number = ChatRoom.getroom_Number();
              boolean temp = chatRoom.addUser(severthread_ID, this);
              severthread_waitRoom.delUser(severthread_ID);

              severthread_buffer.setLength(0);
              severthread_buffer.append(���������);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              send(severthread_buffer.toString());
              modifyWaitRoom();
              modifyRoomUser(severthread_room_Number, id, 1);
            } else {
              sendErrCode(���������, result);
            }
            break;
          }
          case �������û : {
            String id, password;
            int room_Number, result;
            id = st.nextToken();
            room_Number = Integer.parseInt(st.nextToken());
            try{
              password = st.nextToken();
            }catch(NoSuchElementException e){
              password = "0";
            }
            result = severthread_waitRoom.joinRoom(id, this, room_Number, password);

            if (result == 0){
              severthread_buffer.setLength(0);
              severthread_buffer.append(���������);
              severthread_buffer.append(":");
              severthread_buffer.append(room_Number);
              severthread_buffer.append(":");
              severthread_buffer.append(id);
              severthread_room_Number = room_Number;
              send(severthread_buffer.toString());
              modifyRoomUser(room_Number, id, 1);
              modifyWaitRoom();
            } else {
              sendErrCode(���������, result);
            }
            break;
          }
          case �������û : {
            String id;
            int room_Number;
            boolean updateWaitInfo;
            id = st.nextToken();
            room_Number = Integer.parseInt(st.nextToken());

            updateWaitInfo = severthread_waitRoom.quitRoom(id, room_Number, this);

            severthread_buffer.setLength(0);
            severthread_buffer.append(���������);
            severthread_buffer.append(":");
            severthread_buffer.append(id);
            send(severthread_buffer.toString());
            severthread_room_Number = WAITROOM;

            if (updateWaitInfo) {
              modifyWaitRoom();
            } else {
              modifyWaitRoom();
              modifyRoomUser(room_Number, id, 0);
            }
            break;
          }
          case �α׾ƿ���û : {
            String id = st.nextToken();
            severthread_waitRoom.delUser(id);

            severthread_buffer.setLength(0);
            severthread_buffer.append(�α׾ƿ�����);
            send(severthread_buffer.toString());
            modifyWaitUser();
            release();
            break;
          }
          case �۽��ڿ�û : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());

            severthread_buffer.setLength(0);
            severthread_buffer.append(�۽��ڼ���);
            severthread_buffer.append(":");
            severthread_buffer.append(id);
            severthread_buffer.append(":");
            severthread_buffer.append(severthread_room_Number);
            severthread_buffer.append(":");
            try{
              String data = st.nextToken();
              severthread_buffer.append(data);
            }catch(NoSuchElementException e){}

            broadcast(severthread_buffer.toString(), room_Number);
            break;
          }
          case �����ڿ�û : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken(); 
            
            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            if ((client = (ServerThread) room.get(idTo)) != null){            
              severthread_buffer.setLength(0);
              severthread_buffer.append(�����ڼ���);
              severthread_buffer.append(":");
              severthread_buffer.append(id);
              severthread_buffer.append(":");
              severthread_buffer.append(idTo);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              severthread_buffer.append(":");
              try{
                String data = st.nextToken();
                severthread_buffer.append(data);
              }catch(NoSuchElementException e){}
              client.send(severthread_buffer.toString());
              send(severthread_buffer.toString());
              break;
            } else {
              severthread_buffer.setLength(0);
              severthread_buffer.append(�����ڰ���);
              severthread_buffer.append(":");
              severthread_buffer.append(idTo);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              send(severthread_buffer.toString());
              break;
            }
          }
          case �������ۿ�û : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken(); 
            
            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            if ((client = (ServerThread) room.get(idTo)) != null){
              severthread_buffer.setLength(0);
              severthread_buffer.append(�������ۿ�û);
              severthread_buffer.append(":");
              severthread_buffer.append(id);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              client.send(severthread_buffer.toString());
              break;
            } else {
              severthread_buffer.setLength(0);
              severthread_buffer.append(�������۰���);
              severthread_buffer.append(":");
              severthread_buffer.append(����ھ���);
              severthread_buffer.append(":");
              severthread_buffer.append(idTo);
              send(severthread_buffer.toString());
              break;
            }
          }
          case �������۰��� : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken();

            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            client = (ServerThread) room.get(idTo);

            severthread_buffer.setLength(0);
            severthread_buffer.append(�������۰���);
            severthread_buffer.append(":");
            severthread_buffer.append(�źε�);
            severthread_buffer.append(":");
            severthread_buffer.append(id);

            client.send(severthread_buffer.toString());
            break;
          }
          case �������ۼ��� : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken();
            String hostaddr = st.nextToken();

            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            client = (ServerThread) room.get(idTo);

            severthread_buffer.setLength(0);
            severthread_buffer.append(�������ۼ���);
            severthread_buffer.append(":");
            severthread_buffer.append(id);
            severthread_buffer.append(":");
            severthread_buffer.append(hostaddr);

            client.send(severthread_buffer.toString());
            break;
          }
          case �ڽ���û : {
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken();
            boolean updateWaitInfo;
            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            client = (ServerThread) room.get(idTo);
            updateWaitInfo = severthread_waitRoom.quitRoom(idTo, room_Number, client);

            severthread_buffer.setLength(0);
            severthread_buffer.append(�ڽ���û����);
            client.send(severthread_buffer.toString());
            client.severthread_room_Number = 0;

            if (updateWaitInfo) {
              modifyWaitRoom();
            } else {
              modifyWaitRoom();
              modifyRoomUser(room_Number, idTo, 2);
            }
            break;
          }
        }
        Thread.sleep(100);
      }
    }catch(NullPointerException e){
    }catch(InterruptedException e){
      System.out.println(e);

      if(severthread_room_Number == 0){
        severthread_waitRoom.delUser(severthread_ID);
      } else {
        boolean temp = severthread_waitRoom.quitRoom(severthread_ID, severthread_room_Number, this);
        severthread_waitRoom.delUser(severthread_ID);
      } 
      release();
    }catch(IOException e){
      System.out.println(e);

      if(severthread_room_Number == 0){
        severthread_waitRoom.delUser(severthread_ID);
      } else {
        boolean temp = severthread_waitRoom.quitRoom(severthread_ID, severthread_room_Number, this);
        severthread_waitRoom.delUser(severthread_ID);
      } 
      release();
    }
  }

  public void release(){
    try{
      if(severthread_in != null) severthread_in.close();
    }catch(IOException e1){
    }finally{
      severthread_in = null;
    }
    try{
      if(severthread_out != null) severthread_out.close();
    }catch(IOException e1){
    }finally{
      severthread_out = null;
    }
    try{
      if(severthread_sock != null) severthread_sock.close();
    }catch(IOException e1){
    }finally{
      severthread_sock = null;
    }

    if(severthread_ID != null){
      System.out.println(severthread_ID + "�� ������ �����մϴ�.");
      severthread_ID = null;
    }
  }
}
            

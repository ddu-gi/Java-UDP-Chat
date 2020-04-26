import java.util.*;

class ChatRoom
{
  public static int room_Number = 0;
  private Vector ��������;
  private Hashtable �����ؽ�;
  private String Chatroom;
  private int �ִ����ڼ�;
  private int ����ڼ�;
  private boolean lock;
  private String password;
  private String admin;

  public ChatRoom(String Chatroom, int �ִ����ڼ�,
                  boolean lock, String password, String admin){
	  room_Number++;
    this.Chatroom = Chatroom;
    this.�ִ����ڼ� = �ִ����ڼ�;
    this.����ڼ� = 0;
    this.lock = lock;
    this.password = password;
    this.admin = admin;
    this.�������� = new Vector(�ִ����ڼ�);
    this.�����ؽ� = new Hashtable(�ִ����ڼ�);
  }

  public boolean addUser(String id, ServerThread client){
    if (����ڼ� == �ִ����ڼ�){
      return false;
    }
    ��������.addElement(id);
    �����ؽ�.put(id, client);
    ����ڼ�++;
    return true;
  }

  public boolean checkPassword(String passwd){
    return password.equals(passwd);
  }

  public boolean checkUserIDs(String id){
    Enumeration ids = ��������.elements();
    while(ids.hasMoreElements()){
      String tempId = (String) ids.nextElement();
      if (tempId.equals(id)) return true;
    }
    return false;
  }

  public boolean locked(){
    return lock;
  }

  public boolean delUser(String id){
	  ��������.removeElement(id);
	  �����ؽ�.remove(id);
	  ����ڼ�--;
    return ��������.isEmpty();
  }
      
  public synchronized String getUsers(){
    StringBuffer id = new StringBuffer();
    String ids;
    Enumeration enu = ��������.elements();
    while(enu.hasMoreElements()){
      id.append(enu.nextElement());
      id.append("'");
    }
    try{
      ids = new String(id);
      ids = ids.substring(0, ids.length() - 1);
    }catch(StringIndexOutOfBoundsException e){
      return "";
    }
    return ids;
  }

  public Hashtable getClients(){
    return �����ؽ�;
  }

  public String toString(){
    StringBuffer room = new StringBuffer();
    room.append(Chatroom);
    room.append(" = ");
    room.append(String.valueOf(����ڼ�));
    room.append(" = ");
    room.append(String.valueOf(�ִ����ڼ�));
    room.append(" = ");
    if (lock) {
      room.append("�����");
    } else {
      room.append("����");
    }
    room.append(" = ");
    room.append(admin);
    return room.toString();
  }

  public static synchronized int getroom_Number(){
    return room_Number;
  }
}

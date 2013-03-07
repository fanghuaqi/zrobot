package org.hustcse.wifirobot;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class tcp_ctrl_client extends Thread {
	private String TAG = "TCP_CTRL_CLIENT";
	public boolean D = true;

	/* TCP服务器对应的IP和端口 */
	public String IP = "192.168.1.100";
	public int PORT = 1234;

	final static int receive_pool_size = 1024;
	final static int MSG_DATA_REC = 1;
	final static int MSG_DISPLAY_TOAST = 100;
	final static int MSG_FIX_PREFERENCE = 1000;
	final static int FIX_IP_PREFERENCE = 0;

	/* 发送最大尝试次数 */
	final static int MAX_TRIES = 100;
	
	final static int RW_TIMEOUT = 2000;
	final static int TCP_CON_TOUT = 3000;

	final static short no_ack = 0;
	final static short tcp_ack = 1;
	final static short udp_ack = 2;

	/* 用于更新UI显示以及将该类的消息发送到主Activity中 */
	Handler mHandler = null; 
	/* 接收主Activity的上下文 */
	Context mContext;
	/* 用于接收和发送的socket */
	Socket clientSocket; 
	
	InetSocketAddress clentSocketAddr;
	
	/* 尝试连接 */
	boolean tcp_connect_try = false;

	/* sockect 是否准备OK */
	private boolean socketOK = false; 
	
	OutputStream socket_output;
	InputStream socket_input;

	InetAddress myBcastIPAddress; // my broadcast IP addresses
	InetAddress myIPAddress; // my IP addresses

	InetAddress tcpserver_Addr = null;

	/* 接收和发送tcp消息的队列
	 * 这样实现的主要目的防止消息数据丢失 */
	LinkedList<byte[]> tcp_send_msg_queue;
	LinkedList<byte[]> tcp_rec_msg_queue;
	
	/* 发送和接收到的数据 */
	byte[] tcp_msg_send;
	byte[] tcp_msg_receive;
	
	/* 是否需要接收和发送数据 */
	boolean msg_send_available = false;
	boolean msg_rec_available = false;
	
	/* tcp接收的消息计数 */
	int tcp_msg_rec_cnt;
	
	short tcp_ctrl_prefix;
	short tcp_ack_type;
	/* 记录当前尝试的次数 */
	int max_tries = 0;
	receive_msg_handle rec_msg_handle;

	public tcp_ctrl_client(Context currentContext, Handler handler, String ip,
			int port) {
		mHandler = handler;
		mContext = currentContext;

		try {
			tcp_send_msg_queue = new LinkedList<byte[]>();
			tcp_rec_msg_queue = new LinkedList<byte[]>();
			rec_msg_handle = new receive_msg_handle(mHandler);
			rec_msg_handle.start();
			PORT = port;

			try {
				tcpserver_Addr = InetAddress.getByName(ip);
				IP = ip;
			} catch (Exception e) {
				String msg = new String(ip
						+ " is not the right ip format! TCP ip use " + IP);
				mHandler.obtainMessage(MSG_DISPLAY_TOAST, msg).sendToTarget();
				mHandler.obtainMessage(
						(MSG_FIX_PREFERENCE + FIX_IP_PREFERENCE), IP)
						.sendToTarget();
			}
			socketOK = false;
		} catch (Exception e) {
			if (D){
				Log.d(TAG, "tcp init fail");
			}
			socketOK = false;
		}
		tcp_connect_try = false;
	}

	public boolean tcp_connect(boolean forceflag) {
		if ((forceflag) || (!tcp_connect_try)) {
			try { /* close previous socket */
				if (socketOK) {
					socketOK = false;
					clientSocket.close();
				}
			} catch (Exception e) {
				if (D)
					Log.e(TAG, "TCP stop error:" + e.getMessage());
			}
			try {
				/* create new socket */
				tcpserver_Addr = InetAddress.getByName(IP);
				clientSocket = new Socket(tcpserver_Addr, PORT);
				clientSocket.setSoTimeout(RW_TIMEOUT);
				socket_output = clientSocket.getOutputStream();
				socket_input = clientSocket.getInputStream();
				if (D){
					Log.d(TAG, "Connect to Server @" + IP + ":" + PORT);
				}
				String msg = new String("Connect to TCP Server @" + IP + ":"
						+ PORT);
				if (mHandler != null) {
					mHandler.obtainMessage(MSG_DISPLAY_TOAST, msg)
							.sendToTarget();
				}

				socketOK = true;
				if (!this.isAlive()) {
					start();
				}
			} catch (Exception e) {
				socketOK = false;
				String msg = new String("Can't Connect to Server @" + IP + ":"
						+ PORT);
				if (mHandler != null) {
					mHandler.obtainMessage(MSG_DISPLAY_TOAST, msg)
							.sendToTarget();
				}
				if (D){
					Log.e(TAG,"TCP client connect to server error:"
									+ e.getMessage());
				}
			}
		}
		tcp_connect_try = true;
		return socketOK;
	}

	/* if ip or port updated return true */
	public boolean updateIPandPort(String ip, int port) {
		boolean flag = false;
		try {
			if (PORT != port) {
				flag = true;
				PORT = port;
			}
			tcpserver_Addr = InetAddress.getByName(ip);
			if ((ip != null) && !(IP.equals(ip))) {
				flag = true;
				IP = ip;
			}
		} catch (Exception e) {
			String msg = new String(ip
					+ " is not the right ip format! TCP ip use " + IP);
			mHandler.obtainMessage(MSG_DISPLAY_TOAST, msg).sendToTarget();
			mHandler.obtainMessage((MSG_FIX_PREFERENCE + FIX_IP_PREFERENCE), IP)
					.sendToTarget();
		}
		return flag;
	}

	boolean tcpreconnect(String ip, int port) {
		try {
			InetAddress tcpserver_Addr = InetAddress.getByName(ip);
			clientSocket = new Socket(tcpserver_Addr, port);
			clientSocket.setSoTimeout(RW_TIMEOUT);
			socket_output = clientSocket.getOutputStream();
			socket_input = clientSocket.getInputStream();

			socketOK = true;

			if (D){
				Log.d(TAG, "Connect to Server @" + ip + ":" + port);
			}
			disp_toast("Connect to Server @" + ip + ":" + port);

			IP = ip;
			PORT = port;

		} catch (Exception e) {
			socketOK = false;
			disp_toast("Can't Connect to Server @" + ip + ":" + port);
			if (D){
				Log.e(TAG,"TCP client connect to server error:" + e.getMessage());
			}
		}
		return socketOK;
	}

	boolean isSocketOK() {
		return socketOK;
	}

	private void disp_toast(String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	/* 当连接建立好以后 将TCP消息加入到发送队列 */
	public synchronized void post_msg(byte[] msg) {
		if (!socketOK) {
			disp_toast("TCP Socket Client Is not ready!");
			return;
		}
		/* 将消息添加到等待发送的数据队列中 */
		tcp_send_msg_queue.add(msg); 
		msg_send_available = true;
		notifyAll();
	}

	/* 尝试获取队列最前面的消息数据 */
	private synchronized byte[] get_msg() {
		byte[] msg;
		
		/* 返回队列最前面的消息，如果没有消息就返回null */
		msg = tcp_send_msg_queue.peek(); 
		if (msg == null) {
			msg_send_available = false;
			notifyAll();
		}
		return msg;
	}

	/* 移除队列最前的消息 */
	private synchronized byte[] remove_msg() {
		byte[] msg;

		try {
			/* 返回队列最前面的消息，如果没有消息就返回null */
			msg = tcp_send_msg_queue.remove(); 
			if ((tcp_send_msg_queue.peek()) == null) {
				msg_send_available = false;
				notifyAll();
			}
			return msg;
		} catch (Exception e) {
			msg_send_available = false;
			notifyAll();
			return null;
		}
	}

	/* 等待是否有数据待发送 */
	private synchronized void waitMsgSndAvailable() throws InterruptedException {
		while (!msg_send_available) {
			wait();
		}
	}

	/* 将接收到的消息添加到队列中 */
	private synchronized void add_receive_msg(byte[] msg) {
		tcp_rec_msg_queue.add(msg);
		msg_rec_available = true;
		notifyAll();
	}

	/* 获取队列最前面的消息 */
	private synchronized byte[] get_receive_msg() {
		byte[] msg;
		try {
			msg = tcp_rec_msg_queue.remove();
			if (tcp_rec_msg_queue.peek() == null) {
				msg_rec_available = false;
				notifyAll();
			}
			return msg;
		} catch (Exception e) {
			msg_rec_available = false;
			notifyAll();
			return null;
		}
	}

	private synchronized void waitMsgRecAvailable() throws InterruptedException {
		while (!msg_rec_available) {
			wait();
		}
	}

	/* 获取消息的类型 */
	private short get_msg_type(byte ctrl_prefix) {
		return (short) ((ctrl_prefix >> 4) & 0xf);
	}

	/* 检查消息的ACK类型 */
	private short check_ack_type(byte ctrlprefix) {

		short ctrl_prefix = get_msg_type(ctrlprefix);

		short data_request = (short) ((ctrl_prefix & (ctrl_prefixs.data_request_mask)) >> (ctrl_prefixs.data_request_offset));
		short ack = (short) ((ctrl_prefix & (ctrl_prefixs.ack_mask)) >> (ctrl_prefixs.ack_offset));
		short ack_type;

		if ((ack) == ctrl_prefixs.withoutack) {
			ack_type = no_ack;
		} else {
			if (data_request == ctrl_prefixs.less_data_request) {
				ack_type = tcp_ack;
			} else {
				ack_type = udp_ack;
			}
		}
		return ack_type;
	}

	@Override
	public void run() {
		try {
			while (socketOK) {
				try {
					/* 等待客户端待发送的消息到来 */
					waitMsgSndAvailable();
					tcp_msg_send = get_msg();
					if (tcp_msg_send == null) {
						if (D){
							Log.d(TAG, "Send Null Msg");
						}
						continue; /* notice here */
					}
					socket_output.write(tcp_msg_send);
					tcp_ack_type = check_ack_type(tcp_msg_send[0]);
					if (tcp_ack_type == tcp_ack) { /* 如果需要TCP返回数据 */
						/* TODO 个人觉得这种实现方式不好 需要进一步修正  */
						try {
							while ((tcp_msg_rec_cnt = socket_input.available()) == 0);

							if (tcp_msg_rec_cnt >= 4) {
								tcp_msg_receive = new byte[tcp_msg_rec_cnt];
								socket_input.read(tcp_msg_receive);
								byte[] rec_frame_head = new byte[2];
								byte[] snd_frame_head = new byte[2];
								System.arraycopy(tcp_msg_receive, 0,
										rec_frame_head, 0, 2);
								System.arraycopy(tcp_msg_send, 0,
										snd_frame_head, 0, 2);
								if ((snd_frame_head[0] == rec_frame_head[0])
										&& (snd_frame_head[1] == rec_frame_head[1])) {
									remove_msg();
									add_receive_msg(tcp_msg_receive);
									max_tries = 0;
								} else {
									max_tries++;
									if (D){
										Log.e(TAG, "Receive Failed "+ max_tries);
									}
								}
							} else {
								max_tries++;
							}
						} catch (Exception e) {
							if (D){
								Log.e(TAG, "Receive timeout");
							}
							max_tries++;
						}
						if (max_tries > MAX_TRIES) { /* 重试次数过多 */
							max_tries = 0;
							/* 清除本次失败消息，并开始下一个任务 */
							remove_msg(); 
							disp_toast("消息重试次数过多！取消本次发送命令");
						}
					} else {
						remove_msg();
					}
				} catch (Exception e) {
					disp_toast("Some Error Happened in TCP client!");
				}
			}
		} catch (Exception e) {
			if (D){
				Log.e(TAG, e.getMessage());
			}
		} finally {
			try {
				if (clientSocket != null) {
					if (!clientSocket.isClosed()) {
						clientSocket.close();
						if (D){
							Log.i(TAG, "Close Client Socket Success!");
						}
					}
				}
			} catch (Exception e2) {
				if (D){
					Log.e(TAG, "Close Client Socket Error");
				}
			}
		}
	}

	/* TODO 功能实现不完善
	 * 接收TCP数据的处理类 */
	class receive_msg_handle extends Thread {
		Handler rec_Handler;
		byte[] rec_msg;

		public receive_msg_handle(Handler handler) {
			rec_Handler = handler;
		}

		@Override
		public void run() {
			try {
				while (true) {
					waitMsgRecAvailable();
					rec_msg = get_receive_msg();
					if (rec_msg != null) {
						rec_Handler.obtainMessage(MSG_DATA_REC, rec_msg)
								.sendToTarget();
					} else {
						if (D){
							Log.d(TAG, "Receive Null Msg");
						}
					}
				}
			} catch (Exception e) {
				if (D){
					Log.d(TAG,"TCP client receive msg handle error:" + e.getMessage());
				}					
			}

		}
	}

}

                                               README
                                               ======

Author: Fang Huaqi
Date: 2013-03-07 13:01:48 CST


Table of Contents
=================
1 APP说明
2 APP自定义的通讯协议
3 APP中使用到的一些控制命令说明


1 APP说明 
----------
  本android app是由华中科技大学瑞萨实验室方华启(hustfang890312@gmail.com)开发,本app开发的目的是为了
  控制一辆在使用XILINX ZYNQ系列处理器的ZEDBOARD(www.zedboard.org)上搭建起来的智能移动机器人平台,简称
  ZRobot. 本APP和ZROBOT之间主要采用无线网络的方式来进行通讯.

  本APP和ZRobot配合使用可以提供如下功能:
  1. 用户可以根据ZRobot端提供的TCP服务器的IP和端口来设置APP待连接的IP和端口 (menu->settings->(Host
     IP, TCP Server Port))
  2. 提供三种视频源的配置(menu->settings->(Car/ARM/OpenCV Video Address))
  3. 提供切换ZRobot运行模式的切换(Auto:自动避障 Manual:手动摇杆控制)
  4. 提供控制ZRobot激光开关控制(LASER ON:打开激光 LASER OFF:关闭激光)
  5. 支持获取来自三个视频源的视频,可以通过Video Start来打开获取视频,Video Stop来结束获取视频.
  6. 支持动态切换视频源(CAR/ARM/OPENCV),可以参看2来设置视频源地址
  7. 支持显示ZRobot当前的状态(Robot Online/Offline)
  8. 用户打开软件后需要自己Connect到ZRobot端的服务器,通过屏幕上的Connect按钮完成
  9. 屏幕左下方的摇杆用于ZRobot的转向角度和运行速度的控制
  10. 屏幕右下方的摇杆用于ZRobot的机械臂的运动坐标控制

2 APP自定义的通讯协议 
----------------------
  为了方便和ZRobot进行良好稳定的通讯,这里制定了一个简单的协议, 采用TCP方式进行实现. 这里采用TCP的方
  式原因是由于TCP建立的是可靠的连接,而UDP建立的是不可靠的连接. 但是TCP的传输速率较UDP慢许多,关于更详
  细TCP和UDP的知识可以自行搜索.

  这里定义通讯协议(参见ctrl_frame.java)如下
  控制指令编码(2Bytes)+数据长度(2Bytes)+数据(长度不定)
  
  1. 控制指令编码格式 
  高4位表示 控制前缀
  低12位表示 控制命令

  控制前缀格式(参见ctrl_prefixs.java)为
    位数     3      2       1                   0  
    功能   ACK   保留   读/写   请求数据量(大/小)  

    功能名称            功能对应描述   
    ACK                 NACK:0, ACK:1  
    读写                写:0, 读:1     
    请求数据量(大/小)   少:0, 多:1     

  本APP还未对有ACK的命令进行很好的实现, 这里只发出NOACK命令的数据,并且请求数据量只能设置为少(0),请求
  多的数据量推荐使用UDP进行数据的返回.
  2. 数据长度
  数据长度为为后面跟着的数据内容的字节数
  3. 数据
  这里的数据用户可以根据自己的需求来定义数据中的内容分别代表什么含义

  例如对于本APP的控制ZRobot转向方向和速度的摇杆命令而言
  1). 命令前缀 为 0000b = 0x0  (NOACK, 写, 少量数据)
  2). 控制命令 为 0x7

  因此控制字为 (0x012) + 0x07 = 0x7

  3). 数据(长度不定)应该为 角度(2字节)+速度(2字节)
  
  数据长度为4 因为共有四个字节的数据

  角度和速度封包方式
  msg[0] = (byte)(angle & 0xff);
  msg[1] = (byte)((angle  8) & 0xff);
  msg[2] = (byte)(speed & 0xff);
  msg[3] = (byte)((speed >> 8) & 0xff);

3 APP中使用到的一些控制命令说明 
--------------------------------
  由于在本APP中所有的命令均不需要ACK即NOACK,并且请求的数据量为少,所以控制前缀为0. 
  并且这里所有的命令都是写命令(0)
  关于控制命令这里请参见ctrlcmds.java和WifiRobotActicity.java中的代码实现

  本APP中使用到的控制命令全部如下:
    命令用途                                 命令名称                  命令编码   数据长度   数据内容及格式                                 
    左摇杆控制ZRobot转向角度以及行进速度     OPERATE_CAR                      7          4   angle(2字节)(-90~+90) speed(2字节)(-50~+50)    
    进入真实操控模式                         ENTER_REAL_CONTROL_MODE         10          0   无数据                                         
    进入自动避障模式                         ENTER_AUTO_NAV_MODE             11          0   无速度                                         
    右摇杆控制ZRobot的机械臂的运动到的坐标   OPERATE_ARM                     13          4   x坐标(2字节) (-90-+90) y坐标(2字节) (-90~+90)  
    切换视频模式                             ADJUST_VIDEO_MODE               14          1   视频模式(1字节) CAR 0, ARM 1, OPENCV 2         
    控制激光开启或者关闭                     LASER_CTRL                      15          1   激光开关状态(1字节) OFF 0,ON 1                 


#!/bin/sh
kill -9 `ps ax | grep mjpg_streamer  | grep -v grep | awk '{print $1}'`
#kill -9 `ps ax | grep main_plant_recognition_ZYNQ.o  | grep -v grep | awk '{print $1}'`
echo " "
echo "Close mjpeg_streamer to release CAMERA to Face detection Demo!"
echo "------press \"Ctrl\" + \"C\" to exit face detection  Demo-----"
#/root/Demo_ZedBoard/leaf_recognition/main_plant_recognition_ZYNQ.o &
/root/Demo_ZedBoard/face_detection/face_detection.o

#!/bin/sh
kill -9 `ps ax | grep main_plant_recognition_ZYNQ.o  | grep -v grep | awk '{print $1}'`
kill -9 `ps ax | grep mjpeg_streamer | grep -v grep | awk '{print $1}'`
echo "Close leaf_recognition to release camera to web-camera Demo!"
/root/boa/boa &
/root/webcam/mjpg_streamer -i "input_uvc.so -y -f 24 -r 640*360 -q 60" "output_http.so -p 8080 -w /var/www" &

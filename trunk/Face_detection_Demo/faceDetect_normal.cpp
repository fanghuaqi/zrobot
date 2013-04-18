/*
 Project:		Face Detection
 Author:		Jerry Peng
 Created on:	Feb 20,2013
*/

//#include <opencv.hpp>
#include "opencv2/opencv.hpp"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>
#include <sys/stat.h>
#include <iostream>

using namespace std;
using namespace cv;

/**Function Headers*/
void detectAndDisplay( Mat frame );

/**Global variables*/
String face_cascade_name = "haarcascade_frontalface_alt2.xml";
CascadeClassifier face_cascade;

  int main(int argc, const char** argv)
 {
	VideoCapture capture(0);
	Mat frame;
	//-- 1. Load the cascades
	if( !face_cascade.load( face_cascade_name ) ){  
		cout << "--(!)Error loading\n"; 
		return -1; 
	}
	//-- 2. Read the video stream
	if( !capture.isOpened() ){
		cout << "Could not initialize capturing...\n";
        return -1;
    }
	//-- 3. Apply the classifier to the frame
	while( true ){
		capture>>frame;
		
		if( !frame.empty() ){
			Mat Qframe;
			resize(frame, Qframe, Size(320,240), CV_INTER_LINEAR);
			detectAndDisplay( Qframe );
			Qframe.release();
		}
		else
		{ printf(" --(!) No captured frame -- Break!"); break; }
	}
	//-- 4. Release
	capture.release();
	frame.release();
	return 0;
}


 /**@function detectAndDisplay*/
void detectAndDisplay( Mat frame )
{
	double scale = 1.3;
	Mat Sframe;
	Mat frame_gray;


	std::vector<Rect> faces;

	cvtColor( frame, frame_gray, CV_BGR2GRAY );
	resize(frame_gray, Sframe, Size(frame_gray.cols/scale,frame_gray.rows/scale),CV_INTER_LINEAR);
	equalizeHist( Sframe, Sframe );

	//-- Detect faces
	face_cascade.detectMultiScale( Sframe, faces, 1.2, 2, 0|CV_HAAR_SCALE_IMAGE, Size(30, 30) );

	//-- Draw faces areas
	for( int i = 0; i < faces.size(); i++ )
	{
	Point center( (faces[i].x + faces[i].width*0.5)*scale, (faces[i].y + faces[i].height*0.5)*scale );
	ellipse( frame, center, Size( faces[i].width*0.5*scale, faces[i].height*0.5*scale), 0, 0, 360, Scalar( 0, 255, 0 ), 4, 8, 0);
	}

	//-- Release
	frame_gray.release();
	Sframe.release();
	//-- Write image
	imwrite("/var/www/detection.bmp", frame);
//	system("convert /var/www/detection.bmp /var/www/detection.jpg");  //conver bmp image to jpg format

}

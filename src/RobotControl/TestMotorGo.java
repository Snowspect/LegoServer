package RobotControl;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;
import java.lang.*;
import java.net.Socket;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;


public class TestMotorGo extends KeyAdapter{

	static UnregulatedMotor left; 
	static UnregulatedMotor right; 
	
	    public static void main(String[] args) throws IOException
	    {
	    	String ip = "192.168.43.199";
	    	if(args.length > 0) 
	    	{ 
	    		ip = args[0]; 
	    	} 
	    	Socket sock = new Socket(ip, Server.port); 
	    	System.out.println(sock); 
	    	System.out.println("HELLLOOO"); 
	    	InputStream in = sock.getInputStream(); 
	    	DataInputStream recieveData = new DataInputStream(in); 
	    	String str = recieveData.readUTF(); 
	    	System.out.println(str); 
	    	
	    	/*
	    	left = new UnregulatedMotor(MotorPort.A); 
	    	right = new UnregulatedMotor(MotorPort.B); 
	    	
	    	left.setPower(50); 
	    	left.forward(); 
	    	
	    	while(Button.ENTER.isUp()) 
	    	{ 
	    		 
	    	}*/ 
	    }
	}
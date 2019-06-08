	import lejos.hardware.BrickFinder;
	import lejos.hardware.Button;
	import lejos.hardware.Key;
	import lejos.hardware.Sound;
	import lejos.hardware.ev3.LocalEV3;
	import lejos.hardware.lcd.Font;
	import lejos.hardware.lcd.GraphicsLCD;
	import lejos.hardware.motor.BaseRegulatedMotor;
	import lejos.hardware.motor.EV3LargeRegulatedMotor;
	import lejos.hardware.motor.UnregulatedMotor;
	import lejos.hardware.port.MotorPort;
	import lejos.hardware.port.Port;
	import lejos.hardware.port.SensorPort;
	import lejos.hardware.sensor.EV3IRSensor;
	import lejos.hardware.sensor.SensorMode;
	import lejos.robotics.RegulatedMotor;
	import lejos.utility.Delay;

public class OldCode {
		    public static void main(String[] args)
		    {
		    	UnregulatedMotor left = new UnregulatedMotor(MotorPort.A);
		    	UnregulatedMotor right = new UnregulatedMotor(MotorPort.B);
		    	UnregulatedMotor mini = new UnregulatedMotor(MotorPort.C);
		    	
		    	
		    	right.setPower(100);
		    	left.setPower(100);
		    	mini.setPower(100);
		    	
		    	
		    	left.forward();
		    	right.backward();
		    
		    	
		    	
		    	
		    	while(Button.ENTER.isUp()){
		    		//System.out.println(right.getPower());
			    	Button.waitForAnyPress();
			    	if(Button.RIGHT.isDown()){
			    		if(right.getPower() == 100) {
			    			right.setPower(0);
			    		}else {
			    			right.setPower(100);
			    		}
			    		
			    	}
			    	
		    	}
		    	
		    	
		    	//RegulatedMotor left2 = new EV3LargeRegulatedMotor(MotorPort.B);
		    	//System.out.println("hello");
		        //System.out.println("Running...");
		        GraphicsLCD g = BrickFinder.getDefault().getGraphicsLCD();
		        final int SW = g.getWidth();
		        final int SH = g.getHeight();
		        Button.LEDPattern(4);
		       // Sound.beepSequenceUp();
		        
		        g.setFont(Font.getLargeFont());
		        g.drawString("leJOS/EV3", SW/2, SH/2, GraphicsLCD.BASELINE|GraphicsLCD.HCENTER);
		        Button.LEDPattern(3);
		        Delay.msDelay(4000);
		        Button.LEDPattern(5);
		        g.clear();
		        g.refresh();
		      //  Sound.beepSequence();
		        Delay.msDelay(500);
		        Button.LEDPattern(0);
		    }
}

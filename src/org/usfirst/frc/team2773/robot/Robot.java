package org.usfirst.frc.team2773.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 * 
 * @author Darren Midkiff
 * @author Luke Senseney
 * @author Zane Fry
 * @author Stephen Paredes
 * @author Peter Senseney
 * @author Kate Kersgieter
 */
public class Robot extends IterativeRobot {
	RobotDrive drive;
	Joystick drivingStick;
	Jaguar elevator;
	Timer timer;
	DigitalInput leftIR,rightIR;
	AnalogInput limitL, limitR, sonar, encoder;
	double stopTime=0.0;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override public void robotInit() {
		drive = new RobotDrive(0, 1, 2, 3);
		elevator = new Jaguar(4);
		drivingStick = new Joystick(0);
		leftIR=new DigitalInput(0);
		rightIR=new DigitalInput(1);
		timer = new Timer();
		sonar=new AnalogInput(0);
		encoder=new AnalogInput(1);
		limitL=new AnalogInput(2);
		limitR=new AnalogInput(3);
	}
	
	@Override public void autonomousInit()
	{
		drive.setSafetyEnabled(false);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override public void autonomousPeriodic() {
		try{
		// Grabs first and second tote, then moves to third tote position
		for (int i = 0; i < 1; i++) {

			SmartDashboard.putString("Autonomous State:", "grabbing tote");
			grabTote();
			SmartDashboard.putString("Autonomous State:", "//drive back");
			driveTest(0,-.25,0,0);
			Timer.delay(1);
			SmartDashboard.putString("Autonomous State:", "//drive right");
			driveTest(.5, 0, 0, 0);
			Timer.delay(1);
			SmartDashboard.putString("Autonomous State:", "//drive right quarter speed until rightIR");
			driveTest(.25, 0, 0, 0);
			while(rightIR.get());
			SmartDashboard.putString("Autonomous State:", "//drives forward until limit pushed");
			driveTest(0,.175, 0, 0);
			while(limitL.getValue()<2000 || limitR.getValue()<2000);
			SmartDashboard.putString("Autonomous State:", "line up");
			lineUp(0);
		}
		// Grabs third tote and drives into auto zone
		grabTote();
		driveTest(0, -1, 0, 0);
		//Timer.delay(3);
		driveTest(0, 0, 0, 0);
		elevator.set(-1);
		Timer.delay(6);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override public void teleopInit()
	{
		drive.setSafetyEnabled(false);
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override public void teleopPeriodic() {
		SmartDashboard.putString("Autonomous State:", "Not");
		SmartDashboard.putNumber("LimitL",limitL.getValue());
		SmartDashboard.putNumber("LimitR",limitR.getValue());
		SmartDashboard.putBoolean("Right IR:", rightIR.get());
		SmartDashboard.putBoolean("Left IR:", leftIR.get());
		
		driveTest(drivingStick.getX(), -drivingStick.getY(), drivingStick.getZ()*.25, 0);
		
		switch(drivingStick.getPOV(0))
		{
			case -1:
				driveTest(drivingStick.getX(), -drivingStick.getY(), drivingStick.getZ()*.25, 0);
				break;
			case 0:
				driveTest(0,.5,0,0);
				break;
			case 45:
				driveTest(.5,.5,0,0);
				break;
			case 90:
				driveTest(.5,0,0,0);
				break;
			case 135:
				driveTest(.5,-.5,0,0);
				break;
			case 180:
				driveTest(0,-.5,0,0);
				break;
			case 225:
				driveTest(-.5,-.5,0,0);
				break;
			case 270:
				driveTest(-.5,0,0,0);
				break;
			case 315:
				driveTest(-.5, .5, 0,0);
				break;
			
		}
		if(drivingStick.getRawButton(2))
		{
			elevator.set(1);
			stopTime +=2;
			timer.start();
		}
		if(timer.get()>stopTime)
		{
			timer.stop();
			elevator.set(0);
			timer.reset();
			stopTime = 0;
		}
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override public void testPeriodic() {
			driveTest(0, .25, 0, 0);
			Timer.delay(3);
			driveTest(0,0,0,0);
			Timer.delay(3);
	}

	/**
	 * This function is used to retrieve a tote and activate the elevator system
	 * @throws InterruptedException 
	 */
	public void grabTote() throws InterruptedException {
		elevator.set(1);
		// grab tote
		Timer.delay(2);
		elevator.set(0);
		// release tote
		// elevate tote
	}
    
	public void lineUp(double forwardSpeed)
	{
		//strafes while advancing until tote is centered
		while(!(rightIR.get() && leftIR.get()))
		{
			if(!rightIR.get())
				driveTest(.0625,forwardSpeed,0,0);
			if(!leftIR.get())
				driveTest(-.0625,forwardSpeed,0,0);
		}
		driveTest(0,0,0,0);
		//turns and moves forward slightly until both limits are pressed
		while(!(limitL.getValue()>2000 && limitR.getValue()>2000))
		{
			if(limitL.getValue()<2000)
			{
				driveTest(0,.1,.25,0);
				Timer.delay(.5);
				driveTest(0,0,0,0);
			}
			if(limitR.getValue()<2000)
			{
				driveTest(0,.1,-.25,0);
				Timer.delay(.5);
				driveTest(0,0,0,0);
			}
		}
	}
	
	public void driveTest(double x,double y,double rot,double dummy)
	{
		drive.mecanumDrive_Cartesian(-y,-x,rot,0);
	}
}

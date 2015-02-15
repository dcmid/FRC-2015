package org.usfirst.frc.team2773.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
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
	/**
	 * Inverted so they return false when they see something
	 */
	DigitalInput leftIR,rightIR;
	AnalogInput limitL, limitR, sonar;
	Encoder elevatorEncoder;
	Thread elevatorThread;
	Solenoid brake;
	Solenoid mast;
	Solenoid container;
	boolean buttonPushed=false;
	int totesGrabbed = 0;
	/** 
	 * Increment this value by how long you want the elater to lift.
	 */
	int stopCount=0;
	CameraServer camServer;

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
		elevatorEncoder=new Encoder(2,3);
		sonar=new AnalogInput(0);
		limitL=new AnalogInput(1);
		limitR=new AnalogInput(3);
		brake=new Solenoid(0);
		mast=new Solenoid(1);
		container=new Solenoid(2);
		camServer=CameraServer.getInstance();
		camServer.setQuality(50);
		camServer.startAutomaticCapture("cam0");
		elevatorThread=new Thread(new Runnable()
		{
			@Override public void run()
			{
				while(true)
				{
					if(isAutonomous()||isOperatorControl())
					{
						//drive elevator up if stopCount is greater than current encoder value
						if(stopCount>elevatorEncoder.get())
						{
							brake.set(false);
							elevator.set(1);
							SmartDashboard.putString("Elevator State:","Lifting");
						}
						//drive elevator down if stopCount is more than 10 less than current value
						else if(stopCount-elevatorEncoder.get()<-10)
						{
							SmartDashboard.putString("Elevator State", "Dropping");
							brake.set(false);
							elevator.set(-1);
						}
						else
						{
							elevator.set(0);
							brake.set(true);
							SmartDashboard.putString("Elevator State:","Stopped");
						}
					}
					else
					{
						elevator.set(0);
						brake.set(true);
						SmartDashboard.putString("Elevator State:","Inactive");
					}
					Timer.delay(.01);
				}
			}
		});
		elevatorThread.start();
	}
	
	@Override public void autonomousInit()
	{
		drive.setSafetyEnabled(false);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override public void autonomousPeriodic() {
		// Grabs first and second tote, then moves to third tote position
		for (int i = 0; i < 1; i++) {
			SmartDashboard.putString("Autonomous State:", "grabbing tote");
			grabTote();
			container.set(true);
			SmartDashboard.putString("Autonomous State:", "//drive back");
			drive(0,-.5,0,0);
			Timer.delay(.5);
			SmartDashboard.putString("Autonomous State:", "//drive right");
			drive(.3, 0, 0, 0);
			Timer.delay(.75);
			SmartDashboard.putString("Autonomous State:", "//drive right quarter speed until rightIR");
			drive(.5, 0, 0, 0);
			while(rightIR.get());
			SmartDashboard.putString("Autonomous State:", "line up");
			lineUp();
		}
		// Grabs third tote and drives into auto zone
		SmartDashboard.putString("Autonomous State:","Finishing");
		grabTote();
		drive(0, -1, 0, 0);
		//Timer.delay(3);
		dropTotes();
		container.set(false);
		drive(0, -.5, 0, 0);
		Timer.delay(0.25);
		drive(0,0,0,0);
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
		SmartDashboard.putBoolean("Right IR:", rightIR.get());
		SmartDashboard.putBoolean("Left IR:", leftIR.get());
		SmartDashboard.putNumber("Encoder",elevatorEncoder.get());
		//Sets the robot speed to the direction on the POV, or if none, sets to joystick value
		switch(drivingStick.getPOV(0))
		{
			case -1:
				drive(drivingStick.getX(), -drivingStick.getY(), drivingStick.getZ()*.25, 0);
				break;
			case 0:
				drive(0,.5,0,0);
				break;
			case 45:
				drive(.5,.5,0,0);
				break;
			case 90:
				drive(.5,0,0,0);
				break;
			case 135:
				drive(.5,-.5,0,0);
				break;
			case 180:
				drive(0,-.5,0,0);
				break;
			case 225:
				drive(-.5,-.5,0,0);
				break;
			case 270:
				drive(-.5,0,0,0);
				break;
			case 315:
				drive(-.5, .5, 0,0);
				break;
			
		}
		//If the driver has pressed the elevator button, add 2 seconds to time to lift.
		if(drivingStick.getRawButton(1))
		{
			dropTotes();
		}
		else if(drivingStick.getRawButton(2))
		{
			if(!buttonPushed)
			{
				grabTote();
			}
			buttonPushed=true;
		}else
			buttonPushed=false;
		if(drivingStick.getRawButton(3))
			elevator.set(-1);
		else if(drivingStick.getRawButton(5))
			elevator.set(1);
		else if(elevator.get()!=0)
			elevator.set(0);
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override public void testPeriodic() {
			drive(0, .25, 0, 0);
			Timer.delay(3);
			drive(0,0,0,0);
			Timer.delay(3);
	}

	/**
	 * This function is used to retrieve a tote and activate the elevator system
	 * @throws InterruptedException 
	 */
	public void grabTote() {
		stopCount+=200;
		totesGrabbed ++;
		if(totesGrabbed>3)
			mast.set(true);
	}
	
	public void dropTotes()
	{
		//stop robot
		drive(0,0,0,0);
		mast.set(false);
		//lower elevator part way. allow 3 seconds to back off totes
		stopCount-=50;
		Timer.delay(3);
		//lower elevator to base
		stopCount=0;
		totesGrabbed = 0;
	}
    
	/**
	 * This method moves the robot forward while lining up and rotates to be lined up. Only to be called once the IR sensors can see the 
	 * tote.
	 * @param forwardSpeed
	 */
	public void lineUp()
	{
		//strafes while advancing until tote is centered
		while(!limitL()&&!limitR())
			if(!rightIR.get())
			{
				drive(.0625,.25,0,0);
				SmartDashboard.putString("Line Up State:","Moving right and forward");
			}
			else if(!leftIR.get())
			{
				drive(-.0625,.25,0,0);
				SmartDashboard.putString("Line Up State:","Moving left and forward");
			}
			else{
				drive(0,.25,0,0);
				SmartDashboard.putString("Line Up State:","Moving forward");
			}
		//executes until both IR see nothing and both limits switches are pressed.
		while(!rightIR.get()||!leftIR.get()||!limitL()||!limitR())
		{
			while(!rightIR.get()||!leftIR.get())
				if(!rightIR.get())
				{
					drive(.125,0,0,0);
					SmartDashboard.putString("Line Up State:","Moving right");
				}
				else if(!leftIR.get())
				{
					drive(-.125,0,0,0);
					SmartDashboard.putString("Line Up State:","Moving left");
				}
			while(!limitL()||!limitR())
			{
				SmartDashboard.putNumber("LimitL",limitL.getValue());
				SmartDashboard.putNumber("LimitR",limitR.getValue());
				if(!limitL())
				{
					drive(0,.1,.125,0);
					SmartDashboard.putString("Line Up State:","Rotating clockwise");
				}else if(!limitR())
				{
					drive(0,.1,-.125,0);
					SmartDashboard.putString("Line Up State:","Rotating counterclockwise");
				}else
				{
					drive(0,.1,0,0);
					SmartDashboard.putString("Line Up State:","Drive forward");
				}
			}
		}
		drive(0,0,0,0);
	}
	
	//Flips the driving to drive the practice bot because the wheels arn't on the right side.
	public void drive(double x,double y,double rot,double dummy)
	{
		drive.mecanumDrive_Cartesian(-y,-x,rot,0);
	}
	
	public boolean limitL()
	{
		if(limitL.getValue()>2000)
		{
			return true;
		} 
		else
		{
			return false;
		}
	}
	
	public boolean limitR()
	{
		if(limitR.getValue()>700)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

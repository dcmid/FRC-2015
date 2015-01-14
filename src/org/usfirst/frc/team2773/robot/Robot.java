
package org.usfirst.frc.team2773.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.IterativeRobot;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
	RobotDrive drive;
	Joystick drivingStick;
	
    public void robotInit() {
    	
    	drive = new RobotDrive(0, 1, 2, 3);
    	drivingStick = new Joystick(0);
    	

    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        //Grabs first and second tote, then moves to third tote position
        for(int i=0; i<2; i++){    
            grabTote();
            drive.mecanumDrive_Cartesian(1, 0, 0, 0);
            try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            drive.mecanumDrive_Cartesian(.25, 0, 0, 0);
            while(!seesTote())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            drive.mecanumDrive_Cartesian(0, 0, 0, 0);
        }
        //Grabs third tote and drives into auto zone
        grabTote();
        drive.mecanumDrive_Cartesian(0, -1, 0, 0);
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        drive.mecanumDrive_Cartesian(0, 0, 0, 0);
        /*
        Unload tote code to be written
        */


    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	drive.mecanumDrive_Cartesian(drivingStick.getX(), drivingStick.getY(), drivingStick.getZ(), 0);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
    /**
     * This function is used to retrieve a tote and activate the elevator system
     */
    public void grabTote(){
        //actuate piston
        //grab tote
        //reverse piston
        //release tote
    }

    /**
    *  This function uses vision magic to check for a tote in front of the robot
    */
    public boolean seesTote(){
        //vision magic
        return true;
    }
    
}

package org.firstinspires.ftc.teamcode.ftc11587;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
/*TODO: import IMU libraries*/

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Locale;

@Autonomous(name:"Auto_ColorBallOnly_Blue", group "Relic Recovery")
@Disabled

public class AutonomousDrive_Linear extends LinearOpMode {

	/*Motor Declarations*/
	
	DcMotor lfmotor = null;
	DcMotor rfmotor = null;
	DcMotor lrmotor = null;
	DcMotor rrmotor = null;
	
	DcMotor armBaseMotor = null;
	DcMotor firstArmMotor = null;
	DcMotor secondArmMotor = null;
	
	/*TODO: Add claw control motor/servo once design finalized*/
	
	/*Color-Distance Sensor Declarations*/
	ColorSensor sensorColor;
	DistanceSensor sensorDistance;
	
	/*IMU Sensor Declarations*/
	/*TODO: Add IMU components*/
	
	@Override
	public void runOpMode() {
		
		//Telemetry to indicate OpMode initialization
		telemetry.addData("Status","Autonomous_ColorBallOnly_Blue initialized.");
		telemetry.update();
		
		/*Hardware mapping pulls the motor names from the configuration on the robot-side controller phone*/
		lfMotor = hardwareMap.dcMotor.get("lfmotor");
		rfMotor = hardwareMap.dcMotor.get("rfmotor");
		lrMotor = hardwareMap.dcMotor.get("lrmotor");
		rrMotor = hardwareMap.dcMotor.get("rrmotor");

		/*Quickly change motor polarity, if needed, by changing FORWARD to REVERSE*/
		lfMotor.setDirection(DcMotor.Direction.FORWARD);
		rfMotor.setDirection(DcMotor.Direction.FORWARD);
		lrMotor.setDirection(DcMotor.Direction.FORWARD);
		rrMotor.setDirection(DcMotor.Direction.FORWARD);
		
		/*Hardware mapping for arm motors*/
		armBaseMotor = hardwareMap.dcMotor.get("armbasemotor");
		firstArmMotor = harwareMap.dcMotor.get("firstarmmotor");
		secondArmMotor = hardwareMap.dcMotor.get("secondarmmotor");

		/*Change motor polarity for arm motors - 1st/2nd should be opposite each other*/
		firstArmMotor.setDirection(DcMotor.Direction.FORWARD);
		secondArmMotor.setDirection(DcMotor.Direction.REVERSE);
		
		/*Hardware mapping for sensorColor - NOTE: device name will be same for color & distance sensors*/
		sensorColor = hardwareMap.get(ColorSensor.class, "sensorCD");
		
		/*Hardware mapping for sensorDistance*/
		sensorDistance = hardwareMap.get(DistanceSensor.class, "sensorCD");
		
		/*TODO: Map the IMU hardware*/
		
		/*Create arrays to hold HSV data*/
		float hsvValues[] = {0F, 0F, 0F};
		final float values[] = hsvValues;
		final double SCALE_FACTOR = 255;	//Scale values to amplify measured values
		
		/*Relative Layout reference enables changing the background color of the app*/
		int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
		final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
		
		waitForStart();
		
		while (opModeIsActive()) {
			
			/*Access sensorColor and convert the RGB detected values into HSV w/ scalar*/
			Color.RGBToHSV((int) (sensorColor.red * SCALE_FACTOR), (int) (sensorColor.green * SCALE_FACTOR), (int) (sensorColor.blue * SCALE_FACTOR, hsvValues);
			
			/*Extend the robot arm toward the wall*/
			/*TODO: write robot arm extension code based on motor/servo configuration*/
			
			/*Perform the color sample routine*/
			/*When distance = sensor-to-claw tip distance + 2cm, stop extension*/
			
			/*Send detected color values to Driver Station via telemetry*/
			telemetry.addData("Distance(cm): ",String.format(Locale.US, "%.02f", sensorDistance.getDistance(DistanceUnit.CM)));
			telemetry.addData("Alpha: ",sensorColor.alpha());
			telemetry.addData("Red: ",sensorColor.red());
			telemetry.addData("Green: ",sensorColor.green());
			telemetry.addData("Blue: ",sensorColor.blue());
			telemetry.addData("Hue: ",hsvValues[0]);
			
			/*Change the app background color to match the color detected by sensorColor*/
			relativeLayout.post(new Runnable() {
				public void run() {
					relativeLayout.setBackgroundColor(Color.HSVToColor(0xff, values));
				}
			});
			telemetry.update();
			
			/*Pivot the robot 5 degrees to the left and sample color*/
			
			/*Pivot the robot 10 degrees in the other direction and sample color*/
			
			/*Determine which color is on which side while returning the robot to center*/
			if sensorColor.red * SCALE_FACTOR /*range of acceptable values for RED based on testing*/
			else if sensorColor.red * SCALE_FACTOR /*outside of acceptable range for RED*/
			
			/*Determine if RED is left or right side*/
			
			/*Extend the robot arm between the balls and knock the RED alliance ball off the pedestal*/
			
			/*Retract arm to stowed position*/			
		}
		relativeLayout.post(new Runnable() {
			public void run() {
				relativeLayout.setBackgroundColor(Color.WHITE);
			}
		});
	}
}

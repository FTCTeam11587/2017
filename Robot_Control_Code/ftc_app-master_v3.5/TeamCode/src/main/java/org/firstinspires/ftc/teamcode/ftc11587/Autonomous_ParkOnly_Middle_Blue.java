package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Locale;

/*TODO: import IMU libraries*/

@Autonomous(name="Auto_ParkOnly_Middle_Blue", group="Relic Recovery")
//@Disabled

public class Autonomous_ParkOnly_Middle_Blue extends LinearOpMode {

    /*Constants*/

    static final double MM_INCH_CONVERSION = 25.4;

    static final double WHEEL_DIAMETER = 4.0;

    static final double HEXCOUNTS_DEGREE = (288/360);         				//Hex motor counts per degree of motor rotation
	static final double HEXCOUNTS_INCH = (288/(Math.PI * WHEEL_DIAMETER));	//Hex motor counts per inch of wheel rotation (not used)

	static final double HDCOUNTS_DEGREE = (1120/360);		  				//HD motor counts per degree of motor rotation
	static final double HDCOUNTS_INCH = (1120/(Math.PI * WHEEL_DIAMETER));	//HD motor counts per inch of wheel rotation


	/*Motor Declarations*/
	
	DcMotor lfMotor = null;
	DcMotor rfMotor = null;
	DcMotor lrMotor = null;
	DcMotor rrMotor = null;
	
	DcMotor armBaseMotor = null;
	DcMotor firstArmMotor = null;
	DcMotor secondArmMotor = null;
	DcMotor levelingMotor = null;
	
	/*Servo Declarations*/
	Servo clawServo = null;
	
	/*Color-Distance Sensor Declarations*/
	ColorSensor sensorColor;
	DistanceSensor sensorDistance;
	
	/*IMU Sensor Declarations*/
	/*TODO: Add IMU components*/

	@Override
	public void runOpMode() {
		
		//Telemetry to indicate OpMode initialization
		telemetry.addData("Status","Autonomous_ParkOnly_Middle_Blue initialized.");
		telemetry.update();
		
		/*Hardware mapping pulls the motor names from the configuration on the robot-side controller phone*/
		lfMotor = hardwareMap.dcMotor.get("lfmotor");
		lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		lfMotor.setDirection(DcMotor.Direction.FORWARD);


		rfMotor = hardwareMap.dcMotor.get("rfmotor");
		rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		rfMotor.setDirection(DcMotor.Direction.FORWARD);

		lrMotor = hardwareMap.dcMotor.get("lrmotor");
		lrMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		lrMotor.setDirection(DcMotor.Direction.FORWARD);


		rrMotor = hardwareMap.dcMotor.get("rrmotor");
		rrMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		rrMotor.setDirection(DcMotor.Direction.FORWARD);

		/*Hardware mapping for arm motors*/
		armBaseMotor = hardwareMap.dcMotor.get("armbasemotor");
		armBaseMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		armBaseMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armBaseMotor.setDirection(DcMotorSimple.Direction.FORWARD);

		firstArmMotor = hardwareMap.dcMotor.get("firstarmmotor");
		firstArmMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		firstArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		firstArmMotor.setDirection(DcMotorSimple.Direction.FORWARD);

		secondArmMotor = hardwareMap.dcMotor.get("secondarmmotor");
		secondArmMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		secondArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		secondArmMotor.setDirection(DcMotorSimple.Direction.FORWARD);

		levelingMotor = hardwareMap.dcMotor.get("leveling");
		levelingMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		levelingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		levelingMotor.setDirection(DcMotorSimple.Direction.FORWARD);

		/*Hardware mapping for claw servos*/
		clawServo = hardwareMap.servo.get("claw");
		clawServo.scaleRange(0.2,0.8);				//TODO: Adjust this to keep claw from over-tightening

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

		/*Zeroize motors on init*/
		armBaseMotor.setPower(0);
		armBaseMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
		armBaseMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		firstArmMotor.setPower(0);
		firstArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
		firstArmMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		secondArmMotor.setPower(0);
		secondArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
		secondArmMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		levelingMotor.setPower(0);
		levelingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
		levelingMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		
		while (opModeIsActive()) {

			//Move 30 inches to the left
            lfMotor.setTargetPosition((int)Math.round(30 * HDCOUNTS_INCH));		//TODO: may need to adjust for holonomic force vector
			rfMotor.setTargetPosition((int)Math.round(30 * HDCOUNTS_INCH));
			lrMotor.setTargetPosition((int)Math.round(30 * HDCOUNTS_INCH));
			rrMotor.setTargetPosition((int)Math.round(30 * HDCOUNTS_INCH));

			/*Move robot to edge safe zone*/
			/*

			lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			lfMotor.setDirection(DcMotorSimple.Direction.FORWARD);

			rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			rfMotor.setDirection(DcMotorSimple.Direction.REVERSE);

			lrMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			lrMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			lrMotor.setDirection(DcMotorSimple.Direction.FORWARD);

			rrMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			rrMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			rrMotor.setDirection(DcMotorSimple.Direction.REVERSE);

			lfMotor.setTargetPosition((int)Math.round(24 * HDCOUNTS_INCH)); //TODO: May need to adjust for holonomic force vector
			rfMotor.setTargetPosition((int)Math.round(24 * HDCOUNTS_INCH));
			lrMotor.setTargetPosition((int)Math.round(24 * HDCOUNTS_INCH));
			rrMotor.setTargetPosition((int)Math.round(24 * HDCOUNTS_INCH));

			lfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
			rfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
			lrMotor.setDirection(DcMotorSimple.Direction.REVERSE);
			rrMotor.setDirection(DcMotorSimple.Direction.REVERSE);


			lfMotor.setTargetPosition((int)Math.round(12 * HDCOUNTS_INCH)); //TODO: May need to adjust for holonomic force vector
			rfMotor.setTargetPosition((int)Math.round(12 * HDCOUNTS_INCH));
			lrMotor.setTargetPosition((int)Math.round(12 * HDCOUNTS_INCH));
			rrMotor.setTargetPosition((int)Math.round(12 * HDCOUNTS_INCH));

			*/
		}
	}
}

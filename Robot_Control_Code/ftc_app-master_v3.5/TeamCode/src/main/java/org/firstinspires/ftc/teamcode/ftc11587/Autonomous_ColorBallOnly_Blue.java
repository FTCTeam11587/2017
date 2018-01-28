package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
/*TODO: import IMU libraries*/

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Locale;

@Autonomous(name="Auto_ColorBallOnly_Blue", group="Relic Recovery")
//@Disabled

public class Autonomous_ColorBallOnly_Blue extends LinearOpMode {

    /*Constants*/

    static final double MM_INCH_CONVERSION = 25.4;
    static final double WHEEL_DIAMETER = 4.0;
    static final double HEXCOUNTS_DEGREE = 0.8;         //Counts per degree of motor rotation
	static final double DEGREES_INCH = 28.66;
	static final double HEXCOUNTS_INCH = Math.PI*WHEEL_DIAMETER;


    static final double ARM1_LENGTH = 304.8;            //In mm
    static final double ARM2_LENGTH = 304.8;            //In mm
    static final double ARM3_LENGTH = 304.8;            //In mm
    static final double BALANCE_HEIGHT = 19.05;         //In mm
    static final double WHEELBASE_ARMBASE_CTR = 90.93;  //In mm
    static final double WRIST_CLAW_LENGTH = 177.8;      //In mm

	//Motor Declarations
	
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
		telemetry.addData("Status","Autonomous_ColorBallOnly_Blue initialized.");
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
			
			/*Access sensorColor and convert the RGB detected values into HSV w/ scalar*/
			Color.RGBToHSV((int) (sensorColor.red() * SCALE_FACTOR), (int) (sensorColor.green() * (SCALE_FACTOR)), (int) (sensorColor.blue() * SCALE_FACTOR), hsvValues);
			
			/*Extend the robot arm toward the wall*/

			armBaseMotor.setPower(0);
			armBaseMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
			armBaseMotor.setTargetPosition(0);

			firstArmMotor.setPower(.5);
			firstArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
			firstArmMotor.setTargetPosition((int)Math.round(30*HEXCOUNTS_DEGREE));	//Extend the first motor to 30 degrees

			secondArmMotor.setPower(0.5);
			secondArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
			secondArmMotor.setTargetPosition((int)Math.round(180*HEXCOUNTS_DEGREE));	//Extend the second motor to 180 degrees (TODO: Adjust this parameter)

			levelingMotor.setPower(0.5);
			levelingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
			levelingMotor.setTargetPosition((int)Math.round(15*HEXCOUNTS_DEGREE));	//TODO: adjust to get level claw
			
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
			lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			lfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
			lfMotor.setTargetPosition((int)Math.round(5*HEXCOUNTS_DEGREE));
			lfMotor.setPower(0.1);

			rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			rfMotor.setDirection(DcMotorSimple.Direction.FORWARD);
			rfMotor.setTargetPosition((int)Math.round(5*HEXCOUNTS_DEGREE));
			rfMotor.setPower(0.1);

			/*Determine which color is on which side while returning the robot to center*/
			if (sensorColor.red() * SCALE_FACTOR > (sensorColor.blue()*2)) {
				/*Actions if the ball is red*/

				/*Move the claw down to the red ball*/
			    levelingMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
			    levelingMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			    levelingMotor.setDirection(DcMotorSimple.Direction.FORWARD);
			    levelingMotor.setTargetPosition((int)Math.round(10*HEXCOUNTS_DEGREE));

			    /*Pivot the robot 15 degrees to the left to unseat the ball*/
				lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				lfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
				lfMotor.setTargetPosition((int)Math.round(15*HEXCOUNTS_DEGREE));
				lfMotor.setPower(0.1);

				rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				rfMotor.setDirection(DcMotorSimple.Direction.FORWARD);
				rfMotor.setTargetPosition((int)Math.round(15*HEXCOUNTS_DEGREE));
				rfMotor.setPower(0.1);

				telemetry.addLine("The ball is RED");
			    telemetry.update();

			    /*Retract arm to stowed position*/
				armBaseMotor.setTargetPosition(0);
				firstArmMotor.setTargetPosition(0);
				secondArmMotor.setTargetPosition(0);
				levelingMotor.setTargetPosition(0);

				/*Recenter on balance pad*/
				/*Pivot the robot 20 degrees to the right to recenter*/
				lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				lfMotor.setDirection(DcMotorSimple.Direction.FORWARD);
				lfMotor.setTargetPosition((int)Math.round(20*HEXCOUNTS_DEGREE));
				lfMotor.setPower(0.1);

				rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				rfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
				rfMotor.setTargetPosition((int)Math.round(20*HEXCOUNTS_DEGREE));
				rfMotor.setPower(0.1);
            }

			else if (sensorColor.blue() * SCALE_FACTOR > (sensorColor.red()*2)) {
				/*Actions if the ball is blue*/

				/*Pivot the robot 5 degrees to the right of center*/
				lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				lfMotor.setDirection(DcMotorSimple.Direction.FORWARD);
				lfMotor.setTargetPosition((int)Math.round(10*HEXCOUNTS_DEGREE));
				lfMotor.setPower(0.1);

				rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				rfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
				rfMotor.setTargetPosition((int)Math.round(10*HEXCOUNTS_DEGREE));
				rfMotor.setPower(0.1);

				/*Move the claw down to the red ball*/
				levelingMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				levelingMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				levelingMotor.setDirection(DcMotorSimple.Direction.FORWARD);
				levelingMotor.setTargetPosition((int)Math.round(10*HEXCOUNTS_DEGREE));

			    /*Pivot the robot 15 degrees to the left to unseat the ball*/
				lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				lfMotor.setDirection(DcMotorSimple.Direction.FORWARD);
				lfMotor.setTargetPosition((int)Math.round(15*HEXCOUNTS_DEGREE));
				lfMotor.setPower(0.1);

				rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				rfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
				rfMotor.setTargetPosition((int)Math.round(15*HEXCOUNTS_DEGREE));
				rfMotor.setPower(0.1);

			    telemetry.addLine("The ball is BLUE");
			    telemetry.update();

				/*Retract arm to stowed position*/
				armBaseMotor.setTargetPosition(0);
				firstArmMotor.setTargetPosition(0);
				secondArmMotor.setTargetPosition(0);
				levelingMotor.setTargetPosition(0);

				/*Recenter on balance pad*/
				/*Pivot the robot 20 degrees to the left to recenter*/
				lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				lfMotor.setDirection(DcMotorSimple.Direction.REVERSE);
				lfMotor.setTargetPosition((int)Math.round(20*HEXCOUNTS_DEGREE));
				lfMotor.setPower(0.1);

				rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
				rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
				rfMotor.setDirection(DcMotorSimple.Direction.FORWARD);
				rfMotor.setTargetPosition((int)Math.round(20*HEXCOUNTS_DEGREE));
				rfMotor.setPower(0.1);
            }

            else {
			    telemetry.addLine("I don't know what color the ball is!");
			    telemetry.update();
            }

            /*Move to robot safe zone*/

		}
		relativeLayout.post(new Runnable() {
			public void run() {
				relativeLayout.setBackgroundColor(Color.WHITE);
			}
		});
	}
}

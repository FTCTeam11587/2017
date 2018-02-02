package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="Relic Recovery: Basic Holonomic Drive", group="Relic Recovery")
//@Disabled //comment out this line to add this OpMode to the Driver Station select list

public class Teleop_Holonomic_Drive_Basic extends LinearOpMode {

	/*Declarations*/
	DcMotor lfMotor = null;
	DcMotor rfMotor = null;
	DcMotor lrMotor = null;
	DcMotor rrMotor = null;

	DcMotor armBaseMotor = null;
	DcMotor firstArmMotor = null;
	DcMotor secondArmMotor = null;
	DcMotor levelingMotor = null;

	Servo clawServo = null;

	@Override
	public void runOpMode() {

		/*Telemetry data for driver feedback*/
		telemetry.addData("Status", "Holonomic Drive Initialized");
		telemetry.update();

		/*Hardware mapping pulls the motor names from the configuration on the robot-side controller phone*/
		lfMotor = hardwareMap.dcMotor.get("lfmotor");
		lfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		lfMotor.setDirection(DcMotor.Direction.FORWARD);

		rfMotor = hardwareMap.dcMotor.get("rfmotor");
		rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		rfMotor.setDirection(DcMotor.Direction.FORWARD);

		lrMotor = hardwareMap.dcMotor.get("lrmotor");
		lrMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		lrMotor.setDirection(DcMotor.Direction.FORWARD);

		rrMotor = hardwareMap.dcMotor.get("rrmotor");
		rrMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		rrMotor.setDirection(DcMotor.Direction.FORWARD);

		/*Hardware mapping for arm motors*/
		armBaseMotor = hardwareMap.dcMotor.get("armbasemotor");
		armBaseMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		armBaseMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armBaseMotor.setDirection(DcMotorSimple.Direction.FORWARD);

		firstArmMotor = hardwareMap.dcMotor.get("firstarmmotor");
		firstArmMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		firstArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		firstArmMotor.setDirection(DcMotor.Direction.FORWARD);

		secondArmMotor = hardwareMap.dcMotor.get("secondarmmotor");
		secondArmMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		secondArmMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		secondArmMotor.setDirection(DcMotor.Direction.FORWARD);

		/*Hardware mapping for claw servo*/
		clawServo = hardwareMap.servo.get("clawservo");

		/*Wait until the driver presses PLAY*/
		waitForStart();

		while (opModeIsActive()) {

		/* Robot Drivetrain Configuration
		 *
		 *           _______
		 * lfMotor \\       // rfMotor
		 * 			|		|
		 * 			|		|
		 * lrMotor //_______\\ rrMotor
		 *
		 *
		 */

		/* Gamepad1 (Drivetrain) Controller Configuration
		 *
		 *              LEFT JOYSTICK       						RIGHT JOYSTICK
		 *
		 *              Move Forward								    Null
		 *       		      ^										     ^
		 *       			  |										     |
		 * Strafe Left <-----   -----> Strafe Right   Rotate Left <-----   -----> Rotate Right
		 *       			  |											 |
		 *       			  v											 v
		 *              Move Backward									Null
		 *
		 */

		/*Assign Gamepad 1 inputs*/
			float gp1_ljoy_y = -gamepad1.left_stick_y;    //Forward Y normally yields negative value - reverse to make more sense
			float gp1_ljoy_x = gamepad1.left_stick_x;
			float gp1_rjoy_x = gamepad1.right_stick_x;

		/*Basic holonomic drive formulas based on motor output matrix*/
			float lfPwr = -gp1_ljoy_y - gp1_ljoy_x - gp1_rjoy_x;
			float rfPwr = gp1_ljoy_y - gp1_ljoy_x - gp1_rjoy_x;
			float lrPwr = gp1_ljoy_y + gp1_ljoy_x - gp1_rjoy_x;
			float rrPwr = -gp1_ljoy_y + gp1_ljoy_x - gp1_rjoy_x;

		/*The above matrix could return values outside [-1,1]...if so, scale values*/
			//TO-DO...until we work this out, use the clipping method//
			//lfPwr_scale = Range.scale(lfPwr, -2, 2, -1, 1);
			//rfPwr_scale = Range.scale(rfPwr, -2, 2, -1, 1);
			//lrPwr_scale = Range.scale(lrPwr, -2, 2, -1, 1);
			//rrPwr_scale = Range.scale(rrPwr, -2, 2, -1, 1);
			//Can it really be this easy?

		/*Clipping method -- UGLY!*/
			float lfPwr_clip = Range.clip(lfPwr, -1, 1);
			float rfPwr_clip = Range.clip(rfPwr, -1, 1);
			float lrPwr_clip = Range.clip(lrPwr, -1, 1);
			float rrPwr_clip = Range.clip(rrPwr, -1, 1);

		/*Set motor power - replace clipped values w/ scaled once scaling algorithm complete*/
			lfMotor.setPower(lfPwr_clip);
			rfMotor.setPower(rfPwr_clip);
			lrMotor.setPower(lrPwr_clip);
			rrMotor.setPower(rrPwr_clip);

		/*Send telemetry to driver station for feedback*/
			telemetry.addData("Motors", "LF (%.2f) | RF (%.2f) | LR (%.2f) | RR (%.2f)", lfPwr_clip, rfPwr_clip, lrPwr_clip, rrPwr_clip);
			telemetry.update();

		/*Robot arm control mapping
		 *
		 * Gamepad2 (Arm Controller)Mapping
		 *
		 *                                UP
		 *                        (Leveling Motor Down)
		 *
		 *                 LEFT      LEFT JOYSTICK      RIGHT
		 *            (Claw Closed)                  (Claw Open)
		 *
		 *                               DOWN
		 *                          (Leveling Motor Up)
		 *
		 * 								  UP
		 * 						   (secondArm Unfold)
		 * 				LEFT		RIGHT JOYSTICK		RIGHT
		 * 		(first Arm Fold)					(firstArm Unfold)
		 * 								DOWN
		 * 							(secondArm Fold)
		 *
		 *		LEFT TRIGGER (armBase Fold)			RIGHT TRIGGER (armBase Unfold)
		 *	    LEFT BUMPER (Servo Close Override)
		 */

			final double MOTOR_SCALAR = 0.5;        //Adjust to get reasonable arm speed

			float gp2_rjoy_y = -gamepad2.right_stick_y;
			float secondArmPwr = gp2_rjoy_y;
			float secondArmPwr_clip = Range.clip(secondArmPwr, -1, 1);
			secondArmMotor.setPower(secondArmPwr_clip * MOTOR_SCALAR);
			telemetry.addData("Motor", "2nd Arm: (%.2f)", secondArmPwr_clip);
			telemetry.update();

			float gp2_rjoy_x = gamepad2.right_stick_x;
			float firstArmPwr = gp2_rjoy_x;
			float firstArmPwr_clip = Range.clip(firstArmPwr, -1, 1);
			firstArmMotor.setPower(firstArmPwr_clip * MOTOR_SCALAR);
			telemetry.addData("Motor", "1st Arm: (%.2f)", firstArmPwr_clip);
			telemetry.update();

			float gp2_ljoy_y = gamepad2.left_stick_y;
			float levelingMotorPwr = gp1_ljoy_y;
			float levelingMotorPwr_clip = Range.clip(levelingMotorPwr, -1, 1);
			levelingMotor.setPower(levelingMotorPwr_clip * MOTOR_SCALAR);
			telemetry.addData("Motor", "Lvl: (%.2f)", levelingMotorPwr_clip);
			telemetry.update();

			float gp2_ljoy_x = gamepad2.left_stick_x;
			double clawServoPos = gp2_ljoy_x;

			if (gamepad2.left_bumper) {
				clawServoPos = Range.clip(clawServoPos, 0, 1);
				clawServo.scaleRange(0, 1);
				clawServo.setPosition(clawServoPos);
			} else {
				clawServoPos = Range.clip(clawServoPos, 0, 1);
				clawServo.scaleRange(0, 0.5);
				clawServo.setPosition(clawServoPos);
			}
		}
	}
}
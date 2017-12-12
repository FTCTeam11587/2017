package org.firstinspires.ftc.teamcode.ftc11587;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="Relic Recovery: Basic Holonomic Drive", group="Relic Recovery")
@Disabled //comment out this line to add this OpMode to the Driver Station select list

public class BasicHolonomicDrive extends LinearOpMode {

	/*Declarations*/
	DcMotor lfMotor = null;
	DcMotor rfMotor = null;
	DcMotor lrMotor = null;
	DcMotor rrMotor = null;

	DcMotor armBaseMotor = null;
	DcMotor firstArmMotor = null;
	DcMotor secondArmMotor = null;

	Servo clawServo = null;

	@Override
	public void runOpMode() {

		/*Telemetry data for driver feedback*/
		telemetry.addData("Status","Holonomic Drive Initialized");
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

		/*Hardware mapping for claw servo*/
		clawServo = hardwareMap.Servo.get("clawservo");

		/*Wait until the driver presses PLAY*/
		waitForStart();

		while (opModeIsActive()) {

		public void moveRobot() {
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

			/* Gamepad Controller Configuration
			 *
			 *              LEFT JOYSTICK       						RIGHT JOYSTICK
			 *
			 *              Move Forward								 Arm Raise
			 *       		      ^										     ^
			 *       			  |										     |
			 * Strafe Left <-----   -----> Strafe Right   Rotate Left <-----   -----> Rotate Right
			 *       			  |											 |
			 *       			  v											 v
			 *              Move Backward								 Arm Lower
			 *
			 */

			/*Assign gamepad inputs*/
			float gp1_ljoy_y = -gamepad1.left_stick_y;	//Forward Y normally yields negative value - reverse to make more sense
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
			lfPwr_clip = Range.clip(lfPwr, -1, 1);
			rfPwr_clip = Range.clip(rfPwr, -1, 1);
			lrPwr_clip = Range.clip(lrPwr, -1, 1);
			rrPwr_clip = Range.clip(rrPwr, -1, 1);

			/*Set motor power - replace clipped values w/ scaled once scaling algorithm complete*/
			lfMotor.setPower(lfPwr_clip);
			rfMotor.setPower{rfPwr_clip);
			lrMotor.setPower(lrPwr_clip);
			rrMotor.setPower(rrPwr_clip);

			/*Send telemetry to driver station for feedback*/
			telemetry.addData("Motors","LF (%.2f) | RF (%.2f) | LR (%.2f) | RR (%.2f)", lfPwr_clip, rfPwr_clip, lrPwr_clip, rrPwr_clip);
			telemetry.update();
		}

		public void controlArm() {
			/*Robot arm control mapping
			 *
			 * Controller #1 D-pad mapping
			 *
			 *                          UP
			 *                     (Arm Extend)
			 *
			 *                 LEFT           RIGHT
			 *              (Claw Open)   (Claw Closed)
			 *
			 *                         DOWN
			 *                     (Arm Retract)
			 *
			 */

			/*Map right joystick forward/back to arm raise/lower*/
			float gp1_rjoy_y = -gamepad1.right_stick_y;

			/*Arm base motor control*/
			armBasePwr = gp1_rjoy_y;
			armBasePwr_clip = Range.clip(armBasePwr, -1, 1);
			armBaseMotor.setPower(armBasePwr_clip);
			telemetry.addData("Motor","Arm Base: (%.2f)",armBasePwr_clip);
			telemetry.update();

			/* Map D-pad actions to the above scheme
			 * NOTE: D-pad inputs are boolean - motor drive power is tied to a while function
			 */
			boolean gp1_dpad_up = gamepad1.dpad_up;
			boolean gp1_dpad_dn = gamepad1.dpad_down;
			boolean gp1_dpad_lf = gamepad1.dpad_left;
			boolean gp1_dpad_rt = gamepad1.dpad_right;

			/*Set arm power - adjust to get manageable arm speed*/
			double firstArmPwr = .5;
			double secondArmPwr = .5;

			/*Adjust servo open/closed position to hold blocks firmly*/
			double clawOpen = 0;		//Adjust this if you don't need to open the claw all the way to grab an item
			double clawClosed = 0.6; 	//Adjust this value to ensure the claw holds the block tightly
			double clawIdol = 0.7;   	//Adjust this value separately to ensure the claw holds the idol tightly

			/*Logic tree to drive arm correctly*/
			if (gp1_dpad_up = true) {
				firstArmMotor.setPower(firstArmPwr);
				secondArmMotor.setPower(secondArmPwr);
			} else if (gp1_dpad_dn = true) {
				firstArmMotor.setPower(-firstArmPwr);
				secondArmMotor.setPower(-secondArmPwr);
			} else if (gp1_dpad_lf = true) {
				clawServo.setPosition(clawOpen);
			} else if (gp1_dpad_rt = true) {
				clawServo.setPosition(clawClosed);
			} else {
				firstArmMotor.setPower(0);
				secondArmMotor.setPower(0);
				clawServo.setPosition(clawServo.getPosition()); //Hold the servo at present position
			}

		}

		public void autoMountPlatform() {
			/* Code which directs the robot to mount the balance platform
			 * Assumes that the robot is positioned with front drive wheels pressed against the balance platform when initiated
			 */
		}

		}
		}
	}
}
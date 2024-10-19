package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.Range;


@TeleOp(name = "Mecanum TeleOp with Limelight")
public class MecanumTeleOpLimelight extends OpMode {


    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private Limelight3A limelight;


    @Override
    public void init() {
        // Initialize the motors
        frontLeft = hardwareMap.get(DcMotor.class, "front_left_motor");
        frontRight = hardwareMap.get(DcMotor.class, "front_right_motor");
        backLeft = hardwareMap.get(DcMotor.class, "back_left_motor");
        backRight = hardwareMap.get(DcMotor.class, "back_right_motor");


        // Set motor directions for mecanum drive
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);


        // Initialize Limelight
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100); // Set polling rate for Limelight
        limelight.start(); // Start the Limelight
        limelight.pipelineSwitch(0); // Switch to pipeline 0


        telemetry.addData("Status", "Initialized");
    }


    @Override
    public void loop() {
        // Mecanum drive variables
        double drive = -gamepad1.left_stick_y; // Forward/backward
        double strafe = gamepad1.left_stick_x; // Left/right
        double rotate = gamepad1.right_stick_x; // Rotation


        // Limelight vision targeting
        LLResult result = limelight.getLatestResult();
        double targetX = 0;
        boolean hasTarget = false;
        double changeRotate = 0.05;


        if (result != null && result.isValid()) {
            targetX = result.getTx(); // Horizontal offset from target
            hasTarget = true;
            telemetry.addData("Target X", targetX);
        } else {
            telemetry.addData("Limelight", "No Targets");
        }


        // Auto-align with Limelight if 'A' button is pressed
        changeRotate = targetX / 150;
        if (gamepad1.a && hasTarget) {
            rotate = targetX * changeRotate; // Adjust rotation based on target's X-axis offset
        } else {
            rotate = 0;
        }


        // Calculate mecanum drive motor powers
        double frontLeftPower = drive + strafe + rotate;
        double frontRightPower = drive - strafe - rotate;
        double backLeftPower = drive - strafe + rotate;
        double backRightPower = drive + strafe - rotate;


        // Normalize the motor powers to keep them within range [-1, 1]
        frontLeftPower = Range.clip(frontLeftPower, -1.0, 1.0);
        frontRightPower = Range.clip(frontRightPower, -1.0, 1.0);
        backLeftPower = Range.clip(backLeftPower, -1.0, 1.0);
        backRightPower = Range.clip(backRightPower, -1.0, 1.0);


        // Set the motor powers
        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        backLeft.setPower(backLeftPower);
        backRight.setPower(backRightPower);


        // Show telemetry
        telemetry.addData("Front Left Power", frontLeftPower);
        telemetry.addData("Front Right Power", frontRightPower);
        telemetry.addData("Back Left Power", backLeftPower);
        telemetry.addData("Back Right Power", backRightPower);


        telemetry.update();
    }
}



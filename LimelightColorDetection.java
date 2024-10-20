
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="LimelightColorDetection", group="Linear OpMode")
public class LimelightColorDetection extends OpMode {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private Limelight3A limelight;
    private int activePipeline = 0;

    @Override
    public void init() {
        // Initialize the motors
        // Initialize the motors
        frontLeft = hardwareMap.get(DcMotor.class, "front_left_motor");
        frontRight = hardwareMap.get(DcMotor.class, "front_right_motor");
        backLeft = hardwareMap.get(DcMotor.class, "back_left_motor");
        backRight = hardwareMap.get(DcMotor.class, "back_right_motor");

        // Set motor direction
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);


        // Initialize the Limelight object
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100); // Set polling rate for Limelight
        limelight.start();
        limelight.pipelineSwitch(activePipeline);
        telemetry.addData("Status", "Initialized");

    }

    boolean checkTeamCodeRan = false; // Boolean to check if the code to check the team has ran
    boolean team = false; // false is red and true is blue
    double scale;
    public double calculateDistanceFromArea(double area) {
        // Assuming a basic inverse relationship between area and distance
        if (area == 0) {
            return Double.MAX_VALUE; // No target detected, return a large distance
        }
        return 1 / Math.sqrt(area); // Example function; adjust based on calibration
    }
    @Override

    public void loop() {
        if (!checkTeamCodeRan) { // Until you press the right or left bumper (too select your team) the rest of the code won't run. Once they are pressed, they an be used for other mechanisms.
            if (gamepad1.left_bumper) {
                team = false;
                checkTeamCodeRan = true;
            }
            if (gamepad1.right_bumper) {
                team = true;
                checkTeamCodeRan = true;
            }
        } else {
            // Get Limelight data
            // Mecanum drive variables
            double drive = gamepad1.left_stick_y; // Forward/backward
            double strafe = -gamepad1.left_stick_x; // Left/right
            double rotate;



            // Switch between color detection pipelines
            if (gamepad1.y) {
                activePipeline = 0; // Yellow
            } else if (gamepad1.a) {
                if (team) {
                    activePipeline = 1; // Blue
                } else {
                    activePipeline = 2; // Red
                }
            }
            limelight.pipelineSwitch(activePipeline);

            LLResult result = limelight.getLatestResult();
            double targetX = 0;
            double targetArea = 0;
            boolean hasTarget = false;

            if (result != null && result.isValid()) {
                targetX = result.getTx(); // Horizontal offset from target
                if (targetX >= 0) {
                    scale = targetX / 4;
                } else {
                    scale = targetX / -4;
                }
                targetArea = result.getTa(); // Area of the target (useful for distance)
                hasTarget = true;
                telemetry.addData("Target X", targetX);
                telemetry.addData("Target Area", targetArea);
            } else {
                telemetry.addData("Limelight", "No Targets");
                hasTarget = false;
            }
            double distance = calculateDistanceFromArea(targetArea);
            if (gamepad1.a && hasTarget) {
                telemetry.addData("Rotation without scale", -Range.clip(targetX / 150, -1.0, 1.0));
                telemetry.addData("Rotation with scale", scale * -Range.clip(targetX / 150, -1.0, 1.0));
                telemetry.addData("Scale", scale);
                rotate = scale * -Range.clip(targetX / 150, -1.0, 1.0); // Adjust rotation based on target's X-axis offset
            } else {
                rotate = -gamepad1.right_stick_x; // Rotation
            }


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
            telemetry.addData("Distance", distance);
            telemetry.addData("Target", hasTarget);
            if (activePipeline == 0) {
                telemetry.addData("Pipeline", "Yellow");
            } else if (activePipeline == 1) {
                telemetry.addData("Pipeline", "Blue");
            } else if (activePipeline == 2) {
                telemetry.addData("Pipeline", "Red");
            }
            telemetry.update();
        }
    }
}

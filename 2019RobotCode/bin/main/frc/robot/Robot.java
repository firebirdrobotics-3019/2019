/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import frc.robot.subsystems.*;
import frc.robot.*;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionThread;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.*;
import edu.wpi.first.wpilibj.smartdashboard.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  public static OI oi;
  public static ExampleSubsystem m_subsystem;
  public static Drivetrain driveTrain;
  public static ElevatorSystem elevator;
  public static HatchSystem hatchSystem;
  public static Solenoids solenoids;
  
  // Camera
  private static final int IMG_WIDTH = 320;
	private static final int IMG_HEIGHT = 240;
	private VisionThread visionThread;
	private double centerX = 0.0;
  private final Object imgLock = new Object();
  
  // NetworkTables
  NetworkTable contoursTable;
  GripPipeline gripPipeline;
  List<MatOfPoint> contours;
  List<Number> centerXs;
  List<Number> centerYs;


  Command m_autonomousCommand;
  SendableChooser<Command> m_chooser = new SendableChooser<>();

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_subsystem = new ExampleSubsystem();
    driveTrain = new Drivetrain();
    elevator = new ElevatorSystem();
    hatchSystem = new HatchSystem();
    solenoids = new Solenoids();
    oi = new OI(); // Make sure the OI is initialized LAST

    // Camera
    UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
    camera.setResolution(IMG_WIDTH, IMG_HEIGHT);
    
    // NetworkTables
    contoursTable = NetworkTableInstance.getDefault().getTable("/GRIP/contoursTable");
    gripPipeline = new GripPipeline();
    contours = gripPipeline.filterContoursOutput();
    centerXs = new ArrayList<>();
    centerYs = new ArrayList<>();

    // Vision thread
    // visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
    //   if (!pipeline.filterContoursOutput().isEmpty()) {
    //       Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
    //       synchronized (imgLock) {
    //           centerX = r.x + (r.width / 2);
    //       }
    //   }
    // });
    visionThread.start();

    // Add stuff to NetworkTables
    for (MatOfPoint contour : contours) {
      Rect boundingRect = Imgproc.boundingRect(contour);
      centerXs.add(boundingRect.x + boundingRect.width / 2);
      centerYs.add(boundingRect.y + boundingRect.height / 2);
      // etc for width, height, ...
    }
    contoursTable.getEntry("centerX").setNumberArray(centerXs.toArray(new Number[0]));
    contoursTable.getEntry("centerY").setNumberArray(centerYs.toArray(new Number[0]));
    // etc for width, height, ...


    // m_chooser.setDefaultOption("Default Auto", new ExampleCommand());
    // chooser.addOption("My Auto", new MyAutoCommand());
    // SmartDashboard.putData("Auto mode", m_chooser);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   * You can use it to reset any subsystem information you want to clear when
   * the robot is disabled.
   */
  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
    Scheduler.getInstance().run();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString code to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional commands to the
   * chooser code above (like the commented example) or additional comparisons
   * to the switch structure below with additional strings & commands.
   */
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_chooser.getSelected();

    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector",
     * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
     * = new MyAutoCommand(); break; case "Default Auto": default:
     * autonomousCommand = new ExampleCommand(); break; }
     */

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      m_autonomousCommand.start();
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    Scheduler.getInstance().run();
  }

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    Scheduler.getInstance().run();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    
  }
}

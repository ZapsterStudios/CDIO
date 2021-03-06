package sphinx;

import org.opencv.core.Scalar;

public class Config {

	/**
	 * Settings for the camera.
	 */
	public static class Camera {
		
		// Cropping
		public static final int croppingTime = 5;
		public static final boolean shouldCrop = true;
		public static final double minCropPercent = 0.7;
		
		// Video
		public static final int width = 640;
		public static final int height = 480;
		
		public static final boolean useWebcam = true;
		public static final String source = "./src/video4.mov";
		
	}

	/**
	 * Settings for the movement client.
	 */
	public static class Client {
		
		// Toggle
		public static final boolean skip = false;
		public static final boolean connect = true;
		public static final boolean autoStart = false;
		public static final boolean exitFailed = true;
		public static final boolean printTimer = true;

		// Server
		public static final String ip = "192.168.43.44";
		public static final int port = 59898;
		
		// Speeds
		public static final int turnSpeed = 200;
		public static final int turnSlowSpeed = 120;
		
		public static final int slowSpeed = 240;
		public static final int moveSpeed = 440;
		
		public static final int reverseSpeed = 280;
		
		public static final int collectOuterSpeed = 500;
		public static final int collectInnerSpeed = 200;
		
		// Angle
		public static final int degreeOffset = 7;
		public static final int slowDegreeOffset = 3;
		
		// Distance
		public static final int wallSafeDistance = 30;
		public static final int cornerSafeDistance = 30;
		public static final double insideDistOffset = 0.5;
		public static final double insideWallDistOffset = -4;
		public static final double insideCornerDistOffset = -5;
		
		// Thresholds
		public static final int slowThreshold = 40;
		
		// Triangle
		public static final double triangleScale = 2.2;
		
		// Goal
		public static final int goalDirection = 0;
		
	}
	
	/**
	 * Settings for the targets.
	 */
	public static class Targets {
		
		// Dilation
		public static final int kernelSize = 3;
		
		// Circles
		public static final int minRadius = 7;
		public static final int maxRadius = 14;
		public static final int minDistance = 3;
		public static final int param1 = 150;
		public static final int param2 = 20;
		public static final double DP = 1.4;
		
	}
	
	/**
	 * Settings for the obstacles.
	 */
	public static class Obstacle {
		// Areas
		public static final int areaIndex = 1;
		public static final int crossIndex = 0;
		
	}
	
	/**
	 * Settings for the graph.
	 */
	public static class Graph {
		
		// Toggle
		public static final boolean enable = true;
		
	}

	/**
	 * Settings for the color detection.
	 */
	public static class Colors {
		
		// Red
		public static final Scalar redLowLower = new Scalar(0, 65, 65);
		public static final Scalar redLowUpper = new Scalar(12, 255, 255);
		public static final Scalar redHighLower = new Scalar(150, 65, 65);
		public static final Scalar redHighUpper = new Scalar(180, 255, 255);
		
		// Blue
		public static final Scalar blueLower = new Scalar(85, 120, 120);
		public static final Scalar blueUpper = new Scalar(140, 255, 255);
		
		// White
		public static final Scalar whiteLower = new Scalar(0, 0, 193);
		public static final Scalar whiteUpper = new Scalar(255, 62, 255);
		
	}

	/**
	 * Settings for the position transformer.
	 */
	public static class Position {
		
		// Sizes - 3.23 px/cm
		public static final double carHeight = 80;
		public static final double cameraHeight = 545;
		
	}

	/**
	 * Settings for the GUI preview.
	 */
	public static class Preview {
		
		// Sizes
		public static final int displayWidth = 1280;
		public static final int displayHeight = 720;
		
	}
	
}

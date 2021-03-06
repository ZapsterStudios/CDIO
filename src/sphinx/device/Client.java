package sphinx.device;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import sphinx.Config;
import sphinx.Graph;
import sphinx.elements.Vehicle;

public class Client {
	
	/**
	 * The time reaching close point.
	 *
	 * @var long
	 */
	long pointTimer = System.currentTimeMillis();
	
	/**
	 * The speed the vehicle turns.
	 *
	 * @var int
	 */
	int turnSpeed = Config.Client.turnSpeed;
	
	/**
	 * The slow speed the vehicle turns.
	 *
	 * @var int
	 */
	int turnSlowSpeed = Config.Client.turnSlowSpeed;
	
	/**
	 * The speed the vehicle moves.
	 *
	 * @var int
	 */
	int moveSpeed = Config.Client.moveSpeed;
	
	/**
	 * The slow speed when the vehicle moves.
	 *
	 * @var int
	 */
	int slowSpeed = Config.Client.slowSpeed;
	
	/**
	 * The reverse speed for the vehicle.
	 *
	 * @var int
	 */
	int reverseSpeed = Config.Client.reverseSpeed;
	
	/**
	 * The speed for the inner vehicle collection motors.
	 *
	 * @var int
	 */
	int collectInnerSpeed = Config.Client.collectInnerSpeed;
	
	/**
	 * The speed for the outer vehicle collection motors.
	 *
	 * @var int
	 */
	int collectOuterSpeed = Config.Client.collectOuterSpeed;
	
	/**
	 * The dist threshold when the vehicle should slow down.
	 *
	 * @var int
	 */
	int slowThreshold = Config.Client.slowThreshold;
	
	/**
	 * The amount of degree imperfection.
	 *
	 * @var int
	 */
	int degreeOffset = Config.Client.degreeOffset;
	
	/**
	 * The amount of degree imperfection when slowed down.
	 *
	 * @var int
	 */
	int slowDegreeOffset = Config.Client.slowDegreeOffset;
	
	/**
	 * The distance from the wall to start using inner wall dist offset.
	 *
	 * @var int
	 */
	int wallSafeDistance = Config.Client.wallSafeDistance;
	
	/**
	 * The distance from the corners to start using inner corner dist offset.
	 *
	 * @var int
	 */
	int cornerSafeDistance = Config.Client.cornerSafeDistance;
	
	/**
	 * The amount of inner distance imperfection.
	 *
	 * @var double
	 */
	double insideDistOffset = Config.Client.insideDistOffset;
	
	/**
	 * The amount of inner distance imperfection when close to wall.
	 *
	 * @var double
	 */
	double insideWallDistOffset = Config.Client.insideWallDistOffset;
	
	/**
	 * The amount of inner distance imperfection when close to corners.
	 *
	 * @var double
	 */
	double insideCornerDistOffset = Config.Client.insideCornerDistOffset;
	
	/**
	 * The stalled status of the pickup engine.
	 *
	 * @var Boolean
	 */
	public boolean stalled = false;
	
	/**
	 * The running status of the pickup engine.
	 *
	 * @var Boolean
	 */
	public boolean collecting = false;
	
	/**
	 * The socket for the connection.
	 *
	 * @var Socket
	 */
	Socket socket;
	
	/**
	 * The incoming stream using for the scanner.
	 *
	 * @var InputStream
	 */
	InputStream stream;
	
	/**
	 * The input stream for the socket.
	 *
	 * @var Scanner
	 */
	Scanner input;
	
	/**
	 * The output stream for the socket.
	 *
	 * @var PrintWriter
	 */
	PrintWriter output;
	
	/**
	 * The millis counter used for pausing.
	 *
	 * @var long
	 */
	long pauser = 0;
	
	/**
	 * @wip
	 *
	 * @var boolean
	 */
	public boolean done = false;
	
	/**
	 * The upcoming should reverse state.
	 *
	 * @var boolean
	 */
	boolean shouldReverse = false;
	
	/**
	 * @wip
	 *
	 * @var boolean
	 */
	boolean nextReverse = false;
	
	/**
	 * @wip
	 *
	 * @var boolean
	 */
	public boolean wasTowardsGoal = false;
	
	/**
	 * @wip
	 *
	 * @var boolean
	 */
	public boolean doneGoalCheck = false;
	
	/**
	 * The current vehicle paths.
	 *
	 * @var List<Point>
	 */
	public ArrayList<Point> targets = new ArrayList<Point>();
	
	/**
	 * Attempt to connect to the EV3 and open stream.
	 */
	public Client() {
		// Skip socket opening if skipping.
		if (Config.Client.skip) return;
		
		// Attempt to open socket.
		try {
			// Show connection warning.
			System.out.println("Attempting to connect to server!");
			
			// Open socket connection.
			this.socket = new Socket(Config.Client.ip, Config.Client.port);

			// Open input and output steams.
			this.stream = this.socket.getInputStream();
			this.input = new Scanner(this.stream);

			this.output = new PrintWriter(this.socket.getOutputStream(), true);
			
			// Show connection complete.
			System.out.println("Successfully connected to server!");
		} catch (Exception e) {
			// Print the exceptions stack.
			e.printStackTrace();
			
			// Show connection failed error.
			System.out.println("Failed to connect to server!");
			
			// Kill program if exit on failure is enabled.
			if (Config.Client.exitFailed) {
				System.exit(0);
			}
		}
	}
	
	/**
	 * Handle the movement.
	 *
	 * @param vehicle
	 */
	public void run(Vehicle vehicle, Graph graph, int width, int height) {
		// Skip if currently paused.
		if (this.pauser > System.currentTimeMillis()) return;
		
		// @wip
		if (this.nextReverse) {
			this.nextReverse = false;

			this.move(-this.reverseSpeed);
			this.pause(1200); 
			this.collect(this.collectInnerSpeed, this.collectOuterSpeed);
			return;
		}
		
		// @wip
		if (this.wasTowardsGoal) {
			// Reverse further back.
			this.move(-this.reverseSpeed);
			this.collect(this.collectInnerSpeed, this.collectOuterSpeed);
			this.pause(2000);
			
			this.wasTowardsGoal = false;
			this.doneGoalCheck = true;
			return;
		} else if (! this.doneGoalCheck) {
			this.doneGoalCheck = false;
		}
		
		// Stop motors if missing targets and was at goal.
		if (this.targets.isEmpty()) {
			// Stop the vehicle and mark done.
			boolean preCheck = this.doneGoalCheck;
			this.stop();
			
			// Mark the run as done.
			if (preCheck) {
				this.done = true;				
			}
			
			// Stop program execution.
			return;
		}
		
		// Reset was towards goal.
		this.wasTowardsGoal = false;
		
		// Get the first target or skip.
		Point target = this.targets.get(0);
		
		// Find distance and rotation.
		double rotation = this.calculateRotation(vehicle, target);
		
		//
		double distance;
		if (! graph.towardsGoal || this.targets.size() <= 1) {
			distance = this.calculateDistance(vehicle, target);			
		} else {
			//
			Point diff = new Point();
			diff.x = vehicle.center.x - target.x;
			diff.y = vehicle.center.y - target.y;
			
			// Find distance based on the diff point.
			distance = -Math.round(Math.sqrt((diff.x * diff.x) + (diff.y * diff.y)));
		}
		
		// Handle the movement and collecting.
		this.handleMovement(Math.abs(distance), rotation);
		this.handleCollecting(Math.abs(distance), rotation);
		
		// Handle target pathing.
		this.handlePathing(distance, target, vehicle, graph, width, height);	
	}

	/**
	 * Returns whether or not the vehicle should slow down.
	 *
	 * @param distance
	 * @return boolean
	 */
	private boolean shouldSlow(double distance) {
		return distance < this.slowThreshold;
	}

	/**
	 * Handles the movement mechanism logic.
	 *
	 * @param distance
	 * @param rotation
	 */
	private void handleMovement(double distance, double rotation) {
		// Find the degree offset.
		int degree = this.shouldSlow(distance)
			? this.slowDegreeOffset
			: this.degreeOffset;
		
		// Check if rotation is above imperfection limit.
		if (Math.abs(rotation) > degree) {
			// Determine the turning speed.
			int turn = this.shouldSlow(distance)
				? this.turnSlowSpeed
				: this.turnSpeed;
			
			// Turn the vehicle to the found degree.
			this.turn((int) rotation, turn);
			
			// Skip movement while turning.
			return;
		}
		
		// Determine movement speed based on distance.
		int speed = this.shouldSlow(distance)
			? this.slowSpeed
			: this.moveSpeed;
		
		// Make vehicle move at found speed.
		this.move(speed);
	}

	/**
	 * Handles the collecting mechanism logic.
	 *
	 * @param distance
	 * @param rotation
	 */
	private void handleCollecting(double distance, double rotation) {
		// Collect if not active and stalled.
		if (! this.collecting && ! this.stalled) {
			this.collecting = true;
			this.collect(this.collectInnerSpeed, this.collectOuterSpeed);
		}
		
		// Handle collecting when stalled.
		if (this.stream != null && this.input != null) {
			try {
				// Check if any stream content is available.
				if (this.stream.available() > 0) {
					// Get text from input stream.
					String text = this.input.nextLine();
					
					// Make action based on stalled params.
					if (text.equals("stalled inner")) {
						// Stop the collecting mechanism.
						System.out.println("Stalled inner");
						this.stalled = true;
						this.collecting = false;
						
						// Stop inner spinner and clear targets to get to goal.
						this.collect(0, this.collectOuterSpeed);
						this.targets.clear();
					}
					
					if (text.equals("stalled outer")) {
						// @wip
						System.out.println("Stalled outer");
						
						// Mark as not currently collecting.
						this.collecting = false;
						
						// Clear the current target list and reverse.
						if (! this.nextReverse) {
							this.targets.clear();
							this.nextReverse = true;
						}
					}
				}
				
			} catch (Exception e) {
				// Print the error stack trace.
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handle the pathing when reaching targets.
	 *
	 * @param distance
	 * @param rotation
	 * @param width
	 * @param height
	 */
	private void handlePathing(double dist, Point target, Vehicle vehicle, Graph graph, int width, int height) {
		// Find inside distance based on vehicle location.
		double insideDistance = this.insideDistOffset;
		if (this.closeToCorners(vehicle.front, width, height)) {
			insideDistance = this.insideCornerDistOffset;
			this.shouldReverse = true;
		} else if (this.closeToWalls(vehicle.front, width, height)) {
			insideDistance = this.insideWallDistOffset;
			this.shouldReverse = true;
		}
		
		// @wip
		boolean forceSkip = false;
		if (Math.abs(dist) >= 30) {
			this.pointTimer = System.currentTimeMillis();
		} else if ((System.currentTimeMillis() - this.pointTimer) >= 10000) {
			forceSkip = true;
		}
		
		//
		if (graph.towardsGoal && this.targets.size() <= 1) {
			//
			double must = 0;
			double rot = vehicle.rotation;
			
			//
			if (Config.Client.goalDirection == 0) {
				must = 180;
			}
			
			// @wip
			double diff = Math.round(must - rot);
			
			// Find most optimal rotation direction.
			if (diff > 180) {
				diff = diff - 360;
			} else if (diff < -180) {
				diff = diff + 360;
			}
			
			//
			if (diff > this.slowDegreeOffset || diff < -this.slowDegreeOffset) {
				this.turn((int) diff, this.turnSlowSpeed);
				return;
			}
		}
		
		//
		if (graph.towardsGoal && this.targets.size() > 1 && dist >= -10) {
			forceSkip = true;
		}
		
		// Skip if target point is not inside triangle.
		if (dist <= insideDistance && ! forceSkip) return;
		if (this.targets.isEmpty()) return;

		// Remove the target from the list.
		this.targets.remove(0);
		
		// Play beep if last target.
		if (this.targets.isEmpty()) {
			this.beep(4);
		}
		
		// Keep moving forward to prevent turning.
		this.move(this.slowSpeed);
		
		// Check if empty and should reverse next.
		if (this.targets.isEmpty() && this.shouldReverse) {
			//  Make vehicle wait at point for a bit.
			this.move(0);
			this.pause(1250);
			
			// Disable reversing state.
			this.shouldReverse = false;
			this.nextReverse = true;
		}
		
		// Set reversing state for upcoming targets.
		this.shouldReverse = graph.reverse;
		
		// Check if at last path item and is towards the goal.
		if (this.targets.isEmpty() && graph.towardsGoal) {
			// Disable movement and spit out the balls.
			this.move(0);
			this.collect(-this.collectInnerSpeed, -this.collectOuterSpeed);
			
			// Disable the engine stalled state.
			this.stalled = false;
			
			// Save was towards goal state.
			graph.towardsGoal = false;
			this.wasTowardsGoal = true;
			
			// Pause to wait for balls to roll out.
			this.pause(5000);
		}
	}

	/**
	 * Returns the distance between the vehicle and target.
	 *
	 * @param vehicle
	 * @param target
	 * @return double
	 */
	private double calculateDistance(Vehicle vehicle, Point target) {
		return Imgproc.pointPolygonTest(vehicle.triangle, target, true);
	}
	
	/**
	 * Returns the rotation between the vehicle and target.
	 *
	 * @param vehicle 
	 * @param target
	 * @return double
	 */
	private double calculateRotation(Vehicle vehicle, Point target) {
		// Calculate the distance and degree between them.
		double degree = vehicle.findRotation(target, vehicle.back);
		
		// Find the degree difference between requested and current.
		double degreeDiff = Math.round(degree - vehicle.rotation);
		
		// Find most optimal rotation direction.
		if (degreeDiff > 180) {
			degreeDiff = degreeDiff - 360;
		} else if (degreeDiff < -180) {
			degreeDiff = degreeDiff + 360;
		}
		
		// Return the found rotation.
		return degreeDiff;
	}

	/**
	 * Returns wheater or not the vehicle is close to the corners.
	 *
	 * @param vehicle
	 * @param width
	 * @param height
	 * @return boolean
	 */
	private boolean closeToCorners(Point vehicle, int width, int height) {
		// Make short var for distance.
		int dist = this.cornerSafeDistance;
		
		// Check if inside tl, tr, bl, or br corner.
		return this.insideRectangle(vehicle, 0, 0, dist, dist)
			|| this.insideRectangle(vehicle, width - dist, 0, width, dist)
			|| this.insideRectangle(vehicle, 0, height - dist, dist, height)
			|| this.insideRectangle(vehicle, width - dist, height - dist, width, height);
	}
	
	/**
	 * Returns wheater or not the vehicle is close to the walls.
	 *
	 * @param vehicle
	 * @param width
	 * @param height
	 * @return boolean
	 */
	private boolean closeToWalls(Point vehicle, int width, int height) {
		return ! this.insideRectangle(vehicle,
			this.wallSafeDistance, this.wallSafeDistance,
			(width - this.wallSafeDistance), (height - this.wallSafeDistance)
		);
	}
	
	/**
	 * Returns whether or not vehicle is inside rectangle.
	 *
	 * @param vehicle
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private boolean insideRectangle(Point vehicle, int x1, int y1, int x2, int y2) {
		return vehicle.x > x1 && vehicle.x < x2 && vehicle.y > y1 && vehicle.y < y2;
	}
	
	/**
	 * Move the vehicle with the passed speed.
	 *
	 * @param speed
	 */
	private void move(int speed) {
		if (this.output == null) return;
		this.output.println("move " + speed);
	}
	
	/**
	 * Turn the vehicle with the passed angle and speed.
	 *
	 * @param angle
	 * @param speed
	 */
	private void turn(int angle, int speed) {
		if (this.output == null) return;
		this.output.println("turn " + angle + " " + speed);
	}
	
	/**
	 * Enable collecting engines for inner and outer engines.
	 *
	 * @param inner
	 * @param outer
	 */
	private void collect(int inner, int outer) {
		if (this.output == null) return;
		this.output.println("collect " + inner + " " + outer);
	}
	
	/**
	 * Play passed deep index.
	 *
	 * @param inner
	 * @param outer
	 */
	public void beep(int type) {
		if (this.output == null) return;
		this.output.println("beep " + type);
	}
	
	/**
	 * Pause the execution of the pathing for the passed millis.
	 *
	 * @param millis
	 */
	private void pause(int millis) {
		this.pauser = System.currentTimeMillis() + millis;
	}
	
	/**
	 * Stop the vehicle from moving.
	 */
	public void stop() {
		this.move(0);
		this.collect(0, 0);
		
		this.pauser = 0;
		this.done = false;
		this.stalled = false;
		this.collecting = false;
		this.nextReverse = false;
		this.shouldReverse = false;
		this.doneGoalCheck = false;
		this.wasTowardsGoal = false;
	}
	
}

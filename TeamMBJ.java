package ITC;
import robocode.*;

import java.awt.Color;
import java.awt.geom.Point2D;


// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * MichaelRainsfordRyan - a robot by Michael Rainsford Ryan
 */
public class TeamMBJ extends Robot
{
	/**
	 * run: MichaelRainsfordRyan's default behavior
	 */
	
	boolean foundSentry = false;
	boolean inPosition = false;
	boolean lastStand = false;
	boolean clockwise = false;
	boolean circlePatrol = true;
	boolean verticalPatrol = true; // Initial value. Will be changed before used
	boolean aimAndFire = false;
	double sentryX = 0.0;
	double sentryY = 0.0;	
	int corner = 0;
	int minBorder = 325;
	int maxBorder = 475;
	int middleBorder = 400;
	int borderAllowance = 5;
	int LastSpottedTimer = 0;
	Point2D.Double enemyPosition;
	double enemyBearing;
	
// BENS SHIT :)

	// enemy data
	double enemyX = 0.0;
	double enemyY = 0.0;
	double enemyHealth = 0.0;
	
	double[] velocityArray = new double[5];
	boolean arrayFilled = false;
	double avgSpeed = 0;
	
	int bulletStep = 0;
	int step = 0;
	int aimStep = 0;
	int missCount = 0;
	
	double bulletPower = 3.0;
	
	boolean ramming = false;
	
// END BENS SHIT :(
		
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:
			

		setColors(Color.green,Color.green,Color.green); // body,gun,radar
		
		// Robot main loop
		while(true) {
		

			if (!inPosition) { // If not in the right starting point, move to it
				getInPosition();
			}
			else { // If the bot is in position, start the normal routine
				
				if (aimAndFire) {
				//	targeting();
				}
				else if (circlePatrol) {
					System.out.print("()"); // Print out the name of the bot
					if (clockwise) { // Move in the currect direction
						patrolClockwise();
					} else {
						patrolAntiClockwise();
					}
					
					// Make sure the gun is pointing the right way
					if (Math.round(getGunBearing()) != 270) {
						turnGunLeft(getGunBearing() - 270);
					}
				} else { // Patrol in a back and forth motion
					System.out.print("<->"); // Print out the name of the bot
					patrolBackAndForth();
				}
				
				// Check if still in position
				if (!checkInPosition()) {
					if (getEnergy() < 40) {
						lastStand = true;
						System.out.println("\nLast stand"); // Print out the name of the bot
					}
					inPosition = false;
					System.out.println("\nNo longer in position"); // Print out the name of the bot
				}
			}
			
			if (LastSpottedTimer < 2) {
				LastSpottedTimer++;
			} else {
				System.out.println("\nScanning"); // Print out the name of the bot
				turnRadarLeft(360);
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		
		if (!e.isSentryRobot()) { // If the robot scanned isn't the sentry, fire at it
			LastSpottedTimer = 0;		

			enemyBearing = e.getBearing();
			circlePatrol = !checkBestPosition(e); // Make the patrol status equal to the reverse of the function. Firing mode is set here
			
			if (aimAndFire == true){
				targeting(e.getDistance(), e.getHeadingRadians(), e.getBearingRadians(), e.getVelocity());
			}

			if (getGunHeat() == 0) {
				System.out.println("\nFiring at " + e.getName()); // Print out the name of the bot
				fire(bulletPower); // Fire a strong bullet
				bulletStep++; // used for mod shot 
				missCount++; // miss until proven otherwise
			}
			else {
				System.out.print(". "); // Print a dot if gun too hot
			}
		}
		else {
			System.out.print(": "); // Print that the scanned enemy was the sentry
		}
	}

	public void onHitRobot(HitRobotEvent e) {
		if (inPosition) {
			clockwise = !clockwise;
		}
		else {
			back(maxBorder - middleBorder);
		}
		System.out.println("Hit robot!");
		
		ramming = true;
		aimAndFire = true;
	}
	
	public void getInPosition() {
		if (!checkInPosition()) // Check if outside the safe zone
		{
			if (getX() < minBorder) {
				turnLeft(getHeading() - 90);
				ahead(minBorder - getX());
				corner = 3;
			} else if (getX() > maxBorder) {
				turnLeft(getHeading() - 270); // Turn to face the wall
				ahead(getX() - maxBorder); // Move until the bot reaches the wall
				corner = 1;
			}
			
			if (getY() < minBorder) {
				turnLeft(getHeading());
				ahead(minBorder - getY());
				corner = 2;
			} else if (getY() > maxBorder) {
				turnLeft(getHeading() - 180); // Turn to face the wall
				ahead(getY() - maxBorder); // Move until the bot reaches the wall
				corner = 0;
			}
			else
			{
				inPosition = true;
			}
		}
		else
		{
			if (getX() != maxBorder && getX() != minBorder) // IF the robot is not up against the right wall, move to it
			{
				if (getX() > middleBorder) {
					turnLeft(getHeading() - 90); // Turn to face the wall
					ahead(maxBorder - getX()); // Move until the bot reaches the wall
					corner = 0; // Set corner to go to to the currect one
				}
				else {
					turnLeft(getHeading() - 270); // Turn to face the wall
					ahead(getX() - minBorder); // Move until the bot reaches the wall
					corner = 2; // Set corner to go to to the currect one
				}	
			}
			else {
				turnGunLeft(90); // Turn the gun into position
				turnLeft(90); // Turn into position
				inPosition = true; // Tell the robot it's in position
			}
		}
	}
	
	public double getGunBearing() {
		double bearing = getGunHeading() - getHeading(); // Get the gun bearing and return it
		
		if (bearing < 0)
		{
			bearing += 360;
		}

		return bearing;
	}
	
	public boolean checkInPosition() {
		if (getX() > minBorder - borderAllowance && getX() < maxBorder + borderAllowance)
		{
			if (getY() > minBorder - borderAllowance && getY() < maxBorder + borderAllowance)
			{
				return true;
			}
		}
		return false;
	}
	
	public void patrolAntiClockwise() {
		switch (corner) {
			case 0: // Go to top border
				if (getY() < maxBorder) {
					ahead(maxBorder - getY());
				}
				else {
					turnLeft(90);
					corner = 1;
				}
				break;
			case 1: // Go to left border
				if (getX() > minBorder) {
					ahead(getX() - minBorder);
				}
			 	else {
					turnLeft(90);
					corner = 2;
				}
				break;
			case 2: // Go to bottom border
				if (getY() > minBorder) {
					ahead(getY() - minBorder);
				} else {
					turnLeft(90);
					corner = 3;
				}
				break;
			case 3: // Go to right border
				if (getX() < maxBorder) {
					ahead(maxBorder - getX());
				} else {
					turnLeft(90);
					corner = 0;
				}
				break;
		}
	}
	
	public void patrolClockwise() {
		switch (corner) {
			case 0: // Go to bottom border
				if (getY() > minBorder) {
					back(getY() - minBorder);
				}
				else {
					turnRight(90);
					corner = 3;
				}
				break;
			case 1: // Go to right border
				if (getX() < maxBorder) {
					back(maxBorder - getX());
				}
			 	else {
					turnRight(90);
					corner = 0;
				}
				break;
			case 2: // Go to top border
				if (getY() < maxBorder) {
					back(maxBorder - getY());
				} else {
					turnRight(90);
					corner = 1;
				}
				break;
			case 3: // Go to left border
				if (getX() > minBorder) {
					back(getX() - minBorder);
				} else {
					turnRight(90);
					corner = 2;
				}
				break;
		}
	}
	
	public boolean checkBestPosition(ScannedRobotEvent e) {
		enemyPosition = getPosition(e);
		if (checkWithinBounds(enemyPosition)) {
			aimAndFire = false;
			System.out.println("Gun aim X: " + Math.cos(Math.toRadians(getGunBearing())) * 10);
			
			// BUG FIX FOR FACING OUT OF ARENA; NEED TO IMPLEMENT FOR HORIZONTAL PATROL ALSO
			if (verticalPatrol == true){
				// If I'm patrolling right side and gun is facing right, or patrolling left and gun facing left
				if (getX() > middleBorder && Math.sin(Math.toRadians(getGunHeading())) > 0){ // sin(90) = 1
					System.out.println("Warning! Facing away from enemy! Enemy to left");
					turnGunRight(180);
				} else if (getX() < middleBorder && Math.sin(Math.toRadians(getGunHeading())) < 0){ // sin(270) = -1
					System.out.println("Warning! Facing away from enemy! Enemy to right");
					turnGunRight(180);
				}
			}
			
		// Horisontal
		if (getX() < middleBorder && enemyPosition.x > middleBorder) {
			verticalPatrol = true;
			return true;
		}
		if (getX() > middleBorder && enemyPosition.x < middleBorder) {
			verticalPatrol = true;
			return true;
		}
		
		// Vertical
		if (getY() < middleBorder && enemyPosition.y > middleBorder) {
			verticalPatrol = false;
			return true;
		}
		if (getY() > middleBorder && enemyPosition.y < middleBorder) {
			verticalPatrol = false;
			return true;
			}
		} else {
			aimAndFire = true;
		}
		
		return false;
	}
	
	// RETURN THE COORDINATES
	public Point2D.Double getPosition(ScannedRobotEvent e) {
		
		double distance = e.getDistance(); // Gets the distance to the robot
		double angleRads = (getHeading() + e.getBearing()) * (3.14159265359/180); // gets the angle in radians
		
		double botsX = getX() + Math.sin(angleRads) * distance; // Finds the x position
		double botsY = getY() + Math.cos(angleRads) * distance; // Finds the y position
		
		return new Point2D.Double(botsX, botsY);
	}
	
	public void patrolBackAndForth() {
		if (verticalPatrol) {
			if (Math.round(getHeading()) == 0) {
				if (getY() < maxBorder) {
					ahead(maxBorder - getY());
				}
				else if (getY() > minBorder) {
					back(getY() - minBorder);
				}
			} else if (Math.round(getHeading()) == 180) {
				if (getY() < maxBorder) {
					back(maxBorder - getY());
				}
				else if (getY() > minBorder) {
					ahead(getY() - minBorder);
				}
			} else {
				turnLeft(getHeading());
			}
			// Make sure the gun is pointing the right way
			if (enemyPosition.x < middleBorder) {
				if (Math.round(getGunHeading()) != 270) {
					turnGunLeft(getGunBearing() - 270);
					System.out.println("\nTurning1"); // Print out the name of the bot
				}
			} else {
				if (Math.round(getGunHeading()) != 90) {
					turnGunLeft(getGunBearing() - 90);
					System.out.println("\nTurning2"); // Print out the name of the bot
				}
			}

		} else {
			if (Math.round(getHeading()) == 90) {
				if (getX() < maxBorder) {
					ahead(maxBorder - getX());
				}
				else if (getX() > minBorder) {
					back(getX() - minBorder);
				}
			} else if (Math.round(getHeading()) == 270) {
				if (getX() < maxBorder) {
					back(maxBorder - getX());
				}
				else if (getX() > minBorder) {
					ahead(getX() - minBorder);
				}
			} else {
				turnLeft(getHeading() - 90);
			}
			// Make sure the gun is pointing the right way
			if (enemyPosition.y < middleBorder) {
				if (Math.round(getGunHeading()) != 180) {
					turnGunLeft(getGunBearing() - 90);
					System.out.println("\nTurning3"); // Print out the name of the bot
				}
			} else {
				if (Math.round(getGunHeading()) != 0) {
					turnGunLeft(getGunBearing() + 90);
					System.out.println("\nTurning4"); // Print out the name of the bot
				}
			}
		}
	}
	
	public boolean checkWithinBounds(Point2D.Double t_position) {
		if (t_position.x > minBorder && t_position.x < maxBorder) {
			if (t_position.y > minBorder && t_position.y < maxBorder) {
				return true;
			}
		}
		return false;
	}
	
/*	public void aimAndFireAttack(Point2D.Double t_enemyPos) {
		// Find correct aim position
		if ( getGunBearing() < (getTrueBearing(enemyBearing) - 10.0) || getGunBearing() > (getTrueBearing(enemyBearing) + 10.0))
		{
			double turnAngle = getGunBearing() - getTrueBearing(enemyBearing);
			if (turnAngle > 0) {
				turnGunLeft(turnAngle);
				System.out.println("-- Turning gun to the left by: " + turnAngle); // Print out that the robot is trying to face the enemy
			}
			else {
					turnGunRight(getMagnitude(turnAngle));
					System.out.println("-- Turning gun to the right by: " + getMagnitude(turnAngle)); // Print out that the robot is trying to face the enemy
			}			
		}
	}*/
	
	// TAKES A THE INPUT OF A DOUBLE VARIABLE AND OUTPUTS THE ABSOLUTE VALUE
	public double getMagnitude(double t_value) {
		// Get the absolute value by squaring the value and taking the square root of that
		return Math.sqrt(t_value * t_value);
	}
	
	// TAKES AN ANGLE AND MAKES IT IN THE RANGE 0 - 360 RATHER THAN -180 - 0 - 180
	public double getTrueBearing(double t_value) {
		if (t_value < 0) {
			return t_value + 360;
		}
		
		return t_value;
	}
	


// +++++++++ BEN TARGETING CODE ++++++++++++

	public void targeting(double enemyDist, 
						  double enemyHeading,	
						  double enemyBearing, 
  						  double enemySpeed){
	
		// Establish my position
		double myX = getX();
		double myY = getY();
		
		// Absolute bearing of enemy bot
		double absoluteBearing = Math.toRadians(getHeading()) + enemyBearing;
		
		// Convert bearing and distance to absolute coordinates
		enemyX = myX + (enemyDist * Math.sin(absoluteBearing));
		enemyY = myY + (enemyDist * Math.cos(absoluteBearing));
		
		// if enemy close, just fire direct
		if (enemyDist < 100.0){
		
			System.out.println("Direct Fire, dist: " + enemyDist);
			
			if (bulletStep == 3 && bulletPower == 3.0){
				bulletPower = 2.0;
				System.out.println("ModShot!");
			}
			
			firing(enemyX,enemyY); //DIRECT FIRE
		
		}
		
		/* if we've fired more than 2 shots without hitting, reduce power to 2.
		   if we've fired more than 5 shots without hitting, reduce power to 1.*/
		if (missCount > 2){
			bulletPower = 2.0;
		} else if (missCount > 5){
			bulletPower = 1.0;
		} else {
			bulletPower = 3.0;
		}
		
		// calculate bullet velocity based on power
		double bulletVelocity = 20 - 3*bulletPower;
			
		// calculate time on target
		double deltaTime = enemyDist/bulletVelocity;

		// circular array containing most recent 5 velocities (really speed)
		velocityArray[step%5] = enemySpeed;
		step++;
		if (step > 4) arrayFilled = true;
		
		if (arrayFilled){
			avgSpeed = (velocityArray[0] + 
							velocityArray[1] +
							velocityArray[2] +
							velocityArray[3] +
							velocityArray[4]) / 5;
		}

		// weight avg speed/current speed 50/50
		if (arrayFilled == true && enemySpeed != 0.0){
			enemySpeed = (enemySpeed+avgSpeed)/2;
		}
		else{
			enemySpeed = 0.0; // don't shoot ahead of enemy when they've stopped
		}
		
			
		// init to current enemy position
		double predictedX = enemyX;
		double predictedY = enemyY;
		
		// determine future position based off ToT, then repeat twice off refined ToT
		for (int i = 0; i < 3; i++){	
			// position dTime ticks in future based off last heading and velocity
			double deltaX = deltaTime*enemySpeed*Math.sin(enemyHeading);
			double deltaY = deltaTime*enemySpeed*Math.cos(enemyHeading);
			
			// add delta pos to current pos
			predictedX = enemyX + deltaX;
			predictedY = enemyY + deltaY;
			
			// account for walls
			if (predictedX < 0) predictedX = 0.0;
			if (predictedX > 800) predictedX = 800.0;
			if (predictedY < 0) predictedY = 0.0;
			if (predictedY > 800) predictedY = 800.0;
			
			// get new distance to enemy
			double newDist = Math.sqrt(Math.pow(predictedX - myX,2) + Math.pow(predictedY - myY,2));
			
			// recalculate ToT
			deltaTime = newDist/bulletVelocity;
		}
		// pass predicted position to firing
		firing(predictedX, predictedY);
	}
		/// <summary>
	///	Brings guns to bear on the projected position of the enemy, and fires when ready
	/// </summary>
	/// <param predictedX>Predicted X position of enemy</param>
	/// <param predictedY>Predicted Y position of enemy</param>
	public void firing(double predictedX, double predictedY){
		double xOffset = predictedX - getX();
		double yOffset = predictedY - getY();
		
		// returns target vector relative to the positive X-axis - 90
		// resulting in directly up being 0 degrees
		double targetVector = (Math.toDegrees(Math.atan2(xOffset,yOffset)));
		double gunHeading = getGunHeading();
		
		// brings gunHeading into the range -180 to 180
		if (gunHeading > 180){
			gunHeading -= (gunHeading-180)*2;
			gunHeading *= -1;
		}
		
		// keep dVector in the range -180 to 180
		double deltaVector = targetVector - gunHeading;
		if (deltaVector > 180) deltaVector -= 360;
		if (deltaVector < -180) deltaVector += 360;
		
		// direction doesn't matter. 	
		// If target is to left, negative dVec value passed
		// and gun will turn left (negative right).
		turnGunRight(deltaVector);
		
		if (ramming == true){
			fire(3.0);
			ramming = false;
		}
	}
		
	public void onBulletHit(BulletHitEvent e) {	
		missCount = 0; // if we hit the enemy robot, reset our miss count
    }
}
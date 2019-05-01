package pacman.controllers;

import pacman.game.Game;

/**
 * @param <T> The generic type of the move to be returned (either a single move for Ms Pac-Man or an EnumMap for the ghosts).
 */
public abstract class Controller<T> implements Runnable
{
	private boolean alive,wasSignalled,hasComputed;
	private volatile boolean threadStillRunning;
	private long timeDue;
	private Game game;
	protected T lastMove;

	/**
	 * Instantiates a new controller. The constructor initialises the class variables.
	 */
	public Controller()
	{
		alive=true;
		wasSignalled=false;
		hasComputed=false;
		threadStillRunning=false;
	}

	/**
	 * Retrieves the move from the controller (whatever is stored in the class variable).
	 *
	 * @return The move stored in the class variable lastMove
	 */
	public final T getMove()
	{
		return lastMove;
	}

	public final void run() {

		while(alive) {
			synchronized(this) {
				while(!wasSignalled) {
					try {
						wait();
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if(!threadStillRunning) {
					new Thread()
					{
						public void run()
						{
							threadStillRunning=true;
							lastMove = getMove(game,timeDue);
							hasComputed=true;
							threadStillRunning=false;
						}
					}.start();
				}
				
				wasSignalled=false;
			}
		}
	}

	public abstract T getMove(Game game,long timeDue);
}
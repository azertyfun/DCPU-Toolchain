package tk.azertyfun.dcputoolchain.emulator;

public interface InterruptListener {
	void interrupted();

	void queueingEnabled(boolean isQueueingEnabled);

	void sleeping(boolean sleeping);
}

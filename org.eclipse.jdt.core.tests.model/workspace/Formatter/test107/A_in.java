if (!lockListener.isUIWaiting())
	asyncExec(new Runnable() {
	public void run() {
		lockListener.doPendingWork();
	}
});
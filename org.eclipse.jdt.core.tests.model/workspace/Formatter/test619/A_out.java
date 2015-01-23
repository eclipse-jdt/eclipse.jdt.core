public static void run(String args[]) {
	Runnable a = new Runnable() {
		public void run() {
			// The following line is a problem
		}
	};
	int b = 42;
}
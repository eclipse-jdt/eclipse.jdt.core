public boolean execute(IProgressMonitor progressMonitor) {
	if (this.isCancelled
			|| progressMonitor != null && progressMonitor.isCanceled())
		return true;
	IIndex index = manager.getIndex(this.indexPath, true,
			/*reuse index file*/true /*create if none*/);
	if (index == null)
		return true;
	ReadWriteMonitor monitor = manager.getMonitorFor(index);
	if (monitor == null)
		return true;
}
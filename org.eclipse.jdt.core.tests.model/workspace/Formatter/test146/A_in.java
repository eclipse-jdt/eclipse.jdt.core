public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
				marker.setAttributes(
				new String[] {
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IMarker.LOCATION, 
					IJavaModelMarker.CYCLE_DETECTED,
					IJavaModelMarker.CLASSPATH_FILE_FORMAT,
					IJavaModelMarker.ID,
					IJavaModelMarker.ARGUMENTS ,
				});
}
package test;

import java.util.Hashtable;

public class Test {

  public boolean combineScriptsExist() {
		File solvematchshift =
			new File(System.getProperty("user.dir"), "solvematchshift.com");
		File solvematchmod =
			new File(System.getProperty("user.dir"), "solvematchmod.com");
		File matchvol1 =
					new File(System.getProperty("user.dir"), "matchvol1.com");
		File matchorwarp =
					new File(System.getProperty("user.dir"), "matchorwarp.com");
		File patchcorr =
					new File(System.getProperty("user.dir"), "patchcorr.com");
		File volcombine =
					new File(System.getProperty("user.dir"), "volcombine.com");
		File warpvol =
					new File(System.getProperty("user.dir"), "warpvol.com");
		return solvematchshift.exists() && solvematchmod.exists() && atchvol1.exists()
&& matchorwarp.exists() && patchcorr.exists() && volcombine.exists() &&
warpvol.exists();
  }}
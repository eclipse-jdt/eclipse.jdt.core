	public AssistOptions(Map settings) {
		if (settings == null)
			return;
		// filter options which are related to the assist component
		Iterator entries = settings.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			if (!(entry.getKey() instanceof String))
				continue;
			if (!(entry.getValue() instanceof String))
				continue;
			String optionID = (String) entry.getKey();
			String optionValue = (String) entry.getValue();
			if (optionID.equals(OPTION_PerformVisibilityCheck)) {
				if (optionValue.equals(ENABLED)) {
					this.checkVisibility = true;
				} else if (optionValue.equals(DISABLED)) {
					this.checkVisibility = false;
				}
				continue;
			} else if (optionID.equals(OPTION_ForceImplicitQualification)) {
				if (optionValue.equals(ENABLED)) {
					this.forceImplicitQualification = true;
				} else if (optionValue.equals(DISABLED)) {
					this.forceImplicitQualification = false;
				}
				continue;
			} else if (optionID.equals(OPTION_FieldPrefixes)) {
				if (optionValue.length() == 0) {
					this.fieldPrefixes = null;
				} else {
					this.fieldPrefixes = CharOperation.splitAndTrimOn(',',
							optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_StaticFieldPrefixes)) {
				if (optionValue.length() == 0) {
					this.staticFieldPrefixes = null;
				} else {
					this.staticFieldPrefixes = CharOperation.splitAndTrimOn(
							',', optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_LocalPrefixes)) {
				if (optionValue.length() == 0) {
					this.localPrefixes = null;
				} else {
					this.localPrefixes = CharOperation.splitAndTrimOn(',',
							optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_ArgumentPrefixes)) {
				if (optionValue.length() == 0) {
					this.argumentPrefixes = null;
				} else {
					this.argumentPrefixes = CharOperation.splitAndTrimOn(',',
							optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_FieldSuffixes)) {
				if (optionValue.length() == 0) {
					this.fieldSuffixes = null;
				} else {
					this.fieldSuffixes = CharOperation.splitAndTrimOn(',',
							optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_StaticFieldSuffixes)) {
				if (optionValue.length() == 0) {
					this.staticFieldSuffixes = null;
				} else {
					this.staticFieldSuffixes = CharOperation.splitAndTrimOn(
							',', optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_LocalSuffixes)) {
				if (optionValue.length() == 0) {
					this.localSuffixes = null;
				} else {
					this.localSuffixes = CharOperation.splitAndTrimOn(',',
							optionValue.toCharArray());
				}
				continue;
			} else if (optionID.equals(OPTION_ArgumentSuffixes)) {
				if (optionValue.length() == 0) {
					this.argumentSuffixes = null;
				} else {
					this.argumentSuffixes = CharOperation.splitAndTrimOn(',',
							optionValue.toCharArray());
				}
				continue;
			}
		}
	}
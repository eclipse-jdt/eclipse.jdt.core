package $.$;

class $ {               	// $.$.$
	int $;              	// $.$.$.$
	void $() {}         	// $.$.$.$()
	class $I {          	// $.$.$.$I
		int $;          	// $.$.$.$I.$
		void $() {}     	// $.$.$.$I.$()
		class $II {	    	// $.$.$.$I.$II
			int $;      	// $.$.$.$I.$II.$
			void $() {} 	// $.$.$.$I.$II.$()
		}
	}
	static class $SI {  	// $.$.$.$SI
		int $;          	// $.$.$.$SI.$
		void $() {}			// $.$.$.$SI.$()
		static class $SII {	// $.$.$.$SI.$SII
			int $;        	// $.$.$.$SI.$SII.$
			void $() {}		// $.$.$.$SI.$SII.$()
		}
	}

	class $E extends $ {    	// $.$.$.$E
		int $;          		// $.$.$.$E.$
		void $() {}     		// $.$.$.$E.$()
		class $EE extends $E {	// $.$.$.$E.$EE
			int $;      		// $.$.$.$E.$EE.$
			void $() {} 		// $.$.$.$E.$EE.$()
		}
	}
	static class $SE extends $ {// $.$.$.$SE
		int $;          		// $.$.$.$SE.$
		void $() {}     		// $.$.$.$E.$()
		static class $SEE extends $SE {	// $.$.$.$SE.$SEE
			int $;        		// $.$.$.$SE.$SEE.$
			void $() {			// $.$.$.$SE.$SEE.$()
				new $().$();
				(new $().new $I()).$();
				(new $().new $I().new $II()).$();
				new $SI().$();
				(new $SI.$SII()).$();
				new $SE().$();
				(new $SE.$SEE()).$();
			}
		}
	}

}

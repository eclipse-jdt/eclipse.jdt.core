public class ResolveDeepLocalVariable {
	class D1 {
		class D2 {
			class D3 {
				class D4 {
					class D5 {
						class D6 {
							class D7 {
								class D8 {
									class D9 {
										public D9() {
											String foo = "foo";
											foo += "42";
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

public class TypeReferenceAsSingleNameReference {

void hasReference() {
	TypeReferenceAsSingleNameReference x = null;
	TypeReferenceAsSingleNameReference = new Object();
}

void hasNoReference() {
	Object TypeReferenceAsSingleNameReference = null;
}

}

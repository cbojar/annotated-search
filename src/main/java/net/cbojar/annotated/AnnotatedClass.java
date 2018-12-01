package net.cbojar.annotated;

@MyTypeAnnotation
public class AnnotatedClass {
	@MyMethodAnnotation
	public void m1() {
		// Do nothing
	}

	@MyMethodAnnotation
	public void m2() {
		// Also do nothing
	}
}

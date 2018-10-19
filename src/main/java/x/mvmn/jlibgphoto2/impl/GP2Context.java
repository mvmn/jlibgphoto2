package x.mvmn.jlibgphoto2.impl;

import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Gphoto2Library;

public class GP2Context {

	protected final PointerByReference pointer;

	public GP2Context() {
		this.pointer = Gphoto2Library.INSTANCE.gp_context_new();
	}

	protected PointerByReference getPointerByRef() {
		return pointer;
	}
}

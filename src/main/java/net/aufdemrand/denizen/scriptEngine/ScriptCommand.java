package net.aufdemrand.denizen.scriptEngine;

import java.util.Arrays;

public class ScriptCommand<E> {

		private int size = 0;
		private static final int DEFAULT_CAPACITY = 10;
		private Object elements[];
		
		public ScriptCommand() {
			elements = new Object[DEFAULT_CAPACITY];
		}

		public void add(E e) {
			if (size == elements.length) {
				ensureCapa();
			}
			elements[size++] = e;
		}
	 

		private void ensureCapa() {
			int newSize = elements.length * 2;
			elements = Arrays.copyOf(elements, newSize);
		}

		@SuppressWarnings("unchecked")
		public E get(int i) {
			if (i>= elements.length) {
				throw new IndexOutOfBoundsException("Index: " + i + ", Size " + i);
			}
			return (E) elements[i];
		}
	
}

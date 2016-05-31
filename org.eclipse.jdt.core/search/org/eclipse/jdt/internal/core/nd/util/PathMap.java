package org.eclipse.jdt.internal.core.nd.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

/**
 * Maps IPath keys onto values
 */
public class PathMap<T> {
	private static class Node<T> {
		int depth;
		boolean exists;
		T value;
		Map<String, Node<T>> children;

		Node(int depth) {
			this.depth = depth;
		}

		String getSegment(IPath key) {
			return key.segment(this.depth);
		}

		Node<T> createNode(IPath key) {
			if (this.depth == key.segmentCount()) {
				this.exists = true;
				return this;
			}

			if (this.children == null) {
				this.children = new HashMap<>();
			}

			String nextSegment = getSegment(key);
			Node<T> next = createChild(nextSegment);
			return next.createNode(key);
		}

		public Node<T> createChild(String nextSegment) {
			Node<T> next = this.children.get(nextSegment);
			if (next == null) {
				next = new Node<>(this.depth + 1);
				this.children.put(nextSegment, next);
			}
			return next;
		}

		public Node<T> getMostSpecificNode(IPath key) {
			if (this.depth == key.segmentCount()) {
				return this;
			}
			String nextSegment = getSegment(key);

			Node<T> child = getChild(nextSegment);
			if (child == null) {
				return this;
			}
			Node<T> result = child.getMostSpecificNode(key);
			if (result.exists) {
				return result;
			} else {
				return this;
			}
		}

		private Node<T> getChild(String nextSegment) {
			return this.children.get(nextSegment);
		}
	}

	private static class DeviceNode<T> extends Node<T> {
		Node<T> noDevice = new Node<>(0);

		DeviceNode() {
			super(-1);
		}

		@Override
		String getSegment(IPath key) {
			return key.getDevice();
		}

		@Override
		public Node<T> createChild(String nextSegment) {
			if (nextSegment == null) {
				return this.noDevice;
			}
			return super.createChild(nextSegment);
		}
	}

	private Node<T> root = new DeviceNode<T>();

	/**
	 * Inserts the given key into the map.
	 */
	public T put(IPath key, T value) {
		Node<T> node = this.root.createNode(key);
		T result = node.value;
		node.value = value;
		return result;
	}

	/**
	 * Returns the value associated with the given key
	 */
	public T get(IPath key) {
		Node<T> node = this.root.getMostSpecificNode(key);
		if (!node.exists || node.depth < key.segmentCount()) {
			return null;
		}
		return node.value;
	}

	/**
	 * Returns the value associated with the longest prefix of the given key
	 * that can be found in the map.
	 */
	public T getMostSpecific(IPath key) {
		Node<T> node = this.root.getMostSpecificNode(key);
		if (!node.exists) {
			return null;
		}
		return node.value;
	}

	/**
	 * Returns true iff any key in this map is a prefix of the given path.
	 */
	public boolean containsPrefixOf(IPath path) {
		Node<T> node = this.root.getMostSpecificNode(path);
		return node.exists;
	}
}
